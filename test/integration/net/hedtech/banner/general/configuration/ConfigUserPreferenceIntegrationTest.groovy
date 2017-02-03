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
    public void testFetchAll() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigurationProperties configurationProperties = getConfigurationProperties()
        configurationProperties.setGubapplAppId(configApplication.getAppId())
        configurationProperties.save(failOnError: true, flush: true)

        ConfigUserPreference configUserPreference = getConfigUserPreference()
        configUserPreference.setGubapplAppId(configApplication.getAppId())
        configUserPreference.setConfigName(configurationProperties.getConfigName())
        configUserPreference.setConfigType(configurationProperties.getConfigType())
        configUserPreference.save(failOnError: true, flush: true)

        //Save
        def list = configUserPreference.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).configValue == 'TEST_VALUE')

        //Update
        configUserPreference.setConfigValue('NEW_TEST_VALUE')
        configUserPreference.save(failOnError: true, flush: true)
        list = configUserPreference.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).configValue == 'NEW_TEST_VALUE')

        //Delete
        configUserPreference.delete()
        list = configUserPreference.fetchAll()
        assert (list.size() >= 0)
    }

    /**
     * Mocking ConfigUserPreference domain.
     * @return ConfigUserPreference
     */
    private ConfigUserPreference getConfigUserPreference() {
        ConfigUserPreference configUserPreference = new ConfigUserPreference(
                pidm: 1,
                configValue: 'TEST_VALUE'
        )
        return configUserPreference
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: 'PlatformSandboxApp'
        )
        return configApplication
    }

    private ConfigurationProperties getConfigurationProperties() {
        ConfigurationProperties configurationProperties = new ConfigurationProperties(
                configName: 'CONFIG_TEST',
                configType: 'TYPE_TEST',
                configValue: 'TEST_VALUE'
        )
        return configurationProperties
    }
}
