/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
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

    private static ConcurrentHashMap<String, Config> MEPPED_CONFIG_OBJS = new ConcurrentHashMap<String, Config>()
    private static List<String> MEP_CODES = new ArrayList<String>()
    static final String DEFAULT_MEP_KEY = "DEFAULT"

    /**
     * This static method is used to get Config object for MEP environment.
     *
     * @return Config   Config type of config object.
     */
    public static Config getConfig () {
        Config result = MEPPED_CONFIG_OBJS.get( DEFAULT_MEP_KEY )
        // try, catch and finally block, returning the config object in the finally block, we know that
        // request attributes from RequestContextHolder will return null when the
        // call to this method from BootStrap or Cron jobs etc., in this case it should return the non MEP'd config object.
        try {
            final boolean isWebRequest = ( RequestContextHolder.getRequestAttributes() != null )

            // Check if this call is from web-request
            if ( isWebRequest ) {
                String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
                if ( MEP_CODES.contains( sessionMepCode ) ) {
                    result = MEPPED_CONFIG_OBJS.get( sessionMepCode )
                }
            }
        } catch (Exception e) {
            log.debug( "Exception in BannerHolders.setConfiguration()", e.stackTrace );
        } finally {
            // Returning the Config object.
            return result
        }
    }

    def static addMepCodes ( mepCode ) {
        MEP_CODES.add(mepCode)
    }

    def static getMepCodes ( ) {
        return MEP_CODES
    }

    def static getMeppedConfigObjs () {
        return MEPPED_CONFIG_OBJS
    }

}
