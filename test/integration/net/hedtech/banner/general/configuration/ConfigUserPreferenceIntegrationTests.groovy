/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigUserPreferenceIntegrationTests.
 */
class ConfigUserPreferenceIntegrationTests extends BaseIntegrationTestCase {

    def dataSource
    def sql
    def conn
    Integer pidm
    final private static def APP_NAME = Holders.grailsApplication.metadata['app.name']
    private def appName
    private def appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getSsbConnection()
        sql = new Sql(conn)
        pidm = getPidmBySpridenId("HOSH00001")
        appName = Holders.grailsApplication.metadata['app.name']
        appId = 'TESTAPP'
    }


    @After
    public void tearDown() {
        conn?.close()
        sql?.close()
        super.tearDown()
    }


    @Test
    public void testSaveConfigUserPreference() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version
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
        assert (list.getAt(0).configValue == 'TEST_VALUE')

        //Update
        configUserPreference.setConfigValue('NEW_TEST_VALUE')
        configUserPreference.save(failOnError: true, flush: true)
        list = configUserPreference.fetchAll()
        assert (list.size() >= 1)
        assert (list.getAt(0).configValue == 'NEW_TEST_VALUE')
    }


    @Test
    public void testfetchByValidPidm() {
        ConfigUserPreference configUserPreference = createConfigUserPreference()

        assertNotNull configUserPreference.id
        assertEquals 0L, configUserPreference.version

        List list = ConfigUserPreference.fetchByPidm(pidm)
        assertEquals 1, list.size()
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

    private ConfigUserPreference createConfigUserPreference() {
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
        configUserPreference
    }

    /**
     * Mocking ConfigUserPreference domain.
     * @return ConfigUserPreference
     */
    private ConfigUserPreference getConfigUserPreference() {
        ConfigUserPreference configUserPreference = new ConfigUserPreference(
                pidm: pidm,
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
                appName: appName,
                appId: appId
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

    private Integer getPidmBySpridenId(def spridenId) {
        def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=$spridenId"
        Integer pidmValue = sql?.firstRow(query)?.pidm
        return pidmValue
    }
}
