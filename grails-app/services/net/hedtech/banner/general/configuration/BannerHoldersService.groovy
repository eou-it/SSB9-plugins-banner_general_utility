/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import banner.general.utility.BannerHolders
import grails.config.Config
import grails.util.Holders
import org.grails.config.PropertySourcesConfig

class BannerHoldersService {

    def multiEntityProcessingService
    ConfigSlurper configSlurper = new ConfigSlurper()

    private def setMepCodes () {
        if ( BannerHolders.getMepCodes().isEmpty() ) {
            def mepCodes = multiEntityProcessingService.getMepCodes()
            mepCodes.each {
                BannerHolders.addMepCodes( it.code )
            }
        }
    }

    def setMeppedConfigObjs () {
        setMepCodes()
        final List<String> mepCodes = BannerHolders.getMepCodes()

        final Map<Object, Object> configMap = [:]

        final Config cong = Holders.grailsApplication.config
        for ( def entry : cong ) {
            configMap.put( entry.getKey(), cong.get( entry.getKey() ) )
        }

        Map<String, Map<Object, Object>> meppedConfigObjects = new HashMap<String, Map<Object, Object>>()

        mepCodes.each { String mepCode ->
            final Map<Object, Object> originalConfig = configMap.clone()
            configMap.each { String key, value ->
                if ( key.startsWith( "${mepCode}." ) ) {
                    originalConfig.remove( key )
                    originalConfig.put( key.minus( "${mepCode}." ), value )
                }
            }
            meppedConfigObjects.put( mepCode, originalConfig )
        }

        Map<String, Map<Object, Object>> finalMap = meppedConfigObjects.clone()
        meppedConfigObjects.each { key, value ->
            mepCodes.each { mepCode ->
                finalMap.get( key ).remove( mepCode )
            }
        }

        finalMap.each { key, value ->
            Config configToSave = convertMapToConfig( value )
            BannerHolders.getMeppedConfigObjs().put( key, configToSave )
        }

        Config defaultConfig = convertMapToConfig( configMap )
        BannerHolders.getMeppedConfigObjs().put( BannerHolders.DEFAULT_MEP_KEY, defaultConfig )

    }

    private Config convertMapToConfig ( Map<Object, Object> configMap ) {
        Config config = new PropertySourcesConfig()
        configMap.each { key, value ->
            config.put(key, value)
        }
        return config
    }

}
