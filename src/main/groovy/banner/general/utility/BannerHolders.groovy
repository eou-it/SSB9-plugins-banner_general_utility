/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
import grails.util.Holders
import groovy.util.logging.Slf4j
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
                if (!(result.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator)) {
                    if ( !(result.banner.mep.configurations instanceof org.grails.config.NavigableMap.NullSafeNavigator) ) {
                        final List<String> meppedConfigs = result.banner.mep.configurations
                        if (meppedConfigs && MEPPED_CONFIG_OBJ.get( 'config' ) != null) {
                            result = MEPPED_CONFIG_OBJ.get( 'config' )
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

    def static getMeppedConfigObjs () {
        return MEPPED_CONFIG_OBJ
    }

    def static setBaseConfigObjs ( Config config ) {
        MEPPED_CONFIG_OBJ.put('BASE_CONFIG', config)
    }

}
