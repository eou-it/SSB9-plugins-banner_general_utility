/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/*
Integration Test cases for AboutController
 */
class AboutControllerIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new AboutController()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void dataSuccess(){
        controller.data()
        assertEquals controller.response.status, 200
    }
}
