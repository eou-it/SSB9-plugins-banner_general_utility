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
    static final String hideSSBHeaderComps="hideSSBHeaderComps=true";

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
            mnuList = selfServiceMenuService.bannerMenuAppConcept(user.pidm)
            mnuList=setHideSSBHeaderCompsParam(mnuList)
            ssbList =  composeMenuStructure(mnuList, SSB_BANNER_TITLE)
            subMenu = [name:mnuName,caption:caption,items: ssbList]
        }
        return subMenu
    }

    private List setHideSSBHeaderCompsParam(List mnuList){
        mnuList.eachWithIndex{ SelfServiceMenu,  i ->
            String symbol = SelfServiceMenu.url.indexOf(QUESTION_MARK)>-1? AMPERSAND:QUESTION_MARK
            SelfServiceMenu.url=SelfServiceMenu.url+symbol+hideSSBHeaderComps;
        }
        return mnuList
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
