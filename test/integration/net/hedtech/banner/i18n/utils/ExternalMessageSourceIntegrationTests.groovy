/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n.utils

import grails.test.spock.IntegrationSpec
import net.hedtech.banner.i18n.ExternalMessageSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExternalMessageSourceIntegrationTests extends BaseIntegrationTestCase {

    def messageSource
    def externalLocation = 'target/i18n'
    def externalMessageSource

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
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

    @Test
    void testAddBaseName(){
        def exceptionNotThrown = true
        try {
            externalMessageSource.addBasename("Dummy French text1")
        } catch (Exception e) {
            exceptionNotThrown = false
        }
        assert exceptionNotThrown
    }

}
