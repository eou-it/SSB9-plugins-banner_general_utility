/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.converters.JSON
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserPreferenceControllerIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new UserPreferenceController()
        UserPreferenceController.metaClass.render = { Map map ->
            renderMap = map
        }
    }


    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }


    @Test
    void testUserLocale() {
        loginSSB('HOSH00001', '111111')
        controller.fetchUserLocale()
        def result = JSON.parse(controller.response.contentAsString)
        assertEquals(200, controller.response.status)
        assertNotNull result
        assertNotNull result.userLocale
        assertEquals 'ar_SA', result.userLocale
    }


    @Test
    void testLocales() {
        loginSSB('HOSH00001', '111111')
        controller.locales()
        def result = JSON.parse(controller.response.contentAsString)
        assertEquals(200, controller.response.status)
        assertNotNull result
        assertNotNull result.selectedLocale
        assertNotNull result.locales
        assertTrue(result.locales.size() > 0)

    }

    @Test
    void testSaveLocale() {
        loginSSB('HOFH00010', '111111')
        controller.request.contentType = "application/json"
        params = [
                locale                 : "EN_AU"

        ]
        controller.request.parameters = params
        //controller.params.locale = 'EN_AU'
        controller.saveLocale()
        def result = JSON.parse(controller.response.contentAsString)
        assertEquals(200, controller.response.status)
        assertNotNull result.status
    }

}
