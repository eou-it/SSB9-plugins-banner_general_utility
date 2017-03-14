/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import net.hedtech.banner.service.ServiceBase
import grails.util.Holders as CH
import org.apache.log4j.Logger

class ConfigPropertiesService extends ServiceBase{

    static transactional = false
    private static final LOGGER = Logger.getLogger(ConfigPropertiesService.getClass().getName())

    def grailsApplication
    ConfigSlurper configSlurper = new ConfigSlurper()

    def setConfigFromDb() {
        String appName = grailsApplication.metadata['app.name']
        ConfigApplication configApp = ConfigApplication.fetchByAppName(appName)
        if(configApp) {
            def appId = configApp.appId
            ArrayList configProp = ConfigProperties.fetchByAppId(appId)
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
        }
    }
}
