/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import net.hedtech.banner.service.ServiceBase

class ConfigUserPreferenceService extends ServiceBase {
    static transactional = true


    public def getUserPreferenceByConfigNameAppIdAndPidm(String configName, String appId, Integer pidm = null) {
        def configUserPreference
        if (appId) {
            configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(configName, pidm, appId)
        } else {
            configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(configName, pidm, 'GLOBAL')
        }
        if (!configUserPreference) {
            if (appId) {
                configUserPreference = ConfigProperties.fetchByConfigNameAndAppId(configName, appId)

            } else{
                configUserPreference = ConfigProperties.fetchByConfigNameAndAppId(configName, 'GLOBAL')
            }
        }
        return configUserPreference
    }
}
