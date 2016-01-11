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
    void testQuickFlowSearchWithInbForm() {
        def map
        map = quickFlowMenuService.quickFlowLessThan3CharSearch ('TT')
        assert map?.size() > 0
        // test the returned URL to be INB
        Menu m = map.get(0)
        assertNull(quickFlowMenuService.getGubmoduUrlForHsTypeFromQuickFlowCode(m.name))
    }

    @Test
    void testQuickFlowSearchWithHsForm() {
        def map
        map = quickFlowMenuService.quickFlowLessThan3CharSearch ('TT2')
        assert map?.size() > 0
        // test the returned URL to be HS
        Menu m = map.get(0)
        assertNotNull(quickFlowMenuService.getGubmoduUrlForHsTypeFromQuickFlowCode(m.name))
    }

    @Test
    void testQuickflowPersonalMenu() {
        def map
        map = quickFlowMenuService.quickflowPersonalMenu()

        assert map.size() > 0
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {

            // set up INB type quickflow
            def quickflowCode
            sql.eachRow("select * from GTVCALL where GTVCALL_CODE = 'TT'", {quickflowCode = it.GTVCALL_CODE})
            if(!quickflowCode) {
                sql.executeInsert("INSERT INTO GTVCALL(GTVCALL_CODE, GTVCALL_DESC, GTVCALL_ACTIVITY_DATE, GTVCALL_USER_ID, GTVCALL_DATA_ORIGIN) values ('TT','Test quickflow', sysdate, user, 'GRAILS')")
            }
            def quickflowCode2
            sql.eachRow("select * from GURCALL where GURCALL_CALL_CODE = 'TT'", {quickflowCode2 = it.GURCALL_CALL_CODE})
            if(!quickflowCode2) {
                sql.executeInsert("Insert into GURCALL ( GURCALL_CALL_CODE, GURCALL_FORM, GURCALL_SEQNO, GURCALL_USER_ID, GURCALL_ACTIVITY_DATE)  VALUES ('TT','SCACRSE',1,user,sysdate)")
            }
            // INSERT INTO GUBOBJS values('1', '1 description', 'QUICKFLOW', 'G', 'BASELINE', sysdate, 'N', 'N', null, null, null, null, 'B')
            def gubobjVal
            sql.eachRow("select * from GUBOBJS where GUBOBJS_NAME = 'TT'", {gubobjVal = it.GUBOBJS_NAME})
            if(!gubobjVal) {
                sql.executeInsert("INSERT INTO GUBOBJS values('TT', 'TT description', 'QUICKFLOW', 'G', user, sysdate, 'N', 'N', null, null, null, null, 'B')")
            }

            // set up HS type quickflow
            def quickflowCode3
            sql.eachRow("select * from GTVCALL where GTVCALL_CODE = 'TT2'", {quickflowCode3 = it.GTVCALL_CODE})
            if(!quickflowCode3) {
                sql.executeInsert("INSERT INTO GTVCALL(GTVCALL_CODE, GTVCALL_DESC, GTVCALL_ACTIVITY_DATE, GTVCALL_USER_ID, GTVCALL_DATA_ORIGIN) values ('TT2','Test quickflow', sysdate, user, 'GRAILS')")
            }

            def quickflowCode4
            sql.eachRow("select * from GURCALL where GURCALL_CALL_CODE = 'TT2'", {quickflowCode4 = it.GURCALL_CALL_CODE})
            if(!quickflowCode4) {
                sql.executeInsert("Insert into GURCALL ( GURCALL_CALL_CODE, GURCALL_FORM, GURCALL_SEQNO, GURCALL_USER_ID, GURCALL_ACTIVITY_DATE)  VALUES ('TT2','NTVACAT',1,user,sysdate)")
            }
            // INSERT INTO GUBOBJS values('1', '1 description', 'QUICKFLOW', 'G', 'BASELINE', sysdate, 'N', 'N', null, null, null, null, 'B')
            def gubobjVal2
            sql.eachRow("select * from GUBOBJS where GUBOBJS_NAME = 'TT2'", {gubobjVal2 = it.GUBOBJS_NAME})
            if(!gubobjVal2) {
                sql.executeInsert("INSERT INTO GUBOBJS values('TT2', 'TT2 description', 'QUICKFLOW', 'G', user, sysdate, 'N', 'N', null, null, null, null, 'B')")
            }

            // set up personal menu
            //INSERT INTO GURMENU VALUES ('*PERSONAL', 'CYN', 1, 'GRAILS_USER', SYSDATE, 'cyndies Test for System Test', NULL, NULL, NULL, NULL)
            def quickflowName
            sql.eachRow("select * from gurmenu where gurmenu_user_id = user and gurmenu_name = '*PERSONAL' and GURMENU_OBJ_NAME='TT'", {quickflowName = it.GURMENU_OBJ_NAME })
            if (quickflowName != 'TT')
                sql.executeInsert("Insert into gurmenu ( GURMENU_NAME,GURMENU_OBJ_NAME, GURMENU_SORT_SEQ, GURMENU_USER_ID, GURMENU_ACTIVITY_DATE, GURMENU_DESC)  VALUES ('*PERSONAL','TT',1,user,sysdate,'Test quickflow')")

            // set up main menu

        } finally {
            sql?.close()
        }

    }

}
