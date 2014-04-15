/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.supplemental

import groovy.sql.Sql

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.test.ZipTest
/**
 * Integration tests of the supplemental data service.
 */
class SdeServiceIntegrationTests extends BaseIntegrationTestCase {
    def supplementalDataService        // injected by Spring

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        updateGorsdamTableValidation()
    }


    protected void tearDown() {
        super.tearDown()
    }

    /**
     * Tests when the block is SDE enabled
     * */
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
    void testSdeLoad() {

        def tableName = 'GTVZIPC'
        def found = ZipTest.findByCodeAndCity("00001", "newcity")
        def id = found.id

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
    void testSdeData() {

        def modelWithSdeData = ZipTest.findByCodeAndCity("00001", "newcity")
        assertTrue supplementalDataService.hasSdeData(modelWithSdeData)

        def modelWithNoSdeData = net.hedtech.banner.test.ZipTest.findByCode("02186")
        assertFalse supplementalDataService.hasSdeData(modelWithNoSdeData)
    }

    /**
     * Tests loading the entity with SDE defined. (SDE data is not empty).
     * */
    void testLoadNotEmptySdeData() {

        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "comment 1", found.COMMENTS."1".value
        assertEquals "comment 2", found.COMMENTS."2".value
        assertEquals "cmment 3", found.COMMENTS."3".value

        assertEquals "Enter a comment", found.COMMENTS."1".prompt
        assertEquals "Enter a comment", found.COMMENTS."2".prompt
        assertEquals "Enter a comment", found.COMMENTS."3".prompt

        assertEquals "Use record dulicate to add more records", found.COMMENTS."1".attrInfo
        assertEquals "Use record dulicate to add more records", found.COMMENTS."2".attrInfo
        assertEquals "Use record dulicate to add more records", found.COMMENTS."3".attrInfo

        assertEquals "VARCHAR2", found.COMMENTS."1".dataType
        assertEquals "M", found.COMMENTS."1".discType
        assertEquals 3, found.COMMENTS."1".validation
        assertEquals 1, found.COMMENTS."1".attrOrder

        assertEquals "comment 1", found.TEST."1".value
        assertEquals "comment 2", found.TEST."2".value
        assertEquals "comment 3", found.TEST."3".value

        assertEquals "Comment 1", found.TEST."1".prompt
        assertEquals "Comment 2", found.TEST."2".prompt
        assertEquals "Comment 3", found.TEST."3".prompt

        assertEquals "VARCHAR2", found.TEST."1".dataType
        assertEquals "M", found.TEST."1".discType
        assertEquals 6, found.TEST."1".validation
        assertNull found.TEST."1".attrInfo
        assertEquals 2, found.TEST."1".attrOrder


        assertNull found.NUMBER."1".value
        assertEquals "enter a numbere", found.NUMBER."1".prompt

        assertEquals "NUMBER", found.NUMBER."1".dataType
        assertEquals "S", found.NUMBER."1".discType
        assertEquals 1, found.NUMBER."1".validation
        assertEquals 6, found.NUMBER."1".dataLength
        assertEquals 2, found.NUMBER."1".dataScale
        assertEquals "with 2 decimal points", found.NUMBER."1".attrInfo
        assertEquals 3, found.NUMBER."1".attrOrder


        assertEquals 5, found.size()
        assertTrue 'TEST' in found
        assertTrue 'NUMBER' in found
        assertTrue 'COMMENTS' in found
        assertTrue 'USERDEFINED' in found
        assertTrue 'LANGUAGE' in found

        assertEquals 3, found."TEST".size()
        assertEquals 3, found."COMMENTS".size()
        assertEquals 1, found."NUMBER".size()
        assertEquals 2, found."USERDEFINED".size()
        assertTrue found."LANGUAGE".size() > 2

    }

    /**
     * Tests loading the entity with SDE defined. (no SDE data)
     * */
    void testLoadEmptySdeData() {

        def found = supplementalDataService.loadSupplementalDataForModel(net.hedtech.banner.test.ZipTest.findByCodeAndCity("02186", "Milton"))

        assertNull found.COMMENTS."1".value
        assertNull found.TEST."1".value
        assertNull found.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Update SDE data for all attributes
     * */
    void testSaveNotEmptySdeData() {

        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "comment 1", found.COMMENTS."1".value
        assertEquals "comment 1", found.TEST."1".value
        assertNull found.NUMBER."1".value

        found.COMMENTS."1".value = "my comments"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "10"

        supplementalDataService.persistSupplementalDataFor(model, found)

        def foundSdeUpdated = supplementalDataService.loadSupplementalDataForModel(model)
        assertEquals "my comments", foundSdeUpdated.COMMENTS."1".value
        assertEquals "my test", foundSdeUpdated.TEST."1".value
        assertEquals "10", foundSdeUpdated.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     * */
    void testSaveDeleteNotEmptySdeData() {
        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "comment 1", found.COMMENTS."1".value
        assertEquals "comment 1", found.TEST."1".value
        assertNull found.NUMBER."1".value
        assertEquals 3, found.COMMENTS.size()

        found.COMMENTS."1".value = null
        supplementalDataService.persistSupplementalDataFor(model, found)
        def foundDeleted = supplementalDataService.loadSupplementalDataForModel(model)
        assertEquals 2, foundDeleted.COMMENTS.size()

    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     */
    void testSaveDeleteNotEmptySdeDataInTheMiddle() {

        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "comment 2", found.COMMENTS."2".value   // in the middle
        assertEquals "comment 1", found.TEST."1".value
        assertNull found.NUMBER."1".value

        found.COMMENTS."2".value = null
        supplementalDataService.persistSupplementalDataFor(model, found)
        def foundDeleted = supplementalDataService.loadSupplementalDataForModel(model)
        assertEquals 2, foundDeleted.COMMENTS.size()


        assertNotNull foundDeleted.COMMENTS."2".value   // rebuilt discriminator
        assertEquals "cmment 3", foundDeleted.COMMENTS."2".value

        assertNull foundDeleted.COMMENTS."3"
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. No SDE data
     * 2. Add SDE data to these attributes
     * */
    void testLoadAndCreateEmptySdeData() {


        def model = ZipTest.findByCodeAndCity("02186", "Milton")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertNull found.COMMENTS."1".value
        assertNull found.TEST."1".value
        assertNull found.NUMBER."1".value

        found.COMMENTS."1".value = "my comments"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "10"

        supplementalDataService.persistSupplementalDataFor(model, found)


        def foundUpdated = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "my comments", foundUpdated.COMMENTS."1".value
        assertEquals "my test", foundUpdated.TEST."1".value
        assertEquals "10", foundUpdated.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes with wrong Number format
     * */
    void testNumericValidationSdeData() {


        def zip = new ZipTest(code: "BB", city: "BB")
        zip.save()

        try {

            def model = ZipTest.findByCodeAndCity("BB", "BB")
            def zipFound = supplementalDataService.loadSupplementalDataForModel(model)

            zipFound.COMMENTS."1".value = "my comments"
            zipFound.TEST."1".value = "my test"
            zipFound.NUMBER."1".value = "test"

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
    void testDateValidationSdeData() {

        def zip = new ZipTest(code: "BB", city: "BB")

        try {
            zip.save()
            def model = ZipTest.findByCodeAndCity("BB", "BB")
            def zipFound = supplementalDataService.loadSupplementalDataForModel(model)


            zipFound.dataOrigin = "foo"
            zipFound.COMMENTS."1".value = "my comments"
            zipFound.TEST."1".value = "my test"

            zipFound.NUMBER."1".dataType = "DATE" // forced Date
            zipFound.NUMBER."1".value = "15-Apr2010" // wrong format

            supplementalDataService.persistSupplementalDataFor(model, zipFound)
            fail("Should have received an error: Invalid Date")
        }
        catch (ApplicationException e) {
            assertEquals "Invalid Date", e.wrappedException.message
        }
        catch (Exception e) {
            assertEquals "Invalid Date", e.message
        }
    }

    /**
     * Tests User Defined SDE Attributes.
     * */
    void testLoadUseDefinedSdeData() {

        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertNotNull found.USERDEFINED
        assertNotNull found.USERDEFINED."name"
        assertNotNull found.USERDEFINED."phone"

        assertEquals "User Defined name", found.USERDEFINED."name".prompt
        assertEquals "User Defined phone", found.USERDEFINED."phone".prompt

        // adds new values for user-defined attributes
        found.USERDEFINED."name".value = "my name"
        found.USERDEFINED."phone".value = "1234"

        supplementalDataService.persistSupplementalDataFor(model, found)

        def updatedSde = ZipTest.findByCodeAndCity("00001", "newcity")
        def foundUpdated = supplementalDataService.loadSupplementalDataForModel(updatedSde)

        assertEquals "my name", foundUpdated.USERDEFINED."name".value
        assertEquals "1234", foundUpdated.USERDEFINED."phone".value

        // deletes values for user-defined attributes
        foundUpdated.USERDEFINED."name".value = null
        foundUpdated.USERDEFINED."phone".value = null


        supplementalDataService.persistSupplementalDataFor(model, foundUpdated)

        def deletedSdeModel = ZipTest.findByCodeAndCity("00001", "newcity")

        def deletedSde = supplementalDataService.loadSupplementalDataForModel(deletedSdeModel)

        assertNull deletedSde.USERDEFINED."name".value
        assertNull deletedSde.USERDEFINED."phone".value
    }

    /**
     * Tests SQL Based SDE attributes.
     * */
    void testLoadSQLBasedAttributeSdeData() {

        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        assertNotNull found.LANGUAGE
        assertEquals "Language", found.LANGUAGE."ENG".prompt
        assertEquals "Language", found.LANGUAGE."RUS".prompt
        assertEquals "Language", found.LANGUAGE."GRM".prompt


        found.LANGUAGE."GRM".value = "Munchen"

        supplementalDataService.persistSupplementalDataFor(model, found)

        def updatedSde = ZipTest.findByCodeAndCity("00001", "newcity")
        def foundUpdated = supplementalDataService.loadSupplementalDataForModel(model)

        assertEquals "Munchen", foundUpdated.LANGUAGE."GRM".value

        // deletes values for user-defined attributes
        foundUpdated.LANGUAGE."GRM".value = null

        supplementalDataService.persistSupplementalDataFor(model, foundUpdated)
        def foundDeleted = supplementalDataService.loadSupplementalDataForModel(model)

        assertNull foundDeleted.LANGUAGE."GRM".value

    }

    /**
     * Tests Validation.
     * */
    void testValidationSDE() {
        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        found.COMMENTS."1".value = "my comments"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "105666"

        try {
            supplementalDataService.persistSupplementalDataFor(model, found)
            fail "This should have failed"
        }
        catch (ApplicationException ae) {
            if (ae.wrappedException =~ /\*Error\* Invalid Number. Expected format: 999D99/)
                println "Found correct message code *Error* Invalid Number. Expected format: 999D99"
            else
                fail("Did not find expected error code *Error* Invalid Number. Expected format: 999D99, found: ${ae.wrappedException}")
        }
    }


    /**
     * Tests Validation LOV.
     * */
    void testValidationLov() {

        updateGorsdamTableLov()

        def model = ZipTest.findByCodeAndCity("00001", "newcity")
        def found = supplementalDataService.loadSupplementalDataForModel(model)

        found.COMMENTS."1".value = "1234"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "10"

        try {
            def zip = supplementalDataService.persistSupplementalDataFor(model, found)
            fail "This should have failed"
        }
        catch (ApplicationException ae) {
            if (ae.wrappedException =~ /\*Error\* Value 1234 not found in validation table STVTERM./)
                println "Found correct message code *Error* Value 1234 not found in validation table STVTERM."
            else
                fail("Did not find expected error code *Error* Value 1234 not found in validation table STVTERM., found: ${ae.wrappedException}")
        }
    }



    private def updateGORSDAVTable() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("delete gorsdav where gorsdav_table_name = 'GTVZIPC' and gorsdav_disc > 1")
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }

    private def insertUserDefinedAttrGTVZIPCTable() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
            INSERT INTO GORSDAM (GORSDAM_TABLE_NAME, GORSDAM_ATTR_NAME ,
                                 GORSDAM_ATTR_TYPE, GORSDAM_ATTR_ORDER,
                                 GORSDAM_ATTR_REQD_IND, GORSDAM_ATTR_DATA_TYPE,
                                 GORSDAM_ATTR_PROMPT, GORSDAM_ACTIVITY_DATE,
                                 GORSDAM_USER_ID,GORSDAM_SDDC_CODE ) values
            ('GTVZIPC', 'USERDEFINED','A',4,'Y','VARCHAR2','User Defined %DISC%',sysdate,user,'cyndy3')
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
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
