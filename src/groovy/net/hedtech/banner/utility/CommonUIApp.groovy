package net.hedtech.banner.utility

import grails.util.Holders

class CommonUIApp {
    public static boolean isEnabled(){
        return Holders?.grailsApplication?.config?.CommonUIApp
    }
}
