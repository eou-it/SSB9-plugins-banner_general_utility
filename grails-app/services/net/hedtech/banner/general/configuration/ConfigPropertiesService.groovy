/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import net.hedtech.banner.service.ServiceBase
import grails.util.Holders as CH
import org.apache.log4j.Logger

class ConfigPropertiesService extends ServiceBase{

    static transactional = true
    private static final LOGGER = Logger.getLogger(getClass())

    def grailsApplication
    def configSlurper = new ConfigSlurper()

    def setConfigFromDb() {
        def appName = grailsApplication.metadata['app.name']
        def configApp = ConfigApplication.fetchByAppName(appName)
        def appId = ((ConfigApplication)((ArrayList)configApp).get(0)).appId
        def configProp = ConfigProperties.fetchByAppId(appId)
        configProp.each{
            def property = new Properties()
            def key = it.configName
            def value = it.configValue
            if(it.configType == 'boolean')
                value = value.toBoolean()
            property.put(key, value)
            CH.config.merge(configSlurper.parse(property))
        }
    }
}
