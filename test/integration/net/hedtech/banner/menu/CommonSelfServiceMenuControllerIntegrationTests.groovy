package net.hedtech.banner.menu

import grails.converters.JSON
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString

class CommonSelfServiceMenuControllerIntegrationTests extends BaseIntegrationTestCase {

    def renderMap
    def grailsApplication
    def dataSource
    def conn

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        Holders.config.ssbEnabled = true
        Holders.config.banner.sso.authenticationProvider = "default";

        controller = new CommonSelfServiceMenuController()
        CommonSelfServiceMenuController.metaClass.render = { Map map ->
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
    void testSearchSingleCharacter() {
        controller.request.parameters = [q: 'B']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)
        assertNull result.items[0]
    }

    @Test
    void testSearchPromiseFind() {
        def ss = ["http://testhost:8080/StudentRegistrationSsb/ssb/registration", "http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss

        controller.request.parameters = [q: 'http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url = result.items[0].url;
        assertThat(url, containsString("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"));
    }

    @Test
    void testSearchFromUI() {
        def ss = ["http://testhost:8080/StudentRegistrationSsb/ssb/registration", "http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss

        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url = result.items[0].url;
        String parent = result.items[0].parent;
        String name = result.items[0].name;

        assertThat(url, containsString("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"));
        assertThat(parent, containsString("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"));
        assertThat(name, containsString("FACULTY"));
    }


    @Test
    void testSetHideSSBHeaderCompsParamWithMepCode() {
        def ss = ["http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?mepCode=Banner"]
        grailsApplication.config.seamless.selfServiceApps = ss

        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url = result.items[0].url;
        assertEquals("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?mepCode=Banner&hideSSBHeaderComps=true", url);
    }

    @Test
    void testSetHideSSBHeaderCompsParamWithoutMepCode() {
        def ss = ["http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss


        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url = result.items[0].url;
        assertEquals("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?hideSSBHeaderComps=true", url);
    }

}

