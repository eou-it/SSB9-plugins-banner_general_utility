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

class ConfigApplicationIntegrationTests extends BaseIntegrationTestCase{
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
    void testCreateConfigApplication() {
        def configApplication = newConfigApplication()
        save configApplication
        //Test if the generated entity now has an id assigned
        assertNotNull configApplication.id
    }

    @Test
    void testFindAll(){
        def configApplication = newConfigApplication()
        save configApplication
        def list = configApplication.findAll()
        assert (list.size() >= 0)
    }

    @Test
    void testUpdateConfigApplication() {
        def configApplication = newConfigApplication()
        save configApplication

        assertNotNull configApplication.id
        assertEquals 1, configApplication.version
        assertEquals "PlatformSandbox", configApplication.appName
        assertEquals "Banner", configApplication.dataOrigin
        //Update the entity
        configApplication.appName = "TTTTT"
        configApplication.dataOrigin = "UUUUU"
        save configApplication

        configApplication = configApplication.get( configApplication.id )
        assertEquals "TTTTT", configApplication.appName
        }

    @Test
    void testOptimisticLock() {
        def configApplication = newConfigApplication()
        save configApplication

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update general.GUBAPPL set GUBAPPL_VERSION = 999 where GUBAPPL_SURROGATE_ID = ?", [ configApplication.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        configApplication.appName="UUUUU"
        configApplication.dataOrigin= "Banner"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            configApplication.save(flush: true)
        }
    }

    @Test
    void testDeleteConfigApplication() {
        def configApplication = newConfigApplication()
        save configApplication
        def id = configApplication.id
        assertNotNull id
        configApplication.delete()
        assertNull configApplication.get( id )
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

}
