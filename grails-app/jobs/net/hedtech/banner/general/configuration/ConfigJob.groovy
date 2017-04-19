/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.apache.log4j.Logger

class ConfigJob {

    def configPropertiesService
    def generalPageRoleMappingService

    private static final LOGGER = Logger.getLogger(ConfigJob.class.name)
    static def interval = 60000
    static def delay = 60000

    static triggers = {
        simple startDelay: delay, repeatInterval: interval// execute job once in 15 minutes
    }

    def execute() {

        LOGGER.info("Running Config Job to update configurations")

        configPropertiesService.setConfigFromDb()
        generalPageRoleMappingService.reset()

        LOGGER.info("Configurations updated")
    }
}
