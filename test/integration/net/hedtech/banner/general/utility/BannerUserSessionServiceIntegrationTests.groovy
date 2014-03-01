/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.general.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.session.BannerUserSession

import org.junit.Ignore

class BannerUserSessionServiceIntegrationTests extends BaseIntegrationTestCase {

    def bannerUserSessionService

    public static final String SEAMLESS_TOKEN_1 = "98329832032njdskdslk21389320"
    public static final String SEAMLESS_TOKEN_2 = "98329832hdskds89923k21389321"

    protected void setUp() {
		formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
	}

    protected void tearDown() {
        super.tearDown()
    }

    void testStringDataSave () {
        bannerUserSessionService.publish (
                SEAMLESS_TOKEN_1,
                [
                        "goto.currently.opened" : "basicCourseInformation, courseDetailInformation" ,
                ])

        def userSessionInfo = bannerUserSessionService.consume (SEAMLESS_TOKEN_1)

        groovy.util.GroovyTestCase.assertEquals 1L, userSessionInfo.size()

        assertEquals SEAMLESS_TOKEN_1, userSessionInfo[0].sessionToken
        assertEquals "goto.currently.opened", userSessionInfo[0].infoType
        assertEquals "basicCourseInformation, courseDetailInformation", userSessionInfo[0].info

        assertNotNull userSessionInfo[0].infoDataType
        assertNotNull userSessionInfo[0].infoPersisted

        assertNotNull userSessionInfo[0].lastModifiedBy
        assertNotNull userSessionInfo[0].lastModified
        assertNotNull userSessionInfo[0].version
        assertNotNull userSessionInfo[0].id
    }

    void testDateDataSave () {
        def today = new Date ()

        bannerUserSessionService.publish (
                SEAMLESS_TOKEN_1,
                [
                        "banner.globals.global.subject.fromDate" : today
                ])

        def userSessionInfo = bannerUserSessionService.consume (SEAMLESS_TOKEN_1)

        groovy.util.GroovyTestCase.assertEquals 1L, userSessionInfo.size()

        assertEquals SEAMLESS_TOKEN_1, userSessionInfo[0].sessionToken
        assertEquals "banner.globals.global.subject.fromDate", userSessionInfo[0].infoType
        assertEquals today, userSessionInfo[0].info

        assertNotNull userSessionInfo[0].infoDataType
        assertNotNull userSessionInfo[0].infoPersisted

        assertNotNull userSessionInfo[0].lastModifiedBy
        assertNotNull userSessionInfo[0].lastModified
        assertNotNull userSessionInfo[0].version
        assertNotNull userSessionInfo[0].id
    }

    /**
     * Before switching the context, the app persists all the
     * information to be shared.
     */
    @Ignore // re-enable; this runs in isolation but not as part of the suite
    void testMultipleRowSave() {
        def today = new Date()
        bannerUserSessionService.publish (
                SEAMLESS_TOKEN_1,
                [
                        "goto.currently.opened" : "basicCourseInformation, courseDetailInformation" ,
                        "goto.recently.opened" : "courseSearch",
                        "banner.globals.global.subject.label" : "ACCT",
                        "banner.globals.global.subject.fromDate" : today
                ])

        def userSessionInfo = bannerUserSessionService.consume (SEAMLESS_TOKEN_1)

        groovy.util.GroovyTestCase.assertEquals 4L, userSessionInfo.size()

    }

    void testDelete() {
        bannerUserSessionService.publish (
                SEAMLESS_TOKEN_1,
                [
                        "goto.currently.opened" : "basicCourseInformation, courseDetailInformation"
                ])
        bannerUserSessionService.publish (
                SEAMLESS_TOKEN_2,
                [
                        "goto.recently.opened" : "courseSearch"
                ])

        def userSessionInfo = BannerUserSession.findAll ()

        groovy.util.GroovyTestCase.assertEquals 2L, userSessionInfo.size()

        bannerUserSessionService.consume (SEAMLESS_TOKEN_1)

        userSessionInfo = bannerUserSessionService.consume (SEAMLESS_TOKEN_1)
        groovy.util.GroovyTestCase.assertEquals 0L, userSessionInfo.size()

        userSessionInfo = bannerUserSessionService.consume (SEAMLESS_TOKEN_2)
        groovy.util.GroovyTestCase.assertEquals 1L, userSessionInfo.size()

    }

}
