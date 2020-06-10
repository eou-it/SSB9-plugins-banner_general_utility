/*******************************************************************************
 Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders as CH
import org.springframework.dao.InvalidDataAccessResourceUsageException
import groovy.util.logging.Slf4j

@Slf4j
class ConfigJob {

    def configPropertiesService
    def springSecurityService

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

            } catch (InvalidDataAccessResourceUsageException e) {
                log.error("InvalidDataAccessResourceUsageException in execute method of ConfigJob Self Service Config Table doesn't exist")
            }
            log.info("Configurations updated")
        }
    }
}
