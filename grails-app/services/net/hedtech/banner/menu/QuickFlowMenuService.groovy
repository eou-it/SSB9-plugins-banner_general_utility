/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu

import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder


class QuickFlowMenuService {
    def menuAndToolbarPreferenceService
    def sessionFactory

    def quickFlowSearch( String searchVal) {
        searchVal = searchVal.toUpperCase()
        def dataMap = []
        log.debug("QuickFlow menu search started")

        def mnuPrf = getMnuPref()
        def searchValWild = "%" +searchVal +"%"

        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )

        // this query determines if the data is in the temporary table for the database session
        def row = sql.firstRow('select 1 from gutmenu')
        //if the data is not found then load it again by running the menu package
        if (row == null) {
            sql.execute("Begin gukmenu.p_bld_prod_menu('MAG'); End;")
        }

        //sql.eachRow("SELECT DISTINCT GUBOBJS_NAME, GUBOBJS_DESC, GURCALL_FORM  FROM GUBOBJS, GURCALL WHERE (UPPER(GUBOBJS_NAME) LIKE ? OR UPPER(GUBOBJS_DESC) LIKE ?) AND GURCALL_CALL_CODE = GUBOBJS_NAME AND GUBOBJS_OBJT_CODE = 'QUICKFLOW' and gurcall_seqno = 1", [searchValWild, searchValWild])  {
        sql.eachRow("SELECT DISTINCT GTVCALL_CODE, GTVCALL_DESC, GURCALL_FORM FROM GTVCALL, GURCALL WHERE (UPPER(GTVCALL_CODE) LIKE ? OR UPPER(GTVCALL_DESC) LIKE ?) AND GURCALL_CALL_CODE = GTVCALL_CODE and gurcall_seqno = 1", [searchValWild, searchValWild])  {
            def mnu = new Menu()
            mnu.name = it.gtvcall_code
            mnu.page = it.gtvcall_code
            mnu.menu = "QUICKFLOW"
            if (it.GTVCALL_DESC != null)  {
                mnu.caption = it.gtvcall_desc.replaceAll(/\&/, "&amp;")
                mnu.pageCaption = mnu.caption
            }
            mnu.type = "QUICKFLOW"
            def uiVersion = getUiVersionForForm(it.gurcall_form)
            mnu.uiVersion = ((uiVersion == "B") || (uiVersion == "A")) ? "banner8admin" : "bannerHS"
            if((uiVersion != "B") && (uiVersion != "A")){
                mnu.url = getGubmoduUrlForHsType(it.gurcall_form)
            }
            mnu.captionProperty = mnuPrf

            dataMap.add( mnu )
        }

        log.debug( "QuickFlow menu search executed" )
        return dataMap
    }

    def quickflowPersonalMenu() {
        def dataMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Process Quickflow personal Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        sql.execute("Begin gukmenu.p_bld_pers_menu('MAG'); End;")
        log.debug("After gukmenu.p_bld_pers_menu sql.execute" )

        sql.eachRow("select gutpmnu_value, gutpmnu_label, gurcall_form, gutpmnu_level, gutpmnu_seq_no from gutpmnu, gubobjs, gurcall where gubobjs_objt_code = 'QUICKFLOW' " +
                " and gubobjs_name = substr(gutpmnu_value,11,length(gutpmnu_value)) and gurcall_call_code = gubobjs_name" +
                " AND gurcall_seqno = 1" +
                " order by gutpmnu_seq_no", {
            def mnu = new Menu()
            log.debug("Found : " +  it.gutpmnu_value)
            mnu.name = it.gutpmnu_value
            mnu.page = it.gutpmnu_value
            mnu.menu = it.gutpmnu_value.split("\\|")[1]
            if (it.gutpmnu_label != null)  {
                mnu.caption = it.gutpmnu_label.replaceAll(/\&/, "&amp;")
                mnu.pageCaption = mnu.caption
                if (mnuPrf)
                    mnu.caption = mnu.caption + " (" + mnu.name + ")"
            }
            mnu.level = it.gutpmnu_level
            mnu.type = it.gutpmnu_value.split("\\|")[0]
            mnu.parent = setParent(mnu.level, dataMap)
            mnu.seq = it.gutpmnu_seq_no
            def uiVersion = getUiVersionForForm(it.gurcall_form)
            mnu.uiVersion = ((uiVersion == "B") || (uiVersion == "A")) ? "banner8admin" : "bannerHS"
            if((uiVersion != "B") && (uiVersion != "A")){
                mnu.url = getGubmoduUrlForHsType(it.gurcall_form)
            }
            mnu.captionProperty = mnuPrf
            dataMap.add(mnu)
        });
        log.debug("Process Quickflow Personal Menu executed" )
        return dataMap
    }

    def quickflowMenu() {
        def dataMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Process Quickflow Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        // this query determines if the data is in the temporary table for the database session
        def row = sql.firstRow('select 1 from gutmenu')
        //if the data is not found then load it again by running the menu package
        if (row == null) {
            sql.execute("Begin gukmenu.p_bld_prod_menu('MAG'); End;")
        }
        sql.eachRow("select gutmenu_value, gutmenu_desc, gurcall_form, gutmenu_level, gutmenu_objt_code, gutmenu_prior_obj, gutmenu_seq_no from gutmenu, gubobjs, gurcall where gubobjs_objt_code = 'QUICKFLOW' " +
                " AND gubobjs_name = GUTMENU_VALUE AND GURCALL_CALL_CODE = GUBOBJS_NAME" +
                " AND gurcall_seqno = 1" +
                " order by gutmenu_seq_no", {
            def mnu = new Menu()
            log.debug("Found : " +  it.gutmenu_value)
            mnu.name = it.gutmenu_value
            mnu.page = it.gutmenu_value
            mnu.menu = "QUICKFLOW"
            if (it.gutmenu_desc != null)  {
                mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
                mnu.pageCaption = mnu.caption
                if (mnuPrf)
                    mnu.caption = mnu.caption + " (" + mnu.name + ")"
            }
            mnu.level = it.gutmenu_level
            mnu.type = it.gutmenu_objt_code
            mnu.parent = it.gutmenu_prior_obj
            mnu.seq = it.gutmenu_seq_no
            def uiVersion = getUiVersionForForm(it.gurcall_form)
            mnu.uiVersion = ((uiVersion == "B") || (uiVersion == "A")) ? "banner8admin" : "bannerHS"
            if((uiVersion != "B") && (uiVersion != "A")){
                mnu.url = getGubmoduUrlForHsType(it.gurcall_form)
            }
            mnu.captionProperty = mnuPrf
            dataMap.add(mnu)
        });
        log.debug("Process Quickflow Menu executed" )
        return dataMap
    }

    private String getUiVersionForForm(String page) {
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )

        String uiVersion

        sql.eachRow("select gubobjs_ui_version from gubobjs where gubobjs_name = ?", [page]) {
            uiVersion =  it.gubobjs_ui_version
        }

        return uiVersion
    }
    private String getGubmoduUrlForHsType(String page) {

        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )

        String url

        sql.eachRow("select gubmodu_url from gubmodu, gubpage where gubmodu_code = gubpage_gubmodu_code  and gubpage_code = ?", [page]) {
            url =  it.gubmodu_url
        }

        return url

    }
    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */
    private boolean getMnuPref() {
        boolean isMnuPref = false
        try {
            SecurityContextHolder.context?.authentication?.principal?.pidm

            if (menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator == 'B' ||
                    menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator == 'Y')
                isMnuPref = true
        }
        catch (Exception e) {
            log.error("ERROR: Could not get menu preferences. $e")
            throw e
        }
        return isMnuPref
    }

    private String setParent(def level, def map) {
        String parent
        if (level == 1)
            return parent
        def notFound = true;
        map.reverseEach {
            if (notFound && it.level < level) {
                parent = it.formName
                notFound = false
            }
        }
        return parent
    }
}
