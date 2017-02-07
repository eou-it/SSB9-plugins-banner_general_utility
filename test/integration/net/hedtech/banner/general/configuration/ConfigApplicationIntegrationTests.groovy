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


class ConfigApplicationIntegrationTests extends BaseIntegrationTestCase{
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
    void testCreateConfigApplication() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)

        //Test if the generated entity now has an id assigned
        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version
    }


    @Test
    void testUpdateConfigApplication() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version
        assertEquals "PlatformSandbox", configApplication.appName
        assertEquals "Banner", configApplication.dataOrigin

        //Update the entity
        configApplication.appName = "PlatformSandbox 2"
        configApplication = configApplication.save(failOnError: true, flush: true)


        configApplication = configApplication.get( configApplication.id )
        assertEquals "PlatformSandbox 2", configApplication.appName
        }


    @Test
    void testOptimisticLock() {
        ConfigApplication configApplication = newConfigApplication()
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
        shouldFail( HibernateOptimisticLockingFailureException ) {
            configApplication.save(flush: true)
        }
    }


    @Test
    public void testSerialization() {
        try {
            ConfigApplication configApplication = newConfigApplication()
            configApplication = configApplication.save(failOnError: true, flush: true)
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(configApplication)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigApplication configApplication2
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                configApplication2 = (ConfigApplication)is.readObject()
                is.close()
            }
                assertEquals configApplication2.appId, configApplication.appId

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    void testDeleteConfigApplication() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id
        def id = configApplication.id
        configApplication.delete()
        assertNull configApplication.get( id )
    }


    @Test
    void testFetchAll(){
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id
        def configApplications = ConfigApplication.fetchAll()
        assertTrue configApplications.size() ==  (ConfigApplication.findAll().size())
    }


    @Test
    void testFetchByValidAppName(){
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        String appName = "PlatformSandbox"
        def configApplications = ConfigApplication.fetchAllByAppName(appName)
        assertTrue (configApplications.size() >= 1)

        configApplications.each { it ->
            assertTrue it.appName == appName
        }
    }


    @Test
    void testFetchByInValidAppName(){
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        String appName = "Invalid"
        def configApplications = ConfigApplication.fetchAllByAppName(appName)
        assertTrue (configApplications.size() == 0)
    }


    @Test
    void testFetchByNullAppName(){
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        String appName = null
        def configApplications = ConfigApplication.fetchAllByAppName(appName)
        assertNull configApplications
    }


    @Test
    void testToString() {
        ConfigApplication newConfigApplication = newConfigApplication()
        newConfigApplication.save(failOnError: true, flush: true)
        List <ConfigApplication> configApplications = ConfigApplication.fetchAll()
        assertFalse configApplications.isEmpty()
        configApplications.each { configApplication ->
            String configApplicationToString = configApplication.toString()
            assertNotNull configApplicationToString
            assertTrue configApplicationToString.contains('appName')
        }
    }


    @Test
    void testHashCode() {
        ConfigApplication newConfigApplication = newConfigApplication()
        newConfigApplication.save(failOnError: true, flush: true)
        List <ConfigApplication> configApplications = ConfigApplication.fetchAll()
        assertFalse configApplications.isEmpty()
        configApplications.each { configApplication ->
            Integer configApplicationHashCode = configApplication.hashCode()
            assertNotNull configApplicationHashCode
        }
    }


    private ConfigApplication newConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: "PlatformSandbox"
        )
        return configApplication
    }

}
