/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.plugin.springsecurity.InterceptedUrl
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
    private def appName
    private def appId

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
        generalPageRoleMappingService.initialized = false
    }

    /*@Test
    public void testInitialize() {
        try {
            saveDomains()

            def properties = new Properties()
            properties.put('grails.plugin.springsecurity.interceptUrlMap', getInterceptedURLMap())
            def configSlurper = new ConfigSlurper()
            Holders.config.merge(configSlurper.parse(properties))

            //Holders.config.grails.plugin.springsecurity.interceptUrlMap = [:]


            generalPageRoleMappingService.initialize()
            assertTrue generalPageRoleMappingService.fetchCompiledValue()?.size() >= 0
            assertTrue (generalPageRoleMappingService.fetchCompiledValue()?.size()
                            == Holders.config.grails.plugin.springsecurity.interceptUrlMap?.size())

            def list = generalPageRoleMappingService.pageRoleMappingListFromDBAndConfig()
            assertTrue list.size() >= 0

            def compiledData = generalPageRoleMappingService.fetchCompiledValue()
            def foundData = []
            list.each { InterceptedUrl iu ->
                foundData << compiledData.find { InterceptedUrl url ->
                    url?.pattern == iu?.pattern?.toLowerCase()
                }
            }
            assertEquals foundData.size(), list.size()
        } finally {
            Holders.config.remove('grails.plugin.springsecurity.interceptUrlMap')
        }
    }*/

    @Test
    public void testInitializeWithoutSessionFactory() {
        def oldSessionFactory = generalPageRoleMappingService.sessionFactory
        try {
            generalPageRoleMappingService.sessionFactory = null

            saveDomains()

            def properties = new Properties()
            properties.put('grails.plugin.springsecurity.interceptUrlMap', getInterceptedURLMap())
            def configSlurper = new ConfigSlurper()
            Holders.config.merge(configSlurper.parse(properties))

            generalPageRoleMappingService.initialize()
            assertTrue generalPageRoleMappingService.fetchCompiledValue()?.size() >= 0
            assertTrue (generalPageRoleMappingService.fetchCompiledValue()?.size()
                            == Holders.config.grails.plugin.springsecurity.interceptUrlMap?.size())
        } finally {
            generalPageRoleMappingService.sessionFactory = oldSessionFactory
            Holders.config.remove('grails.plugin.springsecurity.interceptUrlMap')
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
        GeneralPageRoleMapping generalRequestMap = new GeneralPageRoleMapping(
                applicationId: appId,
                applicationName: appName,
                pageId: 1001,
                displaySequence: 1,
                pageName: 'TEST_PAGE',
                roleCode: 'TEST_ROLE_12'
        )
        return generalRequestMap
    }

    private def getInterceptedURLMap() {
        return [
                '/'                         : ['IS_AUTHENTICATED_ANONYMOUSLY'],
                '/login/**'                 : ['IS_AUTHENTICATED_ANONYMOUSLY'],
                '/logout/**'                : ['IS_AUTHENTICATED_ANONYMOUSLY'],
                '/ssb/uiCatalog/index'      : ['IS_AUTHENTICATED_ANONYMOUSLY'],
                '/ssb/AuthenticationTesting': ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M',
                                               'WEBUSER'],
                '/ssb/survey/**'            : ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
                '/ssb/userAgreement/**'     : ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
                '/ssb/securityQA/**'        : ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
                '/ssb/theme/**'             : ['IS_AUTHENTICATED_ANONYMOUSLY'],
                '/ssb/themeEditor/**'       : ['ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M'],
                '/**'                       : ['IS_AUTHENTICATED_ANONYMOUSLY']
        ]
    }
}
