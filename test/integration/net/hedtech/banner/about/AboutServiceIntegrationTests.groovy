/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class AboutServiceIntegrationTests extends BaseIntegrationTestCase {
    def aboutService
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
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
        assertNotNull aboutData.get("about.banner.application.name")
        assertTrue((aboutService.getAbout().get("about.banner.application.name").toString().equalsIgnoreCase(grailsApplication.metadata['app.name'])) ||
                    aboutService.getAbout().get("about.banner.application.name").toString().contains(MessageHelper.message("about.application.name")) )
        assertTrue(aboutData.get("about.banner.application.version").toString().contains("Version"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright1"),aboutData.get("about.banner.copyright"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright2"),aboutData.get("about.banner.copyrightLegalNotice"))
    }
}
