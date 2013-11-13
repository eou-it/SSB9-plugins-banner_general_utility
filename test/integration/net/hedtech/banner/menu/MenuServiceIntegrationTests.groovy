/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.menu

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

class MenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def menuService

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    void testBannerMenu() {
        def map
        map = menuService.bannerMenu()
        assertNotNull map

        def mnu = map.find {it -> it.formName == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.caption
        assert mnu.formName == "SCACRSE"
        assert mnu.pageName == "basicCourseInformation"
    }

    void testPersonalMenu() {
        String mnu = menuService.personalMenu()
        assertNotNull mnu

    }

    void testGotoMenu() {
        String mnu = menuService.gotoMenu('SCA')
        assertNotNull mnu

    }

    void testGetFormName() {
        def pageName
        pageName = menuService.getFormName("basicCourseInformation")
        assertNotNull pageName
        assert pageName == "SCACRSE"
    }

    void testBannerCombinedMenu() {
        def map
        map = menuService.bannerCombinedMenu()
        assertNotNull map

        def mnu = map.find {it -> it.name == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.caption
        assert mnu.name == "SCACRSE"
        assert mnu.page == "basicCourseInformation"
    }

    void testPersonalCombinedMenu() {
        String mnu = menuService.personalCombinedMenu()
        assertNotNull mnu

    }

    void testGotoCombinedMenu() {
        String mnu = menuService.gotoCombinedMenu('SCA')
        assertNotNull mnu

    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def courseName
        sql.eachRow("select * from gurmenu where gurmenu_user_id = user and gurmenu_name = '*PERSONAL' and GURMENU_OBJ_NAME='SCACRSE'", {courseName = it.GURMENU_OBJ_NAME })
        if (courseName != 'SCACRSE')
            sql.executeInsert("Insert into gurmenu ( GURMENU_NAME,GURMENU_OBJ_NAME, GURMENU_SORT_SEQ, GURMENU_USER_ID, GURMENU_ACTIVITY_DATE, GURMENU_DESC)  VALUES ('*PERSONAL','SCACRSE',1,user,sysdate,'Basic Course Information')")

    }
}
