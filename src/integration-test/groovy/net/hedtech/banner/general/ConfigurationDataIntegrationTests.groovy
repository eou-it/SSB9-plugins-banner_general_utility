/*******************************************************************************
Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
 
package net.hedtech.banner.general

import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import groovy.sql.Sql
import net.hedtech.banner.general.configuration.ConfigApplication
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import static groovy.test.GroovyAssert.shouldFail

@Integration
@Transactional
@Rollback
class ConfigurationDataIntegrationTests extends BaseIntegrationTestCase {

	@Autowired
	WebApplicationContext ctx

	@Before
    public void setUp() {
		formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
		creatConfigApplication()
	}

	@After
    public void tearDown() {
		super.tearDown()
		deleteConfigApplication()
	}

	@AfterClass
	public static void cleanUp() {
		RequestContextHolder.resetRequestAttributes()
	}

    @Test
	void testCreateConfigurationData() {
		def configurationData = newConfigurationData()
		save configurationData
		//Test if the generated entity now has an id assigned
        assertNotNull configurationData.id
	}

	@Test
	void testFetchByNameAndType(){
		def configurationData = newConfigurationData()
		save configurationData
		configurationData = ConfigurationData.fetchByNameAndType("TTTTT", "json","EXTZTEST")
		assertNotNull configurationData.id
	}

	@Test
	void testFetchTypes(){
		def configurationData = newConfigurationData()
		save configurationData
		configurationData = ConfigurationData.fetchByType("json","EXTZTEST")
		assertNotNull configurationData.id
	}

	@Test
	void testFetchNullTypes(){
		def configurationData = ConfigurationData.fetchByType(null,null)
		 assertTrue configurationData.size() ==0
	}

	@Test
	void testFetchByNameAndTypeNull(){
		def configurationData =  ConfigurationData.fetchByNameAndType(null,null,null)
		assertNull configurationData
	}

    @Test
	void testUpdateConfigurationData() {
		def configurationData = newConfigurationData()
		save configurationData

        assertNotNull configurationData.id
        assertEquals 0L, configurationData.version
        assertEquals "TTTTT", configurationData.name
        assertEquals "json", configurationData.type
        assertEquals "TTTTT", configurationData.value

		//Update the entity
		def testDate = new Date()
		configurationData.name = "UUUUU"
		configurationData.type = "json"
		configurationData.value = "UUUUU"
		configurationData.lastModified = testDate
		configurationData.lastModifiedBy = "test"
		configurationData.dataOrigin = "Banner"
        save configurationData

		configurationData = configurationData.get( configurationData.id )
        assertEquals 1L, configurationData?.version
        assertEquals "UUUUU", configurationData.name
        assertEquals "json", configurationData.type
        assertEquals "UUUUU", configurationData.value
	}

    @Test
    void testOptimisticLock() {
		def configurationData = newConfigurationData()
		save configurationData

        def sql

            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GUROCFG set GUROCFG_VERSION = 999 where GUROCFG_SURROGATE_ID = ?", [ configurationData.id ] )
		//Try to update the entity
		configurationData.name="UUUUU"
		configurationData.type="json"
		configurationData.value="UUUUU"
		configurationData.lastModified= new Date()
		configurationData.lastModifiedBy="test"
		configurationData.dataOrigin= "Banner"
        shouldFail( HibernateOptimisticLockingFailureException ) {
			configurationData.save( flush: true )
        }
    }

    @Test
	void testDeleteConfigurationData() {
		def configurationData = newConfigurationData()
		save configurationData
		def id = configurationData.id
		assertNotNull id
		configurationData.delete()
		assertNull configurationData.get( id )
	}

  private def newConfigurationData() {
	  ConfigApplication configApplication = creatConfigApplication()
	  println(configApplication.appId)
    def configurationData = new ConfigurationData(
    		name: "TTTTT",
    		type: "json",
    		value: "TTTTT",
    		version:  0.0,
            lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner",
			appId: configApplication.appId
        )
        return configurationData
    }

	def  creatConfigApplication() {
		ConfigApplication configApplication = ConfigApplication.find {appName=='EXTZTEST' && appId=='EXTZTEST' }
		if(!configApplication){
		     configApplication = new ConfigApplication(
				lastModified: new Date(),
				appName: 'EXTZTEST',
				appId: 'EXTZTEST'
				)
			configApplication = save configApplication
		}

		return configApplication
	}

	def deleteConfigApplication(){
		ConfigApplication configApplication = ConfigApplication.find {appName=='EXTZTEST' && appId=='EXTZTEST' }
		if(configApplication){
			configApplication.delete( failOnError:true, flush: true )
		}

	}

}
