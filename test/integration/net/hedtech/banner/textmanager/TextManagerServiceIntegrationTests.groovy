/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class TextManagerServiceIntegrationTests extends BaseIntegrationTestCase {
    def textManagerService

    @Before
    public void setUp(){
        formContext= ['GUAGMNU']
        super.setUp()
        textManagerService.createProjectForApp('UNITTEST', 'Integration Test Banner General Utility')
    }

    @After
    public void cleanUp(){
        textManagerService.deleteProjectforApp()
    }

    @Test
    public void testSaveSuccess(){
        def name = "integrationTest"
        def srcProperties = new Properties()
        def srcLocale = textManagerService.ROOT_LOCALE_APP
        def tgtProperties = new Properties()
        def tgtLocale = "frFR"

        srcProperties.put("dummy.label1", "Dummy English text1")
        srcProperties.put("dummy.label2", "Dummy English text2")
        tgtProperties.put("dummy.label1", "Dummy French text1")
        tgtProperties.put("dummy.label2", "Dummy French text2")

        def srcStatus = textManagerService.save(srcProperties, name, srcLocale, srcLocale)
        def tgtStatus = textManagerService.save(tgtProperties, name, srcLocale, tgtLocale)
        def message = textManagerService.findMessage("dummy.label1",tgtLocale)

        assertNull(srcStatus.error)
        assertEquals(2, srcStatus.count)
        assertNull(tgtStatus.error)
        assertEquals(2, tgtStatus.count)
       // assertNotNull(message)
    }
}
