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

/**
 * ConfigPropertiesServiceIntegrationTest.
 */
class ConfigPropertiesServiceIntegrationTest extends BaseIntegrationTestCase {

    def configPropertiesService
    def configApplicationService
    def grailsApplication

    private def appName
    private def appId
    private static final String CONFIG_NAME = 'TEST_CONFIG'
    private static final String CONFIG_VALUE = 'TEST_VALUE'
    private static final String GLOBAL = 'GLOBAL'
    private static final String TESTAPP = 'TESTAPP'
    private static String ACTUALAPPNAME = ''
    private static String ACTUALAPPID = ''

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

    @Test
    public void testGlobalConfiguration() {
        setSurrogateIdForGlobal(999)
        createNewGlobalConfigProps()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get("testing") == GLOBAL
        setSurrogateIdForGlobal(null)
    }

    @Test
    public void testAppPreferenceOverGlobal() {
        setSurrogateIdForGlobal(999)
        createNewGlobalConfigProps()
        createNewAppSpecificConfigProps()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get("testing") == "CUSTOM"
        setSurrogateIdForGlobal(null)
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

    private setSurrogateIdForGlobal(id) {
        Sql sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GUBAPPL set GUBAPPL_SURROGATE_ID = ?  where GUBAPPL_APP_ID = ?", [id, GLOBAL])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
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

}
