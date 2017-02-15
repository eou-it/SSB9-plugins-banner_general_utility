/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration test cases for domain GeneralRequestMap.
 */
class GeneralRequestMapIntegrationTests extends BaseIntegrationTestCase {

    private static final String APP_NAME = 'PlatformSandboxApp'

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
    public void testFetchAll() {
        saveDomains()

        def list = GeneralRequestMap.fetchAll()
        assert list.size() >= 1

        def generalReqMapTestAppName = list.find { p -> p.applicationName = APP_NAME }
        assertTrue generalReqMapTestAppName.applicationName == APP_NAME

        def generalReqMapTestPageName = list.find { p -> p.pageName == 'TEST PAGE' }
        assertTrue generalReqMapTestPageName.pageName == 'TEST PAGE'

        def generalReqMapTestRoleCode = list.find { p -> p.roleCode == 'TEST_ROLE' }
        assertTrue generalReqMapTestRoleCode.roleCode == 'TEST_ROLE'
    }

    @Test
    public void testFetchByAppId() {
        def configApplication = saveDomains()

        def list = GeneralRequestMap.fetchByAppId(configApplication.appId)
        assert list.size() >= 1

        def generalReqMapTestAppName = list.find { p -> p.applicationName = APP_NAME }
        assertTrue generalReqMapTestAppName.applicationName == APP_NAME

        def generalReqMapTestPageName = list.find { p -> p.pageName == 'TEST PAGE' }
        assertTrue generalReqMapTestPageName.pageName == 'TEST PAGE'

        def generalReqMapTestRoleCode = list.find { p -> p.roleCode == 'TEST_ROLE' }
        assertTrue generalReqMapTestRoleCode.roleCode == 'TEST_ROLE'
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
        def list = GeneralRequestMap.fetchByAppId(configApplication.appId)
        shouldFail(Exception) {
            def generalRequestMap = list.get(0)
            generalRequestMap.pageName = 'TEST_PAGE'
            generalRequestMap.save(failOnError: true, flush: true)
        }
    }

    @Test
    public void testFailCaseOnDelete() {
        def configApplication = saveDomains()
        def list = GeneralRequestMap.fetchByAppId(configApplication.appId)
        shouldFail(Exception) {
            def generalRequestMap = list.get(0)
            generalRequestMap.delete(flush: true)
        }
    }

    @Test
    void testToString() {
        ConfigApplication configApp = saveDomains()
        List<ConfigApplication> generalRequestMap = GeneralRequestMap.fetchByAppId(configApp.appId)
        assertFalse generalRequestMap.isEmpty()
        generalRequestMap.each { requestMap ->
            String requestMapToString = requestMap.toString()
            assertNotNull requestMapToString
        }
    }


    @Test
    void testHashCode() {
        ConfigApplication configApp = saveDomains()
        List<ConfigApplication> generalRequestMap = GeneralRequestMap.fetchByAppId(configApp.appId)
        assertFalse generalRequestMap.isEmpty()
        generalRequestMap.each { requestMap ->
            Integer requestMapHashCode = requestMap.hashCode()
            assertNotNull requestMapHashCode
        }
    }

    @Test
    public void testEqualAndHashcode() {
        GeneralRequestMap obj1 = getGeneralRequestMap()
        GeneralRequestMap obj2 = getGeneralRequestMap()
        assertEquals obj1, obj2
        assertEquals obj1.roleCode, obj2.roleCode
        assertEquals obj1.pageName, obj2.pageName
        assertEquals obj1.displaySequence, obj2.displaySequence
        assertEquals obj1.applicationId, obj2.applicationId
        assertEquals obj1.applicationName, obj2.applicationName
        assertEquals obj1.pageId, obj2.pageId

        GeneralRequestMap obj3 = getGeneralRequestMap()
        obj3.applicationId = 100002
        assertNotEquals obj2, obj3
        assertNotEquals obj1.hashCode(), obj3.hashCode()

        GeneralRequestMap obj4 = getGeneralRequestMap()
        obj4.pageId = 1000002
        assertNotEquals obj2, obj4
        assertNotEquals obj1.hashCode(), obj4.hashCode()

        GeneralRequestMap obj5 = getGeneralRequestMap()
        obj5.applicationName = 'TEST_APP_2'
        assertNotEquals obj2, obj5
        assertNotEquals obj1.hashCode(), obj5.hashCode()

        GeneralRequestMap obj6 = getGeneralRequestMap()
        obj6.displaySequence = 2
        assertNotEquals obj2, obj6
        assertNotEquals obj1.hashCode(), obj6.hashCode()

        GeneralRequestMap obj7 = getGeneralRequestMap()
        obj7.pageName = 'PAGE_NAME_TEST'
        assertNotEquals obj2, obj7
        assertNotEquals obj1.hashCode(), obj7.hashCode()

        GeneralRequestMap obj8 = getGeneralRequestMap()
        obj8.roleCode = 'TEST_ROLE_1'
        assertNotEquals obj2, obj8
        assertNotEquals obj1.hashCode(), obj8.hashCode()

        GeneralRequestMap obj9 = getGeneralRequestMap()
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
            GeneralRequestMap generalRequestMap = getGeneralRequestMap()
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(generalRequestMap)
            oos.close()

            byte[] bytes = out.toByteArray()
            GeneralRequestMap generalRequestMapCopy
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                generalRequestMapCopy = (GeneralRequestMap) is.readObject()
                is.close()
            }
            assertEquals generalRequestMapCopy, generalRequestMap

        } catch (e) {
            e.printStackTrace()
        }
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
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private GeneralRequestMap getGeneralRequestMap() {
        GeneralRequestMap generalRequestMap = new GeneralRequestMap(
                applicationId: 100001,
                applicationName: APP_NAME,
                pageId: 1001,
                displaySequence: 1,
                pageName: 'TEST_PAGE',
                roleCode: 'TEST_ROLE_12'
        )
        return generalRequestMap
    }
}
