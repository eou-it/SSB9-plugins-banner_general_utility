/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders as CH
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.dao.InvalidDataAccessResourceUsageException

/**
 * The service is used to fetch all the global/app based config properties from DB
 * and will merge those props to Context Holder by the help of bootstrap.
 */
class ConfigPropertiesService extends ServiceBase {
    static transactional = true

    private static final LOGGER = Logger.getLogger(ConfigPropertiesService.class.name)
    private static final String GLOBAL = "GLOBAL"
    private static String localLogoutEnable="saml/logout?local=true"
    private static String globalLogoutEnable="saml/logout"
    def grailsApplication
    def configApplicationService
    ConfigSlurper configSlurper = new ConfigSlurper()


    /**
     * This method will be get called in bootstrap to load all the config properties from the DB.
     */
    public void setConfigFromDb() {
        String appId = grailsApplication.metadata['app.appId']
        LOGGER.info("Fetching config from DB for appId = ${appId}")
        try {
            ArrayList configProp = ConfigProperties.fetchSimpleConfigByAppId(GLOBAL)
            mergeConfigProperties(configProp)
            // Merge the application related configurations and global configurations
            if (appId) {
                configProp = ConfigProperties.fetchSimpleConfigByAppId(appId)
                mergeConfigProperties(configProp)
            }
        }
        catch (InvalidDataAccessResourceUsageException ex) {
            log.error("Exception occured  while fetching ConfigProperties from DB, Self Service Config Table doesn't exist")
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


    public void seedDataToDBFromConfig() {
        String appName = grailsApplication.metadata['app.name']
        String appId = grailsApplication.metadata['app.appId']
        if(appId)
            {
                try {
                    ConfigApplication configApp = ConfigApplication.fetchByAppId(appId)
                    if (configApp == null) {
                        ConfigApplication newConfigApp = new ConfigApplication()
                        newConfigApp.setAppId(appId)
                        newConfigApp.setAppName(appName)
                        newConfigApp.setLastModifiedBy('BANNER')
                        configApp = configApplicationService.create(newConfigApp)
                    }

                    ArrayList configPropName = []
                    ConfigProperties.fetchByAppId(appId).each { ConfigProperties cp ->
                        configPropName << cp.configName
                    }

                    def seedDataKey = CH.config.ssconfig.app.seeddata.keys
                    LOGGER.debug("seeddata defined in config is :" + seedDataKey)
                    def dataToSeed = []

                    seedDataKey.each { obj ->
                        if (obj instanceof List) {
                            obj.each { keyName ->
                                if (!configPropName.contains(keyName)) {
                                    ConfigProperties cp = new ConfigProperties()
                                    cp.setConfigName(keyName)

                                    def value = CH.config.flatten()."$keyName"
                                    cp.setConfigValue(value.toString())
                                    cp.setConfigApplication(configApp)
                                    cp.setConfigType(value?.getClass()?.simpleName?.toLowerCase())
                                    cp.setLastModifiedBy('BANNER')
                                    cp.setLastModified(new Date())
                                    dataToSeed << cp
                                }
                            }
                        } else if (obj instanceof Map) {
                            obj.each { k, v ->
                                if (!configPropName.contains(k)) {
                                    ConfigProperties cp = new ConfigProperties()
                                    cp.setConfigName(k)
                                    cp.setConfigValue(v.toString())
                                    cp.setConfigApplication(configApp)
                                    cp.setConfigType(v?.getClass()?.simpleName?.toLowerCase())
                                    cp.setLastModifiedBy('BANNER')
                                    cp.setLastModified(new Date())
                                    dataToSeed << cp
                                }
                            }
                        }
                    }
                    create(dataToSeed)
                }
                catch (InvalidDataAccessResourceUsageException ex) {
                    LOGGER.error("Exception occured while running seedDataToDBFromConfig method, Self Service Config Table doesn't exist")

                }
            }else{
                LOGGER.info("No App Id Specified in application.properties");
        }
    }

    public setTransactionTimeOut() {
        grailsApplication?.config?.transactionTimeout = (grailsApplication.config.banner?.transactionTimeout instanceof Integer
                ? grailsApplication.config.banner?.transactionTimeout
                : 30)

    }


    public setLoginEndPointUrl() {
        grailsApplication?.config?.loginEndpoint = grailsApplication.config?.loginEndpoint ?: ""
    }


    public setLogOutEndPointUrl() {
        if (ControllerUtils.isSamlEnabled()) {
            if (ControllerUtils.isLocalLogoutEnabled()) {
                grailsApplication?.config?.logoutEndpoint = localLogoutEnable
            } else {
                grailsApplication?.config?.logoutEndpoint = globalLogoutEnable
            }
        } else {
            grailsApplication?.config?.logoutEndpoint = grailsApplication.config?.logoutEndpoint ?: ""
        }
    }


    public setGuestLoginEnabled() {
        if ((true == grailsApplication.config?.guestAuthenticationEnabled) && (!"default".equalsIgnoreCase(grailsApplication.config?.banner?.sso?.authenticationProvider.toString()))) {
            grailsApplication?.config?.guestLoginEnabled = true
        } else {
            grailsApplication?.config?.guestLoginEnabled = false
        }
    }
}
