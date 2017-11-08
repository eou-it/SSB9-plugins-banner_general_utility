/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestContextHolder

class ConfigUserPreferenceService extends ServiceBase {

    static transactional = true

    def grailsApplication

    private static final LOGGER = Logger.getLogger(ConfigUserPreferenceService.class.name)

    private static String CONFIGNAME_LOCALE = "locale"

    private static String APPID_GLOBAL = "GLOBAL"


    public static Locale getUserLocale() {
        Locale userLocale
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        def currentLocale = LocaleContextHolder.getLocale()
        def userConfig = getUserPreferenceByConfigNameAppIdAndPidm(CONFIGNAME_LOCALE, APPID_GLOBAL, pidm)
        if (userConfig && userConfig.configValue) {
            userLocale = new Locale(userConfig.configValue)
        } else {
            userLocale = currentLocale
        }
        return userLocale
    }


    public static getUserPreferenceByConfigNameAppIdAndPidm(String configName, String appId, Integer pidm = null) {
        LOGGER.debug("Fetching config with config name = ${ configName } and appId = ${ appId }")
        ConfigProperties configProperties
        def userConfiguration
        if (appId) {
            configProperties = ConfigProperties.fetchByConfigNameAndAppId(configName, appId)
        }
        if (!configProperties) {
            configProperties = ConfigProperties.fetchByConfigNameAndAppId(configName, APPID_GLOBAL)
        }
        if (configProperties?.userPreferenceIndicator) {                // GUROCFG_USERPREF_IND = Y
            ConfigUserPreference configUserPreference
            if (appId) {
                configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(configName, pidm, appId)
            }
            if (!configUserPreference) {
                configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(configName, pidm, 'GLOBAL')
            }
            userConfiguration = configUserPreference
        } else {                                                     // GUROCFG_USERPREF_IND = 'N"
            userConfiguration = configProperties
        }
        return userConfiguration
    }


    public def saveLocale(map) {
        println map
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        ConfigUserPreference configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(CONFIGNAME_LOCALE, pidm, APPID_GLOBAL)

        if (configUserPreference && configUserPreference.id) {
            configUserPreference.setConfigValue(map.locale)
        } else {
            ConfigApplication configApp = ConfigApplication.fetchByAppId(APPID_GLOBAL)
            ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId(CONFIGNAME_LOCALE, APPID_GLOBAL)
            configUserPreference.setConfigApplication(configApp)
            configUserPreference.setConfigName(configProperties?.getConfigName())
            configUserPreference.setConfigType(configProperties?.getConfigType())
            configUserPreference.setConfigValue(map.locale)
            configUserPreference.setPidm(pidm)
        }
        String status
        try {
            configUserPreferenceService.create(configUserPreference)
            status = 'success'
        }
        catch (ApplicationException ae) {
            status = 'failure'
        }
        return [status : status]
    }
}
