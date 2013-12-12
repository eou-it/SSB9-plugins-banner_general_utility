/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.converters.JSON
import net.hedtech.banner.menu.Menu
import net.hedtech.banner.utility.CommonUIApp
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommonMenuController {
    def menuService
    def selfServiceMenuService
    def personalPreferenceService
    def grailsApplication

    private final log = Logger.getLogger(getClass())

    static final String BANNER_INB_URL = "bannerInbUrl"
    static final String MAGELLAN = "MAGELLAN"
    static final String SERVER_DESIGNATION = "SERVER_DESIGNATION"
    static final String INB = "INB"
    static final String BANNER_TITLE = "Banner"
    static final String MENU_TYPE_BANNER = "Banner"
    static final String MENU_TYPE_PERSONAL = "Personal"
    static final String MENU_TYPE_SELF_SERVICE = "SelfService"
    static final String MY_BANNER_TITLE = "My Banner"
    static final String BANNER_SELF_SERVICE_TITLE = "Banner Self-Service"
    static final String PERSONAL_COMBINED_MENU_LIST = "personalCombinedMenuList"
    static final String COMBINED_MENU_LIST = "combinedMenuList"


    def data = {
        if(request.parameterMap["q"]){
            search()
        } else if(request.parameterMap["s"]){
            getMenuStructure()
        } else {
            list()
        }
    }

    def getMenuStructure = {
        String path
        Map subMenu
        List nodes = []
        List finalList = []
        Map finalMenu

        path = request.parameterMap["s"][0]
        nodes = path.split('/')
        String root = nodes[0]
        nodes.each { a ->
            if(a == BANNER_TITLE){
                subMenu = getSubMenuData(root, root, root)
            } else {
                subMenu = getSubMenuData(a, root, a)
            }
            finalList.add(subMenu)
        }
        subMenu = [ name:"root", caption:"root", items: finalList ]
        finalMenu = [ data: subMenu ]
        // Support JSON-P callback
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${finalMenu as JSON});", contentType: "text/javascript"
        } else {
            render finalMenu as JSON
        }
    }

    def list = {
        String mnuName
        String mnuType
        String caption
        Map subMenu
        Map finalMenu

        if (request.parameterMap["menu"])
            mnuName = request.parameterMap["menu"][0]

        if(request.parameterMap["type"])
            mnuType = request.parameterMap["type"][0]

        if(request.parameterMap["caption"])
            caption = request.parameterMap["caption"][0]


        subMenu = getSubMenuData(mnuName, mnuType, caption)
        finalMenu = [ data: subMenu ]

        // Support JSON-P callback
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${finalMenu as JSON});", contentType: "text/javascript"
        } else {
            render finalMenu as JSON
        }


    }
    private def getSubMenuData(String mnuName,String mnuType,String caption ){

        Map subMenu
        List adminList
        List personalList
        List selfServiceList
        List finalList
        if (mnuName){

            if (mnuType == MENU_TYPE_BANNER){
                adminList = getMenuData(mnuName)
                finalList = adminList

            } else if (mnuType == MENU_TYPE_PERSONAL){
                personalList = getPersonalMenuData(mnuName)
                finalList = personalList
            } else {
                selfServiceList = getSelfServiceMenuData(mnuName)
                finalList = selfServiceList
            }
            subMenu = [name:mnuName,caption:caption , items: finalList]

        } else {
            finalList =  rootMenu()
            subMenu = [ name:"root", caption:"root", items: finalList ]
        }
        return subMenu
    }

    private def rootMenu = {
        Map finalMenu
        Map subMenu
        Map adminMenu
        Map personalMenu
        Map selfServiceMenu
        List adminList =[]
        List personalList =[]
        List selfServiceList =[]
        List finalList = []

        adminList = getMenuData()
        //selfServiceList = getSelfServiceMenuData()
        selfServiceList = []
        personalList = getPersonalMenuData()
        adminMenu = [ name:BANNER_TITLE, caption:BANNER_TITLE, page:BANNER_TITLE ,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_BANNER+"&menu="+BANNER_TITLE+"&caption="+BANNER_TITLE,type: "MENU",items: null,menu:BANNER_TITLE]
        personalMenu = [ name:MY_BANNER_TITLE, caption:MY_BANNER_TITLE, page:MY_BANNER_TITLE ,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_PERSONAL+"&menu="+MY_BANNER_TITLE+"&caption="+MY_BANNER_TITLE,type: "MENU",items: null,menu:MY_BANNER_TITLE]
        selfServiceMenu = [ name:BANNER_SELF_SERVICE_TITLE, caption:BANNER_SELF_SERVICE_TITLE, page:BANNER_SELF_SERVICE_TITLE,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_SELF_SERVICE+"&menu="+BANNER_SELF_SERVICE_TITLE+"&caption="+BANNER_SELF_SERVICE_TITLE,type: "MENU", items: null,menu:BANNER_SELF_SERVICE_TITLE ]
        if (adminList.size() != 0)
            finalList.add(adminMenu)

        if (personalList.size() != 0)
            finalList.add(personalMenu)

        if(selfServiceList.size() != 0)
            finalList.add(selfServiceMenu)

        return finalList
    }

    def search = {

        Map subMenu
        Map finalMenu
        List adminList
        //def selfServiceList
        List finalList = []
        String searchVal

        if(request.parameterMap["q"])
            searchVal = request.parameterMap["q"][0]
        if(searchVal){
            adminList = getAdminMenuSearchResults(searchVal)
            //selfServiceList = getSelfServiceMenuSearchResults(searchVal)
            finalList.addAll(adminList)
            //finalList.addAll(selfServiceList)
        }
        subMenu = [ name:"root", caption:"root", items: finalList ]
        finalMenu = [ data: subMenu ]
        // Support JSON-P callback
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${finalMenu as JSON});", contentType: "text/javascript"
        } else {
            render finalMenu as JSON
        }
    }

    private def getMenuData(String mnuName){
        List list
        if ((mnuName != null) && (mnuName != BANNER_TITLE)) {
            list = getMenuList(mnuName)
        } else {
            list = getFirstLevelMenuList()
        }
        list.each {it -> it.menu = getParent(getMenu(),it,BANNER_TITLE)}
        return composeMenuStructure(list, MENU_TYPE_BANNER)
    }

    private def getMenu() {
        List list

        log.debug("Menu Controller getmenu")
        if (session[COMBINED_MENU_LIST] == null) {
            list = menuService.bannerCombinedMenu()
            session[COMBINED_MENU_LIST] = list
        }
        else {
            list = session[COMBINED_MENU_LIST]
        }
        return list
    }

    private def getFirstLevelMenuList() {
        List mnuList
        log.debug("Menu Controller getmenu")
        mnuList = getMenu()
        def childMenu = mnuList.findAll{a -> a.level == 1 }
        if (childMenu.size() == 0)  {
            Menu mnu = new Menu()
            childMenu.add(mnu)
        }
        return childMenu
    }

    private def getMenuList(String menuName) {
        List mnuList
        List childMenu  =[]
        log.debug("Menu Controller getmenulist")
        mnuList = getMenu()

        for (it in mnuList) {
            if(it.name == menuName){
                childMenu = getChildren(mnuList, it)
                break;
            }
        }
        return childMenu
    }

    private def getParent(List list, Menu mnu, String rootMenu) {
        String parentChain
        def level = mnu.level
        List temp = list.findAll{ it -> it.seq <= mnu.seq }
        temp.reverseEach {
            if (  it.level < level )  {
                level = it.level
                if (parentChain == null)
                    parentChain = it.formName
                else
                if (it.caption  != null)
                    parentChain = it.formName + "/" + parentChain
            }
        }
        if (parentChain != null)
            return rootMenu + "/" + parentChain
        else
            return rootMenu
    }

    private def getChildren(List list, Menu mnu) {
        List temp = list.findAll{ it -> it.seq > mnu.seq && it.parent == mnu.name}
        return removeDuplicateEntries(temp)
    }

    private def getPersonalMenu() {
        List list
        log.debug("Menu Controller getmenu")
        String pidm
        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            pidm = null
            log.debug("Non logged in user.")
        }

        String personalMenuList = pidm ? PERSONAL_COMBINED_MENU_LIST + pidm : PERSONAL_COMBINED_MENU_LIST
        if (session[personalMenuList] == null) {
            list = menuService.personalCombinedMenu()
            session[personalMenuList] = list
        }
        else {
            list = session[personalMenuList]
        }
        return list
    }

    private def getFirstLevelPersonalMenuList() {
        List mnuList
        log.debug("Menu Controller getmenulist")
        mnuList = getPersonalMenu()
        def childMenu = mnuList.findAll{a -> a.level == 1 }
        if (childMenu.size() == 0)  {
            Menu mnu = new Menu()
            childMenu.add(mnu)
        }
        return childMenu
    }

    private def getPersonalMenuList(String menuName) {
        List mnuList
        List childMenu  =[]
        log.debug("Menu Controller getmenulist")
        mnuList = getPersonalMenu()

        for (it in mnuList) {
            if(it.name == menuName){
                childMenu = getChildren(mnuList, it)
                break;
            }
        }
        return childMenu
    }

    private def getPersonalMenuData(String mnuName){
       List list
       if ((mnuName != null) && (mnuName != MY_BANNER_TITLE)) {
            list = getPersonalMenuList(mnuName)
       } else {
            list = getFirstLevelPersonalMenuList()
       }
       list.each {it -> it.menu = getParent(getPersonalMenu(),it,MY_BANNER_TITLE)}
       return composeMenuStructure(list, MENU_TYPE_PERSONAL)
    }

    private def getSelfServiceMenu( menuName, menuTrail, pidm ) {
        if (log.isDebugEnabled()) log.debug("Menu Controller getmenu")

        String currentMenu = menuName ? menuName : BANNER_SELF_SERVICE_TITLE
        currentMenu = pidm ? currentMenu + pidm : currentMenu
        if (session[currentMenu] == null) {
            session[currentMenu] = selfServiceMenuService.combinedMenu(menuName, menuTrail, pidm)
        }
        return session[currentMenu]
    }

    private def getSelfServiceMenuData(String mnuName){
        List list
        String menuName
        String menu
        List finalList = []

        if ((mnuName != null) && (mnuName != BANNER_SELF_SERVICE_TITLE)) {
            menuName = mnuName
        }
        def pidm

        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("Non User has logged in")
            pidm = null
        }

        list = getSelfServiceMenu(menuName, menu, pidm)

        list.each {a ->
            if (a.type == "MENU"){
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() + "/commonMenu?type="+MENU_TYPE_SELF_SERVICE+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)
            }  else {
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url:a.url,type: "PAGE",menu:a.menu)
            }

        }

        return finalList
    }


    private def getAdminMenuSearchResults(searchVal){

        List list = menuService.gotoCombinedMenu(searchVal)
        list = removeDuplicateEntries(list)
        list.each {it -> it.menu = getParent(getMenu(),it,BANNER_TITLE)}
        return composeMenuStructure(list, MENU_TYPE_BANNER)
    }

    private def getSelfServiceMenuSearchResults(searchVal){
        List finalList = []
        String pidm

        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("Non User has logged in")
            pidm = null
        }
        List list = selfServiceMenuService.gotoCombinedMenu(searchVal, pidm)
        list.each {a ->
            if (a.type == "MENU"){
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() + "/commonMenu?type="+MENU_TYPE_SELF_SERVICE+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)
            }  else {
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url:a.url,type: "PAGE",menu:a.menu)
            }
        }

        return finalList
    }

    private def getServerURL() {
        boolean includePort = true
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = (new org.springframework.security.web.PortResolverImpl()).getServerPort(request)
        String contextPath = request.getContextPath();
        boolean inHttp = "http".equals(scheme.toLowerCase());
        boolean inHttps = "https".equals(scheme.toLowerCase());

        if (inHttp && (serverPort == 80)) {
            includePort = false;
        } else if (inHttps && (serverPort == 443)) {
            includePort = false;
        }
        String redirectUrl = scheme + "://" + serverName + ((includePort) ? (":" + serverPort) : "") + contextPath;
        return redirectUrl
    }

    private def getBannerInbUrl(){
        String bannerInbUrl
        def personalPref

        if(!session.getAttribute(BANNER_INB_URL)){
            if (!isSsbEnabled()){
                personalPref = personalPreferenceService.fetchPersonalPreference(MAGELLAN,SERVER_DESIGNATION,INB)[0]
                session.setAttribute(BANNER_INB_URL, personalPref.value)
            }
        }
        bannerInbUrl = session.getAttribute(BANNER_INB_URL)
        return bannerInbUrl
    }

    private def isSsbEnabled() {
        grailsApplication.config.ssbEnabled instanceof Boolean ? grailsApplication.config.ssbEnabled : false
    }

    private def removeDuplicateEntries(list){
        Map map = [:]
        list.each {it ->
            if(map.get(it.caption) ==null) map.put(it.caption, it)
        }
       map.values().asList()
    }

    private def composeMenuStructure(list, type){
        List finalList = []
        list.each {a ->
            if (a.type == "MENU")
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+type+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)

            if (a.type == "FORM" ){
                if (a.uiVersion =="banner8admin")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu)
                else
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page + "&pageName="+ a.caption +"&global_variables={{params}}" + (CommonUIApp.isEnabled() ?  "&CommonUIApp=true" : "" ),type: "PAGE",menu:a.menu)
            }
        }
        return finalList
    }

}
