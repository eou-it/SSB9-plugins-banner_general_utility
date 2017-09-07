/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.i18n

import grails.converters.JSON
import org.springframework.context.i18n.LocaleContextHolder

/**
 * Tag to include all messages from BannerMessageSource for the current locale.
 */
class BannerMessagesTagLib {

    /**
     * Use bannerMessages to get TextManager message updates.
     *
     * Note that i18n_setup must be contained within a
     * <script></script> tag, which is not a usual place for a tag...
     */
    @Deprecated
    def i18n_setup = { attrs ->
        out << "\nwindow.i18n = ${getMessages()};\n"
    }

    /**
     * Future: May move to using separate downloaded files instead of inline.
     */
    def bannerMessages = { attrs ->
        out << "\n<script>\nwindow.i18n = ${getMessages()};\n</script>"
    }

    def String getMessages() {
        def map = [:]
        grailsApplication.mainContext.getBean('messageSource').getMergedPluginProperties(LocaleContextHolder.getLocale()).properties.each { key ->
            map.put key.key, key.value
        }

        String json = "${map as JSON}"
        log.debug( "BannerMessagesTagLib map: ${map.size()} Text length: ${json.length()}")
        return json
    }
}
