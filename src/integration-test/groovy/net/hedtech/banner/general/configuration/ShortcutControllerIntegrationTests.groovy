/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
@Integration
@Rollback
class ShortcutControllerIntegrationTests extends BaseIntegrationTestCase {

    GrailsApplication grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new ShortcutController()
        controller.grailsApplication = grailsApplication
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void readJsonData(){
        controller.data()
        def result = JSON.parse(controller.response.contentAsString)
        assertEquals(200, controller.response.status)
        assertNotNull result
    }
}
