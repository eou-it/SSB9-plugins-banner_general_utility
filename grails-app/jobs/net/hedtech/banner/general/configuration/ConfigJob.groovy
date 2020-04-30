/*******************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import grails.util.Holders as CH
import groovy.util.logging.Slf4j
import org.springframework.dao.InvalidDataAccessResourceUsageException

@Slf4j
class ConfigJob {

    def configPropertiesService
    def springSecurityService
    def bannerHoldersService
    def multiEntityProcessingService

    // TODO :grails_332_change, needs to revisit
    Boolean concurrent = false

    static triggers ={}

    def execute(context) {
        Integer actualCount = context.mergedJobDataMap.get('actualCount')
        if (actualCount != 0) {
            try {
                configPropertiesService.setConfigFromDb()
                configPropertiesService.setTransactionTimeOut()
                configPropertiesService.updateDefaultWebSessionTimeout()
                configPropertiesService.setLoginEndPointUrl()
                configPropertiesService.setLogOutEndPointUrl()
                configPropertiesService.setGuestLoginEnabled()
                springSecurityService.clearCachedRequestmaps()
                if ( multiEntityProcessingService.isMEP() ) {
                    bannerHoldersService.setBaseConfig()
                    if ( !(Holders.grailsApplication.config.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator) ) {
                        final List<String> meppedConfigs = Holders.grailsApplication.config.banner.mep.configurations
                        if (meppedConfigs) {
                            bannerHoldersService.setMeppedConfigObj ()
                        }
                    }
                }
            } catch (InvalidDataAccessResourceUsageException e) {
                log.error("InvalidDataAccessResourceUsageException in execute method of ConfigJob Self Service Config Table doesn't exist")
            }
            log.info("Configurations updated")
        }
    }
}
