/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
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
        assertEquals(MessageHelper.message("default.copyright.startyear"+ "default.copyright.endyear"+ " " + "default.copyright.message"),aboutData.get("about.banner.copyright"))
        assertEquals(MessageHelper.message("default.copyright.startyear"+ "default.copyright.endyear"+ " " + "default.copyright.message"),aboutData.get("about.banner.copyright"))
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
