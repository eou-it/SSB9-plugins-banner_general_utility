package net.hedtech.banner.general

import grails.converters.JSON

class UserPreferenceController {

    def configUserPreferenceService


    def fetch() {
        println "Test"
        def map = configUserPreferenceService.getUserLocale()
        map as JSON
    }

    def saveLocale(){
        println "Test123"
        configUserPreferenceService.saveLocale(map)
    }
}
