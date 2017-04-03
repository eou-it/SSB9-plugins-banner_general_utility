/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import net.hedtech.banner.testing.BaseIntegrationTestCase
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
        def releasePropertiesFile = new File( "target/classes/release.properties" )
        assertNotNull(aboutService.getAbout())
        releasePropertiesFile.delete()
    }

    @Test
    public void testGetApplicationName() {
        assertNotNull(aboutService.getApplicationName())
    }

    @Test
    public void testGetCopyright() {
        assertNotNull(aboutService.getCopyright())
    }

    @Test
    public void testGetCopyrightLegalNotice() {
        assertNotNull(aboutService.getCopyrightLegalNotice())
    }

    @Test
    public void testGetUserName() {
        assertNotNull(aboutService.getUserName())
    }

    @Test
    public void testformatCamelCaseToEnglish() {
        def value = 'platformSandbox'
        assertNotNull(aboutService.formatCamelCaseToEnglish(value))
        assertNull(aboutService.formatCamelCaseToEnglish())

    }

    @Test
    public void testGetMepDescription() {
        assertNull(aboutService.getMepDescription())
    }

    @Test
    public void testGetAppInfo() {
        assertNotNull(aboutService.getAppInfo())
    }
    @Test
    public void testGetDbInstanceName() {
        assertNull(aboutService.getDbInstanceName())
    }


    @Test
    public void testGetPluginsInfo() {
        def pattern
        assertNotNull(aboutService.getPluginsInfo(pattern))
    }
}
