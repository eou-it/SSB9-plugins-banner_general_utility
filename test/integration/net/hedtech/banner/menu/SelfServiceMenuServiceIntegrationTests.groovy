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
        sql.eachRow("select spriden_pidm from spriden where spriden_id = ? and spriden_change_ind is null", [BANNER_ID_WITH_OUT_STUDENT_ROLE]) {
            pidm = it.spriden_pidm
        }
        map = selfServiceMenuService.bannerMenu(null, null, pidm)
        assert map?.size() > 0
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

        sql.eachRow("""select spriden_pidm from spriden where spriden_id = ?""", [BANNER_ID_WITH_STUDENT_ROLE]) {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.searchMenuAppConcept("", pidm, false)
        assert map?.size() > 0

    }

    @Test
    void TestSearchMenuAppConceptWithNoGovrole() {
        def map
        def pidm

        sql.eachRow("select spriden_pidm from spriden where spriden_id = ?", [BANNER_ID_WITH_OUT_STUDENT_ROLE]) {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.searchMenuAppConcept("", pidm, false)
        assert map?.size() == 0
    }

    @Test
    void testBannerMenuAppConceptWithStudentRole() {
        def map
        def pidm

        sql.eachRow("select spriden_pidm from spriden where spriden_id = ?", [BANNER_ID_WITH_STUDENT_ROLE]) {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.bannerMenuAppConcept(pidm)
        assert map?.size() > 0
    }

    @Test
    void TestBannerMenuAppConceptWithNoGovrole() {
        def map
        def pidm

        sql.eachRow("select spriden_pidm from spriden where spriden_id = ?", [BANNER_MENU_APP_CONCEPT_WITHOUT_ROLE]) {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.bannerMenuAppConcept(pidm)
        assert map?.size() == 0
    }

}
