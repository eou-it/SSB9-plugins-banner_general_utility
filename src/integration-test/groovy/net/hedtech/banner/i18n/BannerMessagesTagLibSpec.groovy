/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.i18n

import grails.test.spock.IntegrationSpec
import grails.test.mixin.TestFor
import spock.lang.Specification

//class StubMessageSource extends BannerMessageSource {
//    static def BAD_VALUE = 'Sentinel/bad value that should not be in messages'
//    Map getAllProperties( Locale locale ) {
//        messages = [ "locale": locale, "about.banner.title": "Message source test stub" ]
//        [ 'timestamp': '0', 'properties': 'messages', BAD_VALUE: BAD_VALUE ]
//    }
//}
//
// TODO: Have not found a way to call applyTemplate using the actual BannerMessageSource in these tests,
// instead of the grails mock messageSource
// For now, the tests in banner_ui_ss_testapp will have to suffice
@TestFor(BannerMessagesTagLib) // allows applyLayout
class BannerMessagesTagLibSpec extends Specification {

//    def messageSource
//    def grailsApplication
//
//    def setup() {
//ctx.registerMockBean("messageSource", messageSource) // from AbstractGrailsTagTests, added via TestFor
////        defineBeans {
////            messageSource(messageSource) // use the real BannerMessageSource inside the test, not the StaticMessageSource - doesn't work
////        }
//    }
//
//    def cleanup() {
//    }
//
//
//    void "gets messages"() {
//        given:
//        output = applyTemplate('<g:bannerMessages/>')
//        expect:
//        output.startsWith( "<window.i18n" )
//        output.contains( "about.banner.title" )
//        output.contains( StubMessageSource.BAD_VALUE ) // !
//        output.length() == 0
//    }
//
//    void "supports i18n_setup tag"() {
//        given:
//        oldway = applyTemplate('<g:i18n_setup/>')
//        newway = applyTemplate('<g:bannerMessages/>')
//        expect:
//        oldway == newway
//        oldway.length() == 0 // !
//    }
//
//    void "calling directly"() {
//        def tag = new BannerMessagesTagLib()
//
//    }
}
