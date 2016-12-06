/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.general.utility

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.session.BannerUserSession
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class BannerUserSessionIntegrationTests extends BaseIntegrationTestCase {

    public static final String GOTO_CURRENTLY_OPENED = "goto.currently.opened"
    public static final String GLOBAL_START_DATE = "global.start.date"

    public static final String SEAMLESS_TOKEN_1 = "98329832032njdskdslk21389320"
    public static final String WRONG_SEAMLESS_TOKEN = "1".padLeft(151,"1")

    public static final Integer SESSION_TOKEN_LENGTH = 150

    public static final String BASIC_COURSE_INFORMATION = "basicCourseInformation"
    public static final String COURSE_DETAIL_INFORMATION = "courseDetailInformation"

    public static final Date LAST_MODIFIED = new Date()

    public static final String DATA_ORIGIN = "Banners"
    public static final Date CURRENT_DATE = new Date()

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateBannerUserSession() {
        def bannerUserSession = newBannerUserSession()
        save bannerUserSession
        //Test if the generated entity now has an id assigned
        assertNotNull bannerUserSession.id
        assertEquals 0L, bannerUserSession.version
        assertNotNull bannerUserSession.sessionToken
        assertTrue(bannerUserSession.sessionToken.length() < SESSION_TOKEN_LENGTH)

        assertEquals GOTO_CURRENTLY_OPENED, bannerUserSession.infoType
        assertEquals BASIC_COURSE_INFORMATION, bannerUserSession.info
        assertEquals(bannerUserSession.infoDataType, "java.lang.String")
        assertEquals BASIC_COURSE_INFORMATION, bannerUserSession.infoPersisted

        assertNotNull bannerUserSession.dataOrigin
        assertNotNull bannerUserSession.lastModified
        assertNotNull bannerUserSession.lastModifiedBy

    }

    @Test
    void testCreateBannerUserSessionWithInvalidToken() {
        def bannerUserSession = newBannerUserSessionWithInvalidToken()

        shouldFail( ValidationException  ) {
            bannerUserSession.save(failOnError: true, flush: true )
        }
    }
    @Test
    void testCreateBannerUserSessionWithDateData() {
        def bannerUserSession = newBannerUserSessionWithDateData()
        save bannerUserSession
        //Test if the generated entity now has an id assigned

        assertNotNull bannerUserSession.id
        assertEquals 0L, bannerUserSession.version
        assertNotNull bannerUserSession.sessionToken
        assertTrue(bannerUserSession.sessionToken.length() < SESSION_TOKEN_LENGTH)

        assertEquals GLOBAL_START_DATE, bannerUserSession.infoType
        assertEquals CURRENT_DATE, bannerUserSession.info
        assertEquals(bannerUserSession.infoDataType, "java.util.Date")
        assertNotNull bannerUserSession.infoPersisted

        assertNotNull bannerUserSession.dataOrigin
        assertNotNull bannerUserSession.lastModified
        assertNotNull bannerUserSession.lastModifiedBy

    }
    @Test
    void testUpdateBannerUserSession() {
        def bannerUserSession = newBannerUserSession()
        save bannerUserSession

        //Test if the generated entity now has an id assigned
        assertNotNull bannerUserSession.id
        assertEquals 0L, bannerUserSession.version
        assertNotNull bannerUserSession.sessionToken
        assertTrue(bannerUserSession.sessionToken.length() < SESSION_TOKEN_LENGTH)

        assertEquals GOTO_CURRENTLY_OPENED, bannerUserSession.infoType
        assertEquals BASIC_COURSE_INFORMATION, bannerUserSession.info
        assertEquals(bannerUserSession.infoDataType, "java.lang.String")
        assertEquals BASIC_COURSE_INFORMATION, bannerUserSession.infoPersisted

        assertNotNull bannerUserSession.dataOrigin
        assertNotNull bannerUserSession.lastModified
        assertNotNull bannerUserSession.lastModifiedBy

        //Update the entity
        bannerUserSession.info = COURSE_DETAIL_INFORMATION
        save bannerUserSession

        bannerUserSession = BannerUserSession.get( bannerUserSession.id )
        assertEquals 1L, bannerUserSession?.version
        assertEquals COURSE_DETAIL_INFORMATION, bannerUserSession.info
    }
    @Test
    void testOptimisticLock() {
        def bannerUserSession = newBannerUserSession()
        save bannerUserSession

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GURSESS set GURSESS_VERSION = 999 where GURSESS_SURROGATE_ID = ?", [ bannerUserSession.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        bannerUserSession.infoType = GOTO_CURRENTLY_OPENED
        bannerUserSession.info =  COURSE_DETAIL_INFORMATION

        shouldFail( HibernateOptimisticLockingFailureException  ) {
            bannerUserSession.save( flush: true )
        }
    }
    @Test
    void testDeleteBannerUserSession() {
        def bannerUserSession = newBannerUserSession()
        save bannerUserSession
        def id = bannerUserSession.id
        assertNotNull id
        bannerUserSession.delete()
        assertNull BannerUserSession.get( id )
    }
    @Test
    void testValidation() {
        def bannerUserSession = newBannerUserSession()
        assertTrue "BannerUserSession could not be validated as expected due to ${bannerUserSession.errors}", bannerUserSession.validate()
    }
    @Test
    void testNullValidationFailure() {
        def bannerUserSession = new BannerUserSession()
        assertNoErrorsFor bannerUserSession,
                [
                        'userName',
                        'infoType',
                        'info',
                        'infoDataType',
                        'infoPersisted',
                        'sessionToken'
                ]
    }

    private BannerUserSession newBannerUserSession() {
        def bannerUserSession = new BannerUserSession(
                infoType:GOTO_CURRENTLY_OPENED,
                info: BASIC_COURSE_INFORMATION,
                sessionToken: SEAMLESS_TOKEN_1
        )
        return bannerUserSession
    }

    private BannerUserSession newBannerUserSessionWithInvalidToken() {
        def bannerUserSession = new BannerUserSession(
                infoType:GOTO_CURRENTLY_OPENED,
                info: BASIC_COURSE_INFORMATION,
                sessionToken: WRONG_SEAMLESS_TOKEN
        )
        return bannerUserSession
    }

    private BannerUserSession newBannerUserSessionWithDateData() {
        def bannerUserSession = new BannerUserSession(
                infoType:GLOBAL_START_DATE,
                info: CURRENT_DATE,
                sessionToken: SEAMLESS_TOKEN_1
        )
        return bannerUserSession
    }

    /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(MenuAndToolbarPreference_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}
