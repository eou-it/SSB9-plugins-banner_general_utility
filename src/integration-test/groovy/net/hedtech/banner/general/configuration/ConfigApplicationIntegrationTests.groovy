/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

@Integration
@Rollback
class ConfigApplicationIntegrationTests extends BaseIntegrationTestCase {

    private String appName
    private String appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        appName = "TESTAPPNAME"
        appId = 'TESTAPPID'
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @AfterClass
    public static void cleanUp() {
        RequestContextHolder.resetRequestAttributes()
    }
    @Test
    void testCreateConfigApplication() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)

        //Test if the generated entity now has an id assigned
        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version
        configApplication = configApplication.refresh()
        assertNotNull configApplication.appId
    }


    @Test
    void testUpdateConfigApplication() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()

        assertNotNull configApplication.id
        assertEquals 0L, configApplication.version
        assertEquals appName, configApplication.appName
        assertEquals "Banner", configApplication.dataOrigin

        //Update the entity
        configApplication.appName = "PlatformSandbox 2"
        configApplication = configApplication.save(failOnError: true, flush: true)


        configApplication = configApplication.get(configApplication.id)
        assertEquals "PlatformSandbox 2", configApplication.appName
    }


    @Test
    void testOptimisticLock() {
        ConfigApplication configApplication = newConfigApplication()
        save configApplication

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update general.GUBAPPL set GUBAPPL_VERSION = 999 where GUBAPPL_SURROGATE_ID = ?", [configApplication.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        configApplication.appName = "UUUUU"
        shouldFail(HibernateOptimisticLockingFailureException) {
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
                configApplication2 = (ConfigApplication) is.readObject()
                is.close()
            }
            assertEquals configApplication2, configApplication

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
        assertNull configApplication.get(id)
    }

    @Test
    void testFetchByValidAppName() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        ConfigApplication configApp = ConfigApplication.fetchByAppName(appName)
        assertNotNull configApp

        assertTrue configApp.appName == appName
    }


    @Test
    void testFetchByInValidAppName() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        String appName = "Invalid"
        def configApp = ConfigApplication.fetchByAppName(appName)
        assertNull(configApp)
    }


    @Test
    void testFetchByValidAppId() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        ConfigApplication configApp = ConfigApplication.fetchByAppId(appId)
        assertNotNull configApp

        assertTrue configApp.appName == appName
    }

    @Test
    void testEqualsApp() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId")
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName1",
                appId: "TestId1")
        assertFalse configApplication2==configApplication1
    }

    @Test
    void testEqualsLastModifiedEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12))
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName",
                appId: "TestId1",lastModified: new Date(12,2,14))
        assertFalse configApplication2==configApplication1
    }

    @Test
    void testEqualsLastModifiedNotEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12))
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName",
                appId: "TestId1",lastModified: new Date(12,2,12))
        assertFalse configApplication2==configApplication1
    }

    @Test
    void testEqualsLastModifiedAppNameNotEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12))
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName1",
                appId: "TestId",lastModified: new Date(12,2,12))
        assertFalse configApplication2==configApplication1
    }

    @Test
    void testEqualsDataOriginNotEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "GENERAL")
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER")
        assertFalse configApplication2==configApplication1
    }


    @Test
    void testEqualsAppIdNotEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER",id: 1234)
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER",id:12345)
        assertFalse configApplication2==configApplication1
    }


    @Test
    void testEqualsLastModifiedByNotEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER",id: 1234,lastModifiedBy: "TestUser")
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER",id:1234,lastModifiedBy: "GRAILS")
        assertFalse configApplication2==configApplication1
    }


    @Test
    void testEqualsVersionNotEqual() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER",id: 1234,lastModifiedBy: "GRAILS",version: 1)
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER",id:1234,lastModifiedBy: "GRAILS",version: 2)
        assertFalse configApplication2==configApplication1
    }


    @Test
    void testFetchByValidAppObject() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",
                appId: "TestId",lastModified: new Date(12,2,15))
        configApplication1 = configApplication1.save(failOnError: true, flush: true)
        assertTrue configApplication1==configApplication1
    }


    @Test
    void testEqualsIs() {
        ConfigApplication configApplication1 = new ConfigApplication()
        assertTrue configApplication1.equals(configApplication1)
    }


    @Test
    void testEqualsClass() {
        ConfigApplication configApplication1 = new ConfigApplication()
        ConfigProperties configProperties=new ConfigProperties()
        assertFalse configApplication1.equals(configProperties)
    }


    @Test
    void testFetchByNullAppId() {
        assertNull ConfigApplication.fetchByAppId(null)
    }

    @Test
    void testFetchByInValidAppId() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        String appId = "XXXXXXXXXXX"
        ConfigApplication configApp = ConfigApplication.fetchByAppId(appId)
        assertNull(configApp)
    }


    @Test
    void testFetchByNullAppName() {
        ConfigApplication configApplication = newConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        assertNotNull configApplication.id

        String appName = null
        def configApp = ConfigApplication.fetchByAppName(appName)
        assertNull configApp
    }


    @Test
    void testToString() {
        ConfigApplication newConfigApplication = newConfigApplication()
        newConfigApplication.save(failOnError: true, flush: true)

        ConfigApplication configApplication = ConfigApplication.fetchByAppName(appName)
        assertNotNull configApplication

        String configApplicationToString = configApplication.toString()
        assertNotNull configApplicationToString
        assertTrue configApplicationToString.contains('appName')
    }


    @Test
    void testHashCode() {
        ConfigApplication newConfigApplication = newConfigApplication()
        newConfigApplication.save(failOnError: true, flush: true)

        ConfigApplication configApp = ConfigApplication.fetchByAppName(appName)
        assertNotNull configApp

        Integer configApplicationHashCode = configApp.hashCode()
        assertNotNull configApplicationHashCode
    }


    private ConfigApplication newConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: appName,
                appId: appId
        )
        return configApplication
    }

}
