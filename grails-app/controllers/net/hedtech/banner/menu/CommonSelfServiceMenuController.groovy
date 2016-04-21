package net.hedtech.banner.menu
/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


import grails.converters.JSON
import net.hedtech.banner.security.BannerUser
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

class CommonSelfServiceMenuController {
    def selfServiceMenuService
    def grailsApplication

    private final log = Logger.getLogger(getClass())

    static final String SSB_BANNER_TITLE = "Banner Self-Service"
    static final String Main_Menu = "bmenu.P_MainMnu"
    static final String AMPERSAND="&";
    static final String QUESTION_MARK="?";

    def data = {
        if(request.parameterMap["q"]){
            searchAppConcept()
        } else {
            list()
        }
    }

    def list = {
        String caption
        Map subMenu

        if(request.parameterMap["caption"])
            caption = request.parameterMap["caption"][0]


        subMenu = getSubMenuData(Main_Menu, caption)


        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${subMenu as JSON});", contentType: "text/javascript"
        } else {
            render subMenu as JSON
        }


    }
    private def getSubMenuData(String mnuName,String caption ){

        Map subMenu
        List ssbList

        if (mnuName){
            List mnuList
            def user = SecurityContextHolder?.context?.authentication?.principal
            mnuList = selfServiceMenuService.bannerMenuAppConcept(mnuName,null,user.pidm)
            mnuList=setHideSSBHeaderCompsParam(mnuList)
            ssbList =  composeMenuStructure(mnuList, SSB_BANNER_TITLE)
            subMenu = [name:mnuName,caption:caption,items: ssbList, _links:getLinks(mnuName)]
        } else {
            ssbList =  rootMenu()
            subMenu = [ name:"root", caption:"root", items: ssbList , _links:getLinks(mnuName)]
        }
        return subMenu
    }

    private List setHideSSBHeaderCompsParam(List mnuList){
        mnuList.eachWithIndex{ SelfServiceMenu,  i ->
            String symbol = SelfServiceMenu.url.indexOf(QUESTION_MARK)>-1? AMPERSAND:QUESTION_MARK
            SelfServiceMenu.url=SelfServiceMenu.url+symbol+"hideSSBHeaderComps=true"
        }
        return mnuList
    }

    private def getLinks(String mnuName) {

        Map self
        Map parent
        String pName
        String pCaption
        List parentList
        log.trace("CommonSelfServiceMenuController.getLinks invoked for $mnuName")

        if(mnuName != null){
            parentList = selfServiceMenuService.getParent(mnuName)
            if(parentList )   {
                pName = parentList[0].name
                pCaption = parentList[0].caption
            }
        }
        if (mnuName != null)
            self = [href:getServerURL() +"/commonSelfServiceMenu?menu="+mnuName]
        else
            self = [href:getServerURL() +"/commonSelfServiceMenu?"]

        if (mnuName != null && !mnuName?.equalsIgnoreCase(Main_Menu))
            parent = [name:pName, caption: pCaption, href: getServerURL() +"/commonSelfServiceMenu?menu="+mnuName]
        else
            parent = [name:"root", caption: "root", href: getServerURL() +"/commonSelfServiceMenu?"]

        return [self: self, parent:parent]
    }


    private def rootMenu = {
        Map ssbMenu
        List finalList = []
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            if (user.pidm)  {
                ssbMenu = [ name:SSB_BANNER_TITLE, caption:SSB_BANNER_TITLE, page:SSB_BANNER_TITLE ,url: getServerURL() +"/commonSelfServiceMenu?menu="+Main_Menu+"&caption="+SSB_BANNER_TITLE+"&type="+SSB_BANNER_TITLE,type: "MENU",items: null,menu:SSB_BANNER_TITLE]
                finalList.add(ssbMenu)
            }
        }
        return finalList
    }


    def search = {

        Map subMenu
        List adminList
        List finalList = []
        String searchVal

        if(request.parameterMap["q"])
            searchVal = request.parameterMap["q"][0]
        if(searchVal){
            def user = SecurityContextHolder?.context?.authentication?.principal
            adminList = selfServiceMenuService.searchMenuAppConcept(searchVal,user.pidm)
            adminList=setHideSSBHeaderCompsParam(adminList)
            finalList.addAll(adminList)
        }
        subMenu = [ name:"root", caption:"root", items: finalList ]
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${subMenu as JSON});", contentType: "text/javascript"
        } else {
            render subMenu as JSON
        }
    }


    def searchAppConcept = {

        Map subMenu
        List adminList
        List finalList = []
        String searchVal

        if(request.parameterMap["q"])
            searchVal = request.parameterMap["q"][0]
        if(searchVal && searchVal.length() >= 3){
            def user = SecurityContextHolder?.context?.authentication?.principal
            adminList = selfServiceMenuService.searchMenuAppConcept(searchVal,user.pidm, request.parameterMap["ui"])
            adminList=setHideSSBHeaderCompsParam(adminList)
            finalList.addAll(adminList)
        }

        subMenu = [ name:"root", caption:"root", items: composeMenuStructure(finalList, SSB_BANNER_TITLE) ]

        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${subMenu as JSON});", contentType: "text/javascript"
        } else {
            render subMenu as JSON
        }
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

    private def composeMenuStructure(list, type){
        List finalList = []
        list.each {a ->
            def tempFormName = a.formName
            def tempParentName = a.parent
            def tempPageName = a.page
            def tempName   = a.name

            if (a.type == "MENU")
                finalList.add(name:tempName,page:tempPageName,caption:a.caption,parent:tempParentName,url: getServerURL() +"/commonSelfServiceMenu?type="+type+"&menu="+tempFormName+"&caption="+a.caption,type: "MENU",menu:tempPageName)

            if (a.type == "FORM" ){
                    finalList.add(name:tempName,page:tempName,caption:a.caption,parent:a.url,url: a.url,type: "SS-APP",menu:tempFormName, pageCaption:a.caption)
            }
        }
        return finalList
    }

}
