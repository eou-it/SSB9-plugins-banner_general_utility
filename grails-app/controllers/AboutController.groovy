/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.converters.JSON

class AboutController {

    def aboutService

    def data() {

        render aboutService.getAbout() as JSON

    }
}
