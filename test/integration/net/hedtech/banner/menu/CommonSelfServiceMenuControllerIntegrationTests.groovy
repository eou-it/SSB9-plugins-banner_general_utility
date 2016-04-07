package net.hedtech.banner.menu

import groovy.sql.Sql
import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.converters.JSON
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class CommonSelfServiceMenuControllerIntegrationTests extends BaseIntegrationTestCase {

    def renderMap
    def selfServiceBannerAuthenticationProvider
    def grailsApplication

    @Before
    public void setUp() {

        formContext = ['SELFSERVICE']

        controller = new CommonSelfServiceMenuController()

        CommonSelfServiceMenuController.metaClass.render = { Map map ->
            renderMap = map
        }

        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken('HOSWEB002', '111111'))
        SecurityContextHolder.getContext().setAuthentication(auth)

        dataSetup()
    }


    @After
    public void tearDown() {
        super.tearDown()
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

        def ss = ["http://localhost:8080/StudentRegistrationSsb/ssb/registration", "http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss


        controller.request.parameters = [q: 'http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        assertEquals "http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry", result.items[0].url

    }

    @Test
    void testSearchFromUI() {

        def ss = ["http://localhost:8080/StudentRegistrationSsb/ssb/registration", "http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss


        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        assertEquals "http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry", result.items[0].url
        assertEquals "http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry", result.items[0].parent
        assertEquals "FACULTY", result.items[0].name

    }


    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {

            sql.executeInsert("insert into TWGRMENU (TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL,TWGRMENU_DB_LINK_IND,TWGRMENU_SUBMENU_IND,TWGRMENU_ACTIVITY_DATE,TWGRMENU_SOURCE_IND,TWGRMENU_ENABLED)\n" +
                    "values ('bmenu.P_MainMnu',99,'Faculty','http://localhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry','Y','N',sysdate,'L','Y')")

        } finally {
            sql?.close()
        }

    }
}

