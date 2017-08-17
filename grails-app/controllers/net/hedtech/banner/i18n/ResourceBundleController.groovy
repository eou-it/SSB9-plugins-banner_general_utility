/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.i18n

import grails.converters.JSON

class ResourceBundleController {

    def messageSource
    def resourceBundleService

    def list() {
        def result
        if (params.name || params.id) {
            return show()
        } else {
            result = resourceBundleService.list()
        }
        render result as JSON
    }

    def show() {
        def result = resourceBundleService.get(params.id?:params.name, params.locale)
        render result as JSON
    }

    def save() {
        def data = request.JSON
        def result = resourceBundleService.save(data)
        render result as JSON
    }

}
