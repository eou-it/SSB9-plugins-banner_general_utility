/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
import grails.util.Holders
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import org.springframework.web.context.request.RequestContextHolder

/**
 * Customized Holders class, and this singleton will used to support the MEP environment for SS-Configurations.
 * This class is used in the BootStrap.groovy file to override 'Holders.config' which will internally calls the 'getConfig'.
 */
@Slf4j
@Singleton
public class BannerHolders {

    /**
     * This static method is used to get Config object for MEP environment.
     *
     * @return Config   Config type of config object.
     */
    public static Config getConfig () {
        Config result = Holders.getGrailsApplication().config
        // try and finally block, returning the config object in the finally block, we know that
        // RequestContextHolder.currentRequestAttributes() method will throws an exception when the
        // call to this block from BootStrap or Cron jobs etc., in this case it should return the non mep'd config object.
        try {
            String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
            setConfiguration( sessionMepCode, result )
        } finally {
            // Returning the Config object.
            return result
        }
    }

    /**
     * This is the private synchronized static method and used to MEP the config object for MEP environment.
     * If the MEP code is empty and config object is not null then it will process the config object and will replace the
     * config properties with respect to MEP code.
     *
     * @param mep       String type of MEP code
     * @param config    Config type of config object.
     */
    @Synchronized
    private static void setConfiguration(mep, config) {
        try {
            if ( mep != null && config != null ) {
                final String mepKey = "_${mep}";
                final ConfigSlurper configSlurper = new ConfigSlurper()
                final Map<Object, Object> configMap = [:]

                for ( def entry : config.entrySet() ) {
                    configMap.put( entry.getKey(), config.get( entry.getKey() ) )
                }

                def foundMap = configMap.findAll { key, value ->
                    key.toLowerCase().endsWith ( mepKey.toLowerCase() )
                }

                foundMap.each { foundKey, foundValue ->
                    configMap.each { key, value ->
                        if ( key.equals( foundKey.minus( mepKey ) ) ) {
                            Properties propertyToMerge = new Properties()
                            propertyToMerge.put( key, foundValue )
                            config.merge( configSlurper.parse( propertyToMerge ) )
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug( "Exception in BannerHolders.setConfiguration()", e.stackTrace );
        }
    }

}
