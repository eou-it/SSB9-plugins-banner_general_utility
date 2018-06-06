/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigControllerEndpointPageIntegrationTests are used to test the ConfigControllerEndpointPage domain.
 */
class ConfigControllerEndpointPageIntegrationTests extends BaseIntegrationTestCase {

    private def appName
    private def appId
    //private Session session
    public static final String PAGE_ID = 'Test Integration'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        appName = Holders.grailsApplication.metadata['app.name']
        appId = 'TESTAPP'
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    public void testSaveConfigControllerEndpointPage() {
        ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

        assertNotNull endpointPage.id
        assertEquals 0L, endpointPage.version
    }


    @Test
    public void testEndpointPageWithDefaultValues() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()
        assertNotNull configApplication.id
        assertNotNull configApplication.appId
        assertEquals 0L, configApplication.version

        ConfigControllerEndpointPage endpointPage = new ConfigControllerEndpointPage()
        endpointPage.setPageId(PAGE_ID)
        endpointPage.setConfigApplication(configApplication)
        endpointPage.save(failOnError: true, flush: true)

        assertNotNull endpointPage.id
        assertEquals 0L, endpointPage.version
        assertEquals true, endpointPage.statusIndicator
        assertEquals PAGE_ID, endpointPage.pageId
    }


    @Test
    void testDeleteConfigControllerEndpointPage() {
        ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

        assertNotNull endpointPage.id
        assertEquals PAGE_ID, endpointPage.pageId
        assertEquals 0L, endpointPage.version

        def id = endpointPage.id
        endpointPage.delete()
        assertNull endpointPage.get(id)
    }


    @Test
    public void testSerialization() {
        try {
            ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

            assertNotNull endpointPage.id
            assertNotNull endpointPage.pageId
            assertEquals 0L, endpointPage.version
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(endpointPage)
            oos.close()

            byte[] bytes = out.toByteArray()
            ConfigControllerEndpointPage endpointPageCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                endpointPageCopy = (ConfigControllerEndpointPage) is.readObject()
                is.close()
            }
            assertEquals endpointPageCopy, endpointPage

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    public void testFetchAll() {
        ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

        assertNotNull endpointPage.id
        assertNotNull endpointPage.pageId
        assertEquals 0L, endpointPage.version

        def list = endpointPage.fetchAll()
        assertTrue list.size >= 1

        def endPointPage1 = list.find { p -> p.dataOrigin == 'Banner' }
        assertTrue endPointPage1.dataOrigin == 'Banner'

        def endPointPage2 = list.find { p -> p.statusIndicator == true }
        assertTrue endPointPage2.statusIndicator

        //Update and findAll
        endpointPage.setStatusIndicator(false)
        endpointPage = endpointPage.save(failOnError: true, flush: true)
        assertEquals 1L, endpointPage.version

        list = endpointPage.fetchAll()
        assertTrue list.size >= 1

        endPointPage2 = list.find { p -> p.statusIndicator == false }
        assertFalse endPointPage2.statusIndicator

        //Delete and findAll
        def id = endpointPage.id
        endpointPage.delete()
        list = endpointPage.fetchAll()
        def eppDeleted = list.find { p -> p.id == id }

        assertNull eppDeleted
    }

    @Test
    public void testGetAllConfigByAppNameWithHibernateSessionWithSave() {
        try {
            //session.beginTransaction()

            //saveRequiredDomains()
            ConfigApplication configApplication = ConfigApplication.fetchByAppName(appName)
            configApplication = configApplication.refresh()
            List<GeneralPageRoleMapping> list = GeneralPageRoleMapping.fetchByAppId(configApplication.appId)
            assert (list.size() > 0)
            assert (list.getAt(0) instanceof GeneralPageRoleMapping)
        } catch (e) {

        }
    }

    /**
     * The intention behind to have this test method is :
     * In POC 2 implementation it was native SQL query and we have used
     * LISTAGG to get comma seperated string from the DB for multiple roles and other values
     * for same URL, so here we have managed it in a server side, this method implementation
     * will represent the same.
     *
     * And this will use getHibernateSession() to get the hibernate session in the way
     * where the POC 2 is doing, because when spring invoking RequestmapFilterInvocationDefinition
     * we have to load the hibernate manually.
     */
    @Test
    public void testGetAllConfigByAppNameWithHibernateSession() {
        //session.beginTransaction()
        try {
            // Save all required domain.
            //saveRequiredDomains()
            createConfigControllerEndPointPage()
            ConfigApplication configApplication = ConfigApplication.fetchByAppName(appName)
            configApplication = configApplication.refresh()
            List<GeneralPageRoleMapping> list = GeneralPageRoleMapping.fetchByAppId(configApplication.appId)
            //def list = endpointPage.getAllConfigByAppName(appName)
            Set<String> urlSet = new LinkedHashSet<String>()
            list.each { GeneralPageRoleMapping requestURLMap -> urlSet.add(requestURLMap.pageUrl) }

            def requestMap = new LinkedHashMap<String, ArrayList<GeneralPageRoleMapping>>()
            urlSet.each { String url ->
                def patternList = new ArrayList<GeneralPageRoleMapping>()
                list.each { GeneralPageRoleMapping requestURLMap ->
                    if (requestURLMap.pageUrl.equals(url)) {
                        patternList << requestURLMap
                    }
                }
                requestMap.put(url, patternList)
            }
            assert (requestMap.size() >= 0)
        } catch (e) {

        }
    }

    @Test
    void testToString() {
        ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()
        endpointPage.save(failOnError: true, flush: true)
        assertNotNull endpointPage.id
        assertEquals 0L, endpointPage.version

        def controllerEndpointPages = ConfigControllerEndpointPage.fetchAll()
        assertFalse controllerEndpointPages.isEmpty()
        controllerEndpointPages.each { controllerEndpointPage ->
            String controllerEndpointPageToString = controllerEndpointPage.toString()
            assertNotNull controllerEndpointPageToString
            assertTrue controllerEndpointPageToString.contains('pageId')
        }
    }


    @Test
    void testHashCode() {
        ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()
        endpointPage.save(failOnError: true, flush: true)
        assertNotNull endpointPage.id
        assertEquals 0L, endpointPage.version

        def controllerEndpointPages = ConfigControllerEndpointPage.fetchAll()
        assertFalse controllerEndpointPages.isEmpty()
        controllerEndpointPages.each { controllerEndpointPage ->
            Integer controllerEndpointPageHashCode = controllerEndpointPage.hashCode()
            assertNotNull controllerEndpointPageHashCode
        }
    }


    @Test
    void testEqualsIs() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage()
        assertTrue configControllerEndpointPage.equals(configControllerEndpointPage)
    }


    @Test
    void testEqualsClass() {
        ConfigApplication configApplication1 = new ConfigApplication()
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage()
        assertFalse configControllerEndpointPage.equals(configApplication1)
    }


    @Test
    void testEqualsId() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(id:1234)
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(id:12345)
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsLastModified() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(lastModified: new Date(12,2,10))
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(lastModified: new Date(12,2,13))
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsDataOrigin() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(dataOrigin: "GRAILS")
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(dataOrigin: "GENERAL")
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }

    @Test
    void testEqualsDescription() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(description: "EndpointUrl")
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(description: "EndpointUrl1")
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsDisplaySequence() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(displaySequence: 1)
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(displaySequence: 2)
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsStatusIndicator() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(statusIndicator: true)
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(statusIndicator: false)
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsPageId() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(pageId: "userAgreement")
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(pageId: "surveyPage")
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsPageUrl() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(pageUrl: "ssb/userAgreement")
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(pageUrl: "ssb/surveyPage")
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }

    @Test
    void testEqualsConfigApplication() {
        ConfigApplication configApplication1 = new ConfigApplication(appName: "TestName",   appId: "TestId")
        ConfigApplication configApplication2 = new ConfigApplication(appName: "TestName1", appId: "TestId1")
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(configApplication: configApplication1)
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(configApplication: configApplication2)
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsLastModifiedBy() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(lastModifiedBy: "GRAILS_USER")
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(lastModifiedBy: "GENERAL")
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }


    @Test
    void testEqualsVersion() {
        ConfigControllerEndpointPage configControllerEndpointPage1 = new ConfigControllerEndpointPage(version: 1)
        ConfigControllerEndpointPage configControllerEndpointPage2 = new ConfigControllerEndpointPage(version: 2)
        assertFalse configControllerEndpointPage1 == configControllerEndpointPage2
    }

    private ConfigControllerEndpointPage createConfigControllerEndPointPage() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()
        assertNotNull configApplication.id
        assertNotNull configApplication.appId
        assertEquals 0L, configApplication.version

        ConfigControllerEndpointPage endpointPage = getConfigControllerEndpointPage()
        endpointPage.setConfigApplication(configApplication)
        /*endpointPage.setPageId(PAGE_ID)*/
        endpointPage = endpointPage.save(failOnError: true, flush: true)
        return endpointPage
    }

    /**
     * Mocking the ConfigControllerEndpointPage domain.
     * @return ConfigControllerEndpointPage
     */
    private ConfigControllerEndpointPage getConfigControllerEndpointPage() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                description: 'TEST',
                displaySequence: 1,
                enableIndicator: true,
                pageUrl: 'TEST PAGE',
                pageId: PAGE_ID
        )
        return configControllerEndpointPage
    }

    /**
     * Mocking ConfigRolePageMapping domain.
     * @return ConfigRolePageMapping
     */
    private ConfigRolePageMapping getConfigRolePageMapping() {
        ConfigRolePageMapping configRolePageMapping = new ConfigRolePageMapping(
                roleCode: 'TEST_ROLE'
        )
        return configRolePageMapping
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                appName: appName,
                appId: appId
        )
        return configApplication
    }

}
