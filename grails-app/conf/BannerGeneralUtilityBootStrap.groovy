/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

import org.apache.log4j.Logger
import net.hedtech.banner.utility.GeneralMenu


/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * -- Fetching the configuration from DB and setting in Holders.Config using ConfigPropertiesService
 * */

class BannerGeneralUtilityBootStrap {

    def log = Logger.getLogger(this.getClass())

    def menuService
    def configPropertiesService

    def init = { servletContext ->

        if(GeneralMenu.isEnabled()){
            def dbInstanceName = menuService.getInstitutionDBInstanceName();
            servletContext.setAttribute("dbInstanceName", dbInstanceName)
        }

        configPropertiesService.setConfigFromDb()
    }

    def destroy = {
        // no-op
    }

}
