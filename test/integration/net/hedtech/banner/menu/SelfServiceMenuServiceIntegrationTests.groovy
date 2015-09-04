/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.menu

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class SelfServiceMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceMenuService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    @Test
    void testSelfServiceBannerMenuPidm() {
        def map
        def pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow ("select spriden_pidm from spriden where spriden_id = 'HOSS001' and spriden_change_ind is not null") {
            pidm = it.spriden_pidm
        }
        map = selfServiceMenuService.processMenu (null,null,pidm)
        assert map.size() > 0
    }

    @Test
    void testSelfServiceBannerMenu() {
        def map
        map = selfServiceMenuService.processMenu (null,null,null)
        assert map.size() > 0
    }

    @Ignore /* Adding Ignore as the method combinedMenu does not exist in selfServiceMenuService*/
    void testSelfServiceCombinedMenuMenu() {
        def map
        map = selfServiceMenuService.combinedMenu(null,null,null)
        assert map.size() > 0
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

    }

    @Ignore       /* Adding Ignore as the method searchMenuSSB does not exist in selfServiceMenuService*/
    void testSelfServiceSearchMenu() {
        def list
        def pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("select spriden_pidm from spriden where spriden_id = 'HOSS001' spriden_change_ind is not null") {
            pidm = it.spriden_pidm
        }

        list = selfServiceMenuService.searchMenuSSB('Banner XE Registration',pidm)
        assert list.size() > 0
    }

}
