/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import groovy.sql.Sql
import net.hedtech.banner.db.BannerConnection
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

class ConfigUserPreferenceService extends ServiceBase {

    static transactional = true

    def grailsApplication

    private static final LOGGER = Logger.getLogger(ConfigUserPreferenceService.class.name)

    private static String CONFIGNAME_LOCALE = "locale"

    private static String APPID_GLOBAL = "GLOBAL"


    public static Locale getUserLocale() {
        String userLocale
        Locale selcetedUserLocale
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        def userConfig = getUserPreferenceByConfigNameAppIdAndPidm(CONFIGNAME_LOCALE, APPID_GLOBAL, pidm)
        if (userConfig && userConfig.configValue) {
            userLocale = userConfig.configValue
            if (userLocale.contains("_") || userLocale.contains("-")) {
                String[] tokens = userLocale.split("-|\\_")
                selcetedUserLocale = new Locale(tokens[0], tokens[1].toUpperCase())
            } else {
                selcetedUserLocale = new Locale(userLocale)
            }
        }
        return selcetedUserLocale
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
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        ConfigUserPreference configUserPreference = ConfigUserPreference.fetchByConfigNamePidmAndAppId(CONFIGNAME_LOCALE, pidm, APPID_GLOBAL)

        if (configUserPreference && configUserPreference.id) {
            configUserPreference.setConfigValue(map.locale)
        } else {
            ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId(CONFIGNAME_LOCALE, APPID_GLOBAL)
            configUserPreference = new ConfigUserPreference(
                    pidm: pidm,
                    configApplication: configProperties.getConfigApplication()
            )
            configUserPreference.setConfigName(configProperties?.getConfigName())
            configUserPreference.setConfigType(configProperties?.getConfigType())
            configUserPreference.setConfigValue(map.locale)
        }
        String status
        try {
            this.create(configUserPreference)
            status = 'success'
        }
        catch (ApplicationException ae) {
            LOGGER.error(ae)
            status = 'failure'
        }
        return [status: status]
    }


    public List getAllBannerSupportedLocales() {
        List supportedLocales = []
        List localeListFromDB = []
        def newlocale
        String localeDisplayName
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("Select LOCALE from NLSUSER.JLOC2ORA where LOCALE <> '00-00'") { it ->
            localeListFromDB.add(it.LOCALE)
        }
        localeListFromDB.each { eachLocale ->
            if (eachLocale.contains("_") || eachLocale.contains("-")) {
                String[] tokens = eachLocale.split("-|\\_")
                newlocale = new Locale(tokens[0], tokens[1].toUpperCase())
            } else {
                newlocale = new Locale(eachLocale)
            }
            localeDisplayName = newlocale.getDisplayName()
            if (localeDisplayName && (!localeDisplayName.equalsIgnoreCase(newlocale.toString())) ) {
                supportedLocales.add([locale: newlocale, description: localeDisplayName])
                supportedLocales = supportedLocales.toUnique { it.locale }
            }
        }
        LOGGER.debug("Banner Supported Locales are = ${supportedLocales}")
        return supportedLocales
    }

}
