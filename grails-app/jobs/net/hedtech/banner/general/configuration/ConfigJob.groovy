/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.apache.log4j.Logger
import grails.util.Holders as CH

class ConfigJob {

    def configPropertiesService
    def generalPageRoleMappingService

    private static final LOGGER = Logger.getLogger(ConfigJob.class.name)
    static def delay = CH.config.configJob?.delay instanceof Integer? CH.config.configJob?.delay : 60000
    static def interval = CH.config.configJob?.interval instanceof Integer? CH.config.configJob?.interval : 60000
    //static def actualCount = 2
    def concurrent = false
    static def actualCount = CH.config.configJob?.actualCount instanceof Integer? CH.config.configJob?.actualCount > 0 ? CH.config.configJob?.actualCount -1 : CH.config.configJob?.actualCount : -1

    static triggers = {
        simple startDelay: delay, repeatInterval: interval, repeatCount : actualCount // execute job once in 15 minutes
    }

    def execute() {

        LOGGER.info("Running Config Job to update configurations")
        if (actualCount != 0) {
            try {
                configPropertiesService.setConfigFromDb()
                generalPageRoleMappingService.reset()
            } catch (Exception ex) {
                LOGGER.error("Exception in execute method of ConfigJob Self Service Config Table doesn't exist")
            }
            LOGGER.info("Configurations updated")
        }
    }
}
