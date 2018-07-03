/*******************************************************************************
 Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.apache.log4j.Logger
import grails.util.Holders as CH
import org.springframework.dao.InvalidDataAccessResourceUsageException
import groovy.util.logging.Slf4j

@Slf4j
class ConfigJob {

    def configPropertiesService
    def springSecurityService

    static def delay = CH.config.configJob?.delay instanceof Integer? CH.config.configJob?.delay : 60000
    static def interval = CH.config.configJob?.interval instanceof Integer? CH.config.configJob?.interval : 60000
    def concurrent = false
    static def actualCount = CH.config.configJob?.actualCount instanceof Integer? CH.config.configJob?.actualCount > 0 ? CH.config.configJob?.actualCount -1 : CH.config.configJob?.actualCount : -1

    static triggers = {
        simple startDelay: delay, repeatInterval: interval, repeatCount : actualCount // execute job once in 15 minutes
    }

    def execute() {

        log.info("Running Config Job with configurations actualCount =  ${actualCount}, delay = ${delay}, repeatInterval= ${interval}")
        if (actualCount != 0) {
            try {
                configPropertiesService.setConfigFromDb()
                configPropertiesService.setTransactionTimeOut()
                configPropertiesService.updateDefaultWebSessionTimeout()
                configPropertiesService.setLoginEndPointUrl()
                configPropertiesService.setLogOutEndPointUrl()
                configPropertiesService.setGuestLoginEnabled()
                springSecurityService.clearCachedRequestmaps()

            } catch (InvalidDataAccessResourceUsageException e) {
                log.error("InvalidDataAccessResourceUsageException in execute method of ConfigJob Self Service Config Table doesn't exist")
            }
            log.info("Configurations updated")
        }
    }
}
