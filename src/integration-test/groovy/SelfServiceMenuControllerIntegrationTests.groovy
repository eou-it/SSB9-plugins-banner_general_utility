/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.menu.SelfServiceMenuService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired


@Integration
@Rollback
class SelfServiceMenuControllerIntegrationTests extends BaseIntegrationTestCase{
    def controller
    def menuName
    def menu
    def pidm
    def selfServiceMenuService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        Holders.config.ssbEnabled = true
        Holders.config.banner.sso.authenticationProvider = "default";
        controller = new SelfServiceMenuController()
        controller.selfServiceMenuService = selfServiceMenuService
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
