/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class SelfServiceMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    SelfServiceMenuService selfServiceMenuService
    def grailsApplication
    def dataSource
    def conn
    def sql
    private static final def BANNER_ID_WITH_STUDENT_ROLE = 'ESSREG02'
    private static final def BANNER_ID_WITH_OUT_STUDENT_ROLE = 'HOSS001'
    private static final def BANNER_MENU_APP_CONCEPT_WITHOUT_ROLE = 'HOS00010'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getSsbConnection()
        sql = new Sql(conn)
        Holders?.config.ssbEnabled = true;
        grailsApplication.config?.seamless?.selfServiceApps = ["http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
    }

    @After
    public void tearDown() {
        super.tearDown();
        if (sql) sql?.close()
    }

    @Test
    void testSelfServiceBannerMenuPidm() {
        def map
        def pidm
        pidm = getPidm(BANNER_ID_WITH_OUT_STUDENT_ROLE)
        map = selfServiceMenuService.bannerMenu(null, null, pidm)
        assert map?.size() > 0
    }


    @Test
    void testSelfServiceBaselineBannerMenu() {
        def map
        def pidm
        pidm = getPidm(BANNER_ID_WITH_OUT_STUDENT_ROLE)
        map = selfServiceMenuService.bannerMenu('b.IntegrationTestCaseMenu', null, pidm)
        assertTrue map?.size() > 0
        map?.each { baselineMenu ->
            assertEquals baselineMenu.sourceIndicator, "B"
            assertEquals baselineMenu.parent, "b.IntegrationTestCaseMenu"
            assertEquals baselineMenu.name, "baselineRecord"
        }
    }


    @Test
    void testSelfServiceLocalBannerMenu() {
        def map
        def pidm
        pidm = getPidm(BANNER_ID_WITH_OUT_STUDENT_ROLE)
        map = selfServiceMenuService.bannerMenu('l.IntegrationTestCaseMenu', null, pidm)
        assertTrue map?.size() > 0
        map?.each { localMenu ->
            assertEquals localMenu.sourceIndicator, "L"
            assertEquals localMenu.parent, "l.IntegrationTestCaseMenu"
            assertEquals localMenu.name, "localRecord"
        }
    }


    @Test
    void testSelfServiceNORoleSpecificBannerMenu() {
        def map
        def pidm
        def menuName = 'FacultyRoleSpecificMenu'
        pidm = getPidm(BANNER_ID_WITH_STUDENT_ROLE)
        map = selfServiceMenuService.bannerMenu(menuName, null, pidm)
        assertTrue map?.size() == 0
    }

    @Test
    void testParentMenu(){
        def parentList = selfServiceMenuService.getParent("bmenu.P_MainMnu");
        assertEquals parentList , []
    }

    @Test
    void testParentMenu1(){
        def menuName = 'FacultyRoleSpecificMenu'
        def parentList = selfServiceMenuService.getParent(menuName);
        parentList?.find { parent ->
            assertTrue parent.name == menuName
        }
    }

    @Test
    void testSelfServiceStudentRoleSpecificBannerMenu() {
        def map
        def pidm
        def menuRole
        def menuName = 'FacultyRoleSpecificMenu'
        def currentRole = "FACULTY"
        pidm = getPidm(BANNER_ID_WITH_OUT_STUDENT_ROLE)
        map = selfServiceMenuService.bannerMenu(menuName, null, pidm)
        menuRole = sql.rows("SELECT TWGRWMRL_ROLE FROM TWGRWMRL WHERE TWGRWMRL_NAME = ? ", [menuName])
        assertTrue map?.size() > 0

        map?.each { menu ->
            assertTrue menu.parent == menuName
            assertTrue menu.parent == menuName
            assertTrue menu.name == menuName
        }
        menuRole?.each { listOfRoles ->
            listOfRoles?.each { key, role ->
                assertTrue role == currentRole
            }

        }

    }

    @Test
    void testSSLinks() {

        def ss = ["a", "B"]
        grailsApplication.config.seamless.selfServiceApps = ss

        assertEquals "A", selfServiceMenuService.getSSLinks()[0]
    }

    @Test
    void testSearchMenuAppConceptWithStudentRole() {
        def map
        def pidm
        pidm = getPidm(BANNER_ID_WITH_STUDENT_ROLE)
        map = selfServiceMenuService.searchMenuAppConcept("", pidm, false)
        assert map?.size() > 0
    }

    @Test
    void TestSearchMenuAppConceptWithNoGovrole() {
        def map
        def pidm
        pidm = getPidm(BANNER_ID_WITH_OUT_STUDENT_ROLE)
        map = selfServiceMenuService.searchMenuAppConcept("", pidm, false)
        assert map?.size() == 0
    }

    @Test
    void testBannerMenuAppConceptWithStudentRole() {
        def map
        def pidm
        pidm = getPidm(BANNER_ID_WITH_STUDENT_ROLE)
        map = selfServiceMenuService.bannerMenuAppConcept(pidm)
        assert map?.size() > 0
    }

    @Test
    void TestBannerMenuAppConceptWithNoGovrole() {
        def map
        def pidm
        pidm = getPidm(BANNER_MENU_APP_CONCEPT_WITHOUT_ROLE)
        map = selfServiceMenuService.bannerMenuAppConcept(pidm)
        assert map?.size() == 0
    }

    private getPidm(String bannerId) {
        def pidm
        sql.eachRow("select spriden_pidm from spriden where spriden_id = ? and spriden_change_ind is null", [bannerId]) {
            pidm = it.spriden_pidm
        }
        return pidm
    }

}
