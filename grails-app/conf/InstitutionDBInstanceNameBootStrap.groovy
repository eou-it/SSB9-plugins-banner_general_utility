/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

import org.apache.log4j.Logger
import net.hedtech.banner.utility.CommonUIApp


/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * */
class InstitutionDBInstanceNameBootStrap {

    def log = Logger.getLogger(this.getClass())

    def menuService

    def init = { servletContext ->

        if(CommonUIApp.isEnabled()){
            def dbInstanceName = menuService.getInstitutionDBInstanceName();
            servletContext.setAttribute("dbInstanceName", dbInstanceName)
        }
    }

    def destroy = {
        // no-op
    }

}
