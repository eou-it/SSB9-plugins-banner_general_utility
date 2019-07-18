/*******************************************************************************
 Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import org.apache.commons.lang.math.RandomUtils
import org.hibernate.Session
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.context.i18n.LocaleContextHolder

/**
 * Service for retrieving Banner menu item for Classic SSB.
 */
@Transactional
class SelfServiceMenuService {

    def sessionFactory
    def grailsApplication
    def messageSource
    static final String AMPERSAND="&";
    static final String QUESTION_MARK="?";
    static final String hideSSBHeaderComps="hideSSBHeaderComps=true";
    static final String FETCH_ROLES = "{? = call TWBKSLIB.F_CASCADEFETCHROLE(?)}"

    /**
     * This is returns map of all menu items based on user access
     * @return List representation of menu objects that a user has access
     */

    def bannerMenu(def menuName, def menuTrail, def pidm) {

        processMenu(menuName, menuTrail, pidm)
    }

    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */

    private def processMenu(def menuName, def menuTrail, def pidm) {

        def dataMap = []
        def firstMenu = messageSource.getMessage("selfService.first.menu", null, LocaleContextHolder.getLocale())

        Boolean hideSSBHeader = false
        def session = RequestContextHolder.currentRequestAttributes()?.request?.session
        if(session['hideSSBHeaderComps'] != null){
            if(session['hideSSBHeaderComps'] instanceof Boolean){
                hideSSBHeader = session['hideSSBHeaderComps']
            }else{
                session['hideSSBHeaderComps'] = hideSSBHeader
            }
        }else{
            session['hideSSBHeaderComps'] = hideSSBHeader
        }
        Sql sql
        log.trace("Process Menu started for nenu:" + menuName)
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.trace("SQL Connection:" + sql.useConnection.toString())

        menuName = menuName ?: "bmenu.P_MainMnu"
        def roleCriteria
        def sqlQuery;
        if (pidm) {
            roleCriteria = getRoleCriteria(""+pidm)
        }

        sqlQuery = "SELECT  TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL,TWGRMENU_URL_DESC,TWGRMENU_IMAGE,TWGRMENU_ENABLED, TWGRMENU_DB_LINK_IND," +
                "TWGRMENU_SUBMENU_IND,TWGRMENU_TARGET_FRAME, TWGRMENU_STATUS_TEXT,TWGRMENU_ACTIVITY_DATE ,TWGRMENU_URL_IMAGE,TWGRMENU_SOURCE_IND "+
                " FROM twgrmenu   WHERE  twgrmenu_name = ? " +
                " AND twgrmenu_enabled = 'Y'" +
                " AND twgrmenu_source_ind =  (select nvl( max(twgrmenu_source_ind ),'B') FROM twgrmenu WHERE  twgrmenu_name = ? AND twgrmenu_source_ind='L')"+
                " AND (twgrmenu_db_link_ind = 'N' OR ( REGEXP_SUBSTR(twgrmenu_url , '[^?]*') IN (select twgrwmrl_name FROM twgrwmrl, twgrmenu WHERE twgrmenu.twgrmenu_name = ?"+
                " AND twgrwmrl_name = REGEXP_SUBSTR(twgrmenu.twgrmenu_url , '[^?]*') "+
                " AND twgrwmrl_source_ind = (select nvl( max(twgrwmrl_source_ind ),'B')" +
                " FROM twgrwmrl WHERE  twgrwmrl_name = REGEXP_SUBSTR(twgrmenu_url , '[^?]*') AND twgrwmrl_source_ind= 'L' )"
        sqlQuery = roleCriteria ? sqlQuery + " AND twgrwmrl_role in " + roleCriteria : sqlQuery + " AND twgrwmrl_role in ('') "
        sqlQuery = sqlQuery + " AND twgrwmrl_name IN ( select TWGBWMNU_NAME from TWGBWMNU where TWGBWMNU_NAME = REGEXP_SUBSTR(twgrmenu.TWGRMENU_URL , '[^?]*') " +
                " AND TWGBWMNU_SOURCE_IND = (SELECT NVL( MAX(TWGBWMNU_source_ind ),'B') " +
                " FROM TWGBWMNU WHERE TWGBWMNU_NAME = REGEXP_SUBSTR(twgrmenu.TWGRMENU_URL , '[^?]*') ) and TWGBWMNU_ENABLED_IND = 'Y')"
        sqlQuery = sqlQuery +
                " ))) ORDER BY twgrmenu_sequence"

        def randomSequence = RandomUtils.nextInt(1000);

        sql.eachRow(sqlQuery, [menuName, menuName, menuName]) {

            def mnu = new SelfServiceMenu()
            String  hideSSBHeaderURL =" "
            mnu.formName = it.twgrmenu_url
            mnu.pageName = it.twgrmenu_submenu_ind == "Y" ? null : it.twgrmenu_url
            mnu.name = it.twgrmenu_url_text
            mnu.caption = toggleSeparator(it.twgrmenu_url_text)
            mnu.pageCaption = mnu.caption
            mnu.type = it.twgrmenu_submenu_ind == "Y" ? 'MENU' : 'FORM'
            mnu.menu = menuTrail ? menuTrail : firstMenu
            mnu.parent = it.twgrmenu_name
            if (hideSSBHeader){
                String symbol = it.twgrmenu_url.indexOf(QUESTION_MARK)>-1? AMPERSAND:QUESTION_MARK
                hideSSBHeaderURL =it.twgrmenu_url+symbol+hideSSBHeaderComps
            }
            mnu.url = it.twgrmenu_db_link_ind == "Y" ? getBanner8SsUrlFromConfig() + it.twgrmenu_url :
                    (hideSSBHeader ? hideSSBHeaderURL : it.twgrmenu_url)
            mnu.seq = randomSequence + "-" + it.twgrmenu_sequence.toString()
            mnu.captionProperty = false
            mnu.sourceIndicator = it.twgrmenu_source_ind
            dataMap.add(mnu)

        };

        log.trace("ProcessMenu executed for Menu name:" + menuName)
        return dataMap

    }


    public def getParent(def menuName) {
        List parentList = []
        def sqlQuery
        Sql sql
        def pName
        def pCaption
        if (!menuName.equalsIgnoreCase("bmenu.P_MainMnu")) {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sqlQuery = "select twgbwmnu_back_url from twgbwmnu where twgbwmnu_name  = ?  and twgbwmnu_source_ind = " +
                    "( select nvl( max(twgbwmnu_source_ind ),'B') from twgbwmnu  where twgbwmnu_name = ?)    "

            sql.eachRow(sqlQuery, [menuName, menuName]) {
                pName = it.twgbwmnu_back_url
            }

            if (pName) {
                sqlQuery = "select twgrmenu_url_text from twgrmenu where twgrmenu_url  = ?  and twgrmenu_source_ind = " +
                        "( select nvl( max(twgrmenu_source_ind ),'B') from twgrmenu  where twgrmenu_url = ?)    "
                sql.eachRow(sqlQuery, [pName, pName]) {
                    pCaption = it.twgrmenu_url_text
                }
                parentList.add(name: pName, caption: pCaption)
            }
            //sql.close()

        }
        if (pName == null && !menuName.equalsIgnoreCase("bmenu.P_MainMnu")) {
            log.trace("SelfServiceMenuService.getParent  backlink url is $parentList ")
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sqlQuery = "select  TWGRMENU_NAME,TWGRMENU_URL_TEXT " +
                    " from twgrmenu   where   twgrmenu_enabled = 'Y'  " +
                    " and twgrmenu_url =  ? " +
                    " and twgrmenu_name not in  ? " +
                    " and  twgrmenu_source_ind =  ( select nvl( max(twgrmenu_source_ind ),'B') from twgrmenu " +
                    " where  twgrmenu_url = ?)    "

            log.trace("Process Menu started for nenu:" + menuName)
            sql.eachRow(sqlQuery, [menuName, "standalone_role_nav_bar", menuName]) {
                parentList.add(name: it.TWGRMENU_NAME, caption: it.TWGRMENU_URL_TEXT)
            }
            //sql.close()
        }
        log.trace("SelfServiceMenuService.getParent  url is $parentList ")
        return parentList
    }

    /**
     * Converts ~ to _ and _ to ~.
     * Aurora uses _ as Separators and will conflict with the menu names.
     * @param stringText
     * @return
     */

    private String toggleSeparator(String stringText) {
        if (stringText == null) return null;

        def oldSeparator = "_"
        def newSeparator = "~"
        stringText = stringText.contains(oldSeparator) ? stringText.replaceAll(oldSeparator, newSeparator) : stringText.replaceAll(newSeparator, oldSeparator)
    }


    /**
     * TO RETURN A LIST OF ALL ROLES AVAILABLE FOR THE PARTICULAR PIDM BASED ON GOVROLE AND TWGRROLE TABLE.
     * @param pidm
     * @return
     */
    private def getRoleCriteria(String pidm) {
        def allRoles = getAllRoles(pidm)
        def allRoleCriteria
        if (allRoles.size() > 0) {

            allRoles.each {
                if (it == allRoles.first())
                    allRoleCriteria = "('" + it.value + "'"
                else {
                    allRoleCriteria = allRoleCriteria + " ,'" + it.value + "'"
                }
            }
            allRoleCriteria = allRoleCriteria + ")"
        }
        return allRoleCriteria;
    }


    private def getAllRoles(String pidm) {
        Sql sql
        def rolesArray
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(FETCH_ROLES, [Sql.VARCHAR, pidm]) { result->
            rolesArray = result?.substring(1)?.split(":")
        }
        return rolesArray
    }


    // gets urls for BANNER 8
    private String getBanner8SsUrlFromConfig() {
        String url
        def mep = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
        url = getLocaleSpecificBanner8Url(mep)
        if(url == null){
           url = getWithoutLocaleSpecificBanner8Url(mep)
        }
        return url
    }


    /*Get banner8 url which is irrespective of locale
    Ex. banner8.SS.url ='http://<host_name>:<port_number>/<banner8>'*/


    private String getWithoutLocaleSpecificBanner8Url(mep) {
       String url
       if( mep && Holders.config?.mep?.banner8?.SS?.url) {
           url = Holders.config?.mep?.banner8?.SS?.url[mep]
       }else {
           url = Holders?.config?.banner8?.SS?.url
       }
       return url
    }


   /* get Locale Specific Banner 8 URL with fall back Mechanism
    Ex. if entry for fr_CA does not exist then entry for fr will be picked.if that also does not exist it will pick Default entry
    Ex. banner8.SS.locale.url =[default : 'http://<host_name>:<port_number>/<banner8>/default', fr_CA : 'http://<host_name>:<port_number>/<banner8>/fr_CA']*/


    private String getLocaleSpecificBanner8Url(mep) {
        String language
        String localeString
        String url
        def banner8SSLocaleUrls
        if (mep && Holders.config?.mep?.banner8?.SS?.locale?.url) {
            banner8SSLocaleUrls = Holders.config?.mep?.banner8?.SS?.locale?.url[mep]
        }else {
            banner8SSLocaleUrls = Holders?.config?.banner8?.SS?.locale?.url
        }
        Locale locale = LocaleContextHolder.getLocale()
        localeString = locale.toString()
        language = locale.getLanguage()
        url=banner8SSLocaleUrls.get(localeString)?: (banner8SSLocaleUrls.get(language)?:banner8SSLocaleUrls.get("default"))
        return url
    }

}
