/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.converters.JSON
import net.hedtech.banner.menu.Menu
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.utility.GeneralMenu
import net.hedtech.banner.security.XssSanitizer
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommonMenuController {
    def menuService
    def selfServiceMenuService
    def personalPreferenceService
    def grailsApplication
    def jobsMenuService
    def quickFlowMenuService

    private final log = Logger.getLogger(getClass())

    static final String BANNER_INB_URL = "bannerInbUrl"
    static final String MAGELLAN = "MAGELLAN"
    static final String SERVER_DESIGNATION = "SERVER_DESIGNATION"
    static final String INB = "INB"
    static final String BANNER_TITLE = "Banner"
    static final String MENU_TYPE_BANNER = "Banner"
    static final String MENU_TYPE_PERSONAL = "Personal"
    static final String BANNER_HS_PARENT = "bannerHS"
    static final String BANNER_INB_PARENT = "banner8admin"
    static final String ZK_PLATFORM_CODE = "ADMZK"
    static final String MENU_TYPE = "MENU"
    static final String FORM_TYPE = "FORM"
    static final String JOB_TYPE = "JOBS"
    static final String QUICKFLOW_TYPE = "QUICKFLOW"
//    static final String MENU_TYPE_SELF_SERVICE = "SelfService"
    static final String MY_BANNER_TITLE = "My Banner"
    static final String SSB_BANNER_TITLE = "Banner Self-Service"
    static final String MENU_TYPE_SSB = "SSB"

//    static final String BANNER_SELF_SERVICE_TITLE = "Banner Self-Service"
    static final String PERSONAL_COMBINED_MENU_LIST = "personalCombinedMenuList"
    static final String COMBINED_MENU_LIST = "combinedMenuList"


    def data = {
        if (params.refresh == 'Y'){
           keepAlive()
        } else if(request.parameterMap["q"]){
            search()
        } else {
            list()
        }
    }

    def list = {
        String mnuName
        String mnuType
        String caption
        Map subMenu
        Map finalMenu
        String callback = XssSanitizer.sanitize(params.callback)

        if (request.parameterMap["menu"])
            mnuName = XssSanitizer.sanitize(request.parameterMap["menu"][0])

        if(request.parameterMap["type"])
            mnuType = XssSanitizer.sanitize(request.parameterMap["type"][0])

        if(request.parameterMap["caption"])
            caption = XssSanitizer.sanitize(request.parameterMap["caption"][0])


        if (!session."disableAdmin") {
            subMenu = getSubMenuData(mnuName, mnuType, caption)
        } else{
            subMenu = [ name:"root", caption:"root", items: [] ]
        }

        //finalMenu = [ data: subMenu ]

        // Support JSON-P callback
        if( callback ) {
            render text: "$callback && $callback(${subMenu as JSON});", contentType: "text/javascript"
        } else {
            render subMenu as JSON
        }


    }
    private def getSubMenuData(String mnuName,String mnuType,String caption ){

        Map subMenu
        List adminList
        List personalList
//        List selfServiceList
        List finalList
        if (mnuName){

            if (mnuType == MENU_TYPE_BANNER){
                adminList = getMenuData(mnuName)
                finalList = adminList

            } else if (mnuType == MENU_TYPE_PERSONAL){
                personalList = getPersonalMenuData(mnuName)
                finalList = personalList
            }

//            else {
//                selfServiceList = getSelfServiceMenuData(mnuName)
//                finalList = selfServiceList
//            }
            subMenu = [name:mnuName,caption:caption,items: finalList, _links:getLinks(mnuName,mnuType,caption)]

        } else {
            finalList =  rootMenu()
            subMenu = [ name:"root", caption:"root", items: finalList , _links:getLinks(mnuName,mnuType,caption)]
        }
        return subMenu
    }

    private def getLinks(String mnuName,String mnuType,String caption) {

        Map self
        Map parent
        String pName
        String pCaption

        def parentMnu = getParent(mnuName, mnuType)
        if(parentMnu !=null){
            pName = parentMnu.name
            pCaption = parentMnu.caption
        } else if(parentMnu == null && mnuName != BANNER_TITLE && mnuType == MENU_TYPE_BANNER ){
            pName = BANNER_TITLE
            pCaption =BANNER_TITLE
        } else if(parentMnu == null && mnuName != MY_BANNER_TITLE && mnuType == MENU_TYPE_PERSONAL ){
            pName = MY_BANNER_TITLE
            pCaption =MY_BANNER_TITLE
        } else {
            pName = "root"
            pCaption = "root"
        }


        self = [href:getMenuLink(mnuName,caption,mnuType)]
        parent = [name:pName, caption: pCaption, href: getMenuLink(pName, pCaption,mnuType )]

        return [self: self, parent:parent]
    }
    private def rootMenu = {
        Map finalMenu
        Map subMenu
        Map adminMenu
        Map personalMenu
        Map ssbMenu
        List adminList =[]
        List personalList =[]
        List finalList = []
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            if (user.oracleUserName)  {
                adminList = getMenuData()
                personalList = getPersonalMenuData()
                adminMenu = [ name:BANNER_TITLE, caption:BANNER_TITLE, page:BANNER_TITLE ,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_BANNER+"&menu="+BANNER_TITLE+"&caption="+BANNER_TITLE,type: "MENU",items: null,menu:BANNER_TITLE]
                personalMenu = [ name:MY_BANNER_TITLE, caption:MY_BANNER_TITLE, page:MY_BANNER_TITLE ,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_PERSONAL+"&menu="+MY_BANNER_TITLE+"&caption="+MY_BANNER_TITLE,type: "MENU",items: null,menu:MY_BANNER_TITLE]
                if (adminList.size() != 0)
                    finalList.add(adminMenu)

                if (personalList.size() != 0)
                    finalList.add(personalMenu)
            }
        }
        return finalList
    }

    def search = {

        Map subMenu
        List adminList
        List quickFlowList
        List finalList = []
        String searchVal
        String callback = XssSanitizer.sanitize(params.callback)

        if(request.parameterMap["q"])
            searchVal = XssSanitizer.sanitize(request.parameterMap["q"][0])

        if (!session."disableAdmin") {

            if (searchVal && searchVal.length() < 3) {
                quickFlowList = getQuickflowLessThanThreeCharSearchResults(searchVal)
                finalList.addAll(quickFlowList)
            } else {
                if (searchVal) {
                    adminList = getAdminMenuSearchResults(searchVal)
                    finalList.addAll(adminList)
                    //it only applies after workflow task
                    if (searchVal.equals("WORKFLOW")) {
                        clearWorkflowArguments()
                    }
                }
            }
        }

        subMenu = [ name:"root", caption:"root", items: finalList ]
        // Support JSON-P callback
        if( callback ) {
            render text: "${callback} && ${callback}(${subMenu as JSON});", contentType: "text/javascript"
        } else {
            render subMenu as JSON
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

    private def getMenuItem(String mnuName, String mnuType) {
        List mnuList
        def mnu
        log.debug("Menu Controller getParentUrl")
        if(mnuType == MENU_TYPE_PERSONAL){
            mnuList = getPersonalMenu()
        } else {
            mnuList = getMenu()
        }

        for (it in mnuList) {
            if(it.name == mnuName){
                mnu = it
                break;
            }
        }
        return mnu
    }

    private def getParent(String mnuName, String mnuType) {
        List mnuList
        def mnu
        def parent
        log.debug("Menu Controller getParentUrl")
        if ((mnuName != null) && (mnuName != BANNER_TITLE) && (mnuName != MY_BANNER_TITLE)) {

            if(mnuType == MENU_TYPE_PERSONAL){
                mnuList = getPersonalMenu()
            } else {
                mnuList = getMenu()
            }

            mnu = getMenuItem(mnuName, mnuType)

            for (it in mnuList) {
                if(it.seq <= mnu.seq && it.name == mnu.parent){
                    parent = it
                    break;
                }
            }
        }
        return parent
    }

    private def getMenuLink(String mnuName,String caption,String type) {
        def mnu
        String link
        log.debug("Menu Controller getParentUrl")
        if ((mnuName != null) && (mnuName != BANNER_TITLE) && (mnuName != MY_BANNER_TITLE)) {
            mnu = getMenuItem(mnuName, type)

            if(mnu != null)
                link = getServerURL() +"/commonMenu?type="+type+"&menu="+mnu.name+"&caption="+caption
            else
                link = getServerURL() +"/commonMenu?"

        } else if(mnuName == BANNER_TITLE) {
            link = getServerURL() +"/commonMenu?type="+type+"&menu="+BANNER_TITLE+"&caption="+caption

        } else if(mnuName == MY_BANNER_TITLE){
            link = getServerURL() +"/commonMenu?type="+type+"&menu="+MY_BANNER_TITLE+"&caption="+caption
        } else {

            link = getServerURL() +"/commonMenu?"
        }
        return link
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
            list.addAll(quickFlowMenuService.quickflowPersonalMenu())
            Collections.sort(list, new Comparator<Menu>() {
                @Override
                public int compare(final Menu object1, final Menu object2) {
                    return object1.getSeq().compareTo(object2.getSeq());
                }
            } );
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

    private def getQuickflowLessThanThreeCharSearchResults(searchVal){

        List list = quickFlowMenuService.quickFlowLessThan3CharSearch(searchVal)
        list = removeDuplicateEntries(list)
        list.each {it -> it.menu = getParent(getMenu(),it,BANNER_TITLE)}
        return composeMenuStructure(list, MENU_TYPE_BANNER)
    }


    private def getAdminMenuSearchResults(searchVal){

        List list = menuService.gotoCombinedMenu(searchVal)
        list = removeDuplicateEntries(list)
        return composeMenuStructure(list, MENU_TYPE_BANNER)
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
                personalPref = personalPreferenceService.fetchPersonalPreference(MAGELLAN,SERVER_DESIGNATION,INB)[0]
                session.setAttribute(BANNER_INB_URL, personalPref.value)
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
        def javaFormsURL = jobsMenuService.getPlatCodeJavaFormsUrl()

        List finalList = []
        list.each {a ->
            if (a.type == "MENU")
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+type+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)

            if (a.type == "FORM" ){
                if (a.uiVersion =="banner8admin")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                else
                    if(a.platCode == ZK_PLATFORM_CODE) {
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page + "&global_variables={{params}}&GeneralMenu=true",type: "PAGE",menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                    } else {
                        if  (session["wf_args"]){ //Only first time invoked
                            def s = a.url +"?wf_args=" + session["wf_args"]
                            finalList.add(name:a.name,page:a.page,caption:a.caption,parent:BANNER_HS_PARENT,url: s,type: "PAGE",menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                        }else{
                            finalList.add(name:a.name,page:a.page,caption:a.caption,parent:BANNER_HS_PARENT,url: a.url +"?form="+a.formName+"&ban_args={{params}}&ban_mode=xe",type: "PAGE",menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                        }
                    }
            }else if(a.type == JOB_TYPE){
                if (!javaFormsURL) {
                    finalList.add(name: a.name, page: a.page, caption: a.caption, parent: "banner8admin", url: getBannerInbUrl() + "?otherParams=launch_form=" + a.page + "+ban_args={{params}}+ban_mode=xe", type: "PAGE", menu: a.menu, pageCaption: a.pageCaption, captionProperty: a.captionProperty)
                } else{
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:BANNER_HS_PARENT,url: javaFormsURL +"?form="+a.formName+"&ban_args={{params}}&ban_mode=xe",type: "PAGE",menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                }
            } else if(a.type == QUICKFLOW_TYPE) {
                def hsUrl = quickFlowMenuService.getGubmoduUrlForHsTypeFromQuickFlowCode(a.name)
                if(hsUrl) {
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:BANNER_HS_PARENT,url: hsUrl +"?form="+ a.name +"&ban_args={{params}}&ban_mode=xe",type: QUICKFLOW_TYPE,menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                } else {
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:BANNER_INB_PARENT,url: getBannerInbUrl() + "?otherParams=launch_form="+ a.page +"+ban_args={{params}}+ban_mode=xe",type: QUICKFLOW_TYPE,menu:a.menu, pageCaption:a.pageCaption, captionProperty: a.captionProperty)
                }
            }
        }
        return finalList
    }

    private def keepAlive(){
        String callback = XssSanitizer.sanitize(params.callback)

        if( callback ) {
            render text: "$callback && $callback({'result':'I am Alive'});", contentType: "text/javascript"
        } else {
            render "I am Alive"
        }
    }

    private def clearWorkflowArguments(){
        if (session["wf_args"]) session["wf_args"] = null
    }

}
