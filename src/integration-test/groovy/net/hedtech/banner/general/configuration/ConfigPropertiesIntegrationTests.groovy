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
        assertEquals "string", configProperties.configType
        assertEquals "TEST_VALUE", configProperties.configValue
        assertEquals "TEST_COMMENT", configProperties.configComment
        assertEquals false, configProperties.userPreferenceIndicator
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
        assertEquals "string", configProperties.configType
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

        list = ConfigProperties.fetchByAppId(null)
        assertTrue list.size() == 0
    }

    @Test
    void testFetchSimpleConfigByAppId() {
        ConfigProperties configProperties = createNewConfigProperties()

        def list = ConfigProperties.fetchSimpleConfigByAppId(configProperties.configApplication.appId)
        list.each { ConfigProperties configProp ->
            assertTrue(configProp.configType == 'boolean' || configProp.configType == 'string' || configProp.configType == 'integer')
        }
    }


    @Test
    void testFetchUserConfigurationByConfigNameAndAppId() {
        ConfigProperties configProperties = createNewConfigProperties()
        def userConfigList = ConfigProperties.fetchUserConfigurationByConfigNameAndAppId('TEST_CONFIG', configProperties.configApplication.appId)
        assertNull userConfigList

        configProperties.setUserPreferenceIndicator(true)
        configProperties.save(failOnError: true, flush: true)

        def userConfigList2 = ConfigProperties.fetchUserConfigurationByConfigNameAndAppId('TEST_CONFIG', configProperties.configApplication.appId)
        assertNotNull userConfigList2
        userConfigList2.each { ConfigProperties configProp ->
            assertTrue(configProp.configType == 'boolean' || configProp.configType == 'string' || configProp.configType == 'integer')
            assertTrue configProp.userPreferenceIndicator
        }
    }


    @Test
    void testFetchByConfigNameAndAppId() {
        ConfigProperties newConfigProperties = createNewConfigProperties()
        ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId('TEST_CONFIG', newConfigProperties.configApplication.appId)
        assertNotNull configProperties

        assertTrue(configProperties.configType == 'boolean' || configProperties.configType == 'string' || configProperties.configType == 'integer' || configProperties.configType == 'encryptedtext')
        assertEquals configProperties.configApplication.appId, "TESTAPP"
        assertEquals configProperties.configName, "TEST_CONFIG"
    }

    @Test
    void testEqualsConfigName() {
        ConfigProperties configProperties1 = new ConfigProperties(configName: "TestName")
        ConfigProperties configProperties2 = new ConfigProperties(configName: "TestName1")
        assertFalse configProperties2==configProperties1
    }

    @Test
    void testEqualsConfigType() {
        ConfigProperties configProperties1 = new ConfigProperties(configType: "String")
        ConfigProperties configProperties2 = new ConfigProperties(configType: "Integer")
        assertFalse configProperties2==configProperties1
    }

    @Test
    void testEqualsConfigValue() {
        ConfigProperties configProperties1 = new ConfigProperties(configValue: "200")
        ConfigProperties configProperties2 = new ConfigProperties(configValue: "300")
        assertFalse configProperties2==configProperties1
    }

    @Test
    void testEqualsLastModifiedEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(lastModified: new Date(12,2,12))
        ConfigProperties configProperties2 = new ConfigProperties(lastModified: new Date(12,2,14))
        assertFalse configProperties2==configProperties1
    }


   @Test
    void testEqualsDataOriginNotEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(dataOrigin: "GENERAL")
        ConfigProperties configProperties2 = new ConfigProperties(dataOrigin: "BANNER")
        assertFalse configProperties2==configProperties1
    }


    @Test
    void testEqualsAppIdNotEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(id: 1234)
        ConfigProperties configProperties2 = new ConfigProperties(id:12345)
        assertFalse configProperties2==configProperties1
    }


    @Test
    void testEqualsLastModifiedByNotEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(lastModifiedBy: "TestUser")
        ConfigProperties configProperties2 = new ConfigProperties(lastModifiedBy: "GRAILS")
        assertFalse configProperties2==configProperties1
    }


    @Test
    void testEqualsVersionNotEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(version: 1)
        ConfigProperties configProperties2 = new ConfigProperties(version: 2)
        assertFalse configProperties2==configProperties1
    }


    @Test
    void testEqualsConfigCommentNotEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(configComment: "Config Value Column")
        ConfigProperties configProperties2 = new ConfigProperties(configComment: "Config Name Column")
        assertFalse configProperties2==configProperties1
    }


    @Test
    void testEqualsUserPreferenceIndicatorNotEqual() {
        ConfigProperties configProperties1 = new ConfigProperties(userPreferenceIndicator: true)
        ConfigProperties configProperties2 = new ConfigProperties(userPreferenceIndicator: false)
        assertFalse configProperties2==configProperties1
    }


    @Test
    void testEqualsConfigApplication() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId")
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName1",
                appId: "TestId1")
        ConfigProperties configProperties1 = new ConfigProperties(configApplication: configApplication1)
        ConfigProperties configProperties2 = new ConfigProperties(configApplication: configApplication2)
        assertFalse configProperties1 == configProperties2
    }


   @Test
    void testEqualsIs() {
        ConfigProperties configProperties1 = new ConfigProperties()
        assertTrue configProperties1.equals(configProperties1)
    }


    @Test
    void testEqualsClass() {
        ConfigApplication configApplication = new ConfigApplication()
        ConfigProperties configProperties=new ConfigProperties()
        assertFalse configProperties.equals(configApplication)
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
                configType: 'string',
                configValue: 'TEST_VALUE',
                configComment: 'TEST_COMMENT'
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
