
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
 
package net.hedtech.banner.general.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase 
import net.hedtech.banner.general.utility.MenuAndToolbarPreference

import grails.test.GrailsUnitTestCase
import groovy.sql.Sql 
import org.hibernate.annotations.OptimisticLock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException


class MenuAndToolbarPreferenceIntegrationTests extends BaseIntegrationTestCase {

	def MenuAndToolbarPreferenceService
	
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
	void testCreateMenuAndToolbarPreference() {
		def MenuAndToolbarPreference = newMenuAndToolbarPreference()
		save MenuAndToolbarPreference
		//Test if the generated entity now has an id assigned		
        assertNotNull MenuAndToolbarPreference.id
	}

    @Test
	void testUpdateMenuAndToolbarPreference() {
		def MenuAndToolbarPreference = newMenuAndToolbarPreference()
		save MenuAndToolbarPreference
       
        assertNotNull MenuAndToolbarPreference.id
        assertEquals 0L, MenuAndToolbarPreference.version
      //  assertEquals "", MenuAndToolbarPreference.tlbBtn
        assertEquals "T", MenuAndToolbarPreference.displayHtCb
        assertEquals "T", MenuAndToolbarPreference.displayVtCb
        assertEquals "T", MenuAndToolbarPreference.displayHint
        assertEquals "T", MenuAndToolbarPreference.formnameCb
        assertEquals "T", MenuAndToolbarPreference.releaseCb
        assertEquals "T", MenuAndToolbarPreference.dbaseInstitutionCb
        assertEquals "T", MenuAndToolbarPreference.dateTimeCb
        assertEquals "T", MenuAndToolbarPreference.requiredItemCb
        assertEquals "Y", MenuAndToolbarPreference.formnameDisplayIndicator
        
		//Update the entity
		def testDate = new Date()
		MenuAndToolbarPreference.tlbBtn = ""
		MenuAndToolbarPreference.displayHtCb = "U"
		MenuAndToolbarPreference.displayVtCb = "U"
		MenuAndToolbarPreference.displayHint = "U"
		MenuAndToolbarPreference.formnameCb = "U"
		MenuAndToolbarPreference.releaseCb = "U"
		MenuAndToolbarPreference.dbaseInstitutionCb = "U"
		MenuAndToolbarPreference.dateTimeCb = "U"
		MenuAndToolbarPreference.requiredItemCb = "U"
		MenuAndToolbarPreference.linescrnXPosition = 0
		MenuAndToolbarPreference.linebtnXPosition = 0
		MenuAndToolbarPreference.formnameDisplayIndicator = "N"
		MenuAndToolbarPreference.lastModified = testDate
		MenuAndToolbarPreference.lastModifiedBy = "test"
		MenuAndToolbarPreference.dataOrigin = "Banner"
        save MenuAndToolbarPreference
        
        MenuAndToolbarPreference = MenuAndToolbarPreference.get( MenuAndToolbarPreference.id )
        assertEquals 1L, MenuAndToolbarPreference?.version
        assertEquals "", MenuAndToolbarPreference.tlbBtn
        assertEquals "U", MenuAndToolbarPreference.displayHtCb
        assertEquals "U", MenuAndToolbarPreference.displayVtCb
        assertEquals "U", MenuAndToolbarPreference.displayHint
        assertEquals "U", MenuAndToolbarPreference.formnameCb
        assertEquals "U", MenuAndToolbarPreference.releaseCb
        assertEquals "U", MenuAndToolbarPreference.dbaseInstitutionCb
        assertEquals "U", MenuAndToolbarPreference.dateTimeCb
        assertEquals "U", MenuAndToolbarPreference.requiredItemCb
        assertEquals 0, MenuAndToolbarPreference.linescrnXPosition
        assertEquals 0, MenuAndToolbarPreference.linebtnXPosition
        assertEquals "N", MenuAndToolbarPreference.formnameDisplayIndicator
	}

    @Test
    void testOptimisticLock() { 
		def MenuAndToolbarPreference = newMenuAndToolbarPreference()
		save MenuAndToolbarPreference
        
        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GURTPRF set GURTPRF_VERSION = 999 where GURTPRF_SURROGATE_ID = ?", [ MenuAndToolbarPreference.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		MenuAndToolbarPreference.tlbBtn=""
		MenuAndToolbarPreference.displayHtCb="U"
		MenuAndToolbarPreference.displayVtCb="U"
		MenuAndToolbarPreference.displayHint="U"
		MenuAndToolbarPreference.formnameCb="U"
		MenuAndToolbarPreference.releaseCb="U"
		MenuAndToolbarPreference.dbaseInstitutionCb="U"
		MenuAndToolbarPreference.dateTimeCb="U"
		MenuAndToolbarPreference.requiredItemCb="U"
		MenuAndToolbarPreference.linescrnXPosition= 0
		MenuAndToolbarPreference.linebtnXPosition= 0
		MenuAndToolbarPreference.formnameDisplayIndicator= "Y"
		MenuAndToolbarPreference.lastModified= new Date()
		MenuAndToolbarPreference.lastModifiedBy="test"
		MenuAndToolbarPreference.dataOrigin= "Banner"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            MenuAndToolbarPreference.save( flush: true )
        }
    }

    @Test
	void testDeleteMenuAndToolbarPreference() {
		def MenuAndToolbarPreference = newMenuAndToolbarPreference()
		save MenuAndToolbarPreference
		def id = MenuAndToolbarPreference.id
		assertNotNull id
		MenuAndToolbarPreference.delete()
		assertNull MenuAndToolbarPreference.get( id )
	}

    @Test
    void testValidation() {
       def MenuAndToolbarPreference = newMenuAndToolbarPreference()
       assertTrue "MenuAndToolbarPreference could not be validated as expected due to ${MenuAndToolbarPreference.errors}", MenuAndToolbarPreference.validate()
    }

    @Test
    void testNullValidationFailure() {
        def MenuAndToolbarPreference = new MenuAndToolbarPreference()
//        assertFalse "MenuAndToolbarPreference should have failed validation", MenuAndToolbarPreference.validate()
        assertNoErrorsFor MenuAndToolbarPreference,
        									   [ 
             									 'tlbBtn', 
             									 'displayHtCb', 
             									 'displayVtCb', 
             									 'displayHint', 
             									 'formnameCb', 
             									 'releaseCb', 
             									 'dbaseInstitutionCb', 
             									 'dateTimeCb', 
             									 'requiredItemCb', 
             									 'linescrnXPosition', 
             									 'linebtnXPosition', 
             									 'formnameDisplayIndicator'                                                 
											   ]
    }

    @Test
    void testMaxSizeValidationFailures() {
        def MenuAndToolbarPreference = new MenuAndToolbarPreference(
        tlbBtn:'XX',
        displayHtCb:'XXX',
        displayVtCb:'XXX',
        displayHint:'XXX',
        formnameCb:'XXX',
        releaseCb:'XXX',
        dbaseInstitutionCb:'XXX',
        dateTimeCb:'XXX',
        requiredItemCb:'XXX' )
		assertFalse "MenuAndToolbarPreference should have failed validation", MenuAndToolbarPreference.validate()
		assertErrorsFor MenuAndToolbarPreference, 'maxSize', [ 'tlbBtn', 'displayHtCb', 'displayVtCb', 'displayHint', 'formnameCb', 'releaseCb', 'dbaseInstitutionCb', 'dateTimeCb', 'requiredItemCb' ]
    }

    @Test
	void testValidationMessages() {
	    def MenuAndToolbarPreference = newMenuAndToolbarPreference()
	}
  
    
  private def newMenuAndToolbarPreference() {
    def MenuAndToolbarPreference = new MenuAndToolbarPreference(
    		tlbBtn: "", 
    		displayHtCb: "T", 
    		displayVtCb: "T", 
    		displayHint: "T", 
    		formnameCb: "T", 
    		releaseCb: "T", 
    		dbaseInstitutionCb: "T", 
    		dateTimeCb: "T", 
    		requiredItemCb: "T", 
    		linescrnXPosition: 1, 
    		linebtnXPosition: 1, 
    		formnameDisplayIndicator:"Y",
            lastModified: new Date(),
			lastModifiedBy: "test", 
			dataOrigin: "Banner"
        )
        return MenuAndToolbarPreference
    }

   /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(MenuAndToolbarPreference_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}
