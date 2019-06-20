/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
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

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        //aboutService = new AboutService()
        //aboutService.messageSource = messageSource
        Holders.config.app.platform.version="9.32"
        Holders.config.info.app.version="9.32"
        Holders.config.EnableLoginAudit='N'
    }

    @After
    public void tearDown() {
        super.tearDown()
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
    public void checkPlatformVersionWithNecessaryRoles(){
        loginSSB("CBUNTE3", "111111")
        def aboutData = aboutService.getAbout()
        String applicationVersion = Holders.config.info.app.version
        String platformVersion = Holders.config.app.platform.version
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        assertEquals(MessageHelper.message("about.banner.platform.version") + " " + platformVersion,aboutData.get("about.banner.platform.version"))
    }

    @Test
    public void checkPlatformVersionWithoutNecessaryRoles(){
        loginSSB("HOSH00001", "111111")
        def aboutData = aboutService.getAbout()
        String applicationVersion = Holders.config.info.app.version
        assertEquals(MessageHelper.message("about.banner.application.version") + " " + applicationVersion,aboutData.get("about.banner.application.version"))
        assertNull(aboutData.get("about.banner.platform.version"))
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
