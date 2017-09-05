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

    private Object savePropLock= new Object();

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


    @Transactional
    def save(properties, name, srcLocale = ROOT_LOCALE_APP, locale) {
        if (!tmEnabled) {
            return
        }
        def project = tranManProject()
        if (project) {
            int cnt = 0
            synchronized (savePropLock){
            def textManagerDB = new TextManagerDB()
            textManagerDB.createConnection()

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
                        textManagerDB.invalidateStrings(dbValues)
                }
                textManagerDB.setModuleRecord(dbValues)

            } catch (e){
                log.error("Exception in saving properties", e)
            }finally{
                textManagerDB.closeConnection()
            }
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
                 |WITH
                 |locales AS (
                 |  SELECT 
                 |  ( 1 + DECODE(gtvlang_code,:locale,0,1) -INSTR(:locale,gtvlang_code) ) AS distance
                 |  ,gtvlang_code AS lang_code
                 |  FROM gtvlang
                 |  WHERE gtvlang_code IN  (SUBSTR(:locale,1,2), :locale)
                 |),
                 |props AS (
                 |    SELECT
                 |      gmrsprp_project      proj
                 |    , gmrsprp_lang_code    lang
                 |    , gmrsprp_module_name  modname
                 |    , gmrsprp_module_type  modtype
                 |    , gmrsprp_parent_name  parname
                 |    , gmrsprp_parent_type  partype
                 |    , gmrsprp_object_name  objname
                 |    , gmrsprp_object_type  objtype
                 |    , gmrsprp_object_prop  objprop
                 |    , gmrsprp_strcode      strcode
                 |    , gmrsprp_activity_date modified
                 |    , gmrsprp_pre_str      pre_str
                 |    , gmrsprp_pst_str      pst_str
                 |    FROM gmrsprp
                 |    WHERE gmrsprp_project = :pc
                 |      AND gmrsprp_stat_code = 2
                 |      AND gmrsprp_lang_code in (select lang_code from locales)
                 |),
                 |hist AS (
                 |    SELECT
                 |      gmrshst_project      proj
                 |    , gmrshst_lang_code    lang
                 |    , gmrshst_module_name  modname
                 |    , gmrshst_module_type  modtype
                 |    , gmrshst_parent_name  parname
                 |    , gmrshst_parent_type  partype
                 |    , gmrshst_object_name  objname
                 |    , gmrshst_object_type  objtype
                 |    , gmrshst_object_prop  objprop
                 |    , gmrshst_activity_date modified 
                 |    FROM gmrshst
                 |    WHERE gmrshst_project = :pc
                 |      AND gmrshst_stat_code = 2
                 |      AND gmrshst_lang_code in (select lang_code from locales)
                 |),
                 |fallback_selection AS ( -- selection without language 
                 |   SELECT proj, modname,modtype,parname,partype,objname,objtype,objprop
                 |   FROM props WHERE modified >= SYSDATE - :days_ago
                 |   UNION
                 |   SELECT proj, modname,modtype,parname,partype,objname,objtype,objprop
                 |   FROM hist 
                 |    WHERE modified >= SYSDATE - :days_ago
                 |      AND :days_ago < 100 -- Don't use very old history
                 |),
                 |translation_selection AS (
                 |  SELECT *
                 |  FROM props
                 |  WHERE (proj,modname,modtype,parname,partype,objname,objtype,objprop)
                 |     IN (SELECT proj,modname,modtype,parname,partype,objname,objtype,objprop
                 |         FROM fallback_selection)
                 |),
                 |uncache_selection AS (
                 |  SELECT proj,modname,modtype,parname,partype,objname,objtype,objprop
                 |  FROM fallback_selection
                 |  MINUS 
                 |  SELECT proj,modname,modtype,parname,partype,objname,objtype,objprop
                 |  FROM translation_selection
                 |),
                 |propstr AS (
                 |  SELECT 
                 |     distance
                 |    ,proj,lang,modname,modtype,parname,partype,objname,objtype,objprop
                 |    ,strcode,pre_str,pst_str
                 |  FROM translation_selection, locales
                 |  WHERE lang_code=lang
                 |)
                 |SELECT 1 srt, 10, :locale lang, SUBSTR(parname,2)||objname AS key, '' string
                 |FROM uncache_selection
                 |UNION
                 |SELECT 2 srt, distance,lang, SUBSTR(parname,2)||objname, pre_str||gmbstrg_string||pst_str string
                 |FROM propstr p, gmbstrg s
                 |WHERE s.gmbstrg_strcode = p.strcode 
                 |  AND p.distance = (SELECT MIN(distance) FROM propstr m
                 |                    WHERE m.parname=p.parname
                 |                     AND m.objname=p.objname)
                 |ORDER BY 1
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
            log.debug "Reloaded ${rows?.size()} modified texts in ${t2.getTime() - t0.getTime()} ms . Query+Fetch time: ${t1.getTime() - t0.getTime()} ms"
        }
        if (msg && log.isDebugEnabled()) {
            log.debug("Using Cached TextManager translation for $key: $msg")
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