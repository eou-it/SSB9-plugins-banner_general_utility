/*******************************************************************************
 Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.supplemental

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.test.ZipTest
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.log4j.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.text.ParseException

/**
 * Integration tests of the supplemental data SSB service.
 */
class SupplementalDataSSBServiceIntegrationTests extends BaseIntegrationTestCase {
    def supplementalDataSSBService        // injected by Spring
    private static final def log = Logger.getLogger(getClass())

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        updateGorsdamTableValidation()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    /**
     * Tests when the block is SDE enabled
     * */
    @Test
    void testIsSde() {
        def isSde = supplementalDataSSBService.hasSde("zipTestBlock")
        assertTrue isSde

        def isSde1 = supplementalDataSSBService.hasSde("fooBlock")
        assertFalse isSde1

        def isSde2 = supplementalDataSSBService.hasSde("zip")
        assertFalse isSde2

        def isSde3 = supplementalDataSSBService.hasSde("studentBlock")
        assertFalse isSde3

        def isSde4 = supplementalDataSSBService.hasSde(null)
        assertFalse isSde4

        def hasSdeForTableName = supplementalDataSSBService.hasSdeForTable(null)
        assertFalse hasSdeForTableName
    }

    /**
     * Tests PL/SQL component integration.
     * */

    @Test
    void testSdeLoad() {

            def tableName = 'GTVZIPC'
        def sdeModel = ZipTest.findByCodeAndCity("02186", "Milton")
        def id = sdeModel.id

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""declare
				         l_pkey GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
				         l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${tableName},${id});
				     begin
				         l_pkey := gp_goksdif.f_get_pk(${tableName},l_rowid);
				         gp_goksdif.p_set_current_pk(l_pkey);
				     end;
                  """)

        def session = sessionFactory.getCurrentSession()
        def resultSet = session.createSQLQuery("SELECT govsdav_attr_name, govsdav_value_as_char FROM govsdav WHERE govsdav_table_name= :tableName").setString("tableName", tableName).list()
        assertNotNull resultSet

        def returnList = []
        resultSet.each() {
            returnList.add([attributeName: "${it[0]}", value: "${it[1]}"])
        }

        assertTrue returnList.size() > 0
    }

    /**
     * Tests if there is any SDE data for the model
     * */

    @Test
    void testSSBSdeData() {

        def modelWithSdeData = ZipTest.findByCodeAndCity("02186", "Milton")
        assertFalse supplementalDataSSBService.hasSdeData(modelWithSdeData)

        def modelWithNoSdeData = ZipTest.findByCode("02186")
        assertFalse supplementalDataSSBService.hasSdeData(modelWithNoSdeData)
       }

    /**
     * Tests loading the entity with SDE defined. (SDE data is not empty).
     * */

    @Test
    void testLoadNotEmptySSBSdeData() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assert model.extensions.size() > 0

        assertNull model.extensions[0].value

        assertEquals "enter a numbere", model.extensions[0].prompt
        assertEquals "NUMBER", model.extensions[0].datatype
        assertEquals "with 2 decimal points", model.extensions[0].attrInfo

        assertEquals 5, model.extensions[0].size()
    }

    /**
     * Tests loading the entity with SDE defined. (no SDE data)
     * */

    @Test
    void testLoadEmptySSBSdeData() {

        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertNull model.extensions[0].value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Update SDE data for all attributes
     * */

    @Test
    void testSaveNotEmptySdeDataForSSB() {

        def model = ZipTest.findByCodeAndCity("00001", "newcity")

        //load sde
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertNotNull  model.extensions
        assertEquals  "number" , model.extensions[0].name.toString()
        assertNull  "value" ,model.extensions[0].value
        assertEquals  "NUMBER" ,model.extensions[0].datatype


        //update the field
        model.extensions[0].value = 1
        //save the field
        supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
        //load sde again
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertNotNull  model.extensions
        assertEquals  "number" , model.extensions[0].name.toString()
        assertEquals   1 ,model.extensions[0].value
        assertEquals  "NUMBER" ,model.extensions[0].datatype

    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     * */
    @Test
    void testSaveDeleteNotEmptySdeDataForSSB() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertEquals "enter a numbere", model.extensions[0].prompt
        assertEquals "NUMBER", model.extensions[0].datatype
        assertEquals "with 2 decimal points", model.extensions[0].attrInfo

        assertEquals 5, model.extensions[0].size()

        model.extensions[0].prompt = null
        supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)
        assertEquals 5, model.extensions[0].size()

    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. No SDE data
     * 2. Add SDE data to these attributes
     * */
    @Test
    void testLoadAndCreateEmptySdeDataForSSB() {


        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertNull "value", model.extensions[0].value

        model.extensions[0].value = 1

        supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertEquals 1, model.extensions[0].value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes with wrong Number format
     * */
    @Test
    void testNumericValidationSdeDataforSSB() {


        def zip = new ZipTest(code: "BB", city: "BB")
        zip.save()

        try {

            def model = ZipTest.findByCodeAndCity("BB", "BB")
            supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

            model.extensions[0].value = "My Comments"

            supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
            fail("Should have received an error: Invalid Number")
        }
        catch (ApplicationException e) {
            assertEquals "Invalid Number", e.wrappedException.message
        }
        catch (Exception e) {
            assertEquals "Invalid Number", e.message
        }
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes with wrong Date format
     * */
    @Test
    void testDateValidationSdeDataforSSB() {

        def zip = new ZipTest(code: "BB", city: "BB")

        try {
            zip.save()
            def model = ZipTest.findByCodeAndCity("BB", "BB")
            supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

            model.extensions[0].datatype = "DATE"
            model.extensions[0].value = "15-Apr2010"

            supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
            fail("Should have received an error: Invalid Date")
        }
        catch (Exception e) {
            assert e.undeclaredThrowable instanceof ParseException
        }
    }

    /**
     * Tests User Defined SDE Attributes.
     * */
    @Test
    void testLoadSdeDataForSSB() {

        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertNotNull model.extensions[0]
        assertNotNull model.extensions[0].prompt
        assertNull model.extensions[0].value

        // adds new values for user-defined attributes
        model.extensions[0].value = 1

        supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertEquals 1, model.extensions[0].value

        // deletes values for user-defined attributes
        model.extensions[0].value = null


        supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        assertNull  model.extensions[0].value
    }


    /**
     * Tests Validation.
     * */
    @Test
    void testValidationSDEForSSB() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        supplementalDataSSBService.getModelExtensionData('GTVZIPC', model.id, model)

        model.extensions[0].value = "my comments"

        try {
            supplementalDataSSBService.saveSdeFromUiRequest('GTVZIPC', model)
            fail "This should have failed"
        }
        catch (ApplicationException ae) {
            if (ae.wrappedException =~ /\*Error\* Invalid Number. Expected format: 999D99/)
                log.debug("Found correct message code *Error* Invalid Number. Expected format: 999D99")
            else
                fail("Did not find expected error code *Error* Invalid Number. Expected format: 999D99, sdeModel: ${ae.wrappedException}")
        }
        catch (AssertionError ae) {
            assertEquals "This should have failed", ae.message
        }
        catch (Exception e) {
            assertEquals "Invalid Number", e.message
        }
    }

    /**
     * Tests Mapped Domain for LOV.
     * */
    @Test
    void testFindMappedDomain() {
        def mappedDomain = supplementalDataSSBService.getMappedDomain("GURSESS")

        assertEquals "net.hedtech.banner.session.BannerUserSession", mappedDomain

    }

    @Test
    void testGetModelExtensionForSSB(){
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def resultList = supplementalDataSSBService.getModelExtension('GTVZIPC', model.id)
        assertTrue resultList.size() > 0
    }

    @Test
    void testFindByLov(){
        def resultList = supplementalDataSSBService.findByLov(null,null)
    }

    private def updateGorsdamTableValidation() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
            UPDATE GORSDAM
              SET GORSDAM_ATTR_DATA_LEN = 6,
                  GORSDAM_ATTR_DATA_SCALE = 2
            WHERE GORSDAM_TABLE_NAME = 'GTVZIPC'
              AND GORSDAM_ATTR_NAME = 'NUMBER'
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }
}
