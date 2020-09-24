/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import banner.general.utility.BannerHolders
import grails.config.Config
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession
import java.util.concurrent.ConcurrentHashMap

@Integration
@Rollback
class BannerHoldersServiceIntegrationTests extends BaseIntegrationTestCase {
    def bannerHoldersService
    Config originalConfig
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        originalConfig = Holders.grailsApplication.config
    }

    @After
    public void tearDown() {
        super.tearDown()
        Holders.grailsApplication.config = originalConfig
    }

    @Test
    public void testSetMeppedConfigObjsForAll () {
        addTestConfigDataToConfigObjForAll()
        def mepCode = 'NORTH'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        Properties properties = new Properties()
        properties.put("banner.mep.configurations", ['all'])
        mergePropToConfig( properties )
        bannerHoldersService.setBaseConfig()
        bannerHoldersService.setMeppedConfigObj()

        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR ${mepCode}")

        mepCode = 'SOUTH'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR ${mepCode}")

        mepCode = 'MAIN'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR DEFAULT")
    }


    @Test
    public void testSetMeppedConfigObjsForLimited () {
        def mepCode = 'NORTH'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        Properties properties = new Properties()
        properties.put("banner.mep.configurations", ['ssbPassword.reset.enabled','footerFadeAwayTime'])
        mergePropToConfig( properties )
        addTestConfigDataForLimitedConfigs()
        bannerHoldersService.setBaseConfig()
        bannerHoldersService.setMeppedConfigObj()

        assertTrue(Holders.config.ssbPassword.reset.enabled == false)

        mepCode = 'SOUTH'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config.ssbPassword.reset.enabled == true)

        mepCode = 'MAIN'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config.ssbPassword.reset.enabled == true)
    }

    @Test
    public void testForEmptyNullConfigsForAll () {
        addTestConfigDataToConfigObjForAll()
        def mepCode = 'NORTH'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        Properties properties = new Properties()
        properties.put("banner.mep.configurations", [])
        mergePropToConfig( properties )
        bannerHoldersService.setBaseConfig()
        bannerHoldersService.setMeppedConfigObj()

        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR DEFAULT")

        mepCode = 'SOUTH'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR DEFAULT")

        mepCode = 'MAIN'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR DEFAULT")


        RequestContextHolder.setRequestAttributes(null)
        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR DEFAULT")
    }

    @Test
    public void testGrailApplicationConfigObject () {
        addTestConfigDataToConfigObjForAll()
        def mepCode = 'EAST'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        Properties properties = new Properties()
        properties.put("banner.mep.configurations", [])
        mergePropToConfig( properties )
        bannerHoldersService.setBaseConfig()
        bannerHoldersService.setMeppedConfigObj()

        assertTrue(Holders.config.banner.test.config == "TEST DATA FOR DEFAULT")

        mepCode = 'WEST'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config == Holders.grailsApplication.config)
        assertTrue(Holders.config == grailsApplication.config)
        assertTrue(Holders.config.banner.test.config == Holders.grailsApplication.config.banner.test.config)
        assertTrue(Holders.config.banner.test.config == grailsApplication.config.banner.test.config)

        mepCode = 'MAIN'
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute('mep', "${mepCode}")
        assertTrue(Holders.config == Holders.grailsApplication.config)
        assertTrue(Holders.config == grailsApplication.config)
        assertTrue(Holders.config.banner.test.config == Holders.grailsApplication.config.banner.test.config)
        assertTrue(Holders.config.banner.test.config == grailsApplication.config.banner.test.config)


        RequestContextHolder.setRequestAttributes(null)
        assertTrue(Holders.config == Holders.grailsApplication.config)
        assertTrue(Holders.config == grailsApplication.config)
        assertTrue(Holders.config.banner.test.config == Holders.grailsApplication.config.banner.test.config)
        assertTrue(Holders.config.banner.test.config == grailsApplication.config.banner.test.config)
    }

    private def addTestConfigDataForLimitedConfigs () {
        Properties properties = new Properties()
        properties.put("ssbPassword.reset.enabled", true)
        properties.put("NORTH.ssbPassword.reset.enabled", false)
        properties.put("SOUTH.ssbPassword.reset.enabled", true)
        mergePropToConfig(properties)
    }

    private def addTestConfigDataToConfigObjForAll() {
        Properties properties = new Properties()
        properties.put("banner.test.config", "TEST DATA FOR DEFAULT")
        properties.put("NORTH.banner.test.config", "TEST DATA FOR NORTH")
        properties.put("SOUTH.banner.test.config", "TEST DATA FOR SOUTH")
        mergePropToConfig(properties)
    }

    private void mergePropToConfig ( Properties properties ) {
        ConfigSlurper configSlurper = new ConfigSlurper()
        Holders.config.merge ( configSlurper.parse(properties) )
    }

}
