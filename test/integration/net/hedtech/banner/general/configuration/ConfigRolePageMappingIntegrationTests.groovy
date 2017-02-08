/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class ConfigRolePageMappingIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
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

    private static ConfigRolePageMapping newConfigRolePageMap() {
        ConfigRolePageMapping configRolePageMap= new ConfigRolePageMapping(
                roleCode: '1'
        )
        return configRolePageMap
    }

    private static ConfigApplication newConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: "PlatformSandbox",
        )
        return configApplication
    }

    private static ConfigControllerEndpointPage newConfigControllerEndPoint() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                description: 'TEST',
                displaySequence: 1,
                enableIndicator: true,
                pageId: 1,
                pageName: 'TEST PAGE'
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
        configRolePageMap.setPageId(endpointPage.pageId)
        configRolePageMap = configRolePageMap.save(failOnError: true, flush: true)
        configRolePageMap
    }
}
