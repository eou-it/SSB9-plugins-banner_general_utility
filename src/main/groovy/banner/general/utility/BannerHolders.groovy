package banner.general.utility

import grails.config.Config
import grails.core.GrailsApplication
import grails.plugins.GrailsPluginManager
import grails.util.Holder
import grails.util.Holders
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.core.io.support.GrailsFactoriesLoader
import org.grails.core.support.GrailsApplicationDiscoveryStrategy
import org.springframework.context.ApplicationContext
import org.springframework.context.Lifecycle
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder

@Slf4j
@Singleton
public class BannerHolders {

    public static Config getConfig() {
        Config result = Holders.getGrailsApplication().config
        try {
            String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
            setConfiguration(sessionMepCode, result)
        } finally {
            return result
        }
    }

    /**
     * This method is used to set configuration
     * @param mep
     */
    @Synchronized
    private static void setConfiguration(mep, config) {
        try {
            if (mep != null && config != null) {
                final String mepKey = "_${mep}";
                final ConfigSlurper configSlurper = new ConfigSlurper()
                final Map<Object, Object> configMap = [:]

                for (def entry : config.entrySet()) {
                    configMap.put(entry.getKey(), config.get(entry.getKey()))
                }

                def foundMap = configMap.findAll { key, value ->
                    key.toLowerCase().contains(mepKey.toLowerCase())
                }

                foundMap.each { foundKey, foundValue ->
                    configMap.each { key, value ->
                        if (key.equals(foundKey.minus(mepKey))) {
                            Properties propertyToMerge = new Properties()
                            propertyToMerge.put(key, foundValue);
                            config.merge(configSlurper.parse(propertyToMerge))
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Exception in BannerHolders.setConfiguration()", e.stackTrace);
        }
    }

}
