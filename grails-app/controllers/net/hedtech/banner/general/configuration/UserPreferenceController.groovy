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
        def response = configUserPreferenceService.saveLocale(map)
        def model = [
                status     : response.status,
                message       : response.message,

        ]
        render model as JSON

    }


    def getAllLocales() {
        def list = [
               /* { id:"AL", name:"Alabama"},
                { localecode : 'ar'   ,  language  : 'Arabic'},
                { localecode : 'ar_SA',  language  : 'Arabic'},
                { localecode : 'en_AU',  language  : 'Arabic'},
                { localecode : 'en_GB',  language  : 'Arabic'},*/

                ar_SA: 'Arabic (Saudi Arabia)',
                en_AU: 'English Australia',
                en_GB: 'English United Kingdom',
        ]
        return list

    }
}
