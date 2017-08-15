/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n

import grails.test.spock.IntegrationSpec


class StubbyTextManagerService {
    public final static String MOCK_PREFIX = "MOCK "
    def findMessage(key, locale) {
        MOCK_PREFIX + "${locale}-${key})"
    }
}

class BannerMessageSourceIntegrationSpec extends IntegrationSpec {

    def messageSource
    def externalLocation = 'target/i18n'

    def setup() {
        def subDir = new File(externalLocation)
        subDir.mkdirs()
        new File(externalLocation+"/test.properties").write("key = Text")
        new File(externalLocation+"/test_fr.properties").write("key = Fr Text")
        //Set up the externalMessageSource
        def externalMessageSource = new ExternalMessageSource(
                externalLocation, 'integrationTest',
                "Setting up external message for integration test")
        messageSource?.setExternalMessageSource(externalMessageSource)

        messageSource?.textManagerService = new StubbyTextManagerService()
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

    void "test getAllProperties"() {
        when:
        def properties = messageSource.getAllProperties(new Locale('en')).properties

        then:
        properties.size()
        0 == properties.count( { _, value -> !value.startsWith( StubbyTextManagerService.MOCK_PREFIX )})
    }

}
