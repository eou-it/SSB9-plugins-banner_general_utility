/*******************************************************************************
 Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
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
    def menuService
    def configPropertiesService
    def generalPageRoleMappingService
    def springSecurityService

    def init = { servletContext ->
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
    }

    def destroy = {
        // no-op
    }
}
