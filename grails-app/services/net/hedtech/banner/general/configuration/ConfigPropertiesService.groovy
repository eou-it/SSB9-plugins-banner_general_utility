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

    def grailsApplication
    ConfigSlurper configSlurper = new ConfigSlurper()

    /**
     * This method will be get called in bootstrap to load all the config properties from the DB.
     */
    public void setConfigFromDb() {
        String appName = grailsApplication.metadata['app.name']
        ConfigApplication configApp = ConfigApplication.fetchByAppName(appName)
        def appId
        ArrayList configProp
        if (configApp) {
            appId = configApp.appId
            configProp = ConfigProperties.fetchByAppId(appId)
            mergeConfigProperties(configProp)
        } else {
            configProp = ConfigProperties.fetchByAppId(null)
            mergeConfigProperties(configProp)
        }
    }

    /**
     * Method is used to merge all the properties in to Context Holder.
     * @param configProp Data type is ArrayList, this list will hold the config properties.
     */
    private void mergeConfigProperties(ArrayList configProp) {
        configProp?.each {
            Properties property = new Properties()
            def key = it.configName
            def value = it.configValue
            if ('boolean' == it.configType)
                value = value.toBoolean()
            else if ('integer' == it.configType)
                value = value.toInteger()
            property.put(key, value)
            CH.config.merge(configSlurper.parse(property))
        }
        LOGGER.info('Setting conig from DB')
    }
}
