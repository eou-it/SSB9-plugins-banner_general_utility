/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class XssSanitizerIntegrationTests extends BaseIntegrationTestCase {

    def xssSanitizer
    def input ="testsanitize 123 \$"

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        xssSanitizer = new XssSanitizer()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testSanitize() {
        assertEquals("testsanitize 123 \$",xssSanitizer.sanitize (input))
    }
}
