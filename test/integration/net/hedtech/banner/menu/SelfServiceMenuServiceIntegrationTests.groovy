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

    def selfServiceMenuService
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        Holders?.config.ssbEnabled = true;
        grailsApplication.config?.seamless?.selfServiceApps = ["http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]

        createWebTailorMenuEntry()
    }

    @After
    public void tearDown() {
        super.tearDown();
        deleteWebtailorMenuEntry();

    }

    @Test
    void testSelfServiceBannerMenuPidm() {
        def map
        def pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow ("select spriden_pidm from spriden where spriden_id = 'HOSS001' and spriden_change_ind is not null") {
            pidm = it.spriden_pidm
        }
        map = selfServiceMenuService.bannerMenu (null,null,pidm)
        assert map?.size() > 0
    }

    @Test
    void testSelfServiceBannerMenu() {
        def map
        map = selfServiceMenuService.bannerMenu (null,null,null)
        assert map.size() > 0
    }

    @Test
    void testSSLinks() {

        def ss = ["a","B"]
        grailsApplication.config.seamless.selfServiceApps = ss

        assertEquals "A", selfServiceMenuService.getSSLinks()[0]

    }

    @Test
    void TestSearchMenuAppConceptWithStudentRole() {
        def map
        def pidm

        def bannerPidm = generatePidm()
        def bannerId = "SSSTNDT"

        generateSpridenRecord(bannerId, bannerPidm)
        addStudentRoleToSpriden(bannerPidm)

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow ("select spriden_pidm from spriden where spriden_id = 'SSSTNDT'") {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.searchMenuAppConcept ("",pidm,false)
        assert map?.size() > 0

        deleteSpriden(bannerPidm)
    }

    @Test
    void TestSearchMenuAppConceptWithNoGovrole() {
        def map
        def pidm

        def bannerPidm = generatePidm()
        def bannerId = "SSNOROLE"

        generateSpridenRecord(bannerId, bannerPidm)

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow ("select spriden_pidm from spriden where spriden_id = 'SSNOROLE'") {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.searchMenuAppConcept ("",pidm,false)
        assert map?.size() == 0

        deleteSpriden(bannerPidm)

    }

    @Test
    void TestBannerMenuAppConceptWithStudentRole() {
        def map
        def pidm

        def bannerPidm = generatePidm()
        def bannerId = "SSSTNDT"

        generateSpridenRecord(bannerId, bannerPidm)
        addStudentRoleToSpriden(bannerPidm)

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow ("select spriden_pidm from spriden where spriden_id = 'SSSTNDT'") {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.bannerMenuAppConcept (null, null,pidm)
        assert map?.size() > 0

        deleteSpriden(bannerPidm)
    }

    @Test
    void TestBannerMenuAppConceptWithNoGovrole() {
        def map
        def pidm

        def bannerPidm = generatePidm()
        def bannerId = "SSNOROLE"

        generateSpridenRecord(bannerId, bannerPidm)

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow ("select spriden_pidm from spriden where spriden_id = 'SSNOROLE'") {
            pidm = it.spriden_pidm
        }

        map = selfServiceMenuService.bannerMenuAppConcept (null, null ,pidm)
        assert map?.size() == 0

        deleteSpriden(bannerPidm)

    }

    private void createWebTailorMenuEntry() {

        def db = getDB();

        db.executeUpdate("INSERT INTO twgbwmnu (TWGBWMNU_NAME,TWGBWMNU_DESC,TWGBWMNU_PAGE_TITLE,TWGBWMNU_HEADER,TWGBWMNU_BACK_MENU_IND,TWGBWMNU_MODULE,TWGBWMNU_ENABLED_IND,TWGBWMNU_INSECURE_ALLOWED_IND,TWGBWMNU_ACTIVITY_DATE,TWGBWMNU_CACHE_OVERRIDE,TWGBWMNU_SOURCE_IND,TWGBWMNU_ADM_ACCESS_IND) VALUES ('http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry','Grade Entry Test SSB','Grade Entry Test SSB','Grade Entry Test SSB','N','WTL','Y','N',TO_TIMESTAMP('13-AUG-02','DD-MON-RR HH.MI.SSXFF AM'),'S','L','N')")
        db.commit()
        db.executeUpdate("INSERT INTO TWGRMENU (TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL,TWGRMENU_ENABLED,TWGRMENU_DB_LINK_IND,TWGRMENU_SUBMENU_IND,TWGRMENU_ACTIVITY_DATE,TWGRMENU_SOURCE_IND) VALUES ('bmenu.P_MainMnu',99,'GradeEntryTestSSB','http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry','Y','N','N',TO_TIMESTAMP('15-JAN-02 02.03.09.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),'B')")
        db.commit()
        db.close()
    }

    private getDB() {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def db = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')
        db
    }



    private void generateSpridenRecord(bannerId, bannerPidm) {

        def sql = getDB();

        sql.call("""
         declare

         Lv_Id_Ref Gb_Identification.Identification_Ref;

         spriden_current Gb_Identification.identification_rec;
         test_pidm spriden.spriden_pidm%type;
         test_rowid varchar2(30);
         begin

         gb_identification.p_create(
         P_ID_INOUT => ${bannerId},
         P_LAST_NAME => 'Miller',
         P_FIRST_NAME => 'Ann',
         P_MI => 'Elizabeth',
         P_CHANGE_IND => NULL,
         P_ENTITY_IND => 'P',
         P_User => User,
         P_ORIGIN => 'banner',
         P_NTYP_CODE => NULL,
         P_DATA_ORIGIN => 'banner',
         P_PIDM_INOUT => ${bannerPidm},
         P_Rowid_Out => Test_Rowid);
         end ;
         """)

        sql.commit()
        sql.close()
    }

    private void addStudentRoleToSpriden(pidm) {

        def db = getDB();

        db.executeUpdate("Insert Into Twgrrole ( Twgrrole_Pidm, Twgrrole_Role, Twgrrole_Activity_Date) values ( ${pidm}, 'STUDENT', Sysdate)")
        db.commit()
        db.executeUpdate("INSERT INTO SGBSTDN (SGBSTDN_PIDM,SGBSTDN_TERM_CODE_EFF,SGBSTDN_STST_CODE,SGBSTDN_LEVL_CODE,SGBSTDN_STYP_CODE,SGBSTDN_TERM_CODE_ADMIT,SGBSTDN_CAMP_CODE,SGBSTDN_RESD_CODE,SGBSTDN_COLL_CODE_1,SGBSTDN_DEGC_CODE_1,SGBSTDN_MAJR_CODE_1,SGBSTDN_ACTIVITY_DATE,SGBSTDN_BLCK_CODE,SGBSTDN_PRIM_ROLL_IND,SGBSTDN_PROGRAM_1,SGBSTDN_DATA_ORIGIN,SGBSTDN_USER_ID,SGBSTDN_SURROGATE_ID,SGBSTDN_VERSION) values (${pidm},'201410','AS','UG','S','201410','M','R','AS','BA','HIST',to_date('02-MAR-14','DD-MON-RR'),'NUTR','N','BA-HIST','Banner','BANPROXY',SGBSTDN_SURROGATE_ID_SEQUENCE.nextval,1)")
        db.commit()
        db.close()

    }
    private def generatePidm() {

        def sql = getDB();

        String idSql = """select gb_common.f_generate_pidm pidm from dual """
        def bannerValues = sql.firstRow(idSql)

        sql?.close() // note that the test will close the connection, since it's our current session's connection

        return bannerValues.pidm
    }

    private void deleteSpriden(pidm) {

        def db = getDB();

        db.executeUpdate("delete spriden where spriden_pidm=${pidm}")
        db.commit()
        db.close()
    }

    private void deleteWebtailorMenuEntry(pidm) {

        def db = getDB();

        db.executeUpdate("delete TWGRMENU where TWGRMENU_URL='http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry'")
        db.commit()
        db.executeUpdate("delete twgbwmnu where TWGBWMNU_NAME='http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry'")
        db.commit()
        db.close()
    }

}
