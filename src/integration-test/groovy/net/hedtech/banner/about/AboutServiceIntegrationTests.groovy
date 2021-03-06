/*******************************************************************************
 Copyright 2016-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class AboutServiceIntegrationTests extends BaseIntegrationTestCase {
    def aboutService
    def messageSource
    def grailsApplication
    def actualAppVersion

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        Holders.config.info.app.version= 9.34
        Holders.config.app.platform.version=9.34
        actualAppVersion=grailsApplication.metadata['info.app.version']
        grailsApplication.metadata['info.app.version']=9.34
        Holders.config.EnableLoginAudit='N'
    }

    @After
    public void tearDown() {
        super.tearDown()
        Holders.config.info.app.version=actualAppVersion
        Holders.config.app.platform.version=actualAppVersion
        grailsApplication.metadata['info.app.version']=actualAppVersion
        logout()
    }

    @Test
    public void getAboutSuccess(){
        def aboutData = aboutService.getAbout()
        assertEquals(MessageHelper.message("about.banner.title"),aboutData.get("api.title"))
        assertEquals(MessageHelper.message("about.banner.close"),aboutData.get("api.close"))
        String startyear = MessageHelper.message("default.copyright.startyear")
        String endyear = MessageHelper.message("default.copyright.endyear")
        def copyrightLegalNotice  = MessageHelper.message("default.copyright.message",startyear,endyear)
        assertEquals (copyrightLegalNotice,aboutData.get("about.banner.copyright"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright2"),aboutData.get("about.banner.copyrightLegalNotice"))
    }

    @Test
    public void checkPlatformVersionWithoutNecessaryRolesAndConfig(){
        loginSSB("HOSH00001", "111111")
        def aboutData = aboutService.getAbout()
        def applicationVersion = Holders.config.info.app.version
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        assertNull(aboutData.get("about.banner.platform.version"))
    }

    @Test
    public void checkPlatformVersionWithoutNecessaryRolesAndWithConfig(){
        loginSSB("HOSH00001", "111111")
        def aboutData = aboutService.getAbout()
        def applicationVersion = Holders.config.info.app.version
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        def platformVersion= Holders.config.app.platform.version
        assertNull(aboutData.get("about.banner.platform.version"))
    }

    @Test
    public void checkPlatformVersionWithNecessaryRolesAndWithoutConfig(){
        loginSSB("CBUNTE3", "111111")
        def aboutData = aboutService.getAbout()
        def applicationVersion = Holders.config.info.app.version
        println "================================================ "
        println "aboutData = "+ aboutData
        println "applicationVersion = "+ applicationVersion
        println "Holders.config.app.platform.version = "+ Holders.config.app.platform.version
        println "Holders.config.info.app.version = "+ Holders.config.info.app.version
        println "================================================ "
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        def platformVersion = ""
        assertNotEquals(MessageHelper.message("about.banner.platform.version") + " " + platformVersion, aboutData.get("about.banner.platform.version"))
    }

    @Test
    public void checkPlatformVersionWithNecessaryRolesAndConfig(){
        loginSSB("CBUNTE3", "111111")
        Holders.config.aboutInfoAccessRoles=["ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M"]
        def aboutData = aboutService.getAbout()
        def applicationVersion = Holders.config.info.app.version
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion, aboutData.get("about.banner.application.version"))
        def platformVersion= Holders.config.app.platform.version
        assertEquals(MessageHelper.message("about.banner.platform.version") + " " + platformVersion, aboutData.get("about.banner.platform.version"))
    }

    @Test
    void testFormatCamelCaseToEnglish() {
        assertEquals("Banner", aboutService.formatCamelCaseToEnglish("banner"))
    }

    @Test
    void testFormatCamelCaseToEnglishEmpty() {
        assertEquals("", aboutService.formatCamelCaseToEnglish(""))
    }

    @Test
    void testGetEllucianPrivacyNotice() {
        assertEquals(MessageHelper.message("net.hedtech.banner.ellucianPrivacyNotice"), aboutService.getEllucianPrivacyNotice())
    }

    @Test
    void testDefaultEllucianPrivacyNotice() {
        assertEquals('https://www.ellucian.com/privacy', aboutService.getEllucianPrivacyNoticeLink())
    }

    @Test
    void testUserDefinedEllucianPrivacyNotice() {
        Holders.config.banner.ellucianPrivacyNotice= 'https://www.google.com/'
        assertEquals(Holders.config.banner.ellucianPrivacyNotice, aboutService.getEllucianPrivacyNoticeLink())
    }
}
