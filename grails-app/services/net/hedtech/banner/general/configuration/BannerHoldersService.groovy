/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import banner.general.utility.BannerHolders
import banner.general.utility.BannerPropertySourcesConfig
import grails.config.Config
import grails.util.Holders

class BannerHoldersService {

    def setMeppedConfigObj () {
        final Map<Object, Object> configMap = [:]

        final Config cong = Holders.grailsApplication.config
        for ( def entry : cong ) {
            configMap.put( entry.getKey(), cong.get( entry.getKey() ) )
        }

        BannerHolders.getMeppedConfigObjs().put('config', convertMapToConfig(configMap))
    }

    private Config convertMapToConfig ( Map<Object, Object> configMap ) {
        Config config = new BannerPropertySourcesConfig()
        configMap.each { key, value ->
            config.put(key, value)
        }
        return config
    }

}
