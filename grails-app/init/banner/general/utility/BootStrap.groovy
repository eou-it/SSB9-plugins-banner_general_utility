/*******************************************************************************
 Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.general.utility

import grails.util.Environment
import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.configuration.ConfigJob

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Fetching the configuration from DB and setting in Holders.Config using ConfigPropertiesService
 * */

@Slf4j
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
        startConfigJobWithParameter()
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setTransactionTimeOut()
        configPropertiesService.setLoginEndPointUrl()
        configPropertiesService.setLogOutEndPointUrl()
        configPropertiesService.setGuestLoginEnabled()
    }

    def destroy = {
        // no-op
    }


    private startConfigJobWithParameter(){
        Integer delay = Holders.config.configJob?.delay instanceof Integer? Holders.config.configJob?.delay : 60000
        Integer interval = Holders.config.configJob?.interval instanceof Integer? Holders.config.configJob?.interval : 60000
        Integer actualCount = Holders.config.configJob?.actualCount instanceof Integer? Holders.config.configJob?.actualCount > 0 ? Holders.config.configJob?.actualCount -1 : Holders.config.configJob?.actualCount : -1
        Map mp = [name: 'configJobTigger', actualCount: actualCount]
        log.info("Running Config Job with parameter delay = ${delay} interval =  ${interval}")
        ConfigJob.schedule(interval, actualCount, mp)
    }
}
