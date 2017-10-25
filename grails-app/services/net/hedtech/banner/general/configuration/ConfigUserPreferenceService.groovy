/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.context.i18n.LocaleContextHolder

class ConfigUserPreferenceService extends ServiceBase {

    static transactional = true
    def grailsApplication
    private static final LOGGER = Logger.getLogger(ConfigUserPreferenceService.class.name)


    public Locale getUserLocale() {
        Locale userLocale
        String configName = "locale"
        String  appId = grailsApplication.metadata['app.appId']
        Integer pidm  = BannerGrantedAuthorityService.getPidm()
        def currentLocale = LocaleContextHolder.getLocale()

        def userConfig = getUserPreferenceByConfigNameAppIdAndPidm(configName, appId, pidm)
        if (userConfig && userConfig.configValue){
            userLocale = new Locale(userConfig.configValue)
        } else {
            userLocale = currentLocale
        }
        return userLocale
    }


    public def getUserPreferenceByConfigNameAppIdAndPidm(String configName, String appId, Integer pidm = null) {
        LOGGER.debug("Fetching config with config name = ${configName } and appId = ${appId}")
        ConfigProperties configProperties
        def userConfiguration
        if (appId) {
            configProperties = ConfigProperties.fetchByConfigNameAndAppId(configName, appId)
        }
        if(!configProperties) {
            configProperties = ConfigProperties.fetchByConfigNameAndAppId(configName, 'GLOBAL')
        }
        if (configProperties?.userPreferenceIndicator) {                // GUROCFG_USERPREF_IND = Y
            ConfigUserPreference configUserPreference
            if (appId) {
                configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(configName, pidm, appId)
            }
            if(!configUserPreference){
                configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(configName, pidm, 'GLOBAL')
            }
            userConfiguration = configUserPreference
        } else {                                                     // GUROCFG_USERPREF_IND = 'N"
            userConfiguration = configProperties
        }
        return userConfiguration
    }
}
