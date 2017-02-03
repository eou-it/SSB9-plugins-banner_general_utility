/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigInstanceIntegrationTest.
 */
class ConfigInstanceIntegrationTest extends BaseIntegrationTestCase {

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
        ConfigApplication application = getConfigApplication()
        application.save(failOnError: true, flush: true)
        application.refresh()

        ConfigInstance configInstance = getConfigInstance()
        configInstance.setGubapplAppId(application.appId)
        configInstance.save(failOnError: true, flush: true)

        //Save
        def list = configInstance.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == 'TEST_URL')

        //Update
        configInstance.setUrl('NEW_TEST_URL')
        configInstance.save(failOnError: true, flush: true)
        list = configInstance.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == 'NEW_TEST_URL')

        //Delete
        configInstance.delete()
        list = configInstance.fetchAll()
        assert (list.size() >= 0)
    }

    private ConfigInstance getConfigInstance() {
        ConfigInstance configInstance = new ConfigInstance(
                env: 1,
                gubapplAppId: 1,
                url: 'TEST_URL',
        )
        return configInstance
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appId: 1,
                appName: 'PlatformSandboxApp',
        )
        return configApplication
    }

}
