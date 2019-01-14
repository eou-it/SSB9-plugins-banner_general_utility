/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.i18n

import grails.util.Holders


class ResourceBundleService {
    static transactional = false //Transactions not managed by hibernate

    //Injected
    def messageSource =  Holders.grailsApplication.mainContext.getBean('messageSource')
    def textManagerService

    def list() {
        def result = []
        def id = 0 //Include a simple numeric counter as dummy id
        messageSource.getNormalizedNames().each { basename ->
            result << [ id: id++, basename: basename,  enableTranslation: false]
        }
        result.toList()
    }

    def save(data) {
        def status
        if (data.enableTranslation) {
            status = saveBundlesForLocales(data.basename, data.sourceLocale, data.locales)
        }
        data.count=status?.count
        data
    }

    def get(name, localeString ) {
        def result = null

        if (!localeString) {
            localeString = textManagerService.ROOT_LOCALE_APP
        }
        List localeParts = localeString.split("_")
        localeParts << "" //Make sure the List has 2 entries at least
        Locale locale = new Locale(localeParts[0],localeParts[1])
        result = [basename: name, locale: localeString,
                  properties: messageSource.getPropertiesByNormalizedName(name, locale)]

        result
    }

    private def saveBundlesForLocales(name, sourceLocale, locales) {
        def status
        def errors = ""
        def count = 0
        //Save the Source Locale first
        def properties = get(name, sourceLocale).properties
        if (textManagerService) {
            status = textManagerService.save(properties, name, sourceLocale, sourceLocale)
        } else {// Return mock status for testing
            status = [error: null, count: 1, mock: true]
        }
        if (status.error) {
            errors = "$errors ${status.error}\n"
        } else {
            count += status.count
        }
        locales.each{ locale ->
            if (locale.enabled && sourceLocale != locale.code) {
                properties = get(name, locale.code).properties
                if (textManagerService) {
                    status = textManagerService.save(properties, name, sourceLocale, locale.code)
                } else {// Return mock status for testing
                    status = [error: null, count: 1, mock: true]
                }
                if (status.error) {
                    errors = "$errors ${status.error}\n"
                } else {
                    count += status.count
                }
            }
        }
        [error: errors, count: count]
    }
}
