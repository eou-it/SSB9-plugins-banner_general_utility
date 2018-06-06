/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestContextHolder

class SelfServiceMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    SelfServiceMenuService selfServiceMenuService
    def grailsApplication
    def dataSource
    def conn
    def sql
    private static final def BANNER_ID_WITH_STUDENT_ROLE = 'ESSREG02'
    private static final def BANNER_ID_WITH_OUT_STUDENT_ROLE = 'HOSS001'
    private final String AR = "ar"
    private final String EN = "en"
    private final String ZH = "zh"
    private final String FR = "fr"
    private final String PT = "pt"
    private final String ES = "es"
    private final String RO = "ro"
    private final String GB = "GB"
    private final String IN = "IN"
    private final String AU = "AU"
    private final String IE = "IE"
    private final String CA = "CA"
    private final String SA = "SA"
    private final String CH = "CH"
    private final String MD = "MD"

    private final String MEP_GVU = "GVU"
    private final String MEP_BANNER = "BANNER"

    private final String BANNER8_URL_AR = "http://<host_name>:<port_number>/<banner8>/AR/"
    private final String BANNER8_URL_EN = "http://<host_name>:<port_number>/<banner8>/EN/"
    private final String BANNER8_URL_ENAU = "http://<host_name>:<port_number>/<banner8>/enAU/"
    private final String BANNER8_URL_ENGB = "http://<host_name>:<port_number>/<banner8>/enGB/"
    private final String BANNER8_URL_ENIE = "http://<host_name>:<port_number>/<banner8>/enIE/"
    private final String BANNER8_URL_ENIN = "http://<host_name>:<port_number>/<banner8>/enIN/"
    private final String BANNER8_URL_FR = "http://<host_name>:<port_number>/<banner8>/FR/"
    private final String BANNER8_URL_FRCA = "http://<host_name>:<port_number>/<banner8>/frCA/"
    private final String BANNER8_URL_PT = "http://<host_name>:<port_number>/<banner8>/PT/"
    private final String BANNER8_URL_ES = "http://<host_name>:<port_number>/<banner8>/ES/"
    private final String BANNER8_URL_DEFAULT = "http://<host_name>:<port_number>/<banner8>/DEFAULT/"

    private final String GVU_BANNER8_URL_AR = "http://<host_name>:<port_number>/<banner8>/GVU/AR/"
    private final String GVU_BANNER8_URL_EN = "http://<host_name>:<port_number>/<banner8>/GVU/EN/"
    private final String GVU_BANNER8_URL_ENAU = "http://<host_name>:<port_number>/<banner8>/GVU/enAU/"
    private final String GVU_BANNER8_URL_ENGB = "http://<host_name>:<port_number>/<banner8>/GVU/enGB/"
    private final String GVU_BANNER8_URL_ENIE = "http://<host_name>:<port_number>/<banner8>/GVU/enIE/"
    private final String GVU_BANNER8_URL_ENIN = "http://<host_name>:<port_number>/<banner8>/GVU/enIN/"
    private final String GVU_BANNER8_URL_FR = "http://<host_name>:<port_number>/<banner8>/GVU/FR/"
    private final String GVU_BANNER8_URL_FRCA = "http://<host_name>:<port_number>/<banner8>/GVU/frCA/"
    private final String GVU_BANNER8_URL_PT = "http://<host_name>:<port_number>/<banner8>/GVU/PT/"
    private final String GVU_BANNER8_URL_ES = "http://<host_name>:<port_number>/<banner8>/GVU/ES/"
    private final String GVU_BANNER8_URL_DEFAULT = "http://<host_name>:<port_number>/<banner8>/GVU/DEFAULT/"

    private final String BANNER_BANNER8_URL_AR = "http://<host_name>:<port_number>/<banner8>/BANNER/AR/"
    private final String BANNER_BANNER8_URL_EN = "http://<host_name>:<port_number>/<banner8>/BANNER/EN/"
    private final String BANNER_BANNER8_URL_ENAU = "http://<host_name>:<port_number>/<banner8>/BANNER/enAU/"
    private final String BANNER_BANNER8_URL_ENGB = "http://<host_name>:<port_number>/<banner8>/BANNER/enGB/"
    private final String BANNER_BANNER8_URL_ENIE = "http://<host_name>:<port_number>/<banner8>/BANNER/enIE/"
    private final String BANNER_BANNER8_URL_ENIN = "http://<host_name>:<port_number>/<banner8>/BANNER/enIN/"
    private final String BANNER_BANNER8_URL_FR = "http://<host_name>:<port_number>/<banner8>/BANNER/FR/"
    private final String BANNER_BANNER8_URL_FRCA = "http://<host_name>:<port_number>/<banner8>/BANNER/frCA/"
    private final String BANNER_BANNER8_URL_PT = "http://<host_name>:<port_number>/<banner8>/BANNER/PT/"
    private final String BANNER_BANNER8_URL_ES = "http://<host_name>:<port_number>/<banner8>/BANNER/ES/"
    private final String BANNER_BANNER8_URL_DEFAULT = "http://<host_name>:<port_number>/<banner8>/BANNER/DEFAULT/"

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getSsbConnection()
        sql = new Sql(conn)
        Holders?.config.ssbEnabled = true;
        grailsApplication.config?.seamless?.selfServiceApps = ["http://abc:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
    }


    public void setUpBanner8LocaleSpecificURL() {
       Holders?.config?.banner8.SS.locale?.url = [default:BANNER8_URL_DEFAULT, en:BANNER8_URL_EN, en_AU:BANNER8_URL_ENAU, en_GB:BANNER8_URL_ENGB, en_IE:BANNER8_URL_ENIE, en_IN:BANNER8_URL_ENIN, fr:BANNER8_URL_FR, fr_CA:BANNER8_URL_FRCA, pt:BANNER8_URL_PT, es:BANNER8_URL_ES, ar:BANNER8_URL_AR]
       Holders?.config?.mep?.banner8?.SS?.locale?.url = [GVU:[default:GVU_BANNER8_URL_DEFAULT, en:GVU_BANNER8_URL_EN, en_AU:GVU_BANNER8_URL_ENAU, en_GB:GVU_BANNER8_URL_ENGB, en_IE:GVU_BANNER8_URL_ENIE, en_IN:GVU_BANNER8_URL_ENIN, fr:GVU_BANNER8_URL_FR, fr_CA:GVU_BANNER8_URL_FRCA, pt:GVU_BANNER8_URL_PT, es:GVU_BANNER8_URL_ES, ar:GVU_BANNER8_URL_AR ],
       BANNER:[default:BANNER_BANNER8_URL_DEFAULT, en:BANNER_BANNER8_URL_EN, en_AU:BANNER_BANNER8_URL_ENAU, en_GB:BANNER_BANNER8_URL_ENGB, en_IE:BANNER_BANNER8_URL_ENIE, en_IN:BANNER_BANNER8_URL_ENIN, fr:BANNER_BANNER8_URL_FR, fr_CA:BANNER_BANNER8_URL_FRCA, pt:BANNER_BANNER8_URL_PT, es:BANNER_BANNER8_URL_ES, ar:BANNER_BANNER8_URL_AR ]]
    }


    public void setUpWithoutLocaleSpecificBanner8URL() {
        Holders?.config?.mep?.banner8?.SS?.url = [GVU: GVU_BANNER8_URL_DEFAULT, BANNER: BANNER_BANNER8_URL_DEFAULT]
        Holders?.config?.banner8?.SS?.url = BANNER8_URL_DEFAULT
    }


    public void tearDownBanner8LocaleSpecificURL() {
        Holders?.config?.banner8.SS.locale?.url = [:]
        Holders?.config?.mep?.banner8?.SS?.locale?.url = [:]
    }


    public void tearDownBanner8URL() {
        Holders?.config?.mep?.banner8?.SS?.url = [:]
        Holders?.config?.banner8?.SS?.url = null
    }


    @After
    public void tearDown() {
        super.tearDown()
        removeMepCode()
        tearDownBanner8URL()
        tearDownBanner8LocaleSpecificURL()
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


    private getPidm(String bannerId) {
        def pidm
        sql.eachRow("select spriden_pidm from spriden where spriden_id = ? and spriden_change_ind is null", [bannerId]) {
            pidm = it.spriden_pidm
        }
        return pidm
    }


    @Test
    void testMepGVUBanner8EnglishLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(EN))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_EN, url)
    }


    @Test
    void testMepGVUBanner8URL() {
        setUpWithoutLocaleSpecificBanner8URL()
        setMepCode(MEP_GVU)
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testMepGVUBanner8EnglishGBLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(EN, GB))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_ENGB, url)
    }


    @Test
    void testMepGVUBanner8EnglishINLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(EN, IN))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_ENIN, url)
    }


    @Test
    void testMepGVUBanner8EnglishIELocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(EN, IE))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_ENIE, url)
    }


    @Test
    void testMepGVUBanner8EnglishAULocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(EN, AU))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_ENAU, url)
    }


    @Test
    void testMepGVUBanner8SpanishLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(ES))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_ES, url)
    }


    @Test
    void testMepGVUBanner8FrenchLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(FR))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_FR, url)
    }


    @Test
    void testMepGVUBanner8FrenchCanadaLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(FR, CA))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_FRCA, url)
    }


    @Test
    void testMepGVUBanner8PortugueseLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(PT))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_PT, url)
    }


    @Test
    void testMepGVUBanner8ArabicLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(AR))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_AR, url)
    }


    @Test
    void testMepGVUBanner8SaudiArabicLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(AR, SA))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_AR, url)
    }


    @Test
    void testMepGVUBanner8FrenchSwitzerlandLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(FR, CH))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_FR, url)
    }


    @Test
    void testMepGVUBanner8RomaniaLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(RO, MD))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testMepGVUBanner8ChineseLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_GVU)
        LocaleContextHolder.setLocale(new Locale(ZH))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(GVU_BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testMepBanner8EnglishLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(EN))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_EN, url)
    }


    @Test
    void testMepBanner8URL() {
        setUpWithoutLocaleSpecificBanner8URL()
        setMepCode(MEP_BANNER)
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testMepBanner8EnglishGBLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(EN, GB))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_ENGB, url)
    }


    @Test
    void testMepBanner8EnglishINLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(EN, IN))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_ENIN, url)
    }


    @Test
    void testMepBanner8EnglishIELocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(EN, IE))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_ENIE, url)
    }


    @Test
    void testMepBanner8EnglishAULocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(EN, AU))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_ENAU, url)
    }


    @Test
    void testMepBanner8SpanishLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(ES))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_ES, url)
    }


    @Test
    void testMepBanner8FrenchLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(FR))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_FR, url)
    }


    @Test
    void testMepBanner8FrenchCanadaLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(FR, CA))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_FRCA, url)
    }


    @Test
    void testMepBanner8PortugueseLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(PT))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_PT, url)
    }


    @Test
    void testMepBanner8ArabicLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(AR))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_AR, url)
    }


    @Test
    void testMepBanner8SaudiArabicLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(AR, SA))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_AR, url)
    }


    @Test
    void testMepBanner8FrenchSwitzerlandLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(FR, CH))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_FR, url)
    }


    @Test
    void testMepBanner8RomaniaLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(RO, MD))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testMepBanner8ChineseLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        setMepCode(MEP_BANNER)
        LocaleContextHolder.setLocale(new Locale(ZH))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER_BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testBanner8EnglishLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(EN))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_EN, url)
    }


    @Test
    void testBanner8URL() {
        setUpWithoutLocaleSpecificBanner8URL()
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testBanner8EnglishGBLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(EN, GB))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_ENGB, url)
    }


    @Test
    void testBanner8EnglishINLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(EN, IN))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_ENIN, url)
    }


    @Test
    void testBanner8EnglishIELocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(EN, IE))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_ENIE, url)
    }


    @Test
    void testBanner8EnglishAULocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(EN, AU))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_ENAU, url)
    }


    @Test
    void testBanner8SpanishLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(ES))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_ES, url)
    }


    @Test
    void testBanner8FrenchLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(FR))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_FR, url)
    }


    @Test
    void testBanner8FrenchCanadaLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(FR, CA))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_FRCA, url)
    }


    @Test
    void testBanner8PortugueseLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(PT))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_PT, url)
    }


    @Test
    void testBanner8ArabicLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(AR))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_AR, url)
    }


    @Test
    void testBanner8SaudiArabicLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(AR, SA))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_AR, url)
    }


    @Test
    void testBanner8FrenchSwitzerlandLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(FR, CH))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_FR, url)
    }


    @Test
    void testBanner8RomaniaLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(RO, MD))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_DEFAULT, url)
    }


    @Test
    void testBanner8ChineseLocaleURL() {
        setUpBanner8LocaleSpecificURL()
        LocaleContextHolder.setLocale(new Locale(ZH))
        String url = selfServiceMenuService.getBanner8SsUrlFromConfig()
        assertEquals(BANNER8_URL_DEFAULT, url)
    }


    private setMepCode(mepCode) {
        RequestContextHolder.currentRequestAttributes()?.request?.session?.setAttribute("mep", mepCode)
    }

    private removeMepCode() {
        RequestContextHolder.currentRequestAttributes()?.request?.session?.removeAttribute("mep")
    }

}
