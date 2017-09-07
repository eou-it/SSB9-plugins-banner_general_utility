/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


class StubbyTextManagerService {
    public final static String MOCK_PREFIX = "MOCK "
    def findMessage(key, locale) {
        MOCK_PREFIX + "${locale}-${key})"
    }
}

class BannerMessageSourceIntegrationTests extends BaseIntegrationTestCase {

    def messageSource
    def externalLocation = 'target/i18n'


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
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

    @After
    public void tearDown() {
        super.tearDown()
        def subDir = new File(externalLocation)
        subDir.deleteDir()
    }

    @Test
    void testMessageSource() {
        def names = messageSource.getNormalizedNames()
        def properties = []
        names.each { name ->
            properties << messageSource.getPropertiesByNormalizedName(name, new Locale('en'))
        }
        assert names.size > 0
        assert properties.size  > 0
    }

    void "test getMergedPluginProperties"() {
        when:
        def properties = messageSource.getMergedPluginProperties(new Locale('en')).properties
        def count = properties.size()

        then:
        count > 0
        count == properties.count( { _, value -> value.startsWith( StubbyTextManagerService.MOCK_PREFIX )})
    }

}
