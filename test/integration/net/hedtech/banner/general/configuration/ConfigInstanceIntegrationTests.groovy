/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigInstanceIntegrationTests.
 */
class ConfigInstanceIntegrationTests extends BaseIntegrationTestCase {

    final private static def APP_NAME = Holders.grailsApplication.metadata['app.name']

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
    public void testSave() {
        ConfigInstance configInstance = createConfigInstance()

        assertNotNull configInstance.env
        assertNotNull configInstance.id
    }


    @Test
    public void testUpdate() {
        ConfigInstance configInstance = createConfigInstance()

        assertNotNull configInstance.env
        assertNotNull configInstance.id

        def list = configInstance.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == '/TEST_URL')

        //Update
        configInstance.setUrl('/NEW_TEST_URL')
        configInstance.save(failOnError: true, flush: true)
        list = configInstance.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == '/NEW_TEST_URL')

        //Delete
        configInstance.delete()
        list = configInstance.fetchAll()
        assert list.size() >= 0
    }


    @Test
    public void testFetchAll() {
        ConfigInstance configInstance = createConfigInstance()

        assertNotNull configInstance.env
        assertNotNull configInstance.id

        def list = ConfigInstance.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == '/TEST_URL')

        //Update
        configInstance.setUrl('/NEW_TEST_URL')
        configInstance.save(failOnError: true, flush: true)
        list = configInstance.fetchAll()
        assert (list.size() > 0)
        assert (list.getAt(0).url == '/NEW_TEST_URL')

        //Delete
        configInstance.delete()
        list = configInstance.fetchAll()
        assert (list.size() >= 0)
    }


    @Test
    public void testSerialization() {
        try {
            ConfigInstance configInstance = createConfigInstance()

            assertNotNull configInstance.env
            assertNotNull configInstance.id

            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(configInstance)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigInstance configInstanceCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                configInstanceCopy = (ConfigInstance)is.readObject()
                is.close()
            }
            assertEquals configInstanceCopy, configInstance

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    void testToString() {
        ConfigInstance newConfigInstance = createConfigInstance()

        assertNotNull newConfigInstance.env
        assertNotNull newConfigInstance.id
        List <ConfigInstance> configInstances = ConfigInstance.fetchAll()
        assertFalse configInstances.isEmpty()
        configInstances.each { configInstance ->
            String configInstanceToString = configInstance.toString()
            assertNotNull configInstanceToString
            assertTrue configInstanceToString.contains('env')
        }
    }


    @Test
    void testHashCode() {
        ConfigInstance newConfigInstance = createConfigInstance()

        assertNotNull newConfigInstance.env
        assertNotNull newConfigInstance.id
        List <ConfigInstance> configInstances = ConfigInstance.fetchAll()
        assertFalse configInstances.isEmpty()
        configInstances.each { configInstance ->
            Integer configInstanceHashCode = configInstance.hashCode()
            assertNotNull configInstanceHashCode
        }
    }


    private ConfigInstance createConfigInstance() {
        ConfigApplication application = getConfigApplication()
        application.save(failOnError: true, flush: true)
        application.refresh()

        assertNotNull application.appId
        assertNotNull application.id

        ConfigInstance configInstance = getConfigInstance()
        configInstance.configApplication = application
        configInstance.save(failOnError: true, flush: true)
        return configInstance.refresh()
    }

    private ConfigInstance getConfigInstance() {
        ConfigInstance configInstance = new ConfigInstance(
                url: '/TEST_URL',
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
                appName: APP_NAME
        )
        return configApplication
    }

}
