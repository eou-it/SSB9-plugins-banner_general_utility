/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import banner.general.utility.BannerHolders
import banner.general.utility.BannerPropertySourcesConfig
import grails.config.Config
import grails.util.Holders

/**
 * The service class to create out customised config object for MEPed DB.
 */
class BannerHoldersService {

    /**
     * This method will clone the Config object to Map and convert all properties to 'BannerPropertySourcesConfig'.
     * @return
     */
    def setMeppedConfigObj () {
        final Map<Object, Object> configMap = [:]

        final Config config = Holders.grailsApplication.config
        for ( def entry : config ) {
            configMap.put( entry.getKey(), config.get( entry.getKey() ) )
        }

        BannerHolders.getMeppedConfigObjs().put('config', convertMapToConfig(configMap))
    }

    /**
     * This method will convert Mep of config properties to "BannerPropertySourcesConfig".
     * @param configMap Map of config object
     * @return Config object with type of BannerPropertySourcesConfig
     */
    private Config convertMapToConfig ( Map<Object, Object> configMap ) {
        Config config = new BannerPropertySourcesConfig()
        configMap.each { key, value ->
            config.put(key, value)
        }
        return config
    }

}
