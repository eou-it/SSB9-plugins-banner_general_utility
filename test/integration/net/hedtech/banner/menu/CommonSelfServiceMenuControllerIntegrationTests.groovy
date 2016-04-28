package net.hedtech.banner.menu

import grails.converters.JSON
import grails.spring.BeanBuilder
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.core.context.SecurityContextHolder

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

class CommonSelfServiceMenuControllerIntegrationTests extends BaseIntegrationTestCase {

    def renderMap
    def grailsApplication
    def dataSource
    def conn
    Sql sqlObj

    @Before
    public void setUp() {

        formContext = ['SELFSERVICE']

        Holders.config.ssbEnabled = true
        Holders.config.banner.sso.authenticationProvider="default";

        if(!dataSource.underlyingSsbDataSource){
            ApplicationContext testSpringContext = createUnderlyingSsbDataSourceBean()
            dataSource.underlyingSsbDataSource =  testSpringContext.getBean("underlyingSsbDataSource")
        }
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql( conn )

        controller = new CommonSelfServiceMenuController()

        CommonSelfServiceMenuController.metaClass.render = { Map map ->
            renderMap = map
        }
        super.SSBSetUp("HOSWEB002","111111");
    }


    @After
    public void tearDown() {
        super.tearDown();
        deletetwgrmenuEntry();
        Holders.config.ssbEnabled = false
        SecurityContextHolder.getContext().setAuthentication( null )

    }

    @Test
    void testSearchSingleCharacter() {
        dataSetup(false)

        controller.request.parameters = [q: 'B']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)
        assertNull result.items[0]

    }

    @Test
    void testSearchPromiseFind() {
        dataSetup(false)

        def ss = ["http://testhost:8080/StudentRegistrationSsb/ssb/registration", "http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss

        controller.request.parameters = [q: 'http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url= result.items[0].url;
        assertThat(url, containsString("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"));

    }

    @Test
    void testSearchFromUI() {
        dataSetup(false)

        def ss = ["http://testhost:8080/StudentRegistrationSsb/ssb/registration", "http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss

        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url= result.items[0].url;
        String parent=result.items[0].parent;
        String name=result.items[0].name;

        assertThat(url, containsString("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"));
        assertThat(parent, containsString("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"));
        assertThat(name, containsString("FACULTY"));

    }


    @Test
    void testSetHideSSBHeaderCompsParamWithMepCode() {
        dataSetup(true)

        def ss = [ "http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?mepCode=Banner"]
        grailsApplication.config.seamless.selfServiceApps = ss

        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url= result.items[0].url;
        assertEquals("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?mepCode=Banner&hideSSBHeaderComps=true",url );

    }

    @Test
    void testSetHideSSBHeaderCompsParamWithoutMepCode() {
        dataSetup(false)

        def ss = [ "http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry"]
        grailsApplication.config.seamless.selfServiceApps = ss


        controller.request.parameters = [q: 'Faculty', ui: 'true']
        controller.searchAppConcept()

        assertEquals controller.response.status, 200
        def result = JSON.parse(controller.response.contentAsString)

        String url= result.items[0].url;
        assertEquals("http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?hideSSBHeaderComps=true",url );

    }


    private def dataSetup(boolean mepCode) {
        try {
            sqlObj.executeInsert("INSERT INTO twgbwmnu (TWGBWMNU_NAME,TWGBWMNU_DESC,TWGBWMNU_PAGE_TITLE,TWGBWMNU_HEADER,TWGBWMNU_BACK_MENU_IND,TWGBWMNU_MODULE,TWGBWMNU_ENABLED_IND," +
                            "TWGBWMNU_INSECURE_ALLOWED_IND,TWGBWMNU_ACTIVITY_DATE,TWGBWMNU_CACHE_OVERRIDE,TWGBWMNU_SOURCE_IND,TWGBWMNU_ADM_ACCESS_IND) VALUES " +
                            "('bmenu.P_MainMnu','Faculty','Faculty','Faculty','N','WTL','Y','N'," +
                            "TO_TIMESTAMP('13-AUG-02','DD-MON-RR HH.MI.SSXFF AM'),'S','L','N')");
            if(mepCode){
                sqlObj.executeInsert("insert into TWGRMENU (TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL,TWGRMENU_DB_LINK_IND,TWGRMENU_SUBMENU_IND,TWGRMENU_ACTIVITY_DATE,TWGRMENU_SOURCE_IND,TWGRMENU_ENABLED)\n" +
                        "values ('bmenu.P_MainMnu',99,'Faculty','http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry?mepCode=Banner','Y','N',sysdate,'L','Y')")

            }else{
                sqlObj.executeInsert("insert into TWGRMENU (TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL,TWGRMENU_DB_LINK_IND,TWGRMENU_SUBMENU_IND,TWGRMENU_ACTIVITY_DATE,TWGRMENU_SOURCE_IND,TWGRMENU_ENABLED)\n" +
                        "values ('bmenu.P_MainMnu',99,'Faculty','http://testhost:8080/StudentFacultyGradeEntry/ssb/gradeEntry','Y','N',sysdate,'L','Y')")
            }
            sqlObj.commit();
        } finally {
        }

    }

    private def deletetwgrmenuEntry() {
        try {
            sqlObj.execute("delete from  twgrmenu where TWGRMENU_SEQUENCE=99");
            sqlObj.execute("delete from twgbwmnu where TWGBWMNU_NAME='bmenu.P_MainMnu' and TWGBWMNU_SOURCE_IND='L'");
            sqlObj.commit();
        } finally {
            sqlObj?.close()
        }

    }

    private ApplicationContext createUnderlyingSsbDataSourceBean() {
        def bb = new BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${Holders.config.bannerSsbDataSource.driver}"
                url = "${Holders.config.bannerSsbDataSource.url}"
                password = "${Holders.config.bannerSsbDataSource.password}"
                username = "${Holders.config.bannerSsbDataSource.username}"
            }
        }
        ApplicationContext testSpringContext = bb.createApplicationContext()
        return testSpringContext
    }
}

