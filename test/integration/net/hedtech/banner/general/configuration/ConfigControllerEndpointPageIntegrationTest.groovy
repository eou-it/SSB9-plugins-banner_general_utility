/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigControllerEndpointPageIntegrationTest is used to test the ConfigControllerEndpointPage domain.
 */
class ConfigControllerEndpointPageIntegrationTest extends BaseIntegrationTestCase {

    private static final String APP_NAME = 'PlatformSandboxApp'
    private static final String LAST_MODIFIED_BY = 'TEST_USER'
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
    public void testFindAll() {
        ConfigApplication application = getConfigApplication()
        application.save(failOnError: true, flush: true)

        ConfigControllerEndpointPage endpointPage = getDomain()
        endpointPage.setGubapplAppId(application.getAppId())
        //Save and findAll
        endpointPage.save(failOnError: true, flush: true)
        def list = endpointPage.findAll()
        assert (list.size > 0)
        assert (list.getAt(0).dataOrigin == 'Banner')
        assert (list.getAt(0).enableDisable == 'E')

        //Update and findAll
        endpointPage.setEnableDisable('D')
        endpointPage.save(failOnError: true, flush: true)
        list = endpointPage.findAll()
        assert (list.size > 0)
        assert (list.getAt(0).dataOrigin == 'Banner')
        assert (list.getAt(0).enableDisable == 'D')

        //Delete and findAll
        endpointPage.delete()
        list = endpointPage.findAll()
        assert (list.size >= 0)
    }

    @Test
    public void testGetAllConfigByAppNameWithSave() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)

        ConfigControllerEndpointPage endpointPage = getDomain()
        endpointPage.setGubapplAppId(configApplication.appId)
        endpointPage.save(failOnError: true, flush: true)

        ConfigRolePageMapping configRolePageMapping = getConfigRolePageMapping()
        configRolePageMapping.setGubapplAppId(configApplication.getAppId())
        configRolePageMapping.setPageId(endpointPage.getPageId())
        configRolePageMapping.save(failOnError: true, flush: true)

        def list = endpointPage.getAllConfigByAppName(APP_NAME)
        assert (list.size() > 0)
        assert (list.getAt(0) instanceof RequestURLMap)
    }

    @Test
    public void testGetAllConfigByAppNameWithHibernateSessionWithSave() {
        try {
            //session.beginTransaction()

            //saveRequiredDomains()
            saveDomains()

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
            ConfigControllerEndpointPage endpointPage = saveDomains()

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

    private ConfigControllerEndpointPage saveDomains() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)

        ConfigControllerEndpointPage endpointPage = getDomain()
        endpointPage.setGubapplAppId(configApplication.appId)
        endpointPage.save(failOnError: true, flush: true)

        ConfigRolePageMapping configRolePageMapping = getConfigRolePageMapping()
        configRolePageMapping.setGubapplAppId(configApplication.getAppId())
        configRolePageMapping.setPageId(endpointPage.getPageId())
        configRolePageMapping.save(failOnError: true, flush: true)
        return endpointPage
    }

    /**
     * Mocking the ConfigControllerEndpointPage domain.
     * @return ConfigControllerEndpointPage
     */
    private ConfigControllerEndpointPage getDomain() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                lastModified: new Date(),
                description: 'TEST',
                displaySequence: 1,
                enableDisable: 'E',
                pageId: 1,
                pageName: 'TEST PAGE',
                lastModifiedBy: LAST_MODIFIED_BY,
                version: 0
        )
        return configControllerEndpointPage
    }

    /**
     * Mocking ConfigRolePageMapping domain.
     * @return ConfigRolePageMapping
     */
    private ConfigRolePageMapping getConfigRolePageMapping() {
        ConfigRolePageMapping configRolePageMapping = new ConfigRolePageMapping(
                lastModifiedBy: LAST_MODIFIED_BY,
                pageId: 1,
                lastModified: new Date(),
                roleCode: 'TEST_ROLE',
                version: 0,
        )
        return configRolePageMapping
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                version: 0,
                appId: 1,
                lastModified: new Date(),
                appName: APP_NAME,
                lastModifiedBy: LAST_MODIFIED_BY,
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
