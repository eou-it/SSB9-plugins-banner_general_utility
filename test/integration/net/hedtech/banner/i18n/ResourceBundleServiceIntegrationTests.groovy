/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n

import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class ResourceBundleServiceIntegrationTests {

    def messageSource
    def resourceBundleService

    def testLocales = ['en_GB','fr_FR']
    def testLocalesSave =   [
            [
                "enabled": true,
                "code": "en_GB"
            ],
            [
                "enabled": true,
                "code": "fr_FR"
            ]
    ]

    @Test
    void testGetList() {
        def resources = resourceBundleService.list()
        assertTrue(resources.size() > 0)
    }

    @Test
    void testGetResourcesWithSingleLocale() {
      def resources = resourceBundleService.list()
      def resList =  []
      testLocales.each { locale ->
                resources.each { it ->
                    resList << resourceBundleService.get(it.basename, locale)
                }
            }
        assertTrue(resList.size()>0)
    }

    @Test
    void testSave() {
        def fr = resourceBundleService.get('testExternalResource/test', 'fr_FR')
        def resources = resourceBundleService.list()
        def data = resources[0]
        data.enableTranslation = true
        data.sourceLocale = 'root'
        data.locales = testLocalesSave
        def saveResult = resourceBundleService.save(data)
        assertTrue(saveResult.count > 0)
        assertEquals(fr.locale,"fr_FR")
    }

    @Test
    void testSaveWithLocale() {
        def ar = resourceBundleService.get('testExternalResource/test', 'ar_SA')
        def resources = resourceBundleService.list()
        def data = resources[0]
        data.enableTranslation = true
        data.sourceLocale = 'root'
        data.locales = testLocalesSave
        def saveResult = resourceBundleService.save(data)
        assertTrue(saveResult.count > 0)
        assertEquals(ar.locale,"ar_SA")
    }
}



