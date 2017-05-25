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
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class ConfigRolePageMappingIntegrationTests extends BaseIntegrationTestCase {

    private def appName
    private def appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
        appName = Holders.grailsApplication.metadata['app.name']
        appId = 'TESTAPP'
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateConfigRolePageMapping() {
        ConfigRolePageMapping configRolePageMap = saveConfigRolePageMapping()
        assertNotNull configRolePageMap.id
        assertEquals 0L, configRolePageMap.version
    }

    @Test
    void testfetchAll() {
        ConfigRolePageMapping configRolePageMap = saveConfigRolePageMapping()

        def list = configRolePageMap.fetchAll()
        assert (list.size() >= 0)
    }

    @Test
    void testUpdateConfigRolePageMapping() {
        ConfigRolePageMapping configRolePageMap = saveConfigRolePageMapping()

        assertNotNull configRolePageMap.id
        assertEquals 0L, configRolePageMap.version
        assertEquals "Banner", configRolePageMap.dataOrigin

        //Update the entity
        configRolePageMap.roleCode = "UUUUU"
        configRolePageMap = configRolePageMap.save(failOnError: true, flush: true)

        assertEquals "UUUUU", configRolePageMap.roleCode
    }

    @Test
    void testOptimisticLock() {
        ConfigRolePageMapping configRolePageMap = saveConfigRolePageMapping()

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update general.GURAPPR set GURAPPR_VERSION = 999 where GURAPPR_SURROGATE_ID = ?", [configRolePageMap.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        configRolePageMap.roleCode = "UUUUU"
        shouldFail(HibernateOptimisticLockingFailureException) {
            configRolePageMap.save(flush: true)
        }
    }

    @Test
    void testDeleteConfigApplication() {
        ConfigRolePageMapping configRolePageMap = saveConfigRolePageMapping()

        def id = configRolePageMap.id
        assertNotNull id

        configRolePageMap.delete()
        assertNull configRolePageMap.get(id)
    }


    @Test
    void testToString() {
        ConfigRolePageMapping newConfigRolePageMap = saveConfigRolePageMapping()
        assertNotNull newConfigRolePageMap.id
        assertEquals 0L, newConfigRolePageMap.version

        def configRolePageMaps = ConfigRolePageMapping.fetchAll()
        assertFalse configRolePageMaps.isEmpty()
        configRolePageMaps.each { configRolePageMap ->
            String configRolePageMapToString = configRolePageMap.toString()
            assertNotNull configRolePageMapToString
            assertTrue configRolePageMapToString.contains('pageId')
        }
    }


    @Test
    void testHashCode() {
        ConfigRolePageMapping newConfigRolePageMap = saveConfigRolePageMapping()
        assertNotNull newConfigRolePageMap.id
        assertEquals 0L, newConfigRolePageMap.version

        def configRolePageMaps = ConfigRolePageMapping.fetchAll()
        assertFalse configRolePageMaps.isEmpty()
        configRolePageMaps.each { configRolePageMap ->
            Integer configRolePageMapHashCode = configRolePageMap.hashCode()
            assertNotNull configRolePageMapHashCode
        }
    }


    @Test
    public void testSerialization() {
        try {
            ConfigRolePageMapping newConfigRolePageMap = saveConfigRolePageMapping()
            assertNotNull newConfigRolePageMap.id
            assertEquals 0L, newConfigRolePageMap.version

            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(newConfigRolePageMap)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigRolePageMapping configRolePageMappingCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                configRolePageMappingCopy = (ConfigRolePageMapping) is.readObject()
                is.close()
            }
            assertEquals newConfigRolePageMap, configRolePageMappingCopy

        } catch (e) {
            e.printStackTrace()
        }
    }

    private ConfigRolePageMapping newConfigRolePageMap() {
        ConfigRolePageMapping configRolePageMap = new ConfigRolePageMapping(
                roleCode: '1'
        )
        return configRolePageMap
    }

    private ConfigApplication newConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: appName,
                appId: appId
        )
        return configApplication
    }

    private ConfigControllerEndpointPage newConfigControllerEndPoint() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                description: 'TEST',
                displaySequence: 1,
                statusIndicator: true,
                pageId: 'ConfigurationEndPoint',
                pageUrl: 'TEST PAGE'
        )
        return configControllerEndpointPage
    }

    private ConfigRolePageMapping saveConfigRolePageMapping() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version
        configApplication = configApplication.refresh()

        ConfigControllerEndpointPage endpointPage = newConfigControllerEndPoint()
        endpointPage.setConfigApplication(configApplication)
        endpointPage = endpointPage.save(failOnError: true, flush: true)
        endpointPage.refresh()
        assertNotNull endpointPage.id
        assertEquals 0L, endpointPage.version



        ConfigRolePageMapping configRolePageMap = newConfigRolePageMap()
        configRolePageMap.setConfigApplication(endpointPage.configApplication)
        configRolePageMap.setEndpointPage(endpointPage)
        configRolePageMap = configRolePageMap.save(failOnError: true, flush: true)
        configRolePageMap
    }
}
