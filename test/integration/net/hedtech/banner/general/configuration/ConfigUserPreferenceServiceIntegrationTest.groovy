/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigUserPreferenceServiceIntegrationTest.
 */
class ConfigUserPreferenceServiceIntegrationTest extends BaseIntegrationTestCase {

    private def appName
    private def appId
    Integer pidm
    def configPropertiesService
    def configApplicationService
    def configUserPreferenceService
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        appName = Holders.grailsApplication.metadata['app.name']
        appId = 'TESTAPP'
        pidm = getPidmBySpridenId("HOSH00001")
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    public void testGetUserPreferenceByConfigNameAppIdAndPidmWhereUserPreferenceTrue(){
        ConfigProperties configProperties  = createNewConfigProperties()
        assertNotNull configProperties?.id

        ConfigUserPreference configUserPreference = createConfigUserPreference(configProperties)
        assertNotNull configUserPreference.id
        assertEquals configProperties.configName, configUserPreference.configName
        assertEquals 'USER_TEST_VALUE', configUserPreference.configValue


        def userConfiguration = configUserPreferenceService.getUserPreferenceByConfigNameAppIdAndPidm(configProperties.configName, appId, pidm)
        assertNotNull userConfiguration
        assertEquals configUserPreference.configValue, userConfiguration.configValue
        assertNotEquals configProperties.configValue, userConfiguration.configValue
    }


    @Test
    public void testGetUserPreferenceByConfigNameAppIdAndPidmWhereUserPreferenceFalse(){
        ConfigProperties configProperties  = createNewConfigProperties()
        assertNotNull configProperties.id
        assertEquals 0L, configProperties.version

        configProperties.setUserPreferenceIndicator(false)
        configPropertiesService.create(configProperties)
        assertNotNull configProperties.id
        assertEquals 1L, configProperties.version

        ConfigUserPreference configUserPreference = createConfigUserPreference(configProperties)
        assertNotNull configUserPreference.id
        assertEquals configProperties.configName, configUserPreference.configName
        assertEquals 'USER_TEST_VALUE', configUserPreference.configValue

        def userConfiguration = configUserPreferenceService.getUserPreferenceByConfigNameAppIdAndPidm(configProperties.configName, appId, pidm)
        assertNotNull userConfiguration
        assertNotEquals configUserPreference.configValue, userConfiguration.configValue
        assertEquals configProperties.configValue, userConfiguration.configValue
    }



    @Test
    public void testGetUserLocale(){
        ConfigProperties configProperties  = createNewConfigPropertiesForLocale()
        assertNotNull configProperties?.id

        ConfigUserPreference configUserPreference = createConfigUserPreference(configProperties)
        assertNotNull configUserPreference.id
        assertEquals configProperties.configName, configUserPreference.configName
        assertEquals 'PT', configUserPreference.configValue

        def userLocale = configUserPreferenceService.getUserLocale()
        assertEquals new Locale('PT'), userLocale
    }


    @Test
    public void testGetAllBannerSupportedLocales(){
        def userLocale = configUserPreferenceService.getAllBannerSupportedLocales()
        assertNotNull userLocale
    }



    private def createNewConfigProperties() {
        ConfigApplication configApplication = getConfigApplication()
        configApplicationService.create(configApplication)
        assertNotNull configApplication?.id

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.setUserPreferenceIndicator(true)
        configPropertiesService.create(configProperties)
    }


    private def createNewConfigPropertiesForLocale() {
        ConfigApplication configApplication = getConfigApplication()
        configApplicationService.create(configApplication)
        assertNotNull configApplication?.id

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigName("locale")
        configProperties.setConfigValue("AR")
        configProperties.setConfigApplication(configApplication)
        configProperties.setUserPreferenceIndicator(true)
        configPropertiesService.create(configProperties)
    }


    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                lastModified: new Date(),
                appName: appName,
                appId: appId
        )
        return configApplication
    }


    private def createConfigUserPreference(configProperties) {
        ConfigUserPreference configUserPreference = getConfigUserPreference()
        configUserPreference.setConfigApplication(configProperties?.getConfigApplication())
        configUserPreference.setConfigName(configProperties?.getConfigName())
        configUserPreference.setConfigType(configProperties?.getConfigType())
        configUserPreferenceService.create(configUserPreference)
    }

    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: 'CONFIG_TEST',
                configType: 'string',
                configValue: 'TEST_VALUE'
        )
        return configProperties
    }


    private ConfigUserPreference getConfigUserPreference() {
        ConfigUserPreference configUserPreference = new ConfigUserPreference(
                pidm: pidm,
                configValue: 'PT'
        )
        return configUserPreference
    }


    private Integer getPidmBySpridenId(def spridenId) {
        Sql sqlObj
        Integer pidmValue
        try{
            sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
            def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=$spridenId"
            pidmValue = sqlObj?.firstRow(query)?.pidm
        } catch(Exception e){
            e.printStackTrace()
        }
        return pidmValue
    }

}
