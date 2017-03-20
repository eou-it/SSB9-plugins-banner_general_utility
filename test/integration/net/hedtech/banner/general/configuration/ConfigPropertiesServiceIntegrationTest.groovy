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
        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version

        def configProps = []

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProps.add(configProperties)

        ConfigProperties configPropertiesBoolean = getConfigProperties()
        configPropertiesBoolean.configType = 'boolean'
        configPropertiesBoolean.configName = CONFIG_NAME + '-boolean'
        configPropertiesBoolean.setConfigApplication(configApplication)
        configProps.add(configPropertiesBoolean)

        ConfigProperties configPropertiesInteger = getConfigProperties()
        configPropertiesInteger.configType = 'integer'
        configPropertiesInteger.configName = CONFIG_NAME + '-integer'
        configPropertiesInteger.configValue = 10
        configPropertiesInteger.setConfigApplication(configApplication)
        configProps.add(configPropertiesInteger)
        configPropertiesService.create(configProps)
    }

    private void createNewConfigPropsWithNoAppId() {
        def configProps = []
        ConfigProperties configPropertiesBoolean = getConfigProperties()
        configPropertiesBoolean.configType = 'boolean'
        configPropertiesBoolean.configName = CONFIG_NAME + '-boolean'
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
                configType: 'String',
                configValue: CONFIG_VALUE,
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
