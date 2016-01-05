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

        sql.eachRow("SELECT DISTINCT GUBOBJS_NAME, GUBOBJS_DESC, GURCALL_FORM  FROM GUBOBJS, GURCALL WHERE (UPPER(GUBOBJS_NAME) LIKE ? OR UPPER(GUBOBJS_DESC) LIKE ?) AND GURCALL_CALL_CODE = GUBOBJS_NAME AND GUBOBJS_OBJT_CODE = 'QUICKFLOW' and gurcall_seqno = 1", [searchValWild, searchValWild])  {
            def mnu = new Menu()
            mnu.name = it.gubobjs_name
            mnu.page = it.gubobjs_name
            mnu.menu = "QUICKFLOW"
            if (it.gubobjs_desc != null)  {
                mnu.caption = it.gubobjs_desc.replaceAll(/\&/, "&amp;")
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
        sql.eachRow("select gutmenu_value, gutmenu_desc, GURCALL_FORM, GUTMENU_LEVEL, GUTMENU_OBJT_CODE, GUTMENU_PRIOR_OBJ, GUTMENU_SEQ_NO from GUTMENU, GUBOBJS, GURCALL WHERE GUBOBJS_OBJT_CODE = 'QUICKFLOW' " +
                " AND gubobjs_name = GUTMENU_VALUE AND GURCALL_CALL_CODE = GUBOBJS_NAME" +
                " AND gurcall_seqno = 1" +
                " order by gutmenu_seq_no", {
            def mnu = new Menu()
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
}
