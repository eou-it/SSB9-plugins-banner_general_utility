/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class ConfigRolePageMappingIntegrationTests extends BaseIntegrationTestCase{
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
       ConfigRolePageMapping configRolePageMap= newConfigRolePageMap()
        configRolePageMap.save(failOnError: true, flush: true)
        assertNotNull configRolePageMap.id
    }

    @Test
    void testFindAll(){
        ConfigRolePageMapping configRolePageMap = newConfigRolePageMap()
        save configRolePageMap
        def list = configRolePageMap.findAll()
        assert (list.size() >= 0)
    }

    @Test
    void testUpdateConfigRolePageMapping() {
        ConfigRolePageMapping configRolePageMap = newConfigRolePageMap()
        save configRolePageMap
        assertNotNull configRolePageMap.id
        assertEquals 1, configRolePageMap.version
        assertEquals "Banner", configRolePageMap.dataOrigin
        //Update the entity
        configRolePageMap.version = 2
        configRolePageMap.roleCode="UUUUU"
        save configRolePageMap
        configRolePageMap = configRolePageMap.get( configRolePageMap.id )
        assertEquals "UUUUU", configRolePageMap.roleCode
    }

    @Test
    void testOptimisticLock() {
        def configRolePageMap = newConfigRolePageMap()
        save configRolePageMap

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update general.GURAPPR set GURAPPR_VERSION = 999 where GURAPPR_SURROGATE_ID = ?", [ configRolePageMap.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        configRolePageMap.roleCode="UUUUU"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            configRolePageMap.save(flush: true)
            }
    }

    @Test
    void testDeleteConfigApplication() {
        def configRolePageMap = newConfigRolePageMap()
        save configRolePageMap
        def id = configRolePageMap.id
        assertNotNull id
        configRolePageMap.delete()
        assertNull configRolePageMap.get( id )
    }

    private def newConfigRolePageMap() {
        def configRolePageMap= new ConfigRolePageMapping(
                lastModified: new Date(),
                dataOrigin: "Banner",
                lastModifiedBy: "TestUser",
                version:1,
                roleCode: '1'
        )
        ConfigApplication configApplication = newConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        ConfigControllerEndpointPage endpointPage = newConfigControllerEndPoint()
        endpointPage.setGubapplAppId(configApplication.appId)
        endpointPage.save(failOnError: true, flush: true)
        configRolePageMap.setGubapplAppId(configApplication.appId)
        configRolePageMap.setPageId(endpointPage.pageId)
        return configRolePageMap
    }

    private def newConfigApplication() {
        def configApplication= new ConfigApplication(
                lastModified: new Date(),
                appId: 1,
                appName: "PlatformSandbox",
                dataOrigin: "Banner",
                lastModifiedBy: 'TEST_USER',
                version:  1,
        )
        return configApplication
    }

    private ConfigControllerEndpointPage newConfigControllerEndPoint() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                lastModified: new Date(),
                description: 'TEST',
                displaySequence: 1,
                enableDisable: 'E',
                pageId: 1,
                pageName: 'TEST PAGE',
                lastModifiedBy: 'Test',
                version: 0
        )
        return configControllerEndpointPage
    }
}
