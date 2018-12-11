/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class ResourceBundleControllerIntegrationTests extends BaseIntegrationTestCase {

    def resourceBundleController

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        resourceBundleController = new ResourceBundleController()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testListOfValues(){
        resourceBundleController.list()
        assert 200,resourceBundleController.response.status
    }

    @Test
    void testFetchValuesWithIdAndType(){
        resourceBundleController.params.id =40
        resourceBundleController.params.name="PLUGINS/CSV/MESSAGES"
        resourceBundleController.params.locale= "en_US"
        resourceBundleController.show()
        assert 200,resourceBundleController.response.status
    }

    @Test
    void testSavingValues(){
        def data = ['id': '40', 'name': 'PLUGINS/CSV/MESSAGES', 'locale': 'en_US']
        resourceBundleController.request.JSON = data
        resourceBundleController.save()
        assert 200,resourceBundleController.response.status
    }
}
