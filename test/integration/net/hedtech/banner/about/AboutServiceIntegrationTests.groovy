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
        assertEquals(MessageHelper.message("about.banner.title"),aboutService.getAbout().get("api.title"))
        assertEquals(MessageHelper.message("about.banner.close"),aboutService.getAbout().get("api.close"))
        assertEquals("banner_general_utility",aboutService.getAbout().get("about.banner.application.name"))
        assertTrue(aboutService.getAbout().get("about.banner.application.version").toString().contains("Version"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright1"),aboutService.getAbout().get("about.banner.copyright"))
        assertEquals(MessageHelper.message("net.hedtech.banner.login.copyright2"),aboutService.getAbout().get("about.banner.copyrightLegalNotice"))
    }
}
