/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import org.springframework.context.i18n.LocaleContextHolder
import grails.converters.JSON
import org.apache.log4j.Logger

class UserPreferenceController {

    static defaultAction = "locales"

    def configUserPreferenceService

    private static final LOGGER = Logger.getLogger(UserPreferenceController.class.name)


    def locales() {
        def locales = getAllLocales()
        def userLocale = configUserPreferenceService.getUserLocale()
        Map returnMap = [
                locales       : locales
        ]
        if(userLocale){
            String userLocaleDescription = userLocale.getDisplayName()
            returnMap.put('selectedLocale', [locale: userLocale, description: userLocaleDescription] )
        }
        render returnMap as JSON
    }


    def fetchUserLocale() {
        def userLocale = configUserPreferenceService.getUserLocale()
        def currentLocale = LocaleContextHolder.getLocale()
        Map returnMap = [
                userLocale: userLocale?: currentLocale
        ]
        render returnMap as JSON
    }


    def saveLocale() {
        def data = request.JSON
        String locale = data.locale
        String description = data.description
        def requestMap = [locale: locale, description: description]
        def response = configUserPreferenceService.saveLocale(requestMap)
        render response as JSON
    }


    def getAllLocales() {
        def localeList = [
                [locale: "ar", description: "Arabic"],
                [locale: "en_AU", description: "English Australia"],
                [locale: "en_GB", description: "English United Kingdom"],
                [locale: "en_IE", description: "English Ireland"],
                [locale: "en_IN", description: "English India"],
                [locale: "es", description: "Spanish"],
                [locale: "fr", description: "French"],
                [locale: "fr_CA", description: "French Canada"],
                [locale: "pt", description: "Portuguese"],
                [locale: "es_MX", description: "Spanish (Mexico)"],
                [locale: "es_PE", description: "Spanish (Peru)"],
                [locale: "es_CO", description: "Spanish (Colombia)"],
                [locale: "es_DO", description: "Spanish (Dominican Republic)"],
                [locale: "es_PR", description: "Spanish (Puerto Rico)"],
                [locale: "es_VE", description: "Spanish (Venezuela)"],
                [locale: "es_CL", description: "Spanish (Canary Islands)"],
                [locale: "es_EC", description: "Spanish (Ecuador)"],
                [locale: "es_CR", description: "Spanish (Costa Rica)"],
                [locale: "es_PA", description: "Spanish (Panama)"],
                [locale: "es_GT", description: "Spanish (Guatemala)"],
                [locale: "es_AR", description: "Spanish (Argentina)"],
                [locale: "ar_SA", description: "Arabic (Saudi Arabia)"]
        ]
        return localeList
    }
}
