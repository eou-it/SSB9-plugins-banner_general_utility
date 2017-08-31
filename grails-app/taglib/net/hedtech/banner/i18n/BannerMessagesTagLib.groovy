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
     * Use bannerMessages instead
     */
    @Deprecated
    def i18n_setup = { attrs ->
        bannerMessages( attrs )
    }

    /**
     * Future: May move to using separate downloaded files instead of inline.
     */
    def bannerMessages = { attrs ->
        def map = grailsApplication.mainContext.getBean('messageSource').getAllProperties( LocaleContextHolder.getLocale() )
        String json = "${map as JSON}"
        log.debug( "BannerMessagesTaglib map: ${map.size()} Text length: ${json.length()}")
        out << "window.i18n = ${json};\n"
    }
}
