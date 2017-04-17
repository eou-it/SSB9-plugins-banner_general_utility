package net.hedtech.banner.eneral.configuration

import grails.util.Holders as CH

class ConfigJob {

    def configPropertiesService
    def generalPageRoleMappingService

    static triggers = {
      simple repeatInterval: 900000 // execute job once in 15 minutes
    }

    def execute() {
       println("Running Config Job to update configurations")
       configPropertiesService.setConfigFromDb()
       generalPageRoleMappingService.reset()
       println("Configurations updated")
    }
}
