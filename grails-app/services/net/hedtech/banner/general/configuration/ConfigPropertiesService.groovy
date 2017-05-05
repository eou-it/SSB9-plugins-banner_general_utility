/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders as CH
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

/**
 * The service is used to fetch all the global/app based config properties from DB
 * and will merge those props to Context Holder by the help of bootstrap.
 */
class ConfigPropertiesService extends ServiceBase {
    static transactional = false

    private static final LOGGER = Logger.getLogger(ConfigPropertiesService.getClass().getName())
    private static final String GLOBAL = "GLOBAL"
    def grailsApplication
    ConfigSlurper configSlurper = new ConfigSlurper()

    /**
     * This method will be get called in bootstrap to load all the config properties from the DB.
     */
    public void setConfigFromDb() {
        String appName = grailsApplication.metadata['app.name']
        ConfigApplication configApp = ConfigApplication.fetchByAppName(appName)
        def appId = configApp?.appId
        ArrayList configProp
        // merge the global configurations
        configProp = ConfigProperties.fetchByAppId(GLOBAL)
        mergeConfigProperties(configProp)
        // Merge the application related configurations and global configurations
        if (appId) {
            configProp = ConfigProperties.fetchByAppIdOrNullAppId(appId)
            mergeConfigProperties(configProp)
        }

    }

    /**
     * Method is used to merge all the properties in to Context Holder.
     * @param configProp Data type is ArrayList, this list will hold the config properties.
     */
    private void mergeConfigProperties(ArrayList configProp) {
        LOGGER.info('Config fetched from DB' + configProp)
        configProp?.each {
            Properties property = new Properties()
            def key = it?.configName
            def value = it?.configValue

            if ('boolean' == it.configType)
                value = value ? value?.toBoolean() : false
            else if ('integer' == it.configType)
                value = value ? value?.toInteger() : 0
            else if ('string' == it.configType)
                value = value ? value?.toString() : ''

            property.put(key, value)
            CH.config.merge(configSlurper.parse(property))
        }
        LOGGER.info('Setting config from DB')
    }
}
