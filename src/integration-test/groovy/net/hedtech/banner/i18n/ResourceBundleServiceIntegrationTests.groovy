/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
//import org.codehaus.groovy.grails.web.context.ServletContextHolder
import grails.web.context.ServletContextHolder
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class ResourceBundleServiceIntegrationTests extends BaseIntegrationTestCase {

    def messageSource
    def resourceBundleService

    def testLocales = ['en_GB','en']
    def testLocalesSave =   [
            [
                "enabled": true,
                "code": "en_GB"
            ],
            [
                "enabled": true,
                "code": "en"
            ]
    ]

    def textManagerService

    def underlyingDataSource
    def underlyingSsbDataSource
    def project = 'UNITTEST'


    static final String PROJECT_CFG_KEY_APP = 'BAN_APP'

    def createProjectForApp(projectCode, projectDescription) {
        if (!textManagerService.tmEnabled) {
            return
        }
        if (!textManagerService.tranManProject()) {
            Sql sql = new Sql(underlyingSsbDataSource?: underlyingDataSource)
            def appName = Holders.grailsApplication.metadata['app.name']
            try {
                def statement = """
                                BEGIN
                                    insert into GMBPROJ (GMBPROJ_PROJECT, GMBPROJ_ACTIVITY_DATE, GMBPROJ_DESC,
                                   GMBPROJ_OWNER,GMBPROJ_USER_ID) values ($project, sysdate, $projectDescription,
                                   'TRANMGR','ban_ss_user');
                                   insert into GMRPCFG (GMRPCFG_PROJECT, GMRPCFG_KEY, GMRPCFG_VALUE, GMRPCFG_DESC,
                                   GMRPCFG_USER_ID,GMRPCFG_ACTIVITY_DATE) values ($project, $PROJECT_CFG_KEY_APP,
                                   $appName, 'Banner Application in this project','ban_ss_user',sysdate );
                                   commit;
                                END;
                            """
                sql.execute(statement)
                textManagerService.cacheTime = null
            } finally {
                sql?.close()
            }
        }
    }

    //Used to clean test project
    def deleteProjectforApp() {
        if (project) {
            Sql sql = new Sql(underlyingSsbDataSource?: underlyingDataSource)
            try {
                def statement = """
                                   begin
                                        delete from GMRPCFG where GMRPCFG_project=$project;
                                        delete from GMRSPRP where GMRSPRP_project=$project;
                                        delete from GMRSHST where GMRSHST_project=$project;
                                        delete from GMRPOBJ where GMRPOBJ_project=$project;
                                        delete from GMBPROJ where GMBPROJ_project=$project;
                                        delete from GMRMDUL where GMRMDUL_PROJECT=$project;
                                    commit;
                                   end;
                                """
                sql.execute(statement)
                textManagerService.cacheTime = null
            } catch(e){
            } finally {
                sql?.close()
            }
        }
    }
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        Holders.config.ssbEnabled = true
        super.setUp();
        createProjectForApp('UNITTEST', 'Integration Test Banner General Utility')
    }

    @After
    public void tearDown() {
        deleteProjectforApp()
        super.tearDown()
    }

    @Test
    void testGetList() {
        def resources = resourceBundleService.list()
        assertTrue(resources.size() > 0)
    }

    @Test
    void testGetResourcesWithSingleLocale() {
      def resources = resourceBundleService.list()
      def resList =  []
      testLocales.each { locale ->
                resources.each { it ->
                    resList << resourceBundleService.get(it.basename, locale)
                }
            }
        assertTrue(resList.size()>0)
    }

    @Test
    void testSave() {
        def fr = resourceBundleService.get('testExternalResource/test', 'fr_FR')
        def resources = resourceBundleService.list()
        def data = resources[0]
        data.enableTranslation = true
        data.sourceLocale = 'root'
        data.locales = testLocalesSave
        def saveResult = resourceBundleService.save(data)
        //assertTrue(saveResult.count > 0)
        assertEquals(fr.locale,"fr_FR")
    }

    @Test
    void testSaveWithLocale() {
        def ar = resourceBundleService.get('testExternalResource/test', 'ar_SA')
        def resources = resourceBundleService.list()
        def data = resources[0]
        data.enableTranslation = true
        data.sourceLocale = 'root'
        data.locales = testLocalesSave
      //  def saveResult = resourceBundleService.save(data)
        //assertTrue(saveResult.count > 0)
        assertEquals(ar.locale,"ar_SA")
    }
}



