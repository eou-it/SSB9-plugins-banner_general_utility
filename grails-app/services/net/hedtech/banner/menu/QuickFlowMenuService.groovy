/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu

import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder


class QuickFlowMenuService {
    def menuAndToolbarPreferenceService
    def sessionFactory
    def grailsApplication

    def quickFlowLessThan3CharSearch( String searchVal) {
        searchVal = searchVal.toUpperCase()
        def dataMap = []
        log.debug("QuickFlow menu search started")

        def mnuPrf = getMnuPref()

        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )

        sql.eachRow("""
                       select * from (
                       select distinct DECODE(gutmenu_value,gutmenu_value,a.gubobjs_name) value,
                       gutmenu_level,gutmenu_seq_no,gubobjs_ui_version,gutmenu_prior_obj,DECODE(gutmenu_objt_code,gutmenu_objt_code,a.gubobjs_objt_code) objt_code,
                       DECODE(gutmenu_desc,gutmenu_desc,a.gubobjs_desc) description,
                       gubpage_code, gubpage_name, gubmodu_url,gubmodu_code,gubmodu_plat_code
                       from gutmenu,gubmodu, gubpage,gubobjs a
                       where gutmenu_value  = gubpage_code (+)
                       AND  a.gubobjs_name = gutmenu_value(+)
                       and gubpage_gubmodu_code  = gubmodu_code(+)
                       )
                       WHERE  (upper(value) = ?
                       OR upper(description) = ? OR upper(gubpage_name) = ?)
                       AND objt_code = 'QUICKFLOW'""" +
                " order by objt_code, value"
                ,[searchVal,searchVal,searchVal] ) {

            def mnu = new Menu()
            mnu.name = it.value
            mnu.page = it.value
            mnu.menu = "QUICKFLOW"
            if (it.description != null)  {
                mnu.caption = it.description.replaceAll(/\&/, "&amp;")
                mnu.pageCaption = mnu.caption
            }
            mnu.type = "QUICKFLOW"
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
            mnu.name = it.gutpmnu_value.split("\\|")[1]
            mnu.page = it.gutpmnu_value.split("\\|")[1]
            mnu.menu = it.gutpmnu_value.split("\\|")[0]
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
        String url

        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )

        String gubmoduUrl
        String moduleCode
        sql.eachRow("select gubmodu_code, gubmodu_url from gubmodu, gubpage where gubmodu_code = gubpage_gubmodu_code  and gubpage_code = ?", [page]) {
            gubmoduUrl =  it.gubmodu_url
            moduleCode = it.gubmodu_code
        }

        if(gubmoduUrl) {
            url = getModuleUrlFromConfig(moduleCode) ?: gubmoduUrl
        }

        return url

    }
    public String getGubmoduUrlForHsTypeFromQuickFlowCode(String quickflowCode) {
        String url

        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )

        String gubmoduUrl
        String moduleCode
        sql.eachRow("select gubmodu_code, gubmodu_url from gubmodu, gubpage, gurcall where gurcall_call_code = ? and  gurcall_seqno = 1 and gurcall_form = gubpage_code and gubmodu_code = gubpage_gubmodu_code and gubmodu_plat_code = 'ADMJF'", [quickflowCode]) {
            gubmoduUrl =  it.gubmodu_url
            moduleCode = it.gubmodu_code
        }
        if(gubmoduUrl) {
            url = getModuleUrlFromConfig(moduleCode) ?: gubmoduUrl
        }

        return url

    }

    private String getModuleUrlFromConfig(String moduleCode){
        String url
        if (moduleCode && grailsApplication.config?.module?.deployments){
            url = grailsApplication.config.module.deployments[moduleCode]
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
