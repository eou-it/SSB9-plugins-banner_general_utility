/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.             *
 ******************************************************************************/

package net.hedtech.banner.i18n


import grails.test.spock.IntegrationSpec
import grails.util.Holders
import org.junit.Test

class TextManagerServiceIntegrationSpec extends IntegrationSpec {

    def textManagerService

    def setup() {
        Holders.config.bannerSsbDataSource.username="general"
        Holders.config.bannerSsbDataSource.url="10.42.4.24:1521:BAN83"//"localhost:1521:BAN83"//
        Holders.config.bannerSsbDataSource.password="u_pick_it"
        textManagerService = new TextManagerService()
        textManagerService.createProjectForApp('UNITTEST', 'Integration Test i18n Core')
    }

    def cleanup() {
        textManagerService.deleteProjectforApp()
    }

    @Test
    void "test saving a source and target locales"() {
        given:
        def name = "integrationTest"
        def sourceProperties = new Properties()
        def sourceLocale = textManagerService.ROOT_LOCALE_APP
        sourceProperties.put("dummy.label1", "Dummy English text1")
        sourceProperties.put("dummy.label2", "Dummy English text2")
        def targetProperties = new Properties()
        def targetLocale = "frFR"
        targetProperties.put("dummy.label1", "Dummy French text1")
        targetProperties.put("dummy.label2", "Dummy French text2")
        when: "Saved OK"
        def sourceStatus = textManagerService.save(sourceProperties, name, sourceLocale, sourceLocale)
        def targetStatus = textManagerService.save(targetProperties, name, sourceLocale, targetLocale)
        def message = textManagerService.findMessage("dummy.label1",targetLocale)
        then: "Great"
        sourceStatus.error == null
        sourceStatus.count == 2
        targetStatus.error == null
        targetStatus.count == 2
        message !=null
    }

    //Test result of saving when the query part is implemented


}
