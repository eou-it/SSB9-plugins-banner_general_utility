/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.converters.JSON

class UserPreferenceController {

    def configUserPreferenceService


    def locales() {
        def locales = getAllLocales()
        def userLocale = configUserPreferenceService.getUserLocale()
        Map returnMap = [
                locales: locales,
                selectedLocale : userLocale
        ]
        render returnMap as JSON
    }


    def fetchUserLocale() {
        def userLocale = configUserPreferenceService.getUserLocale()
        Map returnMap = [
                userLocale: userLocale
        ]
        render returnMap as JSON
    }


    def saveLocale() {
        //def response = configUserPreferenceService.saveLocale(map)
        def response = [status : 'success']
        def model = [
                status          : response.status
        ]
        render model as JSON
    }


    def getAllLocales() {
        def localeList = [
                [locale: 'ar_SA', description :'Arabic (Saudi Arabia)'],
                [locale: 'en_AU', description : 'English Australia'],
                [locale: 'en_GB', description : 'English Australia']
        ]
        return localeList

    }
}
