/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class QuickFlowMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def quickFlowMenuService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    @Test
    void testQuickFlowSearch() {
        def map
        map = quickFlowMenuService.quickFlowSearch ('CAT')
        assert map?.size() > 0
    }

    @Test
    void testQuickflowMenu() {
        def map
        map = quickFlowMenuService.quickflowMenu ()
        assert map.size() > 0
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {

            def quickflowCode
            sql.eachRow("select * from GURCALL where GURCALL_CALL_CODE = 'CAT'", {quickflowCode = it.GURCALL_CALL_CODE})
            if(!quickflowCode) {
                sql.executeInsert("Insert into GURCALL ( GURCALL_CALL_CODE, GURCALL_FORM, GURCALL_SEQNO, GURCALL_USER_ID, GURCALL_ACTIVITY_DATE)  VALUES ('CAT','SCACRSE',1,user,sysdate)")
            }

        } finally {
            sql?.close()
        }

    }

}
