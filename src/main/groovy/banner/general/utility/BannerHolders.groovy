/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.db.BannerConnection
import org.grails.config.NavigableMap
import org.springframework.web.context.request.RequestContextHolder

import java.util.concurrent.ConcurrentHashMap

/**
 * Customized Holders class, and this singleton will used to support the MEP environment for SS-Configurations.
 * This class is used in the BootStrap.groovy file to override 'Holders.config' which will internally calls the 'getConfig'.
 */
@Slf4j
@Singleton
public class BannerHolders {
    private static ConcurrentHashMap<String, Config> MEPPED_CONFIG_OBJ = new ConcurrentHashMap<String, Config>()

    /**
     * This static method is used to get Config object for MEP environment.
     *
     * @return Config   Config type of config object.
     */
    public static Config getConfig () {
        Config result = Holders.grailsApplication.config
        // try, catch and finally block, returning the config object in the finally block, we know that
        // request attributes from RequestContextHolder will return null when the
        // call to this method from BootStrap or Cron jobs etc., in this case it should return the non MEP'd config object.
        try {
            final boolean isWebRequest = ( RequestContextHolder.getRequestAttributes() != null )

            if ( isWebRequest ) {
                if (!(Holders.grailsApplication.config.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator)) {
                    String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")

                    if ( !(Holders.grailsApplication.config.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator) ) {
                        final List<String> meppedConfigs = Holders.grailsApplication.config.banner.mep.configurations
                        if (meppedConfigs && meppedConfigs?.get(0) == 'all') {
                            if ( MEPPED_CONFIG_OBJ.get( 'config' ) != null ) {
                                result = MEPPED_CONFIG_OBJ.get( 'config' )
                            }
                        } else if ( meppedConfigs && meppedConfigs?.get( 0 ) != 'all' ) {
                            result = setConfigObject(meppedConfigs, sessionMepCode, result)
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug( "Exception in BannerHolders.setConfiguration()", e.stackTrace );
        } finally {
            // Returning the Config object.
            return result
        }
    }

    /**
     * This method is used when 'banner.mep.configurations' configuration is not empty and not having data as all.
     * In this case the use may defined configuration key in the list which needs to be mepped.
     * @param meppedConfigs List of key to be MEPed
     * @param sessionMepCode Mep code
     * @param result Config object
     * @return MEPed Config object
     */
    private static Config setConfigObject(List<String> meppedConfigs, String sessionMepCode, Config result) {
        meppedConfigs?.each { key ->
            if (Holders.grailsApplication.config."${sessionMepCode}.${key}" instanceof NavigableMap.NullSafeNavigator) {
                if (Holders.grailsApplication.config."DEFAULT.${key}" instanceof NavigableMap.NullSafeNavigator) {
                    ConfigSlurper configSlurper = new ConfigSlurper()
                    Properties properties = new Properties()
                    def defaultKey = "DEFAULT.${key}"
                    def defaultValue = Holders.grailsApplication.config."${key}"
                    properties.put(defaultKey, defaultValue)
                    result.merge(configSlurper.parse(properties))
                }

                ConfigSlurper configSlurper = new ConfigSlurper()
                Properties properties = new Properties()
                properties.put(key, Holders.grailsApplication.config."DEFAULT.${key}")
                result.merge(configSlurper.parse(properties))
            } else {
                ConfigSlurper configSlurper = new ConfigSlurper()
                Properties properties = new Properties()

                if ( Holders.grailsApplication.config."DEFAULT.${key}" instanceof NavigableMap.NullSafeNavigator ) {
                    def defaultKey = "DEFAULT.${key}"
                    def defaultValue = Holders.grailsApplication.config."${key}"
                    properties.put(defaultKey, defaultValue)
                }

                def meppedKey = key
                def meppedValue = Holders.grailsApplication.config."${sessionMepCode}.${key}"
                properties.put(meppedKey, meppedValue)

                result.merge(configSlurper.parse(properties))
            }
        }
        result
    }

    def static getMeppedConfigObjs () {
        return MEPPED_CONFIG_OBJ
    }

}
