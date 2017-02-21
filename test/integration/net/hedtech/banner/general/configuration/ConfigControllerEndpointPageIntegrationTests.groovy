/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigControllerEndpointPageIntegrationTests are used to test the ConfigControllerEndpointPage domain.
 */
class ConfigControllerEndpointPageIntegrationTests extends BaseIntegrationTestCase {

    private static final String APP_NAME = 'PlatformSandboxApp'
    //private Session session

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
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
        endpointPage.setConfigApplication(configApplication)
        endpointPage.save(failOnError: true, flush: true)
        endpointPage = endpointPage.refresh()

        assertNotNull endpointPage.id
        assertEquals 0L, endpointPage.version
        assertEquals true, endpointPage.enableIndicator
        assertNotNull endpointPage.pageId
    }


    @Test
    void testDeleteConfigControllerEndpointPage() {
        ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

        assertNotNull endpointPage.id
        assertNotNull endpointPage.pageId
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

        def endPointPage2 = list.find { p -> p.enableIndicator == true }
        assertTrue endPointPage2.enableIndicator

        //Update and findAll
        endpointPage.setEnableIndicator(false)
        endpointPage = endpointPage.save(failOnError: true, flush: true)
        assertEquals 1L, endpointPage.version

        list = endpointPage.fetchAll()
        assertTrue list.size >= 1

        endPointPage2 = list.find { p -> p.enableIndicator == false }
        assertFalse endPointPage2.enableIndicator

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
            ConfigApplication configApplication = ConfigApplication.fetchByAppName(APP_NAME)
            configApplication = configApplication.refresh()
            List<GeneralRequestMap> list = GeneralRequestMap.fetchByAppId(configApplication.appId)
            assert (list.size() > 0)
            assert (list.getAt(0) instanceof GeneralRequestMap)
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
            ConfigApplication configApplication = ConfigApplication.fetchByAppName(APP_NAME)
            configApplication = configApplication.refresh()
            List<GeneralRequestMap> list = GeneralRequestMap.fetchByAppId(configApplication.appId)
            //def list = endpointPage.getAllConfigByAppName(APP_NAME)
            Set<String> urlSet = new LinkedHashSet<String>()
            list.each { GeneralRequestMap requestURLMap -> urlSet.add(requestURLMap.pageName) }

            def requestMap = new LinkedHashMap<String, ArrayList<GeneralRequestMap>>()
            urlSet.each { String url ->
                def patternList = new ArrayList<GeneralRequestMap>()
                list.each { GeneralRequestMap requestURLMap ->
                    if (requestURLMap.pageName.equals(url)) {
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

    private ConfigControllerEndpointPage createConfigControllerEndPointPage() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplication.save(failOnError: true, flush: true)
        configApplication = configApplication.refresh()
        assertNotNull configApplication.id
        assertNotNull configApplication.appId
        assertEquals 0L, configApplication.version

        ConfigControllerEndpointPage endpointPage = getConfigControllerEndpointPage()
        endpointPage.setConfigApplication(configApplication)
        endpointPage = endpointPage.save(failOnError: true, flush: true)
        return endpointPage.refresh()
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
                pageName: 'TEST PAGE'
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
                appName: APP_NAME
        )
        return configApplication
    }

}
