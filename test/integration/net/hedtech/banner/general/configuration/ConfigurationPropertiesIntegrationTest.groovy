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
    public void testFindAll() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)

        ConfigurationProperties configurationProperties = getConfigurationProperties()
        configurationProperties.setGubapplAppId(configApplication.getAppId())
        configurationProperties.save(failOnError: true, flush: true)

        def list = configurationProperties.findAll()
        assert (list.size() >= 0)
    }

    @Test
    public void testFindByAppName() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)

        ConfigurationProperties configurationProperties = getConfigurationProperties()
        configurationProperties.setGubapplAppId(configApplication.getAppId())
        configurationProperties.save(failOnError: true, flush: true)

        def list = configurationProperties.findByAppName(APP_NAME)
        assert (list.size() > 0)
    }

    /**
     * Mocking ConfigurationProperties domain.
     * @return ConfigurationProperties
     */
    private ConfigurationProperties getConfigurationProperties() {
        ConfigurationProperties configurationProperties = new ConfigurationProperties(
                lastModifiedBy: 'TEST_USER',
                lastModified: new Date(),
                configName: 'TEST_CONFIG',
                configType: 'TEST_CONFIG_TYPE',
                configValue: 'TEST_VALUE',
                version: 0
        )
        return configurationProperties
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                version: 0,
                lastModified: new Date(),
                appName: APP_NAME,
                appId: 1,
                lastModifiedBy: 'TEST_USER',
        )
        return configApplication
    }
}
