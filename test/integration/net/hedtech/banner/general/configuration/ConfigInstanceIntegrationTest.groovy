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
    public void testFindAll() {
        ConfigApplication application = getConfigApplication()
        application.save(failOnError: true, flush: true)

        ConfigInstance configInstance = getConfigInstance()
        configInstance.setGubapplAppId(application.getAppId())
        configInstance.save(failOnError: true, flush: true)

        //Save
        def list = configInstance.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == 'TEST_URL')

        //Update
        configInstance.setUrl('NEW_TEST_URL')
        configInstance.save(failOnError: true, flush: true)
        list = configInstance.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == 'NEW_TEST_URL')

        //Delete
        configInstance.delete()
        list = configInstance.findAll()
        assert (list.size() >= 0)
    }

    private ConfigInstance getConfigInstance() {
        ConfigInstance configInstance = new ConfigInstance(
                version: 0,
                lastModified: new Date(),
                env: 1,
                gubapplAppId: 1,
                url: 'TEST_URL',
                lastModifiedBy: 'TEST_USER'
        )
        return configInstance
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                version: 0,
                appId: 1,
                lastModified: new Date(),
                appName: 'PlatformSandboxApp',
                lastModifiedBy: 'TEST_USER',
        )
        return configApplication
    }

}
