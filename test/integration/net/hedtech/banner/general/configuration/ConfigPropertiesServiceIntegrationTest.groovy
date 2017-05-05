/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.util.Holders
import grails.util.Holders as CH
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigPropertiesServiceIntegrationTest.
 */
class ConfigPropertiesServiceIntegrationTest extends BaseIntegrationTestCase {

    def configPropertiesService
    def configApplicationService
    def grailsApplication

    private def appName
    private def appId
    private static final String CONFIG_NAME = 'TEST_CONFIG'
    private static final String CONFIG_VALUE = 'TEST_VALUE'
    private static final String GLOBAL = 'GLOBAL'
    private static final String TESTAPP = 'TESTAPP'
    private static String ACTUALAPPNAME =''
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        ACTUALAPPNAME = Holders.grailsApplication.metadata['app.name']
        Holders.grailsApplication.metadata['app.name'] = TESTAPP
        appName = Holders.grailsApplication.metadata['app.name']
        appId = TESTAPP
    }

    @After
    public void tearDown() {
        super.tearDown()
        Holders.grailsApplication.metadata['app.name'] = ACTUALAPPNAME
    }

    @Test
    public void testSetConfigFromDb() {
        createNewConfigProperties()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME) == CONFIG_VALUE
    }

    @Test
    public void testGlobalConfiguration() {
        setSurrogateIdForGlobal(999)
        createNewGlobalConfigProps()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get("testing") == GLOBAL
        setSurrogateIdForGlobal(null)
    }

    @Test
    public void testAppPreferenceOverGlobal() {
        setSurrogateIdForGlobal(999)
        createNewGlobalConfigProps()
        createNewAppSpecificConfigProps()
        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get("testing") == "CUSTOM"
        setSurrogateIdForGlobal(null)
    }

    /**
     * Saving the ConfigProperties
     * @return
     */
    private void createNewConfigProperties() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        assertNotNull configApplication?.id

        def configProps = []

        ConfigProperties configProperties = getConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProperties.setConfigValue(CONFIG_VALUE)
        configProps.add(configProperties)

        ConfigProperties configPropertiesBoolean = getConfigProperties()
        configPropertiesBoolean.configType = 'boolean'
        configPropertiesBoolean.configName = CONFIG_NAME + '-boolean'
        configPropertiesBoolean.configValue = 'true'
        configPropertiesBoolean.setConfigApplication(configApplication)
        configProps.add(configPropertiesBoolean)

        ConfigProperties configPropertiesInteger = getConfigProperties()
        configPropertiesInteger.configType = 'integer'
        configPropertiesInteger.configName = CONFIG_NAME + '-integer'
        configPropertiesInteger.configValue = '10'
        configPropertiesInteger.setConfigApplication(configApplication)
        configProps.add(configPropertiesInteger)

        configPropertiesService.create(configProps)
    }

    @Test
    public void testEmptyStringValue() {
       ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        ConfigProperties configPropertiesNullValueString = getConfigProperties()
        configPropertiesNullValueString.setConfigApplication(configApplication)
        configPropertiesNullValueString.configValue = ''

        ConfigProperties configProp = configPropertiesService.create(configPropertiesNullValueString)
        configProp.refresh()
        assertNull(configProp.configValue)

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME) == ''
    }

    @Test
    public void testEmptyBooleanValue() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()

        ConfigProperties configPropertiesBooleanNullValue = getConfigProperties()
        configPropertiesBooleanNullValue.configType = 'boolean'
        configPropertiesBooleanNullValue.configName = CONFIG_NAME + '-boolean-null'
        configPropertiesBooleanNullValue.configValue = ''
        configPropertiesBooleanNullValue.setConfigApplication(configApplication)

        ConfigProperties configProp = configPropertiesService.create(configPropertiesBooleanNullValue)
        configProp.refresh()
        assert configProp.configValue == null

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME + '-boolean') == true
    }

    @Test
    public void testEmptyIntegerValue() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        ConfigProperties configPropertiesIntegerNullValue = getConfigProperties()
        configPropertiesIntegerNullValue.configType = 'integer'
        configPropertiesIntegerNullValue.configName = CONFIG_NAME + '-integer-null'
        configPropertiesIntegerNullValue.configValue = ''
        configPropertiesIntegerNullValue.setConfigApplication(configApplication)

        ConfigProperties configProp = configPropertiesService.create(configPropertiesIntegerNullValue)
        configProp.refresh()
        assert configProp.configValue == null

        configPropertiesService.setConfigFromDb()
        assertTrue CH.config.get(CONFIG_NAME + '-integer-null') == 0
    }


    private void createNewGlobalConfigProps() {
        ConfigApplication configApplication  = ConfigApplication.fetchByAppName(GLOBAL)
        assertNotNull configApplication?.id
        configApplication.refresh()
        def configProps = []

        ConfigProperties configProperties = getConfigProperties()
        configProperties.configName= "testing"
        configProperties.configType = 'string'
        configProperties.setConfigValue("GLOBAL")
        configProperties.setConfigApplication(configApplication)
        configProps.add(configProperties)
        configPropertiesService.create(configProps)

    }

    private createNewAppSpecificConfigProps(){
        ConfigApplication configApplication = getConfigApplication()
        configApplication = configApplicationService.create(configApplication)
        configApplication.refresh()
        assertNotNull configApplication?.id

        def configProps = []

        ConfigProperties configProperties = getAppSpecificConfigProperties()
        configProperties.setConfigApplication(configApplication)
        configProps.add(configProperties)

        configPropertiesService.create(configProps)
    }


    /**
     * Mocking ConfigProperties domain.
     * @return ConfigProperties
     */
    private ConfigProperties getConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: CONFIG_NAME,
                configType: 'string',
                lastModified: new Date()
        )
        return configProperties
    }

    private ConfigProperties getAppSpecificConfigProperties() {
        ConfigProperties configProperties = new ConfigProperties(
                configName: "testing",
                configType: 'string',
                configValue: "CUSTOM",
                lastModified: new Date()
        )
        return configProperties
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                lastModified: new Date(),
                appName: appName,
                appId: appId
        )
        return configApplication
    }

    private setSurrogateIdForGlobal(id){
        Sql sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GUBAPPL set GUBAPPL_SURROGATE_ID = ?  where GUBAPPL_APP_ID = ?", [id, GLOBAL])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }

}
