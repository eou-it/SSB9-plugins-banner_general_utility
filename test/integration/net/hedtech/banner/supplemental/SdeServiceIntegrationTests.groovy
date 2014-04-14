/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.supplemental

import groovy.sql.Sql

import net.hedtech.banner.testing.BaseIntegrationTestCase

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
        def found = net.hedtech.banner.test.ZipTest.findByCodeAndCity("00001", "newcity")
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

        def modelWithSdeData = net.hedtech.banner.test.ZipTest.findByCodeAndCity("00001", "newcity")
        assertTrue supplementalDataService.hasSdeData(modelWithSdeData)

        def modelWithNoSdeData = net.hedtech.banner.test.ZipTest.findByCode("02186")
        assertFalse supplementalDataService.hasSdeData(modelWithNoSdeData)
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
