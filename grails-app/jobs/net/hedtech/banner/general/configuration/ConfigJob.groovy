/*******************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import grails.util.Holders as CH
import org.springframework.dao.InvalidDataAccessResourceUsageException
import groovy.util.logging.Slf4j

@Slf4j
class ConfigJob {

    def configPropertiesService
    def springSecurityService
    def bannerHoldersService

    // TODO :grails_332_change, needs to revisit
    Boolean concurrent = false
    static Integer actualCount = 0
    //static def actualCount = CH.config.configJob?.actualCount instanceof Integer? CH.config.configJob?.actualCount > 0 ? CH.config.configJob?.actualCount -1 : CH.config.configJob?.actualCount : -1

    static triggers = {
        Integer delay = CH.config.configJob?.delay instanceof Integer? CH.config.configJob?.delay : 60000
        Integer interval = CH.config.configJob?.interval instanceof Integer? CH.config.configJob?.interval : 60000
        actualCount = CH.config.configJob?.actualCount instanceof Integer? CH.config.configJob?.actualCount > 0 ? CH.config.configJob?.actualCount -1 : CH.config.configJob?.actualCount : -1

        simple startDelay: delay, repeatInterval: interval, repeatCount : actualCount // execute job once in 15 minutes
    }

    def execute() {
        log.info("Running Config Job with configurations actualCount =  ${actualCount}")
        if (actualCount != 0) {
            try {
                configPropertiesService.setConfigFromDb()
                configPropertiesService.setTransactionTimeOut()
                configPropertiesService.updateDefaultWebSessionTimeout()
                configPropertiesService.setLoginEndPointUrl()
                configPropertiesService.setLogOutEndPointUrl()
                configPropertiesService.setGuestLoginEnabled()
                springSecurityService.clearCachedRequestmaps()
                bannerHoldersService.setBaseConfig()
                if ( !(Holders.grailsApplication.config.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator) ) {
                    final List<String> meppedConfigs = Holders.grailsApplication.config.banner.mep.configurations
                    if (meppedConfigs && meppedConfigs?.get(0)?.toLowerCase() == 'all') {
                        bannerHoldersService.setMeppedConfigObj ()
                    }
                }
            } catch (InvalidDataAccessResourceUsageException e) {
                log.error("InvalidDataAccessResourceUsageException in execute method of ConfigJob Self Service Config Table doesn't exist")
            }
            log.info("Configurations updated")
        }
    }
}
