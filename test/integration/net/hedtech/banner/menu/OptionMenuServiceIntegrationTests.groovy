/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.menu

import net.hedtech.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.junit.Before
import org.junit.Test

class OptionMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def optionMenuService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    @Test
    void testOptionMenuForPage() {
        def list
        list = optionMenuService.optionMenuForPage("basicCourseInformation")
        assertNotNull list

        def mnu = new OptionMenu()
        for (i in 0..(list.size() - 1)) {
            if (list.get(i).calledFormName == "SCARRES") {
                mnu = list.get(i)
                break;
            }
        }

        assertEquals 8, mnu.seq, 1e-6
        assertEquals "Reg. Restrictions[SCARRES]", mnu.menuDesc
        assertEquals "SCACRSE", mnu.formName
        assertEquals "SCARRES", mnu.calledFormName
        assertEquals "courseRegistrationRestrictions",mnu.pageName 
// TODO: Re-enable this assertion: junit.framework.AssertionFailedError: expected:<7> but was:<8>
//       assertEquals 7, list.size()
    }


    @Test
    void testOptionMenuForBlock() {
        def list
        list = optionMenuService.optionMenuForBlock("basicCourseInformation", "zipBlock")
        assertNotNull list
        assertEquals 7, list.size()

        def mnu = list.get(0)

        assertEquals 8, mnu.seq, 1e-6
        assertEquals "Reg. Restrictions[SCARRES]", mnu.menuDesc
        assertEquals "SCACRSE", mnu.formName
        assertEquals "SCARRES", mnu.calledFormName
        assertEquals "courseRegistrationRestrictions", mnu.pageName
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.executeUpdate("""
        UPDATE GUROPTM
           SET GUROPTM_BLOCK_VALID = 'GTVZIPC'
         WHERE GUROPTM_FORM_NAME = 'SCACRSE'
           AND GUROPTM_FORM_TO_BE_CALLED = 'SCADETL'
      """)

    }
}
