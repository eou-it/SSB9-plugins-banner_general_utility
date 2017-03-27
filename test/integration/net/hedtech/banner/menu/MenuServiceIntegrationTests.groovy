/*******************************************************************************
Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.menu

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.junit.After
import org.junit.Before
import org.junit.Test

class MenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def menuService
    def gubmoduIntegrationValue = 'N'
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    @After
    public void tearDown() {
        resetGubmoduIntegrationValue()
        super.tearDown()
    }

    @Test
    void testPersonalMenu() {
        String mnu = menuService.personalMenu()
        assertNotNull mnu

    }

    @Test
    void testPersonalCombinedMenu() {
        String mnu = menuService.personalCombinedMenu()
        assertNotNull mnu

    }

    @Test
    void testSearchExcludeObjects() {
        grailsApplication.config?.seamless?.excludeObjectsFromSearch = [
                "GUAGMNU",'GUAINIT','FOQMENU','SOQMENU','TOQMENU','AOQMEMU','GOQMENU','ROQMENU','NOQMENU','POQMENU','GUQSETI'
        ]

        ArrayList map = menuService.gotoCombinedMenu('GUAINIT')
        assertNull map[0]

        map = menuService.gotoCombinedMenu('GTVZIPC')
        assertNotNull map[0]
    }


    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.eachRow("select GUBMODU_INTEGRATION_VALUE from gubmodu where GUBMODU_CODE = 'SC'", {gubmoduIntegrationValue = it.GUBMODU_INTEGRATION_VALUE })
            if(gubmoduIntegrationValue == 'Y'){
                sql.executeUpdate("update gubmodu set GUBMODU_INTEGRATION_VALUE='N' where GUBMODU_CODE = 'SC'")
                sql.call("{call gukmenu.p_refresh_horizon_menu()}")
            }
            def courseName
            sql.eachRow("select * from gurmenu where gurmenu_user_id = user and gurmenu_name = '*PERSONAL' and GURMENU_OBJ_NAME='SCACRSE'", {courseName = it.GURMENU_OBJ_NAME })
            if (courseName != 'SCACRSE')
                sql.executeInsert("Insert into gurmenu ( GURMENU_NAME,GURMENU_OBJ_NAME, GURMENU_SORT_SEQ, GURMENU_USER_ID, GURMENU_ACTIVITY_DATE, GURMENU_DESC)  VALUES ('*PERSONAL','SCACRSE',1,user,sysdate,'Basic Course Information')")
        } finally {
            sql?.close()
        }

    }

    private def resetGubmoduIntegrationValue() {
        if (gubmoduIntegrationValue == 'Y') {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            try {
                sql.executeUpdate("update gubmodu set GUBMODU_INTEGRATION_VALUE='Y' where GUBMODU_CODE = 'SC'")
                sql.call("{call gukmenu.p_refresh_horizon_menu()}")
                sql.commit()
            } finally {
                sql?.close()
            }
        }

        gubmoduIntegrationValue == 'N'
    }
}
