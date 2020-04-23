/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import banner.general.utility.BannerHolders
import banner.general.utility.BannerPropertySourcesConfig
import grails.config.Config
import grails.util.Holders
import org.grails.config.NavigableMap
import org.grails.config.PropertySourcesConfig

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

        final Config config = BannerHolders.getMeppedConfigObjs()?.get( 'BASE_CONFIG' )
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

    public def setBaseConfig ( ) {
        final Map<Object, Object> configMap = [:]

        final Config config = Holders.config
        for ( def entry : config ) {
            if ( !entry.getKey().startsWith( 'DEFAULT.' ) ) {
                configMap.put( entry.getKey(), config.get( entry.getKey() ) )
            }
        }

        Config pConfig = new PropertySourcesConfig()
        BannerHolders.clearOriginalNavigableMap()
        configMap.each { key, value ->
            pConfig.put(key, value)
            if ( value instanceof NavigableMap ) {
                BannerHolders.setOriginalNavigableMap( key, value )
            }
        }

        List<String> mepConfigList = configMap.get( "banner.mep.configurations" )
        if ( mepConfigList ) {
            ConfigSlurper configSlurper = new ConfigSlurper()
            Config configDB = new PropertySourcesConfig()
            mepConfigList.each { configDB.merge( configSlurper.parse( it ) ) }
            BannerHolders.getMeppedConfigObjs().put('mepConfigList', configDB)
        }

        BannerHolders.setBaseConfigObjs(pConfig)
    }

}
