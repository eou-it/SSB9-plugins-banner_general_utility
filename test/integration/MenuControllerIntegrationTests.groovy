/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.converters.JSON
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class MenuControllerIntegrationTests extends BaseIntegrationTestCase{
    def controller
    def menu
    def menuType
    def seq = 0
    def menuName
    def pageName


    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        Holders.config.ssbEnabled = true
        Holders.config.banner.sso.authenticationProvider = "default";
        controller = new MenuController()
        MenuController.metaClass.render = { Map map ->
            renderMap = map
        }
        super.SSBSetUp("ESSREG02", "111111");
    }


    @After
    public void tearDown() {
        Holders.config.ssbEnabled = false;
        logout();
        super.tearDown();
    }
    @Test
    void testGetMenuWithType() {
        controller.request.parameters = [type: 'Banner']
        controller.data()
        assertEquals controller.response.status, 200
    }
    @Test
    void testGetMenuWithPageNameAndSeq() {
        controller.request.parameters = [pageName: 'Banner']
        controller.request.parameters = [seq: '0']
        controller.data()
        assertEquals controller.response.status, 200
        def result = controller.response.contentAsString
        assertNotNull(result)
    }
    @Test
    void testGetMenuWithPageName() {
        controller.request.parameters = [pageName: 'Banner']
        controller.data()
        assertEquals controller.response.status, 200
        def result = controller.response.contentAsString
        assertNotNull(result)
    }

    @Test
    void testGetMenuWithMenuNameAndSeq() {
        controller.request.parameters = [menuName: 'Banner']
        controller.request.parameters = [seq: '0']
        controller.data()
        assertEquals controller.response.status, 200
        def result = controller.response.contentAsString
        assertNotNull(result)
    }

    @Test
    void testGetMenuWithWithoutParameter() {
        controller.data()
        assertEquals controller.response.status, 200
        def result = controller.response.contentAsString
        assertNotNull(result)
    }

    @Test
    void testGetMenuMethod() {
        controller.getMenu()
        assertEquals controller.response.status, 200
        def result = controller.response.contentAsString
        assertNotNull(result)
    }
    @Test
    void testGetPersonalMenuMethod() {
        controller.getPersonalMenu()
        assertEquals controller.response.status, 200
    }
    @Test
    void testGetListMethodWithNull() {
        controller.getFirstList(null)
        assertEquals controller.response.status, 200
    }

    @Test
    void testGetListMethod() {
        controller.getFirstList(menuType)
        assertEquals controller.response.status, 200
    }

    @Test
    void testGetMenuListMethodWithNull() {
        controller.getMenuList(menuName,null,seq)
        assertEquals controller.response.status, 200
    }

    @Test
    void testGetMenuListMethod() {
        controller.getMenuList(menuName,menuType,seq)
        assertEquals controller.response.status, 200
    }

    @Test
    void testGetCrumbMethodWithNull() {
        controller.getCrumb(pageName,null,seq)
        assertEquals controller.response.status, 200
    }

    @Test
    void testGetCrumbMethod() {
        controller.getCrumb(pageName,menuType,seq)
        assertEquals controller.response.status, 200
    }


}
