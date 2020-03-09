/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
import grails.util.Holders
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
        // try, catch and finally block, returning the config object in the finally block, we know that
        // request attributes from RequestContextHolder will return null when the
        // call to this method from BootStrap or Cron jobs etc., in this case it should return the non MEP'd config object.
        try {
            final boolean isWebRequest = ( RequestContextHolder.getRequestAttributes() != null )

            // Check if this call is from web-request
            if ( isWebRequest ) {
                String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
                setConfiguration( sessionMepCode, result )
            }
        } catch (Exception e) {
            log.debug( "Exception in BannerHolders.setConfiguration()", e.stackTrace );
        } finally {
            // Returning the Config object.
            return result
        }
    }

    /**
     * This is the private static method and used to MEP the config object for MEP environment.
     * If the MEP code is empty and config object is not null then it will process the config object and will replace the
     * config properties with respect to MEP code.
     *
     * @param mep       String type of MEP code
     * @param config    Config type of config object.
     */
    private static void setConfiguration(mep, config) {
        try {
            if ( mep != null && config != null ) {
                final String mepKey = "${mep}.";
                final String defaultKey = "DEFAULT.";
                final ConfigSlurper configSlurper = new ConfigSlurper()
                final Map<Object, Object> configMap = [:]

                for ( def entry : config.entrySet() ) {
                    configMap.put( entry.getKey(), config.get( entry.getKey() ) )
                }

                Map<Object, Object> foundMap = configMap.findAll { key, value ->
                    key.toLowerCase().startsWith ( mepKey.toLowerCase() )
                }

                foundMap.each { foundKey, foundValue ->
                    configMap.each { key, value ->
                        if ( key.equals( foundKey.minus( mepKey ) ) ) {
                            if ( !Holders.getGrailsApplication().config.get( "${defaultKey}" + key ) ) {
                                Properties defaultToMerge = new Properties()
                                defaultToMerge.put ( "${defaultKey}" + key, value );
                                config.merge( configSlurper.parse( defaultToMerge ) )
                            }

                            Properties propertyToMerge = new Properties()
                            propertyToMerge.put( key, foundValue )
                            config.merge( configSlurper.parse( propertyToMerge ) )
                        } else {
                            if ( Holders.getGrailsApplication().config.get( "${defaultKey}" + key ) ) {
                                Properties propertyToMerge = new Properties()
                                propertyToMerge.put( key, Holders.getGrailsApplication().config.get( "${defaultKey}" + key ) )
                                config.merge( configSlurper.parse( propertyToMerge ) )
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug( "Exception in BannerHolders.setConfiguration()", e.stackTrace );
        }
    }

}
