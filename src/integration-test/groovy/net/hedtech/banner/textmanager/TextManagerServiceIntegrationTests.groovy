/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class TextManagerServiceIntegrationTests extends BaseIntegrationTestCase {
    def textManagerService
    def message
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
            def appName = "UNITTEST"//Holders.grailsApplication.metadata['app.name']
            try {
                def statement = """
                                BEGIN
                                    insert into GMBPROJ (GMBPROJ_PROJECT, GMBPROJ_ACTIVITY_DATE, GMBPROJ_DESC,
                                   GMBPROJ_OWNER,GMBPROJ_USER_ID) values ('$project', sysdate, '$projectDescription',
                                   'TRANMGR','ban_ss_user');
                                   insert into GMRPCFG (GMRPCFG_PROJECT, GMRPCFG_KEY, GMRPCFG_VALUE, GMRPCFG_DESC,
                                    GMRPCFG_USER_ID,GMRPCFG_ACTIVITY_DATE) values ('$project', '$PROJECT_CFG_KEY_APP',
                                    '$appName', 'Banner Application in this project','ban_ss_user',sysdate );
                                   commit;
                                END;
                            """
                sql.execute(statement)
                sql.commit()
                textManagerService.cacheTime = null
            } finally {
                //sql?.close()
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
                sql.commit()
                textManagerService.cacheTime = null
            } catch(e){
            } finally {
                sql?.close()
            }
        }
    }

    @Before
    public void setUp(){
        formContext = ['GUAGMNU']
        Holders.config.ssbEnabled = true
        Holders.config.app.name = 'UNITTEST'
        super.setUp()
        createProjectForApp('UNITTEST', 'Integration Test Banner General Utility')
    }

    @After
    public void tearDown() {
        deleteProjectforApp()
        super.tearDown()
    }


    @Test
    public void testSaveSuccess(){
        def name = "UNITTEST"
        def srcProperties = new Properties()
        def srcLocale = textManagerService.ROOT_LOCALE_APP
        def tgtProperties = new Properties()
        def tgtLocale = "enIN"

        srcProperties.put("dummy.label1", "Dummy English text1")
        srcProperties.put("dummy.label2", "Dummy English text2")
        tgtProperties.put("dummy.label1", "Dummy French text1")
        tgtProperties.put("dummy.label2", "Dummy French text2")

        def srcStatus = textManagerService.save(srcProperties, name, srcLocale, srcLocale)
        def tgtStatus = textManagerService.save(tgtProperties, name, srcLocale, tgtLocale)
        def message = textManagerService.findMessage("dummy.label1",srcLocale)

        assertNull(srcStatus.error)
        assertEquals(2, srcStatus.count)
        assertNull(tgtStatus.error)
        assertEquals(2, tgtStatus.count)
        //assertNotNull(message)
    }
}
