/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/

package net.hedtech.banner.textmanager

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.textmanager.TextManagerDB
import net.hedtech.banner.textmanager.TextManagerUtil
import java.sql.Timestamp

class TextManagerService {
    def dataSource

    static transactional = false //Transaction not managed by hibernat

    static final String ROOT_LOCALE_APP = 'en' // This will be the locale assumed for properties without locale
    // Save the chosen source language as root (as user cannot change translation)
    static final String PROJECT_CFG_KEY_APP = 'BAN_APP'

    private def tranManProjectCache
    private def cacheTime
    private Boolean tmEnabled = true

    private def tranManProject() {
        if (!tmEnabled) {
            return
        }
        if (cacheTime && (new Date().getTime() - cacheTime.getTime()) < 5 * 60 * 1000) {
            return tranManProjectCache
        }
        Sql sql = new Sql(dataSource.underlyingSsbDataSource)
        String appName = Holders.grailsApplication.metadata['app.name']
        String result = ""
        int matches = 0
        try {
            // Find projects with a matching application name in GMRPCFG
            // If more matches exist pick the project with the latest activity date
            def statement = """
          select GMRPCFG_PROJECT from GMRPCFG join GMBPROJ on GMBPROJ_PROJECT=GMRPCFG_PROJECT
          where GMRPCFG_KEY = $PROJECT_CFG_KEY_APP
          and GMRPCFG_VALUE = $appName
          order by GMRPCFG_ACTIVITY_DATE
        """
            sql.eachRow(statement) { row ->
                result = row.GMRPCFG_PROJECT
                matches++
            }
        } catch (e) {
            log.error "Error initializing text manager $e"
            tmEnabled = false
        } finally {
            sql?.close()
        }
        if (matches > 1) {
            log.warn "Multiple TranMan projects configured for application $appName. Please correct."
        }
        if (matches == 0) {
            log.warn "No TranMan project configured for application $appName."
        }
        tranManProjectCache = result
        cacheTime = new Date()
        result
    }

    def createProjectForApp(projectCode, projectDescription) {
        if (!tmEnabled) {
            return
        }
        if (!tranManProject()) {
            Sql sql = new Sql(dataSource.underlyingSsbDataSource)
            def appName = Holders.grailsApplication.metadata['app.name']
            try {
                def statement = """
                   insert into GMBPROJ (GMBPROJ_PROJECT, GMBPROJ_ACTIVITY_DATE, GMBPROJ_DESC, GMBPROJ_OWNER,GMBPROJ_USER_ID)
                   values ($projectCode, sysdate, $projectDescription, 'TRANMGR','ban_ss_user')
                """
                sql.execute(statement)
                statement = """
                   insert into GMRPCFG (GMRPCFG_PROJECT, GMRPCFG_KEY, GMRPCFG_VALUE,GMRPCFG_DESC,GMRPCFG_USER_ID,GMRPCFG_ACTIVITY_DATE)
                   values ($projectCode, $PROJECT_CFG_KEY_APP, $appName, 'Banner Application in this project','ban_ss_user',sysdate )
                """
                sql.execute(statement)
                cacheTime = null
                log.info "Created TranMan project $projectCode"
            } finally {
                sql?.close()
            }
        }
    }

    //Used to clean test project
    def deleteProjectforApp() {
        if (!tmEnabled) {
            return
        }
        def project = tranManProject()
        if (project) {
            Sql sql = new Sql(dataSource.underlyingSsbDataSource)
            try {
                def statement = """
                  begin
                    delete from GMRPCFG where GMRPCFG_project=$project;
                    delete from GMRSPRP where GMRSPRP_project=$project;
                    delete from GMRSHST where GMRSHST_project=$project;
                    delete from GMRPOBJ where GMRPOBJ_project=$project;
                    delete from GMBPROJ where GMBPROJ_project=$project;
                  end;
                """
                sql.execute(statement)
                cacheTime = null
                log.info "Deleted TranMan project $project"
            } finally {
                sql?.close()
            }
        }
    }

    def save(properties, name, sourceLocale = ROOT_LOCALE_APP, locale) {
        if (!tmEnabled) {
            return
        }
        def project = tranManProject()
        if (project) {
            def textManagerUtil = new TextManagerUtil()
            def textManagerDB = new TextManagerDB()
            textManagerDB.dataSource = dataSource
            textManagerDB.createConnection()
            int cnt = 0
            String sl = sourceLocale.replace('_','')
            try {
                String[] args = [
                        "projectCode=${project}", //Todo configure project in translation manager
                        "moduleName=${name.toUpperCase()}",
                        "srcLocale=$sl",
                        locale == "$ROOT_LOCALE_APP" ? "srcFile=${name}.properties" : "srcFile=${name}_${locale}.properties",
                        locale == "$sourceLocale" ? 'srcIndicator=s' : 'srcIndicator=r',
                        locale == "$sourceLocale" ? '' : "tgtLocale=${locale.replace('_', '')}"
                ]

                textManagerUtil.parseArgs(args)
                textManagerDB.setDBContext(textManagerUtil)
                textManagerDB.setDefaultProp(textManagerUtil)
                def defaultObjectProp = textManagerDB.getDefaultObjectProp()
                final String sep = "."
                int sepLoc

                properties.each { property ->
                    sepLoc = 0
                    String key = property.key
                    String value = property.value
                    sepLoc = key.lastIndexOf(sep)
                    if (sepLoc == -1) {
                        sepLoc = 0
                    }
                    defaultObjectProp.parentName = sep + key.substring(0, sepLoc) //. plus expression between brackets in [x.y...].z
                    defaultObjectProp.objectName = key.substring(sepLoc)       // expression between brackets in x.y....[z]
                    defaultObjectProp.string = TextManagerUtil.smartQuotesReplace(value)
                    log.info key + " = " + defaultObjectProp.string
                    textManagerDB.setPropString(defaultObjectProp)
                    cnt++
                }
                //Invalidate strings that are in db but not in property file
                if (textManagerUtil.dbValues.srcIndicator.equals("s")) {
                    textManagerDB.invalidateStrings()
                }
                textManagerDB.setModuleRecord(textManagerUtil)

            } catch (e){
                log.error("Exception in saving properties")
            }finally{
                textManagerDB.closeConnection()
            }
            return [error: null, count: cnt]
        }
        return [error: "Unable to save - no Project configured", count: 0]
    }

    def cacheMsg = [:]
    def localeLoaded = [:]
    def timeOut = 60 * 1000 as long //milli seconds

    def findMessage(key, locale) {
        if (!tmEnabled) {
            return null
        }
        def msg
        def t0 = new Date()
        if (localeLoaded[locale] && (t0.getTime() - localeLoaded[locale].getTime()) < timeOut) {
            msg = cacheMsg[key] ? cacheMsg[key][locale] : null
        } else {
            def tmLocale = locale?.toString().replace('_', '')
            tmLocale = getAppropriateLocale(tmLocale)
            def tmProject = tranManProject()
            if (!tmEnabled) {
                return null
            }
            def since = new Timestamp(localeLoaded[locale] ? localeLoaded[locale].getTime() : 0)
            // 0 is like beginning of time
            def params = [locale: tmLocale, pc: tmProject, now: new Timestamp(t0.getTime()), since: since]
            Sql sql = new Sql(dataSource.underlyingSsbDataSource)
            sql.cacheStatements = false
            //Query fetching changed messages. Don't use message with status pending (11).
            //Can change to use mod_date > :since when changing :since to time in database timezone.
            def statement = """select GMRSPRP_PARENT_NAME||GMRSPRP_OBJECT_NAME as key
                              ,decode(GMRSPRP_STAT_CODE,11,null,GMRSPRP_PRE_STR||GMBSTRG_STRING||GMRSPRP_PST_STR) as string
                               from GMBSTRG join GMRSPRP on GMBSTRG_STRCODE=GMRSPRP_STRCODE
                               where GMRSPRP_PROJECT = :pc
                                 and GMRSPRP_MODULE_TYPE = 'J'
                                 and GMRSPRP_LANG_CODE = :locale
                                and (sysdate - GMRSPRP_ACTIVITY_DATE) <= (cast(:now as date) - cast(:since as date) )
                            """

            //and parent_type = 10 and parent_name = :pn and object_type = 26 and object_name = :on and object_prop = 438
            def rows
            try {
                rows = sql.rows(statement, params, null)
            }
            catch (e) {
                log.error("Exception in findMessage for key=$key, locale=$locale \n$e")
            }
            finally {
                sql?.close()
            }
            def t1 = new Date()
            if (rows.size()) {
                rows.each { row ->
                    def translations = cacheMsg[row.key] ? cacheMsg[key] : [:]
                    translations[locale] = row.string
                    cacheMsg[row.key.substring(1)] = translations
                }
            }
            localeLoaded[locale] = t0
            msg = cacheMsg[key] ? cacheMsg[key][locale] : null
            def t2 = new Date()
            println "$t0: Reloaded ${rows.size()} modified texts in ${t2.getTime() - t0.getTime()} ms . Query+Fetch time: ${t1.getTime() - t0.getTime()}"
        }
        msg
    }

    String getAppropriateLocale(String locale) {
        if (locale.contains("ar"))
            return "arSA"
        else if (locale.contains("es"))
            return "esMX"
        else return locale
    }
}
