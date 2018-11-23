/*******************************************************************************
 Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders
import grails.util.Holders as CH
import groovy.sql.Sql
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.db.dbutility.DBUtility
import net.hedtech.banner.security.AuthenticationProviderUtility
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

    private static String CONFIGNAME_LOCALE = "locale"

    private static String localLogoutEnable = "saml/logout?local=true"

    private static String globalLogoutEnable = "saml/logout"

    private static final String DECRYPT_TEXT_FUNCTION = "{?= call GSKDSEC.decrypt_string(?)}"

    private static final String ENCRYPT_TEXT_FUNCTION = '{call  GSPCRPT.p_apply(?,?)}'

    def grailsApplication

    def configApplicationService

    ConfigSlurper configSlurper = new ConfigSlurper()

    def dataSource

    /**
     * This method will be get called in bootstrap to load all the config properties from the DB.
     */
    public void setConfigFromDb() {
        String appId = grailsApplication.metadata['app.appId']
        LOGGER.info("Fetching config from DB for appId = ${ appId }")
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
            LOGGER.error("Exception occured  while fetching ConfigProperties from DB, Self Service Config Table doesn't exist")
        }

    }

    /**
     * Method is used to merge all the properties in to Context Holder.
     * @param configProps Data type is ArrayList, this list will hold the config properties.
     */
    private void mergeConfigProperties(ArrayList configProps) {
        LOGGER.debug('Config fetched from DB' + configProps)
        configProps?.each {configProp ->
            Properties property = new Properties()
            def configKey   = configProp?.configName
            def configValue = getConfigValueFromAppropriateConfigType(configProp)
            if ('locale' == configKey) {
                property.put('locale_userPreferenceEnable', configProp.userPreferenceIndicator ?: false)
            }
            property.put(configKey, configValue)
            CH.config.merge(configSlurper.parse(property))
        }
        LOGGER.debug('Setting config from DB')
    }


    private def getConfigValueFromAppropriateConfigType(configProp) {
        def key = configProp?.configName
        def value = configProp?.configValue
        def decryptedValue
        if ('boolean' == configProp.configType)
            value = value ? value?.toBoolean() : false
        else if ('integer' == configProp.configType)
            value = value ? value?.toInteger() : 0
        else if ('string' == configProp.configType)
            value = value ? value?.toString() : ''
        else if ('encryptedtext' == configProp.configType) {
            decryptedValue = getDecryptedValue(value)
            value = decryptedValue ? decryptedValue : ''
        } else if ('map' == configProp.configType) {
            value = value ? Eval.me(value) : [:]
        } else if ('list' == configProp.configType) {
            value = (value && value != "[]") ? value[1..-2].split(',') : []
        } else if ('closure' == configProp.configType) {
            if (value) {
                def tempValue = new ConfigSlurper().parse(key + """${value}""")
                value = tempValue.get(key)
            } else {
                value = '{}'
            }
        }
        return value
    }


    public void seedDataToDBFromConfig() {
        String appName = grailsApplication.metadata['app.name']
        String appId = grailsApplication.metadata['app.appId']
        if (appId) {
            try {
                ConfigApplication configApp = ConfigApplication.fetchByAppId(appId)
                if (configApp == null) {
                    ConfigApplication newConfigApp = new ConfigApplication()
                    newConfigApp.setAppId(appId)
                    newConfigApp.setAppName(appName)
                    newConfigApp.setLastModifiedBy('BANNER')
                    configApplicationService.create(newConfigApp)
                }
                def appSeedDataKey = CH.config.ssconfig.app.seeddata.keys
                def globalSeedDataKey = CH.config.ssconfig.global.seeddata.keys
                LOGGER.debug("App seeddata defined in config is : " + appSeedDataKey)
                LOGGER.debug("Global seeddata defined in config is : " + globalSeedDataKey)

                seedConfigDataToDB(appId, appSeedDataKey)
                seedConfigDataToDB(GLOBAL, globalSeedDataKey)
            }
            catch (InvalidDataAccessResourceUsageException ex) {
                LOGGER.error("Exception occured while running seedDataToDBFromConfig method, SelfService Config Table doesn't exist" + ex.getMessage())
            }
        } else {
            LOGGER.error("No App Id Specified in application.properties");
        }
    }


    public void seedUserPreferenceConfig() {
        ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId(CONFIGNAME_LOCALE, GLOBAL)
        if (!configProperties) {
            try {
                ConfigApplication configApp = ConfigApplication.fetchByAppId(GLOBAL)
                ConfigProperties cp = new ConfigProperties()
                cp.setConfigName(CONFIGNAME_LOCALE)
                cp.setConfigApplication(configApp)
                cp.setConfigType('string')
                cp.setLastModifiedBy('BANNER')
                cp.setUserPreferenceIndicator(true)
                cp.setLastModified(new Date())
                this.create(cp)
            }
            catch (InvalidDataAccessResourceUsageException ex) {
                LOGGER.error("Exception occured while executing seedUserPreferenceConfig " + ex.getMessage())
            }
        }
    }


    private void seedConfigDataToDB(appId, seedDataKey) {
        ConfigApplication configApp = ConfigApplication.fetchByAppId(appId)
        ArrayList configPropName = []
        ConfigProperties.fetchByAppId(appId).each { ConfigProperties cp ->
            configPropName << cp.configName
        }
        def dataToSeed = []
        seedDataKey.each { obj ->
            if (obj instanceof List) {
                obj.each { keyName ->
                    if (!configPropName.contains(keyName)
                            && keyName != 'grails.plugin.springsecurity.interceptUrlMap') {
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
                    if (!configPropName.contains(k)
                            && k != 'grails.plugin.springsecurity.interceptUrlMap') {
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


    public void updateDefaultWebSessionTimeout() {
        def defaultWebSessionTimeoutFromConfig = CH.config.defaultWebSessionTimeout
        if (!(defaultWebSessionTimeoutFromConfig instanceof Map)) {
            if (AuthenticationProviderUtility.defaultWebSessionTimeout != defaultWebSessionTimeoutFromConfig) {
                AuthenticationProviderUtility.defaultWebSessionTimeout = defaultWebSessionTimeoutFromConfig
            }
        }
    }

    /**
     * This Method will used to decrypt the encrypted value.
     * @Param encryptedValue
     * */
    public String getDecryptedValue(def encryptedValue) {
        def conn
        String decryptedValue
        try {
            if (encryptedValue) {
                conn = dataSource.getConnection()
                Sql db = new Sql(conn)
                db.call(DECRYPT_TEXT_FUNCTION, [Sql.VARCHAR, encryptedValue]) { y_string ->
                    decryptedValue = y_string
                }
            }
        } finally {
            conn?.close()
        }
        return decryptedValue
    }

    /**
     * This Method will used to encrypt the clear text .
     * @Param clearText of type String
     * */
    public String getEncryptedValue(String clearText) {
        def conn
        String encryptedValue
        try {
            conn = dataSource.getSsbConnection()
            Sql db = new Sql(conn)
            if (clearText) {
                db.call(ENCRYPT_TEXT_FUNCTION, [clearText, Sql.VARCHAR]) { v_bdmPasswd ->
                    encryptedValue = v_bdmPasswd
                }
            }
        } finally {
            conn?.close()
        }
        return encryptedValue
    }
}
