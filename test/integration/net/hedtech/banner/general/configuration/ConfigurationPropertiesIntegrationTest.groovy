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
    public void testSaveConfigurationProperties() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version
    }


    @Test
    public void testFetchAll() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version

        def list = configurationProperties.fetchAll()
        assert (list.size() >= 1)
    }


    @Test
    public void testFetchByValidAppName() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version

        def list = configurationProperties.fetchByConfigName("TEST_CONFIG")
        assertTrue (list.size() >= 1)
    }


    @Test
    public void testFetchByInValidAppName() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version

        def list = configurationProperties.fetchByConfigName("XXX")
        assertTrue list.size() == 0
    }


    @Test
    public void testFetchByNullAppName() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version

        def list = configurationProperties.fetchByConfigName(null)
        assertNull list
    }

    @Test
    public void testSerialization() {
        try {
            ConfigurationProperties configurationProperties = createConfigurationProperties()
            assertNotNull configurationProperties.id
            assertNotNull configurationProperties.configName
            assertEquals 0L, configurationProperties.version

            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(configurationProperties)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigurationProperties configurationPropertiesCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                configurationPropertiesCopy = (ConfigurationProperties)is.readObject()
                is.close()
            }
            assertEquals configurationPropertiesCopy, configurationProperties

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    void testToString() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version

        List <ConfigurationProperties> configurationPropertyList = ConfigurationProperties.fetchAll()
        assertFalse configurationPropertyList.isEmpty()
        configurationPropertyList.each { each ->
            String configurationPropertiesToString = each.toString()
            assertNotNull configurationPropertiesToString
            assertTrue configurationPropertiesToString.contains('configName')
        }
    }


    @Test
    void testHashCode() {
        ConfigurationProperties configurationProperties = createConfigurationProperties()
        assertNotNull configurationProperties.id
        assertNotNull configurationProperties.configName
        assertEquals 0L, configurationProperties.version

        List <ConfigurationProperties> configurationPropertyList = ConfigurationProperties.fetchAll()
        assertFalse configurationPropertyList.isEmpty()
        configurationPropertyList.each { each ->
            Integer configurationPropertiesHashCode = each.hashCode()
            assertNotNull configurationPropertiesHashCode
        }
    }


    private ConfigurationProperties createConfigurationProperties() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigurationProperties configurationProperties = getConfigurationProperties()
        configurationProperties.setConfigApplication(configApplication)
        configurationProperties.save(failOnError: true, flush: true)
        return configurationProperties
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
