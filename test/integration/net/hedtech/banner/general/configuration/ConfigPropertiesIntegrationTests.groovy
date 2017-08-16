/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.QueryTimeoutException

/**
 * ConfigPropertiesIntegrationTests are used to test the ConfigProperties domain.
 */
class ConfigPropertiesIntegrationTests extends BaseIntegrationTestCase {

    private def appName
    private def appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        appName = Holders.grailsApplication.metadata['app.name']
        appId = 'TESTAPP'
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testCreateConfigProperties() {
        ConfigProperties configProperties = createNewConfigProperties()

        //Test if the generated entity now has an id assigned
        assertNotNull configProperties.id
        assertEquals 0L, configProperties.version
        assertEquals "TEST_CONFIG", configProperties.configName
        assertEquals "TEST_CONFIG_TYPE", configProperties.configType
        assertEquals "TEST_VALUE", configProperties.configValue
    }


    @Test
    void testSuccessCreateLongConfigName() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.configName = "Y" * 256
        configProperties.save(failOnError: true, flush: true)

        assertNotNull configProperties.id
        assertEquals 0L, configProperties.version
        assertEquals "Y" * 256 , configProperties.configName
        assertEquals "TEST_CONFIG_TYPE", configProperties.configType
        assertEquals "TEST_VALUE", configProperties.configValue
    }


    @Test
    void testFailureCreateLongConfigName() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.configName = "Y" * 257
        try {
            configProperties.save(failOnError: true, flush: true)
        }
        catch (QueryTimeoutException ex){
            assertTrue ex.getCause().getCause().message.contains('ORA-12899: value too large for column "GENERAL"."GUROCFG"."GUROCFG_NAME" (actual: 257, maximum: 256)')

        }
    }


    @Test
    void testDeleteConfigApplication() {
        ConfigProperties configProperties = createNewConfigProperties()

        assertNotNull configProperties.id
        assertEquals 0L, configProperties.version

        def id = configProperties.id
        configProperties.delete()
        assertNull configProperties.get(id)
    }


    @Test
    public void testSerialization() {
        try {
            ConfigProperties configProperties = createNewConfigProperties()

            assertNotNull configProperties.id
            assertEquals 0L, configProperties.version
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(configProperties)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigProperties configPropertiesCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                configPropertiesCopy = (ConfigProperties) is.readObject()
                is.close()
            }
            assertEquals configPropertiesCopy, configProperties

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    void testToString() {
        ConfigProperties newConfigProperties = createNewConfigProperties()

        assertNotNull newConfigProperties.id
        assertEquals 0L, newConfigProperties.version

        List<ConfigProperties> configPropertyList = ConfigProperties.fetchByAppId(newConfigProperties.configApplication.appId)
        assertFalse configPropertyList.isEmpty()
        configPropertyList.each { configProperties ->
            String configPropertiesToString = configProperties.toString()
            assertNotNull configPropertiesToString
            assertTrue configPropertiesToString.contains('configName')
        }
    }


    @Test
    void testHashCode() {
        ConfigProperties newConfigProperties = createNewConfigProperties()

        assertNotNull newConfigProperties.id
        assertEquals 0L, newConfigProperties.version

        List<ConfigProperties> configPropertyList = ConfigProperties.fetchByAppId(newConfigProperties.configApplication.appId)
        assertFalse configPropertyList.isEmpty()
        configPropertyList.each { configProperties ->
            String configPropertiesHashCode = configProperties.hashCode()
            assertNotNull configPropertiesHashCode
        }
    }

    @Test
    public void testFetchByValidAppId() {
        ConfigProperties configProperties = createNewConfigProperties()

        assertNotNull configProperties.id
        assertEquals 0L, configProperties.version

        def list = ConfigProperties.fetchByAppId(configProperties.configApplication.appId)
        assertTrue list.size() >= 1

        list.each { it ->
            assertEquals it.configApplication.appId, configProperties.configApplication.appId
        }
    }


    @Test
    void testFetchByInValidAppId() {
        ConfigProperties configProperties = createNewConfigProperties()

        assertNotNull configProperties.id

        String appID = 'invalidApp'
        def list = ConfigProperties.fetchByAppId(appID)
        assertTrue list.size() == 0
    }

    @Test
    void testFetchSimpleConfigByAppId() {
        ConfigProperties configProperties = createNewConfigProperties()

        def list = ConfigProperties.fetchSimpleConfigByAppId(configProperties.configApplication.appId)
        list.each { ConfigProperties configProp ->
            assertTrue(configProp.configType?.appId == 'boolean' || configProp.configType == 'string' || configProp.configType == 'integer')
        }
    }

    private ConfigProperties createNewConfigProperties() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.save(failOnError: true, flush: true)
    }


    /**
     * Mocking ConfigProperties domain.
     * @return ConfigProperties
     */
    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: 'TEST_CONFIG',
                configType: 'TEST_CONFIG_TYPE',
                configValue: 'TEST_VALUE'
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
                appName: appName,
                appId: appId
        )
        return configApplication
    }
}
