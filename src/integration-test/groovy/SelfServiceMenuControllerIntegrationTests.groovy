/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class SelfServiceMenuControllerIntegrationTests extends BaseIntegrationTestCase{
    def controller
    def menuName
    def menu
    def pidm

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        Holders.config.ssbEnabled = true
        Holders.config.banner.sso.authenticationProvider = "default";
        controller = new SelfServiceMenuController()
        super.SSBSetUp("ESSREG02", "111111");
    }


    @After
    public void tearDown() {
        Holders.config.ssbEnabled = false;
        logout();
        super.tearDown();
    }

    @Test
    void testGetSelfServiceMenuWithMenuName() {
        controller.request.parameters = [menuName: 'menuName']
        controller.data()
        assertEquals controller.response.status, 200
     }

    @Test
    void testGetSelfServiceMenuWithMenu() {
        controller.request.parameters = [menu: 'menu']
        controller.data()
        assertEquals controller.response.status, 200
    }

    @Test
    void testGetMenuMethod() {
        controller.getMenu(menuName,menu,pidm)
        assertEquals controller.response.status, 200
    }

}
