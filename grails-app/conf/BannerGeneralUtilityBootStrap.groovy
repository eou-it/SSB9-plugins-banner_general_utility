/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

import grails.util.Environment
import grails.util.Holders
import net.hedtech.banner.general.configuration.GeneralPageRoleMappingService
import net.hedtech.banner.utility.GeneralMenu
import org.apache.log4j.Logger

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * -- Fetching the configuration from DB and setting in Holders.Config using ConfigPropertiesService
 * */

class BannerGeneralUtilityBootStrap {

    def log = Logger.getLogger(BannerGeneralUtilityBootStrap.class.name)

    def menuService
    def configPropertiesService
    def generalPageRoleMappingService
    def springSecurityService

    def init = { servletContext ->

        if (GeneralMenu.isEnabled()) {
            def dbInstanceName = menuService.getInstitutionDBInstanceName()
            servletContext.setAttribute("dbInstanceName", dbInstanceName)
        }
        if (Environment.current != Environment.TEST) {
            configPropertiesService.seedDataToDBFromConfig()
            configPropertiesService.seedUserPreferenceConfig()

            // Seed the data for InterceptUrlMap at server startup.
            ArrayList seedDataKey = Holders.config.ssconfig.app.seeddata.keys
            if (seedDataKey && seedDataKey.contains(['grails.plugin.springsecurity.interceptUrlMap'])) {
                generalPageRoleMappingService.seedInterceptUrlMapAtServerStartup()
                springSecurityService.clearCachedRequestmaps()
            }
        }
        configPropertiesService.setConfigFromDb()
        configPropertiesService.setTransactionTimeOut()
        configPropertiesService.setLoginEndPointUrl()
        configPropertiesService.setLogOutEndPointUrl()
        configPropertiesService.setGuestLoginEnabled()
    }

    def destroy = {
        // no-op
    }

}
