/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n.utils

import grails.test.spock.IntegrationSpec
import net.hedtech.banner.i18n.ExternalMessageSource

class ExternalMessageSourceIntegrationSpec extends IntegrationSpec {

    def messageSource
    def externalLocation = 'target/i18n'
    def externalMessageSource

    def setup() {
        def subDir = new File(externalLocation)
        subDir.mkdirs()
        new File(externalLocation+"/test.properties").write("key = Text")
        new File(externalLocation+"/test_fr.properties").write("key = Fr Text")
        //Set up the externalMessageSource
        externalMessageSource = new ExternalMessageSource(
                externalLocation, 'integrationTest',
                "Setting up external message for integration test")
        messageSource?.setExternalMessageSource(externalMessageSource)
    }

    def cleanup() {
        def subDir = new File(externalLocation)
        subDir.deleteDir()
    }

    void "test message source"() {
        when:
        def names = messageSource.getNormalizedNames()
        def properties = []
        names.each { name ->
            properties << messageSource.getPropertiesByNormalizedName(name, new Locale('en'))
        }
        then:
        names.size > 0
        properties.size  > 0
    }


    void "test add baseName" (){
        given:
        when:
        externalMessageSource.addBasename("Dummy French text1")
        then:
        noExceptionThrown()
    }

}
