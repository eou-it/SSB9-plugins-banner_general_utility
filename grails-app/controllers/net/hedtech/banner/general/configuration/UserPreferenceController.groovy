/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.springframework.context.i18n.LocaleContextHolder
import grails.converters.JSON

class UserPreferenceController {


    def configUserPreferenceService

    def locales() {
        List locales = configUserPreferenceService.getAllBannerSupportedLocales()
        Locale userLocale = configUserPreferenceService.getUserLocale()
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
        log.debug("User seleceted locale is = ${request.JSON}")
        def data = request.JSON
        String locale = data.locale
        String description = data.description
        def requestMap = [locale: locale, description: description]
        def response = configUserPreferenceService.saveLocale(requestMap)
        render response as JSON
    }
}
