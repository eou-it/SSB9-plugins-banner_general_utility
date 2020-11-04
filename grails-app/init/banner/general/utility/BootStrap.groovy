/*******************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.general.utility

import grails.plugins.quartz.GrailsJobClassConstants
import grails.util.Environment
import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.configuration.ConfigJob
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.impl.StdScheduler

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Fetching the configuration from DB and setting in Holders.Config using ConfigPropertiesService
 * */

@Slf4j
class BootStrap {
    def configPropertiesService
    def generalPageRoleMappingService
    def springSecurityService
    def bannerHoldersService
    def multiEntityProcessingService
    StdScheduler quartzScheduler

    def init = { servletContext ->
        if ( multiEntityProcessingService.isMEP() ) {
            bannerHoldersService.setBaseConfig()
            // Overriding the static getConfig() from the Holders class using meta-programming.
            // Whenever we call Holders.config or grailsApplication.config then the 'BannerHolders.config" will get called.
            Holders.metaClass.static.getConfig = {
                return BannerHolders.config
            }
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
        startConfigJobWithParameter()
/*
   The below code required to be executed here as few configuration required by other BootStrap file will not be available
   in Grails ConfigHolder. These configurations are only available in GUROCFG not in the config file.
*/
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setTransactionTimeOut()
        configPropertiesService.setLoginEndPointUrl()
        configPropertiesService.setLogOutEndPointUrl()
        configPropertiesService.setGuestLoginEnabled()
        if ( multiEntityProcessingService.isMEP() ) {
            if ( !(Holders.grailsApplication.config.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator) ) {
                final List<String> meppedConfigs = Holders.grailsApplication.config.banner.mep.configurations
                if (meppedConfigs) {
                    bannerHoldersService.setMeppedConfigObj ()
                }
            }
        }
    }

    def destroy = {
        // no-op
    }


    private startConfigJobWithParameter(){
        //Integer delay = Holders.config.configJob?.delay instanceof Integer? Holders.config.configJob?.delay : 60000
        Integer interval = Holders.config.configJob?.interval instanceof Integer? Holders.config.configJob?.interval : 60000
        Integer actualCount = Holders.config.configJob?.actualCount instanceof Integer? Holders.config.configJob?.actualCount > 0 ? Holders.config.configJob?.actualCount -1 : Holders.config.configJob?.actualCount : -1
        Map parameterMap = [name: 'configJobTigger', actualCount: actualCount]
        log.info("Running Config Job with parameter interval =  ${interval}")

        SimpleTrigger trigger = null;
        Trigger oldTrigger = quartzScheduler.getTrigger( new TriggerKey('net.hedtech.banner.general.configuration.ConfigJob', GrailsJobClassConstants.DEFAULT_TRIGGERS_GROUP));
        if(oldTrigger) {
            TriggerBuilder builder = oldTrigger.getTriggerBuilder();
            trigger = builder
                    .forJob('net.hedtech.banner.general.configuration.ConfigJob', GrailsJobClassConstants.DEFAULT_GROUP)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).withRepeatCount(actualCount))
                    .build();
            ConfigJob.reschedule(trigger, parameterMap);
        } else {
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity('net.hedtech.banner.general.configuration.ConfigJob', GrailsJobClassConstants.DEFAULT_TRIGGERS_GROUP)
                    .withPriority(6)
                    .forJob('net.hedtech.banner.general.configuration.ConfigJob', GrailsJobClassConstants.DEFAULT_GROUP)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).withRepeatCount(actualCount))
                    .build();
            ConfigJob.schedule(trigger, parameterMap);
        }
    }
}
