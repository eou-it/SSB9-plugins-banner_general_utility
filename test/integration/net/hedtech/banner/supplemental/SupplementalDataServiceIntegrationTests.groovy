/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
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
 * Integration tests of the supplemental data service.
 */
class SupplementalDataServiceIntegrationTests extends BaseIntegrationTestCase {
    def supplementalDataService        // injected by Spring
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
        def isSde = supplementalDataService.hasSde("zipTestBlock")
        assertTrue isSde

        def isSde1 = supplementalDataService.hasSde("fooBlock")
        assertFalse isSde1

        def isSde2 = supplementalDataService.hasSde("zip")
        assertFalse isSde2

        def isSde3 = supplementalDataService.hasSde("studentBlock")
        assertFalse isSde3
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
    void testSdeData() {

        def modelWithSdeData = ZipTest.findByCodeAndCity("02186", "Milton")
        assertFalse supplementalDataService.hasSdeData(modelWithSdeData)

        def modelWithNoSdeData = ZipTest.findByCode("02186")
        assertFalse supplementalDataService.hasSdeData(modelWithNoSdeData)
    }

    /**
     * Tests loading the entity with SDE defined. (SDE data is not empty).
     * */

    @Test
    void testLoadNotEmptySdeData() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        assertNull sdeModel.USERDEFINED."name".value

        assertEquals "User Defined name", sdeModel.USERDEFINED.name.prompt

        assertEquals "VARCHAR2", sdeModel.USERDEFINED."name".dataType
        assertEquals "M", sdeModel.USERDEFINED."name".discType
        assertEquals 1, sdeModel.USERDEFINED."name".validation
        assertEquals 4, sdeModel.USERDEFINED."name".attrOrder, 0

        assertNull sdeModel.LANGUAGE."ENG".value
        assertEquals "Language", sdeModel.LANGUAGE."ENG".prompt

        assertEquals "VARCHAR2", sdeModel.LANGUAGE."ENG".dataType
        assertEquals "M", sdeModel.LANGUAGE."ENG".discType
        assertEquals 1, sdeModel.LANGUAGE."ENG".validation
        assertEquals 5, sdeModel.LANGUAGE."ENG".attrOrder, 0

        assertEquals 5, sdeModel.size()
        assertTrue 'USERDEFINED' in sdeModel
        assertTrue 'LANGUAGE' in sdeModel

        assertEquals 2, sdeModel."USERDEFINED".size()
        assertTrue sdeModel."LANGUAGE".size() > 0

    }

    /**
     * Tests loading the entity with SDE defined. (no SDE data)
     * */

    @Test
    void testLoadEmptySdeData() {

        def sdeModel = supplementalDataService.loadSupplementalDataForModel(ZipTest.findByCodeAndCity("02186", "Milton"))

        assertNull sdeModel.USERDEFINED."name".value
        assertNull sdeModel.LANGUAGE."ENG".value
        //assertNull sdeModel.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Update SDE data for all attributes
     * */

    @Test
    void testSaveNotEmptySdeData() {

        def model = ZipTest.findByCodeAndCity("02186", "Milton");
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        assert sdeModel.size() > 0
        assertEquals "User Defined name", sdeModel.USERDEFINED."name".prompt
        assertEquals "Language", sdeModel.LANGUAGE."ENG".prompt

        sdeModel.USERDEFINED."name".prompt = "User Defined name"
        sdeModel.LANGUAGE."ENG".prompt = "Language"

        supplementalDataService.persistSupplementalDataFor(model, sdeModel)

        supplementalDataService.loadSupplementalDataForModel(model)
        assertEquals "User Defined name", sdeModel.USERDEFINED."name".prompt
        assertEquals "Language", sdeModel.LANGUAGE."ENG".prompt
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     * */
    @Test
    void testSaveDeleteNotEmptySdeData() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "User Defined name", sdeModel.USERDEFINED."name".prompt
        assertEquals "Language", sdeModel.LANGUAGE."ENG".prompt
        assertEquals 5, sdeModel.size()

        sdeModel.USERDEFINED."name".prompt = null
        supplementalDataService.persistSupplementalDataFor(model, sdeModel)
        def sdeModelDeleted = supplementalDataService.loadSupplementalDataForModel(model)
        assertEquals 5, sdeModelDeleted.size()

    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     */
    @Test
    void testSaveDeleteNotEmptySdeDataInTheMiddle() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton");
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        /*  assertEquals "comment 2", sdeModel.COMMENTS."2".value   // in the middle
          assertEquals "comment 1", sdeModel.TEST."1".value*/
        assertNull sdeModel.USERDEFINED."name".value

        sdeModel.USERDEFINED."name".prompt = null
        supplementalDataService.persistSupplementalDataFor(model, sdeModel)
        def sdeModelDeleted = supplementalDataService.loadSupplementalDataForModel(model)
        assertEquals 2, sdeModelDeleted.USERDEFINED.size()

        assertNull sdeModelDeleted.USERDEFINED."name".value   // rebuilt discriminator
        assertNotNull sdeModelDeleted.USERDEFINED."name".prompt

        /*assertNull sdeModelDeleted.USERDEFINED."3"*/
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. No SDE data
     * 2. Add SDE data to these attributes
     * */
    @Test
    void testLoadAndCreateEmptySdeData() {


        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        assertNull sdeModel.USERDEFINED."name".value
        assertNull sdeModel.LANGUAGE."ENG".value

        sdeModel.USERDEFINED."name".value = "USER DEFINED"
        sdeModel.LANGUAGE."ENG".value = "LANGUAGE ONE"

        supplementalDataService.persistSupplementalDataFor(model, sdeModel)


        def sdeModelUpdated = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "USER DEFINED", sdeModelUpdated.USERDEFINED."name".value
        assertEquals "LANGUAGE ONE", sdeModelUpdated.LANGUAGE."ENG".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes with wrong Number format
     * */
    @Test
    void testNumericValidationSdeData() {


        def zip = new ZipTest(code: "BB", city: "BB")
        zip.save()

        try {

            def model = ZipTest.findByCodeAndCity("BB", "BB")
            def zipFound = supplementalDataService.loadSupplementalDataForModel(model)
            zipFound."NUMBER"."1".value = "my comments"

            supplementalDataService.persistSupplementalDataFor(model, zipFound)
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
    void testDateValidationSdeData() {

        def zip = new ZipTest(code: "BB", city: "BB")

        try {
            zip.save()
            def model = ZipTest.findByCodeAndCity("BB", "BB")
            def zipFound = supplementalDataService.loadSupplementalDataForModel(model)


            zipFound.dataOrigin = "foo"
            zipFound.USERDEFINED."name".value = "my comments"

            zipFound.LANGUAGE."ENG".dataType = "DATE" // forced Date
            zipFound.LANGUAGE."ENG".value = "15-Apr2010" // wrong format

            supplementalDataService.persistSupplementalDataFor(model, zipFound)
            fail("Should have received an error: Invalid Date")
        }
        catch (Exception e) {
            assertEquals "Invalid Date", e.message
        }
    }

    /**
     * Tests User Defined SDE Attributes.
     * */
    @Test
    void testLoadUseDefinedSdeData() {

        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        assertNotNull sdeModel.USERDEFINED
        assertNotNull sdeModel.USERDEFINED."name".prompt
        assertNotNull sdeModel.USERDEFINED."name".prompt
        assertNull sdeModel.USERDEFINED."name".value

        // adds new values for user-defined attributes
        sdeModel.USERDEFINED."name".value = "my name 12"

        supplementalDataService.persistSupplementalDataFor(model, sdeModel)

        def updatedSde = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModelUpdated = supplementalDataService.loadSupplementalDataForModel(updatedSde)

        assertEquals "my name 12", sdeModelUpdated.USERDEFINED."name".value

        // deletes values for user-defined attributes
        sdeModelUpdated.USERDEFINED."name".value = null
        sdeModelUpdated.USERDEFINED."name".value = null


        supplementalDataService.persistSupplementalDataFor(model, sdeModelUpdated)

        def deletedSdeModel = ZipTest.findByCodeAndCity("02186", "Milton")

        def deletedSde = supplementalDataService.loadSupplementalDataForModel(deletedSdeModel)

        assertNull deletedSde.USERDEFINED."name".value
    }

    /**
     * Tests SQL Based SDE attributes.
     * */
    @Test
    void testLoadSQLBasedAttributeSdeData() {

        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        assertNotNull sdeModel.LANGUAGE
        assertEquals "Language", sdeModel.LANGUAGE."ENG".prompt
        //assertEquals "Language", sdeModel.LANGUAGE."RUS".prompt
        //assertEquals "Language", sdeModel.LANGUAGE."GRM".prompt


        sdeModel.LANGUAGE."ENG".value = "ENGLISH"

        supplementalDataService.persistSupplementalDataFor(model, sdeModel)

        ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModelUpdated = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "ENGLISH", sdeModelUpdated.LANGUAGE."ENG".value

        // deletes values for user-defined attributes
        sdeModelUpdated.LANGUAGE."ENG".value = null

        supplementalDataService.persistSupplementalDataFor(model, sdeModelUpdated)
        def sdeModelDeleted = supplementalDataService.loadSupplementalDataForModel(model)

        assertNull sdeModelDeleted.LANGUAGE."ENG".value

    }

    /**
     * Tests Validation.
     * */
    @Test
    void testValidationSDE() {
        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        sdeModel.USERDEFINED."name".value = "my comments"
        sdeModel.LANGUAGE."ENG".value = "my test"

        try {
            supplementalDataService.persistSupplementalDataFor(model, sdeModel)
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
    }

    /**
     * Tests Validation LOV.
     * */
    @Test
    void testValidationLov() {

        updateGorsdamTableLov()

        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def sdeModel = supplementalDataService.loadSupplementalDataForModel(model)

        sdeModel.USERDEFINED."name".value = "1234"
        sdeModel.LANGUAGE."ENG".value = "my test"

        try {
            supplementalDataService.persistSupplementalDataFor(model, sdeModel)
            fail "This should have failed"
        }
        catch (ApplicationException ae) {
            if (ae.wrappedException =~ /\*Error\* Value 1234 not found in validation table STVTERM./)
                log.debug("Found correct message code *Error* Value 1234 not found in validation table STVTERM.")
            else
                fail("Did not find expected error code *Error* Value 1234 not found in validation table STVTERM., sdeModel: ${ae.wrappedException}")
        }
        catch (AssertionError ae) {
            assertEquals "This should have failed", ae.message
        }
    }

    /**
     * Tests Mapped Domain for LOV.
     * */
    @Test
    void testFindMappedDomain() {
        def mappedDomain = supplementalDataService.getMappedDomain("GURSESS")

        assertEquals "net.hedtech.banner.session.BannerUserSession", mappedDomain

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

    private def updateGorsdamTableLov() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
                 UPDATE GORSDAM
                SET GORSDAM_LOV_FORM = 'STVTERM',
                    GORSDAM_GJAPDEF_VALIDATION = 'LOV_VALIDATION',
                    GORSDAM_ATTR_DATA_LEN = 20,
                    GORSDAM_LOV_LOW_SYSDATE_IND = 'N',
                    GORSDAM_LOV_HIGH_SYSDATE_IND = 'N'
              WHERE GORSDAM_TABLE_NAME = 'GTVZIPC'
                AND GORSDAM_ATTR_NAME = 'COMMENTS'
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }
}
