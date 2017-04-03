/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders
import grails.util.Holders as CH
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

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        appName = Holders.grailsApplication.metadata['app.name']
        appId = 'TESTAPP'
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testSetConfigFromDb() {
        createNewConfigProperties()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME) == CONFIG_VALUE
    }

    @Test
    public void testSetConfigFromDBWithNoAppId() {
        createNewConfigPropsWithNoAppId()
        configPropertiesService.setConfigFromDb()

        assertTrue CH.config.get(CONFIG_NAME) == ''
        assertTrue CH.config.get(CONFIG_NAME + '-boolean') == true
        assertTrue CH.config.get(CONFIG_NAME + '-integer') == 12
    }

    /**
     * Saving the ConfigProperties
     * @return
     */
    private void createNewConfigProperties() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version

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

        ConfigProperties configPropertiesBooleanNullValue = getConfigProperties()
        configPropertiesBooleanNullValue.configType = 'boolean'
        configPropertiesBooleanNullValue.configName = CONFIG_NAME + '-boolean-null'
        configPropertiesBooleanNullValue.configValue = ''
        configPropertiesBooleanNullValue.setConfigApplication(configApplication)

        ConfigProperties configProp = configPropertiesService.create(configPropertiesBooleanNullValue)
        configProp.refresh()
        assert configProp.configValue == null

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME + '-boolean-null') == false
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

    private void createNewConfigPropsWithNoAppId() {
        def configProps = []
        ConfigProperties configPropertiesString = getConfigProperties()
        configPropertiesString.configValue = ''
        configProps.add(configPropertiesString)

        ConfigProperties configPropertiesBoolean = getConfigProperties()
        configPropertiesBoolean.configType = 'boolean'
        configPropertiesBoolean.configName = CONFIG_NAME + '-boolean'
        configPropertiesBoolean.configValue = 'true'
        configProps.add(configPropertiesBoolean)

        ConfigProperties configPropertiesInteger = getConfigProperties()
        configPropertiesInteger.configType = 'integer'
        configPropertiesInteger.configName = CONFIG_NAME + '-integer'
        configPropertiesInteger.configValue = 12
        configProps.add(configPropertiesInteger)
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
                //configValue: CONFIG_VALUE,
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
}
