/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/

package net.hedtech.banner.textmanager

import grails.transaction.Transactional
import org.springframework.transaction.annotation.Propagation
import grails.util.Holders
import groovy.sql.Sql
import org.apache.log4j.Logger

class TextManagerService {
    def sessionFactory

    def underlyingDataSource
    def underlyingSsbDataSource


    private final static Logger log = Logger.getLogger(TextManagerService.class.name)
    static final String ROOT_LOCALE_APP = 'en' // This will be the locale assumed for properties without locale
    // Save the chosen source language as root (as user cannot change translation)
    static final String PROJECT_CFG_KEY_APP = 'BAN_APP'
    final def ROOT_LOCALE_TM   = 'root'

    private def tranManProjectCache
    private def cacheTime
    private Boolean tmEnabled = true

    public static dbValues = [:]

    private def tranManProject() {
        if (!tmEnabled) {
            return
        }
        if (cacheTime && (new Date().getTime() - cacheTime.getTime()) < 5 * 60 * 1000) {
            return tranManProjectCache
        }
        Sql sql = new Sql(underlyingSsbDataSource?: underlyingDataSource)
        String appName = Holders.grailsApplication.metadata['app.name']
        String result = ""
        int matches = 0
        try {
            // Find projects with a matching application name in GMRPCFG
            // If more matches exist pick the project with the latest activity date
            def statement = """
                               select GMRPCFG_PROJECT from GMRPCFG join GMBPROJ on GMBPROJ_PROJECT=GMRPCFG_PROJECT
                               where GMRPCFG_KEY = $PROJECT_CFG_KEY_APP and GMRPCFG_VALUE = $appName
                               order by GMRPCFG_ACTIVITY_DATE
                            """
            sql.eachRow(statement) { row ->
                result = row.GMRPCFG_PROJECT
                matches++
            }
        } catch (e) {
            log.error("Error initializing text manager", e)
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
            Sql sql = new Sql(underlyingSsbDataSource?: underlyingDataSource)
            def appName = Holders.grailsApplication.metadata['app.name']
            try {
                def statement = """
                                   insert into GMBPROJ (GMBPROJ_PROJECT, GMBPROJ_ACTIVITY_DATE, GMBPROJ_DESC,
                                   GMBPROJ_OWNER,GMBPROJ_USER_ID) values ($projectCode, sysdate, $projectDescription,
                                   'TRANMGR','ban_ss_user')
                                """
                sql.execute(statement)
                statement = """
                               insert into GMRPCFG (GMRPCFG_PROJECT, GMRPCFG_KEY, GMRPCFG_VALUE, GMRPCFG_DESC,
                               GMRPCFG_USER_ID,GMRPCFG_ACTIVITY_DATE) values ($projectCode, $PROJECT_CFG_KEY_APP,
                               $appName, 'Banner Application in this project','ban_ss_user',sysdate )
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
            Sql sql = new Sql(underlyingSsbDataSource?: underlyingDataSource)
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
            } catch(e){
                log.error("Failed in deleting the project", e)
            } finally {
                sql?.close()
            }
        }
    }

    @Transactional
    def save(properties, name, srcLocale = ROOT_LOCALE_APP, locale) {
        if (!tmEnabled) {
            return
        }
        def project = tranManProject()
        if (project) {
            def textManagerDB = new TextManagerDB()
            textManagerDB.createConnection()
            int cnt = 0
            try {
                String msg = """
                                Arguments: mo=<mode> ba=<batch> lo=<db logon> pc=<TranMan Project> sl=<source language>
                                tl=<target language>  sf=<source file> tf=<target file>
                                mode: s (extract) | r (reverse extract) | t (translate) | q (quick translate - no check)
                                batch: [y|n]. n (No) is default. If y (Yes), the module record will be updated with
                                file locations etc.
                             """
                dbValues.projectCode = project
                dbValues.moduleName =  name.toUpperCase()
                dbValues.srcLocale =  ROOT_LOCALE_TM
                dbValues.srcFile = locale == ROOT_LOCALE_APP ? "${name}.properties" : "${name}_${locale}.properties"
                dbValues.srcIndicator = locale == srcLocale ? 's' : 'r'
                dbValues.tgtLocale = locale == srcLocale ? '' : "${locale.replace('_','')}"

                if (dbValues.srcIndicator == null) {
                    dbValues << [srcIndicator:"s"]
                } else if (dbValues.srcIndicator.equals("t")) {
                    if (dbValues.tgtFile == null) {
                        log.error "No target file specified (tgtFile=...) \n" + msg
                    }
                    if (dbValues.tgtLocale == null) {
                        log.error "No target language specified (tgtLocale=...) \n" + msg
                    }
                } else if (dbValues.srcIndicator.equals("r")) {
                    if (dbValues.tgtLocale == null) {
                        log.error "No target language specified (tgtLocale=...) \n" + msg
                    }
                }

                textManagerDB.setDBContext(dbValues)
                textManagerDB.setDefaultProp(dbValues)
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
                    defaultObjectProp.string = smartQuotesReplace(value)
                    log.info key + " = " + defaultObjectProp.string
                    textManagerDB.setPropString(defaultObjectProp)
                    cnt++
                }
                //Invalidate strings that are in db but not in property file
                if (dbValues.srcIndicator.equals("s")) {
                    textManagerDB.invalidateStrings()
                }
                textManagerDB.setModuleRecord(dbValues)

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
            Sql sql = new Sql(underlyingSsbDataSource?: underlyingDataSource)
            sql.cacheStatements = false
            //Query fetching changed messages. Don't use message with status pending (11).
            def statement = """
                 |WITH locales AS
                 |(
                 |  SELECT ( 1 + DECODE(gtvlang_code,:locale,0,1) -INSTR(:locale,gtvlang_code) ) AS distance
                 |  ,gtvlang_code AS lang_code
                 |  FROM gtvlang
                 |  WHERE gtvlang_code IN  (SUBSTR(:locale,1,2), :locale)
                 |),
                 |props_with_changes AS
                 |(
                 |  SELECT DISTINCT
                 |  gmrsprp_project,gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type,
                 |  gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop
                 |  FROM gmrsprp
                 |  WHERE gmrsprp_project = :pc
                 |  AND GMRSPRP_STAT_CODE=2
                 |    AND gmrsprp_activity_date >= (SYSDATE - :days_ago)
                 |    AND gmrsprp_lang_code IN (SELECT lang_code FROM locales) -- Only select relevant records
                 |)
                 |SELECT
                 |:locale request_locale
                 |,MIN(locales.distance) distance
                 |,gmrsprp_project, gmrsprp_module_name
                 |,SUBSTR(MIN( TO_CHAR(locales.distance)||locales.lang_code),2) AS lang_code
                 |,SUBSTR(gmrsprp_parent_name,2)||gmrsprp_object_name AS key
                 |,SUBSTR(MIN( TO_CHAR(locales.distance)||DECODE(gmrsprp_stat_code,11,'',gmrsprp_pre_str||gmbstrg_string||gmrsprp_pst_str)),2) AS string
                 |FROM locales, gmrsprp p1, gmbstrg
                 |WHERE locales.lang_code = gmrsprp_lang_code
                 |  AND gmbstrg_strcode=gmrsprp_strcode
                 |  AND GMRSPRP_STAT_CODE=2
                 |  AND (gmrsprp_project, gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type,
                 |       gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop)
                 |       IN (SELECT * FROM props_with_changes)
                 |GROUP BY
                 |gmrsprp_project, gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type,
                 |gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop
                 |""".stripMargin()

            //and parent_type = 10 and parent_name = :pn and object_type = 26 and object_name = :on and object_prop = 438
            def rows
            try {
                rows = sql.rows(statement, params, null)
            }
            catch (e) {
                log.error("Exception in finding message for key=$key, locale=$locale", e)
            }
            finally {
                sql?.close()
            }
            def t1 = new Date()
            if (rows?.size()) {
                rows.each { row ->
                    def translations = cacheMsg[row.key] ?: [:]
                    translations[locale] = row.string
                    cacheMsg[row.key] = translations
                }
            }
            localeLoaded[locale] = t0
            msg = cacheMsg[key] ? cacheMsg[key][locale] : null
            def t2 = new Date()
            log.debug "Reloaded ${rows?.size()} modified texts in ${t2.getTime() - t0.getTime()} ms . Query+Fetch time: //${t1.getTime() - t0.getTime()}"
        }
        msg
    }

    String smartQuotesReplace(String s) {
        StringBuffer res = new StringBuffer()
        char c
        s.eachWithIndex{ item, index ->
            c = item
            if (c == '\'') {
                // look ahead
                if (index + 1 < s.length() && s[index + 1] == '\'') {
                    res.append(c)
                } else {
                    res.append("\u2019")
                }
            } else {
                res.append(c)
            }
        }
        return res.toString()
    }
}