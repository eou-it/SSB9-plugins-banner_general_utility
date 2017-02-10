/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigPropertiesIntegrationTests are used to test the ConfigProperties domain.
 */
class ConfigPropertiesIntegrationTests extends BaseIntegrationTestCase {

    private static final String APP_NAME = 'PlatformSandboxApp'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testFetchByAppId() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.save(failOnError: true, flush: true)

        def list = configProperties.fetchByAppId(1)
        assert (list.size() > 0)
    }

    /**
     * Mocking ConfigProperties domain.
     * @return ConfigProperties
     */
    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: 'TEST_CONFIG',
                configType: 'TEST_CONFIG_TYPE',
                configValue: 'TEST_VALUE',
                configAppId: '1'
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
                appName: APP_NAME
        )
        return configApplication
    }
}
