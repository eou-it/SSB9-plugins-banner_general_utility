/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class ShortcutControllerIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new ShortcutController()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void readJsonData(){
        def fetchData = controller.data()
        assertEquals controller.response.status, 200
    }
}
