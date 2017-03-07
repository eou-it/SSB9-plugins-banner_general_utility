/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import net.hedtech.banner.testing.BaseIntegrationTestCase
import java.lang.reflect.Method
import org.junit.After
import org.junit.Before
import org.junit.Test

class AboutServiceIntegrationTests extends BaseIntegrationTestCase {

    def aboutService = new AboutService()

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
        assertNotNull(aboutService.getAbout())
    }
}
