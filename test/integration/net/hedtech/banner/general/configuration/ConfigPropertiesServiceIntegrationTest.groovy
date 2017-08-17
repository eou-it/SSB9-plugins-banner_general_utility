/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders
import grails.util.Holders as CH
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import groovy.sql.Sql
/**
 * ConfigPropertiesServiceIntegrationTest.
 */
class ConfigPropertiesServiceIntegrationTest extends BaseIntegrationTestCase {

    def configPropertiesService
    def configApplicationService
    def grailsApplication
    def dataSource
    private def appName
    private def appId
    private static final String CONFIG_NAME = 'TEST_CONFIG'
    private static final String CONFIG_VALUE = 'TEST_VALUE'
    private static final String CONFIG_TYPE_STRING = 'string'
    private static final String CONFIG_TYPE_INTEGER = 'integer'
    private static final String CONFIG_TYPE_CLEAR_TEXT = 'clear_text'
    private static final String CONFIG_NAME_TRANSACTION_TIMEOUT = 'banner.transactionTimeout'
    private static final String CONFIG_NAME_LOGIN_ENDPOINT_URL = 'loginEndpoint'
    private static final String CONFIG_NAME_LOGOUT_ENDPOINT_URL = 'logoutEndpoint'
    private static final String CONFIG_NAME_DEFAULT_WEBSESSION_TIMEOUT = 'defaultWebSessionTimeout'
    private static final String CONFIG_NAME_AUTH_PROVIDER = 'authenticationProvider'
    private static final String CONFIG_NAME_LOCAL_LOGOUT = 'localLogout'
    private static final String GLOBAL = 'GLOBAL'
    private static final String TESTAPP = 'TESTAPP'
    private static String ACTUALAPPNAME = ''
    private static String ACTUALAPPID = ''
    private static final String CONFIG_NAME_TESTAPP_PASSWORD = 'testapp.password'
    private static String CONFIG_VALUE_TESTAPP_PASSWORD = "111111"
    private static final String ENCRYPT_TEXT_FUNCTION = '{call  GSPCRPT.p_apply(?,?)}'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        ACTUALAPPNAME = Holders.grailsApplication.metadata['app.name']
        Holders.grailsApplication.metadata['app.name'] = TESTAPP
        ACTUALAPPID = Holders.grailsApplication.metadata['app.appId']
        Holders.grailsApplication.metadata['app.appId'] = TESTAPP
        appName = Holders.grailsApplication.metadata['app.name']
        appId = TESTAPP
        mergeSeedDataKeysIntoConfigForTest()
    }

    @After
    public void tearDown() {
        super.tearDown()
        Holders.grailsApplication.metadata['app.name'] = ACTUALAPPNAME
    }

    @Test
    public void testSetConfigFromDb() {
        createNewConfigProperties()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME) == CONFIG_VALUE
    }

    /**
     * Saving the ConfigProperties
     * @return
     */
    private void createNewConfigProperties() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        assertNotNull configApplication?.id

        def configProps = []

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.setConfigValue(CONFIG_VALUE)
        configProps.add(configProperties)

        ConfigProperties configPropertiesBoolean = getConfigProperties()
        configPropertiesBoolean.configType = 'boolean'
        configPropertiesBoolean.configName = CONFIG_NAME + '-boolean'
        configPropertiesBoolean.configValue = 'true'
        configPropertiesBoolean.setConfigApplication(configApplication)
        configProps.add(configPropertiesBoolean)

        ConfigProperties configPropertiesInteger = getConfigProperties()
        configPropertiesInteger.configType = 'integer'
        configPropertiesInteger.configName = CONFIG_NAME + '-integer'
        configPropertiesInteger.configValue = '10'
        configPropertiesInteger.setConfigApplication(configApplication)
        configProps.add(configPropertiesInteger)

        configPropertiesService.create(configProps)
    }

    @Test
    public void testEmptyStringValue() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        ConfigProperties configPropertiesNullValueString = getConfigProperties()
        configPropertiesNullValueString.setConfigApplication(configApplication)
        configPropertiesNullValueString.configValue = ''

        ConfigProperties configProp = configPropertiesService.create(configPropertiesNullValueString)
        configProp.refresh()
        assertNull(configProp.configValue)

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME) == ''
    }

    @Test
    public void testEmptyBooleanValue() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()

        ConfigProperties configPropertiesBooleanNullValue = getConfigProperties()
        configPropertiesBooleanNullValue.configType = 'boolean'
        configPropertiesBooleanNullValue.configName = CONFIG_NAME + '-boolean-null'
        configPropertiesBooleanNullValue.configValue = ''
        configPropertiesBooleanNullValue.setConfigApplication(configApplication)

        ConfigProperties configProp = configPropertiesService.create(configPropertiesBooleanNullValue)
        configProp.refresh()
        assert configProp.configValue == null

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME + '-boolean') == true
    }

    @Test
    public void testEmptyIntegerValue() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        ConfigProperties configPropertiesIntegerNullValue = getConfigProperties()
        configPropertiesIntegerNullValue.configType = 'integer'
        configPropertiesIntegerNullValue.configName = CONFIG_NAME + '-integer-null'
        configPropertiesIntegerNullValue.configValue = ''
        configPropertiesIntegerNullValue.setConfigApplication(configApplication)

        ConfigProperties configProp = configPropertiesService.create(configPropertiesIntegerNullValue)
        configProp.refresh()
        assert configProp.configValue == null

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME + '-integer-null') == 0
    }

    @Test
    public void testSeedDataToDBFromConfig() {
        ConfigApplication configApp = getConfigApplication()
        configApp.appName = grailsApplication.metadata['app.name']
        configApplicationService.create(configApp)


        configPropertiesService.seedDataToDBFromConfig()

        def list = ConfigProperties.findAll()
        assertFalse(list.isEmpty())

        def seedDataKeys = getSeedDataKeysForTest()
        seedDataKeys.each { v ->
            if (v instanceof List) {
                v.each { vKey ->
                    list.each { key ->
                        if (key == vKey) {
                            def keyValue = CH.config.flatten()."$key.configName"
                            def vKeyValue = CH.config.flatten()."$vKey"
                            assertTrue(keyValue == vKeyValue)
                        }
                    }
                }
            } else if (v instanceof Map) {
                v.each { x, y ->
                    list.each { map ->
                        def vMap = [:]
                        vMap.put(x, y)
                        if (map == vMap) {
                            map.each { a, b ->
                                assertTrue(b == vMap.getAt(a))
                            }
                        }
                    }
                }
            }
        }
    }


    @Test
    public void testTransactionTimeOutDefault() {
        configPropertiesService.setTransactionTimeOut()
        def result = CH.config?.transactionTimeout
        assertTrue(result == CH.config.banner?.transactionTimeout || result == 30)
    }


    @Test
    public void testTransactionTimeOut() {
        def oldTransactionTimeout = CH.config.banner?.transactionTimeout
        int transactionTimeout = 150
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_TRANSACTION_TIMEOUT, transactionTimeout.toString(), CONFIG_TYPE_INTEGER)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setTransactionTimeOut()
        def result = CH.config.banner?.transactionTimeout
        assertEquals transactionTimeout, result
        CH.config.banner?.transactionTimeout = oldTransactionTimeout
    }


    @Test
    public void testLoginEndPointUrlDefault() {
        def oldLoginEndpoint = CH.config?.loginEndpoint
        String loginEndpoint = ''
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_LOGIN_ENDPOINT_URL, loginEndpoint, CONFIG_TYPE_STRING)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setLoginEndPointUrl()
        def result = CH.config?.loginEndpoint
        assertEquals loginEndpoint, result
        CH.config?.loginEndpoint = oldLoginEndpoint
    }


    @Test
    public void testLoginEndPointUrl() {
        def oldLoginEndpoint = CH.config?.loginEndpoint
        String loginEndpoint = 'test/testURL'
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_LOGIN_ENDPOINT_URL, loginEndpoint, CONFIG_TYPE_STRING)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setLoginEndPointUrl()
        def result = CH.config?.loginEndpoint
        assertEquals loginEndpoint, result
        CH.config?.loginEndpoint = oldLoginEndpoint
    }


    @Test
    public void testLogOutEndPointUrl() {
        def oldLogoutEndpoint = CH.config?.logoutEndpoint
        String logoutEndpoint = ''
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_LOGOUT_ENDPOINT_URL, logoutEndpoint, CONFIG_TYPE_STRING)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setLogOutEndPointUrl()
        def result = CH.config?.logoutEndpoint
        assertEquals logoutEndpoint, result
        CH.config?.logoutEndpoint = oldLogoutEndpoint
    }


    @Test
    public void testLogOutEndPointUrlLocal() {
        def oldAuthProvider = CH.config.banner.sso.authenticationProvider
        def oldLocalLogout = CH.config.banner?.sso?.authentication.saml.localLogout
        CH.config.banner.sso.authenticationProvider = 'saml'
        CH.config.banner?.sso?.authentication.saml.localLogout = 'true'
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_AUTH_PROVIDER, CH.config.banner.sso.authenticationProvider, CONFIG_TYPE_STRING)
        createConfigProperties(configApplication, CONFIG_NAME_LOCAL_LOGOUT, CH.config.banner?.sso?.authentication.saml.localLogout, CONFIG_TYPE_STRING)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setLogOutEndPointUrl()
        def result = CH.config?.logoutEndpoint
        assertEquals "saml/logout?local=true", result
        CH.config?.banner?.sso.authenticationProvider = oldAuthProvider
        CH.config?.banner?.sso?.authentication.saml.localLogout = oldLocalLogout
    }


    @Test
    public void testLogOutEndPointUrlNotLocal() {
        def oldAuthProvider = CH.config.banner.sso.authenticationProvider
        def oldLocalLogout = CH.config.banner?.sso?.authentication.saml.localLogout
        CH.config.banner.sso.authenticationProvider = 'saml'
        CH.config.banner?.sso?.authentication.saml.localLogout = 'false'
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_AUTH_PROVIDER, CH.config.banner.sso.authenticationProvider, CONFIG_TYPE_STRING)
        createConfigProperties(configApplication, CONFIG_NAME_LOCAL_LOGOUT, CH.config.banner?.sso?.authentication.saml.localLogout, CONFIG_TYPE_STRING)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setLogOutEndPointUrl()
        def result = CH.config.logoutEndpoint
        assertEquals "saml/logout", result
        CH.config.banner.sso.authenticationProvider = oldAuthProvider
        CH.config.banner?.sso?.authentication.saml.localLogout = oldLocalLogout
    }


    @Test
    public void testUpdateDefaultWebSessionTimeout() {
        def oldDefaultWebSessionTimeout = CH.config.defaultWebSessionTimeout
        ConfigApplication configApplication = createNewConfigApplication()
        Integer newDefaultWebSessionTimeout = 2000
        createConfigProperties(configApplication, CONFIG_NAME_DEFAULT_WEBSESSION_TIMEOUT, newDefaultWebSessionTimeout, CONFIG_TYPE_INTEGER)
        configPropertiesService.setConfigFromDb()
        configPropertiesService.updateDefaultWebSessionTimeout()
        def result = CH.config.defaultWebSessionTimeout
        assertEquals newDefaultWebSessionTimeout, result
        CH.config.defaultWebSessionTimeout = oldDefaultWebSessionTimeout
    }


    @Test
    public void testGetDecryptedValue() {
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_TESTAPP_PASSWORD, getEncryptedValue(CONFIG_VALUE_TESTAPP_PASSWORD), CONFIG_TYPE_CLEAR_TEXT)
        assertEquals CONFIG_VALUE_TESTAPP_PASSWORD, configPropertiesService.getDecryptedValue(appId, CONFIG_NAME_TESTAPP_PASSWORD)
    }


    @Test
    public void testGetDecryptedValueWithNoAppId() {
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_TESTAPP_PASSWORD, getEncryptedValue(CONFIG_VALUE_TESTAPP_PASSWORD), CONFIG_TYPE_CLEAR_TEXT)
        assertEquals null, configPropertiesService.getDecryptedValue(null, CONFIG_NAME_TESTAPP_PASSWORD)
    }


    @Test
    public void testGetDecryptedValueWithNoConfigName() {
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_TESTAPP_PASSWORD, getEncryptedValue(CONFIG_VALUE_TESTAPP_PASSWORD), CONFIG_TYPE_CLEAR_TEXT)
        assertEquals null, configPropertiesService.getDecryptedValue(appId, null)
    }


    @Test
    public void testGetDecryptedValueWithNoAppIdAndConfigName() {
        def configApplication = createNewConfigApplication()
        createConfigProperties(configApplication, CONFIG_NAME_TESTAPP_PASSWORD, getEncryptedValue(CONFIG_VALUE_TESTAPP_PASSWORD), CONFIG_TYPE_CLEAR_TEXT)
        assertEquals null, configPropertiesService.getDecryptedValue(null, null)
    }


    private String getEncryptedValue(String clearText ) {
        def conn
        String encryptedValue
        try {
            conn = dataSource.getSsbConnection()
            Sql db = new Sql(conn)
            db.call(ENCRYPT_TEXT_FUNCTION, [clearText, Sql.VARCHAR]) { v_bdmPasswd ->
                encryptedValue = v_bdmPasswd
        }
        }finally {
            conn?.close()
        }
        return encryptedValue
    }


    private void createNewGlobalConfigProps() {
        ConfigApplication configApplication = ConfigApplication.fetchByAppName(GLOBAL)
        assertNotNull configApplication?.id
        configApplication.refresh()
        def configProps = []

        ConfigProperties configProperties = getConfigProperties()
        configProperties.configName = "testing"
        configProperties.configType = 'string'
        configProperties.setConfigValue("GLOBAL")
        configProperties.setConfigApplication(configApplication)
        configProps.add(configProperties)
        configPropertiesService.create(configProps)

    }

    private createNewAppSpecificConfigProps() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        assertNotNull configApplication?.id

        def configProps = []

        ConfigProperties configProperties = getAppSpecificConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProps.add(configProperties)

        configPropertiesService.create(configProps)
    }

    /**
     * Mocking ConfigProperties domain.
     * @return ConfigProperties
     */
    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: CONFIG_NAME,
                configType: 'string',
                lastModified: new Date()
        )
        return configProperties
    }


    private ConfigProperties getAppSpecificConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: "testing",
                configType: 'string',
                configValue: "CUSTOM",
                lastModified: new Date()
        )
        return configProperties
    }


    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                lastModified: new Date(),
                appName: appName,
                appId: appId
        )
        return configApplication
    }

    private def mergeSeedDataKeysIntoConfigForTest() {
        ConfigSlurper configSlurper = new ConfigSlurper()
        Properties property = new Properties()

        property.put('ssconfig.app.seeddata.keys', getSeedDataKeysForTest())
        property.put('ssconfig.seedData.test.key1', 'test1')
        property.put('ssconfig.seedData.test.key2', 'test2')
        CH.config.merge(configSlurper.parse(property))
    }


    private def getSeedDataKeysForTest() {
        return [
                ['banner.applicationName': 'Sandbox'],
                ['ssconfig.seedData.test.key1', 'ssconfig.seedData.test.key2'],
                ['banner.applicationName1': 'SandboxTest']
        ]
    }


    private ConfigApplication createNewConfigApplication() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        return configApplication
    }


    private void createConfigProperties(ConfigApplication configApplication, String configName, configValue, String configType) {
        def configProps = []
        ConfigProperties configProperties = new ConfigProperties(
                configName: configName,
                configType: configType,
                configValue: configValue,
                configApplication: configApplication,
                lastModified: new Date()
        )
        configProps.add(configProperties)
        configPropertiesService.create(configProps)
    }

}
