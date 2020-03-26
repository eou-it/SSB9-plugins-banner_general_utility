/*******************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.general.utility

import grails.util.Environment
import grails.util.Holders

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Fetching the configuration from DB and setting in Holders.Config using ConfigPropertiesService
 * */

class BootStrap {
    def configPropertiesService
    def generalPageRoleMappingService
    def springSecurityService
    def bannerHoldersService

    def init = { servletContext ->
        // Overriding the static getConfig() from the Holders class using meta-programming.
        // Whenever we call Holders.config or grailsApplication.config then the 'BannerHolders.config" will get called.
        Holders.metaClass.static.getConfig = {
            return BannerHolders.config
        }
        if (Environment.current != Environment.TEST) {
            configPropertiesService.seedDataToDBFromConfig()
            configPropertiesService.seedUserPreferenceConfig()

            // Seed the data for InterceptUrlMap at server startup.
            ArrayList seedDataKey = Holders.config.ssconfig.app.seeddata.keys
            if (seedDataKey && seedDataKey.contains(['grails.plugin.springsecurity.interceptUrlMap'])) {
                generalPageRoleMappingService.seedInterceptUrlMapAtServerStartup()
                springSecurityService.clearCachedRequestmaps()
            }
        }
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setTransactionTimeOut()
        configPropertiesService.setLoginEndPointUrl()
        configPropertiesService.setLogOutEndPointUrl()
        configPropertiesService.setGuestLoginEnabled()
        if ( !(Holders.grailsApplication.config.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator) ) {
            final List<String> meppedConfigs = Holders.grailsApplication.config.banner.mep.configurations
            if (meppedConfigs && meppedConfigs?.get(0) == 'all') {
                bannerHoldersService.setMeppedConfigObj ()
            }
        }
    }

    def destroy = {
        // no-op
    }
}
