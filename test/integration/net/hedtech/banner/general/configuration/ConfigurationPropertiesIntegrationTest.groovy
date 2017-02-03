/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigurationPropertiesIntegrationTest is used to test the ConfigurationProperties domain.
 */
class ConfigurationPropertiesIntegrationTest extends BaseIntegrationTestCase {

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
    public void testFetchAll() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigurationProperties configurationProperties = getConfigurationProperties()
        configurationProperties.setGubapplAppId(configApplication.getAppId())
        configurationProperties.save(failOnError: true, flush: true)

        def list = configurationProperties.fetchAll()
        assert (list.size() >= 0)
    }

    @Test
    public void testFetchByAppName() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigurationProperties configurationProperties = getConfigurationProperties()
        configurationProperties.setGubapplAppId(configApplication.getAppId())
        configurationProperties.save(failOnError: true, flush: true)

        def list = configurationProperties.fetchByAppName(APP_NAME)
        assert (list.size() > 0)
    }

    /**
     * Mocking ConfigurationProperties domain.
     * @return ConfigurationProperties
     */
    private ConfigurationProperties getConfigurationProperties() {
        ConfigurationProperties configurationProperties = new ConfigurationProperties(
                configName: 'TEST_CONFIG',
                configType: 'TEST_CONFIG_TYPE',
                configValue: 'TEST_VALUE'
        )
        return configurationProperties
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
