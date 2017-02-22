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
 * Integration test cases for GeneralPageRoleMappingService.
 */
class GeneralPageRoleMappingServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalPageRoleMappingService
    final private static def APP_NAME = Holders.grailsApplication.metadata['app.name']

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
    public void testInitialize() {
        saveDomains()

        assertFalse generalPageRoleMappingService.initialized
        generalPageRoleMappingService.initialize()
        assertTrue generalPageRoleMappingService.initialized
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
    private GeneralPageRoleMapping getGeneralRequestMap() {
        GeneralPageRoleMapping generalRequestMap = new GeneralPageRoleMapping(
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
