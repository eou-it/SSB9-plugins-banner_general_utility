/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
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
import org.springframework.context.i18n.LocaleContextHolder

@Integration
@Rollback
class AboutServiceIntegrationTests extends BaseIntegrationTestCase {
    def aboutService
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        app.platform.version="9.32"
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void getAboutSuccess(){
        def aboutData = aboutService.getAbout()
        println aboutData
        assertEquals(MessageHelper.message("about.banner.title"),aboutData.get("api.title"))
        assertEquals(MessageHelper.message("about.banner.close"),aboutData.get("api.close"))
        String startyear = MessageHelper.message("default.copyright.startyear")
        String endyear = MessageHelper.message("default.copyright.endyear")
        def copyrightLegalNotice  = MessageHelper.message("default.copyright.message",startyear,endyear)
        assertEquals (copyrightLegalNotice,aboutData.get("about.banner.copyright"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright2"),aboutData.get("about.banner.copyrightLegalNotice"))
    }

    @Test
    public void getAboutSuccessWithNecessaryRoles(){
        loginSSB("CBUNTE3", "111111")
        def aboutData = aboutService.getAbout()
        println aboutData
        logout()
        String applicationVersion = Holders.config.info.app.version
        String platformVersion = Holders.config.app.platform.version
        assertEquals(MessageHelper.message("about.banner.title"),aboutData.get("api.title"))
        assertEquals(MessageHelper.message("about.banner.close"),aboutData.get("api.close"))
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        assertEquals(MessageHelper.message("about.banner.platform.version") + " " + platformVersion,aboutData.get("about.banner.platform.version"))
        def copyrightLegalNotice = MessageHelper.message("default.copyright.startyear")
        copyrightLegalNotice+=MessageHelper.message("default.copyright.endyear")
        copyrightLegalNotice += ' ' + MessageHelper.message("default.copyright.message")
        assertEquals (copyrightLegalNotice,aboutData.get("about.banner.copyright"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright2"),aboutData.get("about.banner.copyrightLegalNotice"))
    }

    @Test
    public void getAboutSuccessWithoutNecessaryRoles(){
        loginSSB("HOSH00001", "111111")
        def aboutData = aboutService.getAbout()
        println aboutData
        logout()
        String applicationVersion = Holders.config.info.app.version
        assertEquals(MessageHelper.message("about.banner.title"),aboutData.get("api.title"))
        assertEquals(MessageHelper.message("about.banner.close"),aboutData.get("api.close"))
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        assertNull(aboutData.get("about.banner.platform.version"))
        def copyrightLegalNotice = MessageHelper.message("default.copyright.startyear")
        copyrightLegalNotice+=MessageHelper.message("default.copyright.endyear")
        copyrightLegalNotice += ' ' + MessageHelper.message("default.copyright.message")
        assertEquals (copyrightLegalNotice,aboutData.get("about.banner.copyright"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright2"),aboutData.get("about.banner.copyrightLegalNotice"))
    }

    @Test
    void testFormatCamelCaseToEnglish() {
        assertEquals("Banner", aboutService.formatCamelCaseToEnglish("banner"))
    }

    @Test
    void testFormatCamelCaseToEnglishEmpty() {
        assertEquals("", aboutService.formatCamelCaseToEnglish(""))
    }
}
