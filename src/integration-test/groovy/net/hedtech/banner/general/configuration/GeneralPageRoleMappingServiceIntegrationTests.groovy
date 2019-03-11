/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.gorm.transactions.Rollback
import grails.plugin.springsecurity.InterceptedUrl
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration test cases for GeneralPageRoleMappingService.
 */
@Integration
@Rollback
class GeneralPageRoleMappingServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalPageRoleMappingService
    private def appName
    private def appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        appName = 'TESTAPP'
        appId = 'TESTAPP'
    }

    @After
    public void tearDown() {
        super.tearDown()
        generalPageRoleMappingService.initialized = false
        generalPageRoleMappingService.isDataIsSeededForInterceptUrlMap = false
    }

    @Test
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
    }


    @Test
    public void testInvalidInterceptUrlMap() {
        try {
            def originalInterceptUrlMap = getInterceptedURLMap()
            assertTrue (originalInterceptUrlMap.size() >= 0)
            String invalidEntry = '/ssb/invalid/**'
            Map invalidEntryMap = new LinkedHashMap()
            invalidEntryMap.put('pattern',invalidEntry)
            invalidEntryMap.put('access',[])
            originalInterceptUrlMap.add(invalidEntryMap)
            Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(invalidEntryMap)

            generalPageRoleMappingService.initialize()
            assertTrue generalPageRoleMappingService.fetchCompiledValue()?.size() >= 0
            assertTrue (generalPageRoleMappingService.fetchCompiledValue()?.size()
                    == Holders.config.grails.plugin.springsecurity.interceptUrlMap?.size())

            def processedInterceptUrlMap = generalPageRoleMappingService.pageRoleMappingListFromDBAndConfig()
            assertTrue processedInterceptUrlMap.size() >= 0

            processedInterceptUrlMap.each { InterceptedUrl iu ->
                assertNotEquals(iu.pattern, '/ssb/invalid/**')
            }
            assertEquals originalInterceptUrlMap.size() - 1, processedInterceptUrlMap.size()                    
        } finally {
            Holders.config.remove('grails.plugin.springsecurity.interceptUrlMap')
        }
    }

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
            assertTrue(generalPageRoleMappingService.fetchCompiledValue()?.size()
                    == Holders.config.grails.plugin.springsecurity.interceptUrlMap?.size())
        } finally {
            generalPageRoleMappingService.sessionFactory = oldSessionFactory
            Holders.config.remove('grails.plugin.springsecurity.interceptUrlMap')
        }
    }

    @Test
    public void testIsDuplicatePageId() {
        saveDomains()
        boolean result = generalPageRoleMappingService.isDuplicatePageId("EndPointPage", "TESTAPP")
        assertTrue(result)
    }

    @Test
    public void testSeedInterceptUrlMapAtServerStartup() {
        Holders.config.app.name=appName
        Holders.config.app.appId=appId
        def properties = new Properties()
        properties.put('grails.plugin.springsecurity.interceptUrlMap', getInterceptedURLMap())
        def configSlurper = new ConfigSlurper()
        Holders.config.merge(configSlurper.parse(properties))

        generalPageRoleMappingService.seedInterceptUrlMapAtServerStartup()

        LinkedHashSet<Map<String, ?>> pageRoleMappingList = generalPageRoleMappingService.getPageRoleMappingList()
        ArrayList<Map<String, ?>> interceptedUrlMapFromDB = new ArrayList<Map<String, ?>>()
        pageRoleMappingList.each {grmList ->
            Map generalPageMap = new LinkedHashMap()
            generalPageMap.put('pattern',grmList.pattern)
            generalPageMap.put('access',grmList.access)
            interceptedUrlMapFromDB.add(generalPageMap)
        }

        assertEquals(interceptedUrlMapFromDB, Holders.config.grails.plugin.springsecurity.interceptUrlMap)
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
                pageId: 'EndPointPage',
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
        GeneralPageRoleMapping generalRequestMap = new GeneralPageRoleMapping(
                applicationId: appId,
                applicationName: appName,
                pageId: 'EndPointPageRequestMap',
                displaySequence: 1,
                pageUrl: 'TEST_PAGE',
                roleCode: 'TEST_ROLE_12'
        )
        return generalRequestMap
    }

    private def getInterceptedURLMap() {
        return [
                [pattern:'/',                  access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
                [pattern:'/login/**',                 access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
                [pattern:'/logout/**',            access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
                [pattern:'/ssb/uiCatalog/index',      access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
                [pattern:'/ssb/AuthenticationTesting',access: ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M',
                                               'WEBUSER']],
                [pattern:'/ssb/survey/**',            access: ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M']],
                [pattern:'/ssb/userAgreement/**',     access: ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M']],
                [pattern:'/ssb/securityQA/**',        access: ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                               'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M']],
                [pattern:'/ssb/theme/**',             access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
                [pattern:'/ssb/themeEditor/**',       access: ['ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M']],
                [pattern:'/ssb/themeEditor/test**',       access: ['ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M']],
                [pattern:'/ssb/AuthenticationTesting/testingEndPoint1/testingEndPoint2/homePage',access: ['ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M']],
                [pattern:'/**',                     access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
        ]
    }
}
