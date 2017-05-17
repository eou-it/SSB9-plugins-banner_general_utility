/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/

package net.hedtech.banner.i18n

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.textmanager.TextManagerDB
import net.hedtech.banner.textmanager.TextManagerUtil
import org.apache.log4j.Logger

import javax.annotation.PostConstruct
import java.sql.Timestamp

class TextManagerService {

    static transactional = false //Transaction not managed by hibernate

    def dataSource

    private final static Logger log = Logger.getLogger(TextManagerService.class)

    static final String ROOT_LOCALE_APP = 'en' // This will be the locale assumed for properties without locale
    static final String ROOT_LOCALE_TM = 'root'
    // Save the chosen source language as root (as user cannot change translation)
    static final String PROJECT_CFG_KEY_APP = 'BAN_APP'

    String connectionString

    private def tranManProjectCache
    private def cacheTime
    private Boolean tmEnabled = true

    
    @PostConstruct
    def init() {
        String dbUrl = dataSource.underlyingSsbDataSource.url
        String url = dbUrl.substring(dbUrl.lastIndexOf("@") + 1)
        String username = dataSource.underlyingSsbDataSource.username
        String password = dataSource.underlyingSsbDataSource.password
        connectionString = "${username}/${password}@${url}"
    }

    private def tranManProject() {
        if (!tmEnabled) {
            return
        }
        if (cacheTime && (new Date().getTime() - cacheTime.getTime()) < 5 * 60 * 1000) {
            return tranManProjectCache
        }

        TextManagerDB textManagerDB = new TextManagerDB(connectionString, null) // get a standard connection
        Sql sql = new Sql(textManagerDB.conn)
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
            textManagerDB.closeConnection()
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
            def textManagerDB = new TextManagerDB(connectionString, null) // get a standard connection
            def sql = new Sql(textManagerDB.conn)
            def appName = Holders.grailsApplication.metadata['app.name']
            def curDate = new Date()
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
                textManagerDB.closeConnection()
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
            def textManagerDB = new TextManagerDB(connectionString, null) // get a standard connection
            def sql = new Sql(textManagerDB.conn)
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
                textManagerDB.closeConnection()
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
            def textManagerDB
            int cnt = 0
            String sl = sourceLocale.replace('_', '')
            try {
                String[] args = [
                        "pc=${project}", //Todo configure project in translation manager
                        "lo=${connectionString}",
                        "mn=${name.toUpperCase()}",
                        "sl=$ROOT_LOCALE_TM",
                        locale == "$ROOT_LOCALE_APP" ? "sf=${name}.properties" : "sf=${name}_${locale}.properties",
                        locale == "$sourceLocale" ? 'mo=s' : 'mo=r',
                        locale == "$sourceLocale" ? '' : "tl=${locale.replace('_', '')}"
                ]

                textManagerUtil.parseArgs(args)
                textManagerDB = new TextManagerDB(connectionString, textManagerUtil)
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
                if (textManagerUtil.getValue(TextManagerUtil.mo).equals("s")) {
                    textManagerDB.invalidateStrings()
                }
                textManagerDB.setModuleRecord(textManagerUtil)

            } finally {
                textManagerDB?.closeConnection()
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
            def textManagerDB = new TextManagerDB(connectionString, null) // get a standard connection
            Sql sql = new Sql(textManagerDB.conn)
            sql.cacheStatements = false
            //Query fetching changed messages. Don't use message with status pending (11).
            def statement = """
                    |WITH locales AS
                    |( 
                    |  SELECT * FROM (
                    |     SELECT ( 1 + DECODE(gtvlang_code,:locale,0,1) -INSTR(:locale,gtvlang_code)) AS distance
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
                    |/*locales.code, locales.distance, gmrsprp_module_name AS source_name,*/
                    |SUBSTR(gmrsprp_parent_name,2)||gmrsprp_object_name AS key
                    |,DECODE(gmrsprp_stat_code,11,'',gmrsprp_pre_str||gmbstrg_string||gmrsprp_pst_str) AS string
                    |FROM locales, gmrsprp p1, gmbstrg
                    |WHERE gmrsprp_project = :pc
                    |AND gmrsprp_module_type = 'J'
                    |AND (gmrsprp_project, gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type,
                    |     gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop, gmrsprp_lang_code)
                    | IN (SELECT * FROM props_with_changes)
                    |--    
                    |AND gmrsprp_lang_code = locales.lang_code
                    |AND gmbstrg_strcode=gmrsprp_strcode
                    |AND locales.distance = (
                    |   SELECT
                    |   MIN( locales.distance ) 
                    |   FROM locales, gmrsprp 
                    |   WHERE gmrsprp_lang_code=locales.lang_code 
                    |     AND (gmrsprp_project, gmrsprp_module_name, gmrsprp_module_type, gmrsprp_parent_name, gmrsprp_parent_type,
                    |          gmrsprp_object_name, gmrsprp_object_type, gmrsprp_object_prop, gmrsprp_lang_code)
                    |      IN (SELECT * FROM props_with_changes)
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
                sql.close()
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
