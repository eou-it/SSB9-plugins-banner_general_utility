/*******************************************************************************
 Copyright 2015-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.menu.jobs

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class JobsMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def jobsMenuService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    @Test
    void testJobsSetup() {
        def javaFormsURL = jobsMenuService.getPlatCodeJavaFormsUrl()
        assertEquals javaFormsURL,"zzz"
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.executeUpdate("""
         insert into GUBMODU( GUBMODU_CODE, GUBMODU_NAME,GUBMODU_URL,
                     GUBMODU_SURROGATE_ID,
                     GUBMODU_VERSION,
                     GUBMODU_USER_ID,
                     GUBMODU_DATA_ORIGIN,
                     GUBMODU_ACTIVITY_DATE,
                     GUBMODU_VPDI_CODE,
                     GUBMODU_INTEGRATION_VALUE,
                     GUBMODU_PLAT_CODE) values ('ZZZ','zzz','zzz',-1,0,user,'banner',sysdate,null,null,'ADMJF')
      """)


        sql.executeUpdate("""
                       insert into gubpage
                       select 'GJAPCTL','zzz','ZZZ',-1,0,user,'banner',sysdate,null from dual
                       WHERE NOT EXISTS (select GUBPAGE_CODE from GUBPAGE where GUBPAGE_CODE = 'GJAPCTL')
       """)


    }
}
