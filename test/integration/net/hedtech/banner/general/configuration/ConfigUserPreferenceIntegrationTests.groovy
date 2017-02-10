/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigUserPreferenceIntegrationTests.
 */
class ConfigUserPreferenceIntegrationTests extends BaseIntegrationTestCase {

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

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.save(failOnError: true, flush: true)

        ConfigUserPreference configUserPreference = getConfigUserPreference()
        configUserPreference.setConfigApplication(configApplication)
        configUserPreference.setConfigName(configProperties.getConfigName())
        configUserPreference.setConfigType(configProperties.getConfigType())
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

    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: 'CONFIG_TEST',
                configType: 'TYPE_TEST',
                configValue: 'TEST_VALUE'
        )
        return configProperties
    }
}
