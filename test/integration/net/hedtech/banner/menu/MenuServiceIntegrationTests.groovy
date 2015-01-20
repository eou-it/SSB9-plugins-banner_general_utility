/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.menu

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.junit.Before
import org.junit.Test

class MenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def menuService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    @Test
    void testBannerMenu() {
        def map
        map = menuService.bannerMenu()
        assertNotNull map

        def mnu = map.find {it -> it.formName == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.caption
        assertNotNull mnu.platCode
        assert mnu.formName == "SCACRSE"
        assert mnu.pageName == "basicCourseInformation"
    }

    @Test
    void testPersonalMenu() {
        String mnu = menuService.personalMenu()
        assertNotNull mnu

    }

    @Test
    void testGotoMenu() {
        ArrayList map = menuService.gotoMenu('SCA')
        assertNotNull map

        def mnu = map.find {it -> it.formName == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.caption
        assertNotNull mnu.platCode
        assert mnu.formName == "SCACRSE"
        assert mnu.pageName == "basicCourseInformation"
    }

    @Test
    void testGetFormName() {
        def pageName
        pageName = menuService.getFormName("basicCourseInformation")
        assertNotNull pageName
        assert pageName == "SCACRSE"
    }

    @Test
    void testBannerCombinedMenu() {
        def map
        map = menuService.bannerCombinedMenu()
        assertNotNull map

        def mnu = map.find {it -> it.name == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.caption
        assertNotNull mnu.platCode
        assert mnu.name == "SCACRSE"
        assert mnu.page == "SCACRSE"
    }

    @Test
    void testPersonalCombinedMenu() {
        String mnu = menuService.personalCombinedMenu()
        assertNotNull mnu

    }

    @Test
    void testGotoCombinedMenu() {
        ArrayList map = menuService.gotoCombinedMenu('SCA')
        assertNotNull map

        def mnu = map.find {it -> it.formName == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.caption
        assertNotNull mnu.platCode
        assert mnu.formName == "SCACRSE"
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def courseName
        sql.eachRow("select * from gurmenu where gurmenu_user_id = user and gurmenu_name = '*PERSONAL' and GURMENU_OBJ_NAME='SCACRSE'", {courseName = it.GURMENU_OBJ_NAME })
        if (courseName != 'SCACRSE')
            sql.executeInsert("Insert into gurmenu ( GURMENU_NAME,GURMENU_OBJ_NAME, GURMENU_SORT_SEQ, GURMENU_USER_ID, GURMENU_ACTIVITY_DATE, GURMENU_DESC)  VALUES ('*PERSONAL','SCACRSE',1,user,sysdate,'Basic Course Information')")

    }
}
