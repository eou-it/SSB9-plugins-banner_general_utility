/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

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
        assertEquals("About Banner",aboutService.getAbout().get("api.title"))
        assertEquals("banner_general_utility",aboutService.getAbout().get("about.banner.application.name"))
        assertTrue(aboutService.getAbout().get("about.banner.application.version").toString().contains("Version"))
        assertEquals("2017 Ellucian Company L.P. and its affiliates. All rights reserved.",aboutService.getAbout().get("about.banner.copyright"))
        assertTrue(aboutService.getAbout().get("about.banner.copyrightLegalNotice").toString().contains("This software contains confidential and proprietary information of Ellucian or its subsidiaries"))
    }
}
