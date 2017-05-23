/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/

package net.hedtech.banner.textmanager

import grails.util.Holders
import groovy.sql.Sql
import org.apache.log4j.Logger

class TextManagerService {
    def dataSource

    static transactional = false //Transaction not managed by hibernate

    private final static Logger log = Logger.getLogger(TextManagerService.class.name)
    static final String ROOT_LOCALE_APP = 'en' // This will be the locale assumed for properties without locale
    // Save the chosen source language as root (as user cannot change translation)
    static final String PROJECT_CFG_KEY_APP = 'BAN_APP'
    final def ROOT_LOCALE_TM   = 'root'

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
            log.warn "Multiple Text Manager projects are configured for application $appName. Using ${result}. It is recommended to correct this."
        }
        if (matches == 0) {
            log.warn "No Text Manager project is configured for application $appName. Text Manager customization is not possible."
        }
        tranManProjectCache = result
        cacheTime = new Date()
        log.debug "Using Text Manager project $result"
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
            try {
                String[] args = [
                        "projectCode=${project}", //Todo configure project in translation manager
                        "moduleName=${name.toUpperCase()}",
                        "srcLocale=$ROOT_LOCALE_TM",
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
                log.error("Exception in saving properties", e)
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
        def cacheAgeMilis = t0.getTime() - (localeLoaded[locale]?.getTime()?: 0L)
        if (cacheAgeMilis < timeOut) {
            msg = cacheMsg[key] ? cacheMsg[key][locale] : null
        } else {
            def tmLocale = locale?.toString().replace('_', '')
            def tmProject = tranManProject()
            if (!tmEnabled) {
                return null
            }
            // Add timeOut to the cacheAge to be sure no updates in TextManager are missed
            def params = [locale: tmLocale, pc: tmProject, days_ago: (cacheAgeMilis+timeOut)/1000/24/3600 , max_distance: 1]
            //max_distance: 1 means use strings with a matching locale, do not use a string from a different territory
            //max_distance: 2 means also use a string from a different territory (but just picks one if multiple territories exist)
            Sql sql = new Sql(dataSource.underlyingSsbDataSource)
            sql.cacheStatements = false
            //Query fetching changed messages. Don't use message with status pending (11).
            def statement = """
                 |WITH locales AS
                 |( 
                 |  SELECT * FROM (
                 |     SELECT ( 1 + DECODE(gtvlang_code,:locale,0,1) -INSTR(:locale,gtvlang_code) ) AS distance
                 |     ,gtvlang_code AS lang_code 
                 |     FROM gtvlang
                 |     WHERE gtvlang_code LIKE SUBSTR(:locale,1,2)||'%'
                 |  ) 
                 |  WHERE distance <=:max_distance
                 |),
                 |props_with_changes AS 
                 |(
                 |  SELECT DISTINCT 
                 |  gmrsprp_project,gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type, 
                 |  gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop, locales.lang_code
                 |  FROM gmrsprp, locales /*Carthesian Join with locales*/
                 |  WHERE gmrsprp_project = :pc 
                 |    AND gmrsprp_activity_date >= (SYSDATE - :days_ago)
                 |    AND gmrsprp_lang_code LIKE  SUBSTR(:locale,1,2)||'%'
                 |)
                 |SELECT 
                 |:locale request_locale,  locales.lang_code, locales.distance, gmrsprp_module_name AS source_name,
                 |to_char(gmrsprp_activity_date, 'YYYY-MM-DD HH24:MI:SS'),
                 |SUBSTR(gmrsprp_parent_name,2)||gmrsprp_object_name AS key
                 |,DECODE(gmrsprp_stat_code,11,'',gmrsprp_pre_str||gmbstrg_string||gmrsprp_pst_str) AS string
                 |FROM locales, gmrsprp p1, gmbstrg
                 |WHERE (gmrsprp_project, gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type,
                 |       gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop, gmrsprp_lang_code)
                 |       IN (SELECT * FROM props_with_changes)
                 |  AND locales.lang_code = gmrsprp_lang_code
                 |  AND gmbstrg_strcode=gmrsprp_strcode
                 |  AND locales.distance = ( 
                 |    SELECT MIN( locales.distance ) FROM locales, gmrsprp p2
                 |    WHERE p2.gmrsprp_project=p1.gmrsprp_project
                 |      AND p2.gmrsprp_module_name=p1.gmrsprp_module_name
                 |      AND p2.gmrsprp_module_type=p1.gmrsprp_module_type
                 |      AND p2.gmrsprp_lang_code=p1.gmrsprp_lang_code 
                 |      AND p2.gmrsprp_parent_type=p1.gmrsprp_parent_type
                 |      AND p2.gmrsprp_parent_name=p1.gmrsprp_parent_name
                 |      AND p2.gmrsprp_object_type=p1.gmrsprp_object_type
                 |      AND p2.gmrsprp_object_name=p1.gmrsprp_object_name
                 |      AND p2.gmrsprp_object_prop=p1.gmrsprp_object_prop
                 |      AND locales.lang_code=p2.gmrsprp_lang_code
                 |) 
                 |""".stripMargin()

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
                    def translations = cacheMsg[row.key] ?: [:]
                    translations[locale] = row.string
                    cacheMsg[row.key] = translations
                }
            }
            localeLoaded[locale] = t0
            msg = cacheMsg[key] ? cacheMsg[key][locale] : null
            def t2 = new Date()
            log.debug"Reloaded ${rows.size()} modified texts in ${t2.getTime() - t0.getTime()} ms . Query+Fetch time: ${t1.getTime() - t0.getTime()}"
        }
        msg
    }

    }
