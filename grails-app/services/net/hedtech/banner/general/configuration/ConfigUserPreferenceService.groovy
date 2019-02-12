/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase


@Transactional
class ConfigUserPreferenceService extends ServiceBase {

    def grailsApplication
    def sessionFactory

    private static String CONFIGNAME_LOCALE = "locale"

    private static String APPID_GLOBAL = "GLOBAL"


    public static Locale getUserLocale() {
        String userLocale
        Locale selectedUserLocale
        Integer pidm = BannerGrantedAuthorityService.getPidm()
        def userConfig = getUserPreferenceByConfigNameAppIdAndPidm(CONFIGNAME_LOCALE, APPID_GLOBAL, pidm)
        if (userConfig && userConfig.configValue) {
            userLocale = userConfig.configValue
            if (userLocale.contains("_") || userLocale.contains("-")) {
                String[] tokens = userLocale.split("-|\\_")
                selectedUserLocale = new Locale(tokens[0], tokens[1].toUpperCase())
            } else {
                selectedUserLocale = new Locale(userLocale)
            }
        }
        log.debug("User locale is = ${selectedUserLocale}")
        return selectedUserLocale
    }


    public static getUserPreferenceByConfigNameAppIdAndPidm(String configName, String appId, Integer pidm = null) {
        log.debug("Fetching config with config name = ${ configName } and appId = ${ appId }")
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
        log.debug("Fetched config with config name = ${ configName } and value = ${ userConfiguration?.configValue }")
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
            log.error('SaveLocale failed with error ', ae)
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
            }
        }
        log.debug("Banner Supported Locales are before = ${supportedLocales}")
        supportedLocales.unique()
        log.debug("Banner Supported Locales are after = ${supportedLocales}")
        return supportedLocales
    }
}
