/*******************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.config.Config
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.ReflectionUtils
import grails.util.Holders
import org.grails.config.NavigableMap
import org.grails.config.NavigableMapConfig
import groovy.sql.Sql
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.security.AuthenticationProviderUtility
import net.hedtech.banner.service.ServiceBase
import org.grails.config.PropertySourcesConfig
import org.springframework.dao.InvalidDataAccessResourceUsageException
import banner.general.utility.BannerPropertySourcesConfig

/**
 * The service is used to fetch all the global/app based config properties from DB
 * and will merge those props to Context Holder by the help of bootstrap.
 */


@Transactional
class ConfigPropertiesService extends ServiceBase {

    private static final String GLOBAL = "GLOBAL"

    private static String CONFIGNAME_LOCALE = "locale"

    private static String localLogoutEnable = "saml/logout?local=true"

    private static String globalLogoutEnable = "saml/logout"

    private static final String DECRYPT_TEXT_FUNCTION = "{?= call GSKDSEC.decrypt_string(?)}"

    private static final String ENCRYPT_TEXT_FUNCTION = '{call  GSPCRPT.p_apply(?,?)}'

    private static def initialConfig = new BannerPropertySourcesConfig()

    def grailsApplication

    def configApplicationService

    ConfigSlurper configSlurper = new ConfigSlurper()

    def dataSource

    /**
     * This method will be get called in bootstrap to load all the config properties from the DB.
     */
    public void setConfigFromDb() {
        String appId = Holders.config.app.appId
        log.info("Fetching config from DB for appId = ${ appId }")
        clearGrailsConfiguration()
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
     * @param configProps Data type is ArrayList, this list will hold the config properties.
     */
    private void mergeConfigProperties(ArrayList configProps) {
        log.debug('Config fetched from DB' + configProps)
        def properties = new PropertySourcesConfig()
        configProps?.each {configProp ->
            Properties property = new Properties()
            def configKey   = configProp?.configName
            def configValue = getConfigValueFromAppropriateConfigType(configProp)
            if ('locale' == configKey) {
                property.put('locale_userPreferenceEnable', configProp.userPreferenceIndicator ?: false)
            }
            property.put(configKey, configValue)
            properties << (configSlurper.parse(property)).flatten()
        }
        Holders.config.merge(initialConfig)
        log.debug ("Properties fetched are = {} ", properties)
        Holders.config.merge(properties)
        log.debug('Setting config from DB')
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
        String appName = Holders.config.app.name
        String appId = Holders.config.app.appId
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
                def appSeedDataKey = Holders.config.ssconfig.app.seeddata.keys
                def globalSeedDataKey = Holders.config.ssconfig.global.seeddata.keys
                log.debug("App seeddata defined in config is : " + appSeedDataKey)
                log.debug("Global seeddata defined in config is : " + globalSeedDataKey)

                seedConfigDataToDB(appId, appSeedDataKey)
                seedConfigDataToDB(GLOBAL, globalSeedDataKey)
            }
            catch (InvalidDataAccessResourceUsageException ex) {
                log.error("Exception occured while running seedDataToDBFromConfig method, SelfService Config Table doesn't exist" + ex.getMessage())
            }
        } else {
            log.error("No App Id Specified in application.properties");
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
                log.error("Exception occured while executing seedUserPreferenceConfig " + ex.getMessage())
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

                        def value = Holders.config.flatten()."$keyName"
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
        def defaultWebSessionTimeoutFromConfig = Holders.config.defaultWebSessionTimeout
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
        Boolean ssbEnabled= Holders?.config?.ssbEnabled instanceof Boolean ? Holders?.config?.ssbEnabled : false
        if(ssbEnabled) {
            try {
                if (encryptedValue) {
                    conn = dataSource.getSsbConnection()
                    Sql db = new Sql(conn)
                    db.call(DECRYPT_TEXT_FUNCTION, [Sql.VARCHAR, encryptedValue]) { y_string ->
                        decryptedValue = y_string
                    }
                }

            } catch (Exception ex) {
                log.error("Failed to decrypt the encrypted text type in ConfigPropertiesService.getDecryptedValue() with exception = {}", ex)
            }
            finally {
                conn?.close()
            }
        }
        else{
            log.info("Failed to decrypt the encrypted text type  as ssbEnabled flag is false")
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
        Boolean ssbEnabled= Holders?.config?.ssbEnabled instanceof Boolean ? Holders?.config?.ssbEnabled : false
        if(ssbEnabled) {
            try {
                conn = dataSource.getSsbConnection()
                Sql db = new Sql(conn)
                if (clearText) {
                    db.call(ENCRYPT_TEXT_FUNCTION, [clearText, Sql.VARCHAR]) { v_bdmPasswd ->
                        encryptedValue = v_bdmPasswd
                    }
                }
            } catch (Exception ex) {
                log.info("Failed to encrypt in ConfigPropertiesService.getEncryptedValue()")
            }
            finally {
                conn?.close()
            }
        }
        else{
            log.info("Failed to encrypt the text as ssbEnabled flag is false")
        }
        return encryptedValue
    }


    public void backupInitialConfiguration(){
        if (initialConfig?.size() == 0) {
            Config config = Holders.config
            Map<Object, Object> configMap = [:]
            for ( def entry : config ) {
                configMap.put( entry.getKey(), config.get( entry.getKey() ) )
            }
            initialConfig = new PropertySourcesConfig(configMap)
        }
    }


    public void clearGrailsConfiguration(){
        Holders.config.clear()
    }
}
