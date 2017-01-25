/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigUserPreferenceIntegrationTest.
 */
class ConfigUserPreferenceIntegrationTest extends BaseIntegrationTestCase {

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

        ConfigUserPreference configUserPreference = getConfigUserPreference()
        configUserPreference.setGubapplAppId(configApplication.getAppId())
        configUserPreference.setConfigName(configurationProperties.getConfigName())
        configUserPreference.setConfigType(configurationProperties.getConfigType())
        configUserPreference.save(failOnError: true, flush: true)

        //Save
        def list = configUserPreference.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).configValue == 'TEST_VALUE')

        //Update
        configUserPreference.setConfigValue('NEW_TEST_VALUE')
        configUserPreference.save(failOnError: true, flush: true)
        list = configUserPreference.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).configValue == 'NEW_TEST_VALUE')

        //Delete
        configUserPreference.delete()
        list = configUserPreference.findAll()
        assert (list.size() >= 0)
    }

    /**
     * Mocking ConfigUserPreference domain.
     * @return ConfigUserPreference
     */
    private ConfigUserPreference getConfigUserPreference() {
        ConfigUserPreference configUserPreference = new ConfigUserPreference(
                lastModifiedBy: 'TEST_USER',
                lastModified: new Date(),
                pidm: 1,
                configValue: 'TEST_VALUE',
                version: 0
        )
        return configUserPreference
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                version: 0,
                lastModified: new Date(),
                appName: 'PlatformSandboxApp',
                appId: 1,
                lastModifiedBy: 'TEST_USER',
        )
        return configApplication
    }

    private ConfigurationProperties getConfigurationProperties() {
        ConfigurationProperties configurationProperties = new ConfigurationProperties(
                version: 0,
                configName: 'CONFIG_TEST',
                configType: 'TYPE_TEST',
                configValue: 'TEST_VALUE',
                lastModified: new Date()
        )
        return configurationProperties
    }
}
