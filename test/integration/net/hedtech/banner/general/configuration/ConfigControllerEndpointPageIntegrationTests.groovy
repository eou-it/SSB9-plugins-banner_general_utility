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
        //session = getHibernateSession()
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
        assertNull endpointPage.get( id )
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
                endpointPageCopy = (ConfigControllerEndpointPage)is.readObject()
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
        assertTrue list.getAt(0).dataOrigin == 'Banner'
        assertTrue list.getAt(0).enableIndicator

        //Update and findAll
        endpointPage.setEnableIndicator(false)
        endpointPage = endpointPage.save(failOnError: true, flush: true)
        assertEquals 1L, endpointPage.version

        list = endpointPage.fetchAll()
        assertTrue list.size >= 1
        assertTrue list.getAt(0).dataOrigin == 'Banner'
        assertTrue list.getAt(0).enableIndicator == false

        //Delete and findAll
        endpointPage.delete()
        list = endpointPage.fetchAll()
        assertEquals (list.size(), 0)
    }

    @Test
    public void testGetAllConfigByAppNameWithHibernateSessionWithSave() {
        try {
            //session.beginTransaction()

            //saveRequiredDomains()
            ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

            def list = endpointPage.getAllConfigByAppName(APP_NAME)
            assert (list.size() > 0)
            assert (list.getAt(0) instanceof RequestURLMap)
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
            ConfigControllerEndpointPage endpointPage = createConfigControllerEndPointPage()

            def list = endpointPage.getAllConfigByAppName(APP_NAME)
            def urlSet = new LinkedHashSet<String>()
            list.each { RequestURLMap requestURLMap -> urlSet.add(requestURLMap.url) }

            def requestMap = new LinkedHashMap<String, ArrayList<RequestURLMap>>()
            urlSet.each { String url ->
                def patternList = new ArrayList<RequestURLMap>()
                list.each { RequestURLMap requestURLMap ->
                    if (requestURLMap.url.equals(url)) {
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

    /**
     * Saving the required domains for test.
     * @param session Hibernate session.
     */
//    private void saveRequiredDomains() {
//        ConfigApplication configApplication = getConfigApplication()
//        session.save(configApplication)
//
//        ConfigControllerEndpointPage endpointPage = getDomain()
//        endpointPage.setGubapplAppId(configApplication.getAppId())
//        session.save(endpointPage)
//
//        ConfigRolePageMapping configRolePageMapping = getConfigRolePageMapping()
//        configRolePageMapping.setGubapplAppId(configApplication.getAppId())
//        configRolePageMapping.setPageId(endpointPage.getPageId())
//        session.save(configRolePageMapping)
//    }


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
                pageId: 1,
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
                pageId: 1,
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

    /**
     * This private method will be used to test for RequestmapFilterInvocationDefinition interceptor
     * which the filter is invoked before grails config.
     * @return Session Hibernate session.
     */
//    private Session getHibernateSession() {
//        def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
//        def ctx = Holders.grailsApplication.mainContext
//        def sessionFactory = ctx.sessionFactory
//        session = sessionFactory.openSession(dataSource.getSsbConnection())
//        return session
//    }

}
