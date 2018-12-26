/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigUserPreferenceIntegrationTests.
 */
@Integration
@Rollback
class ConfigUserPreferenceIntegrationTests extends BaseIntegrationTestCase {

    Integer pidm
    private String appName
    private def appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        pidm = getPidmBySpridenId("HOSH00001")
        appName = 'TESTAPP'
        appId = 'TESTAPP'
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    public void testSaveConfigUserPreference() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version
    }


    @Test
    void testSuccessCreateLongConfigName() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()
        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.configName = "Y" * 256
        configProperties.save(failOnError: true, flush: true)

        assertNotNull configProperties.id
        assertEquals 1L, configProperties.version
        assertEquals "Y" * 256 , configProperties.configName
        assertEquals "string", configProperties.configType
        assertEquals "TEST_VALUE", configProperties.configValue


        configUserPreference.configName = "Y" * 256
        configUserPreference.save(failOnError: true, flush: true)
        assertNotNull configUserPreference.id
        assertEquals 1L, configUserPreference.version
        assertEquals "Y" * 256 , configUserPreference.configName
        assertEquals "string", configUserPreference.configType
        assertEquals "USER_TEST_VALUE", configUserPreference.configValue
    }

    @Test
    void testDeleteConfigUserPreference() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        def id = configUserPreference.id
        configUserPreference.delete()
        assertNull configUserPreference.get(id)
    }


    @Test
    public void testFetchAll() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        def list = ConfigUserPreference.fetchAll()
        assert (list.size() >= 1)
    }


    @Test
    public void testfetchByValidPidm() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        List list = ConfigUserPreference.fetchByPidm(pidm)
        assertTrue (list.size() > 1)
        list.each() { it ->
            assertEquals pidm, it.pidm
        }
    }


    @Test
    public void testfetchByInValidPidm() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        List list = ConfigUserPreference.fetchByPidm(-99)
        assertEquals 0, list.size()

        list = ConfigUserPreference.fetchByPidm(null)
        list = ConfigUserPreference.fetchByPidm(null)
        assertEquals 0, list.size()
    }


    @Test
    public void testfetchByValidPidmAndConfig() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        ConfigUserPreference configUserPreference2 = ConfigUserPreference.fetchByConfigNamePidmAndAppId('CONFIG_TEST', pidm, appId)
        assertEquals configUserPreference, configUserPreference2
        assertEquals appId, configUserPreference2.configApplication.appId
        assertEquals pidm, configUserPreference2.pidm
    }


    @Test
    public void testfetchByInValidPidmAndConfig() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        ConfigUserPreference configUserPreference2 = ConfigUserPreference.fetchByConfigNamePidmAndAppId('CONFIG_TEST', -99, "appId")
        assertNull configUserPreference2

        configUserPreference2 = ConfigUserPreference.fetchByConfigNamePidmAndAppId(null,null,null)
        assertNull configUserPreference2

        configUserPreference2 = ConfigUserPreference.fetchByConfigNamePidmAndAppId(null,pidm,"appId")
        assertNull configUserPreference2

        configUserPreference2 = ConfigUserPreference.fetchByConfigNamePidmAndAppId('CONFIG_TEST',pidm,null)
        assertNull configUserPreference2
    }


    @Test
    public void testSerialization() {
        try {
            ConfigUserPreference newConfigUserPreference = createConfigUserPreference()

            assertNotNull newConfigUserPreference.id
            assertEquals 0L, newConfigUserPreference.version

            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(newConfigUserPreference)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigUserPreference configUserPreferenceCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                configUserPreferenceCopy = (ConfigUserPreference) is.readObject()
                is.close()
            }
            assertEquals configUserPreferenceCopy, newConfigUserPreference

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    void testToString() {
        ConfigUserPreference newConfigUserPreference = createConfigUserPreference()

        assertNotNull newConfigUserPreference.id
        assertEquals 0L, newConfigUserPreference.version

        List<ConfigUserPreference> configUserPreferences = ConfigUserPreference.fetchAll()
        assertFalse configUserPreferences.isEmpty()
        configUserPreferences.each { configUserPreference ->
            String configUserPreferenceToString = configUserPreference.toString()
            assertNotNull configUserPreferenceToString
            assertTrue configUserPreferenceToString.contains('configName')
        }
    }


    @Test
    void testHashCode() {
        ConfigUserPreference newConfigUserPreference = createConfigUserPreference()

        assertNotNull newConfigUserPreference.id
        assertEquals 0L, newConfigUserPreference.version

        List<ConfigUserPreference> configUserPreferences = ConfigUserPreference.fetchAll()
        assertFalse configUserPreferences.isEmpty()
        configUserPreferences.each { configUserPreference ->
            Integer configUserPreferenceHashCode = configUserPreference.hashCode()
            assertNotNull configUserPreferenceHashCode
        }
    }


    @Test
    void testEqualsConfigName() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(configName: "TestName")
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(configName: "TestName1")
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsConfigType() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(configType: "String")
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(configType: "Integer")
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsConfigValue() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(configValue: "200")
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(configValue: "300")
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsLastModifiedEqual() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(lastModified: new Date(12,2,12))
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(lastModified: new Date(12,2,14))
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsDataOriginNotEqual() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(dataOrigin: "GENERAL")
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(dataOrigin: "BANNER")
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsAppIdNotEqual() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(id: 1234)
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(id:12345)
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsLastModifiedByNotEqual() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(lastModifiedBy: "TestUser")
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(lastModifiedBy: "GRAILS")
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsVersionNotEqual() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(version: 1)
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(version: 2)
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsPidmNotEqual() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(pidm: 49627)
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(pidm: 49628)
        assertFalse configUserPreference2==configUserPreference1
    }


    @Test
    void testEqualsConfigApplication() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId")
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName1",
                appId: "TestId1")
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference(configApplication: configApplication1)
        ConfigUserPreference configUserPreference2 = new ConfigUserPreference(configApplication: configApplication2)
        assertFalse configUserPreference1 == configUserPreference2
    }


    @Test
    void testEqualsIs() {
        ConfigUserPreference configUserPreference1 = new ConfigUserPreference()
        assertTrue configUserPreference1.equals(configUserPreference1)
    }


    @Test
    void testEqualsClass() {
        ConfigApplication configApplication = new ConfigApplication()
        ConfigUserPreference configUserPreference=new ConfigUserPreference()
        assertFalse configUserPreference.equals(configApplication)
    }

    private ConfigUserPreference createConfigUserPreference() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication.refresh()

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.save(failOnError: true, flush: true)

        ConfigUserPreference configUserPreference = getConfigUserPreference()
        configUserPreference.setConfigApplication(configApplication)
        configUserPreference.setConfigName(configProperties.getConfigName())
        configUserPreference.setConfigType(configProperties.getConfigType())
        configUserPreference.save(failOnError: true, flush: true)
        configUserPreference
    }

    /**
     * Mocking ConfigUserPreference domain.
     * @return ConfigUserPreference
     */
    private ConfigUserPreference getConfigUserPreference() {
        ConfigUserPreference configUserPreference = new ConfigUserPreference(
                pidm: pidm,
                configValue: 'USER_TEST_VALUE'
        )
        return configUserPreference
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: appName,
                appId: appId
        )
        return configApplication
    }

    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: 'CONFIG_TEST',
                configType: 'string',
                configValue: 'TEST_VALUE'
        )
        return configProperties
    }


    private Integer getPidmBySpridenId(def spridenId) {
        Sql sqlObj
        Integer pidmValue
        try{
            sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
            def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=$spridenId"
            pidmValue = sqlObj?.firstRow(query)?.pidm
        } catch(Exception e){
            e.printStackTrace()
        }
        return pidmValue
    }
}
