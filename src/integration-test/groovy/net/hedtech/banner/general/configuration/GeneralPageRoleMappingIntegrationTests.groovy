/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


class GeneralPageRoleMappingIntegrationTests extends BaseIntegrationTestCase {

    private String appName
    private String appId


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
    public void testFetchAll() {
        saveDomains()

        def list = GeneralPageRoleMapping.fetchAll()
        assert list.size() >= 1

        def generalReqMapTestPageName = list.find { p -> p.pageUrl == 'TEST PAGE' }
        assertEquals generalReqMapTestPageName.applicationName, appName
        assertEquals generalReqMapTestPageName.pageUrl, 'TEST PAGE'
        assertEquals generalReqMapTestPageName.roleCode, 'TEST_ROLE'
    }


    @Test
    public void testFetchByAppId() {
        def configApplication = saveDomains()

        def list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(configApplication.appId, true)
        assert list.size() >= 1

        list.each { requestMap ->
            assertEquals configApplication.appId, requestMap.applicationId
            assertEquals appName, requestMap.applicationName
            assertEquals "TEST PAGE", requestMap.pageUrl
            assertEquals "TEST_ROLE", requestMap.roleCode
        }

    }

    @Test
    public void testFailCaseOnSave() {
        def generalRequestMap = getGeneralRequestMap()
        shouldFail(Exception) {
            generalRequestMap.save(failOnError: true, flush: true)
        }
    }

    @Test
    public void testFailCaseOnUpdate() {
        def configApplication = saveDomains()
        def list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(configApplication.appId, true)
        shouldFail(Exception) {
            def generalRequestMap = list.get(0)
            generalRequestMap.pageUrl = 'TEST_PAGE'
            generalRequestMap.save(failOnError: true, flush: true)
        }
    }

    @Test
    public void testFailCaseOnDelete() {
        def configApplication = saveDomains()
        def list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(configApplication.appId, true)
        shouldFail(Exception) {
            def generalRequestMap = list.get(0)
            generalRequestMap.delete(flush: true)
        }
    }

    @Test
    void testToString() {
        ConfigApplication configApp = saveDomains()
        List<ConfigApplication> generalRequestMap = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(configApplication.appId, true)
        assertFalse generalRequestMap.isEmpty()
        generalRequestMap.each { requestMap ->
            String requestMapToString = requestMap.toString()
            assertNotNull requestMapToString
        }
    }


    @Test
    void testHashCode() {
        ConfigApplication configApp = saveDomains()
        List<ConfigApplication> generalRequestMap = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(configApplication.appId, true)
        assertFalse generalRequestMap.isEmpty()
        generalRequestMap.each { requestMap ->
            Integer requestMapHashCode = requestMap.hashCode()
            assertNotNull requestMapHashCode
        }
    }

    @Test
    public void testEqualAndHashcode() {
        GeneralPageRoleMapping obj1 = getGeneralRequestMap()
        GeneralPageRoleMapping obj2 = getGeneralRequestMap()
        assertEquals obj1, obj2
        assertEquals obj1.roleCode, obj2.roleCode
        assertEquals obj1.pageUrl, obj2.pageUrl
        assertEquals obj1.displaySequence, obj2.displaySequence
        assertEquals obj1.applicationId, obj2.applicationId
        assertEquals obj1.applicationName, obj2.applicationName
        assertEquals obj1.pageId, obj2.pageId

        GeneralPageRoleMapping obj3 = getGeneralRequestMap()
        obj3.applicationId = 100002
        assertNotEquals obj2, obj3
        assertNotEquals obj1.hashCode(), obj3.hashCode()

        GeneralPageRoleMapping obj4 = getGeneralRequestMap()
        obj4.pageId = 1000002
        assertNotEquals obj2, obj4
        assertNotEquals obj1.hashCode(), obj4.hashCode()

        GeneralPageRoleMapping obj5 = getGeneralRequestMap()
        obj5.applicationName = 'TEST_APP_2'
        assertNotEquals obj2, obj5
        assertNotEquals obj1.hashCode(), obj5.hashCode()

        GeneralPageRoleMapping obj6 = getGeneralRequestMap()
        obj6.displaySequence = 2
        assertNotEquals obj2, obj6
        assertNotEquals obj1.hashCode(), obj6.hashCode()

        GeneralPageRoleMapping obj7 = getGeneralRequestMap()
        obj7.pageUrl = 'PAGE_NAME_TEST'
        assertNotEquals obj2, obj7
        assertNotEquals obj1.hashCode(), obj7.hashCode()

        GeneralPageRoleMapping obj8 = getGeneralRequestMap()
        obj8.roleCode = 'TEST_ROLE_1'
        assertNotEquals obj2, obj8
        assertNotEquals obj1.hashCode(), obj8.hashCode()

        GeneralPageRoleMapping obj9 = getGeneralRequestMap()
        obj9.version = 1
        assertNotEquals obj2, obj9
        assertNotEquals obj1.hashCode(), obj9.hashCode()

        assertEquals obj1.hashCode(), obj2.hashCode()

        def obj = new Object()
        assertNotEquals obj2, obj
    }

    @Test
    public void testSerialization() {
        try {
            GeneralPageRoleMapping generalRequestMap = getGeneralRequestMap()
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(generalRequestMap)
            oos.close()

            byte[] bytes = out.toByteArray()
            GeneralPageRoleMapping generalRequestMapCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                generalRequestMapCopy = (GeneralPageRoleMapping) is.readObject()
                is.close()
            }
            assertEquals generalRequestMapCopy, generalRequestMap

        } catch (e) {
            e.printStackTrace()
        }
    }


    @Test
    void testEqualsClass() {
        ConfigApplication configApplication = new ConfigApplication()
        GeneralPageRoleMapping generalPageRoleMapping=new GeneralPageRoleMapping()
        assertFalse generalPageRoleMapping.equals(configApplication)
    }


    @Test
    void testStatusIndicatorEquals(){
        GeneralPageRoleMapping generalPageRoleMapping1=new GeneralPageRoleMapping(statusIndicator: true)
        GeneralPageRoleMapping generalPageRoleMapping2=new GeneralPageRoleMapping(statusIndicator: false)
        assertFalse generalPageRoleMapping1 == generalPageRoleMapping2
    }


    @Test
    void testIdEquals(){
        GeneralPageRoleMapping generalPageRoleMapping1=new GeneralPageRoleMapping(id: 1234)
        GeneralPageRoleMapping generalPageRoleMapping2=new GeneralPageRoleMapping(id: 12345)
        assertFalse generalPageRoleMapping1 == generalPageRoleMapping2
    }

    @Test
    void testEqualsIs() {
        GeneralPageRoleMapping generalPageRoleMapping1 = new GeneralPageRoleMapping()
        assertTrue generalPageRoleMapping1.equals(generalPageRoleMapping1)
    }

    private ConfigApplication saveDomains() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)
        configApplication.refresh()

        ConfigControllerEndpointPage endpointPage = getConfigControllerEndpointPage()
        endpointPage.setConfigApplication(configApplication)
        endpointPage.save(failOnError: true, flush: true)
        endpointPage.refresh()

        ConfigRolePageMapping configRolePageMapping = getConfigRolePageMapping()
        configRolePageMapping.setConfigApplication(configApplication)
        configRolePageMapping.setEndpointPage(endpointPage)
        configRolePageMapping.save(failOnError: true, flush: true)
        return configApplication
    }

    /**
     * Mocking the ConfigControllerEndpointPage domain.
     * @return ConfigControllerEndpointPage
     */
    private ConfigControllerEndpointPage getConfigControllerEndpointPage() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                description: 'TEST',
                displaySequence: 1,
                pageId: 1,
                pageUrl: 'TEST PAGE'
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

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private GeneralPageRoleMapping getGeneralRequestMap() {
        GeneralPageRoleMapping generalRequestMap = new GeneralPageRoleMapping("TEST_PAGE", "TEST_ROLE_12", appName,
                1, "1001", appId, 0L)
        return generalRequestMap
    }
}
