package net.hedtech.banner.utility

import grails.util.Holders

class GeneralMenu {
    public static boolean isEnabled(){
        return Holders?.grailsApplication?.config?.GeneralMenu
    }
}
