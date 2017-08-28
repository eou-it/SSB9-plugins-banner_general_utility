/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

/**
 * SelfService controller returns menu as XML format
 * Request parameters
 *  menuName current menu
 */
class SelfServiceMenuController {

    def selfServiceMenuService
    def mnuLabel = "Banner"
    private final log = Logger.getLogger(getClass())

    def data = {
        def menuType
        def mnuParams
        def list
        def currentMenu
        def menuName
        def menu

        if (request.parameterMap["menuName"] != null) {
            menuName = request.parameterMap["menuName"][0]
        }
        if (request.parameterMap["menu"] != null) {
            menu = request.parameterMap["menu"][0]
        }

        def pidm

        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            //non logged in user
            pidm = null
        }



        list = getMenu(menuName, menu, pidm )

        def sw = new StringWriter()
        def xml = new groovy.xml.MarkupBuilder(sw)
        xml.NavigationEntries {
            list.each { a ->
                def pageName = a.pageName ? a.pageName : "null"
                NavigationEntryValueObject(id: a.seq, menu: a.menu, form: a.formName, path: pageName, name: a.name, caption: a.caption, type: a.type, url: a.url, parent: a.parent, params: mnuParams, captionProperty: a.captionProperty, pageCaption: a.pageCaption)
            }
        }
        header "Expires", "-1"
        render(text: sw.toString(), contentType: "text/xml", encoding: "UTF-8")

    }


    /**
     * Driver for banner menu
     */
    private def getMenu( menuName, menuTrail, pidm ) {
        if (log.isDebugEnabled()) log.debug("Menu Controller getmenu")

        def menulist
        def currentMenu = menuName ? menuName : "Banner"
        currentMenu = pidm ? currentMenu + pidm : currentMenu

        if (session[currentMenu] == null) {
            menulist = selfServiceMenuService.bannerMenu(menuName, menuTrail, pidm)
            session[currentMenu] = updateURL(menulist)
        }

        return session[currentMenu]
    }

    private def updateURL(def mnuList){
        mnuList.each{ SelfServiceMenu ->
            if(SelfServiceMenu.url.indexOf(MEPCODE) >- 1 && session["mep"] != null){
                SelfServiceMenu.url=SelfServiceMenu.url.replace("{mepCode}", session["mep"])
            }
            if(session['hideSSBHeaderComps'] != null && session['hideSSBHeaderComps'].trim() == 'true'){
                String symbol = SelfServiceMenu.url.indexOf(QUESTION_MARK)>-1? AMPERSAND:QUESTION_MARK
                SelfServiceMenu.url = SelfServiceMenu.url+symbol+hideSSBHeaderComps
            }
        }
        return mnuList
    }


    static final String AMPERSAND="&"
    static final String QUESTION_MARK="?"
    static final String hideSSBHeaderComps="hideSSBHeaderComps=true"
    static final String MEPCODE="{mepCode}"
}
