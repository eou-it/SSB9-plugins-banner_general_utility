/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
import groovy.util.logging.Slf4j
import org.grails.config.NavigableMap
import org.grails.config.PropertySourcesConfig
import org.springframework.web.context.request.RequestContextHolder

/**
 * This class is extends the PropertySourcesConfig to customize for getting the "MEPed" configurations.
 */
@Slf4j
class BannerPropertySourcesConfig extends PropertySourcesConfig {
    @Override
    Object get(Object key) {
        final boolean isWebRequest = (RequestContextHolder.getRequestAttributes() != null)
        Object result = super.get(key)
        if ( result instanceof NavigableMap ) {
            result = cloneNavigableMap( BannerHolders.getOriginalNavigableMap( key ) )
        }
        try {
            if (isWebRequest) {
                String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
                if (sessionMepCode) {
                    List<String> configList = super.get( "banner.mep.configurations" )
                    if ( configList && configList.get(0)?.toLowerCase() == 'all' ) {
                        if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                            if ( result instanceof NavigableMap ) {
                                result.merge( super.get("${sessionMepCode}.${key}") )
                            } else {
                                result = super.get("${sessionMepCode}.${key}")
                            }
                        }
                    } else if ( configList ) {
                        Config configDB = BannerHolders.getMeppedConfigObjs().get('mepConfigList')
                        if ( !(configDB."${key}" instanceof NavigableMap.NullSafeNavigator) ) {
                            if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                                if ( result instanceof NavigableMap ) {
                                    result.merge( super.get("${sessionMepCode}.${key}") )
                                } else {
                                    result = super.get("${sessionMepCode}.${key}")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e) {
            log.error( "Error in BannerPropertySorucesConfig key = ${key}", e.stackTrace )
        } finally {
            return result
        }
    }

    private NavigableMap cloneNavigableMap ( navigableMap ) {
        NavigableMap map = new PropertySourcesConfig()
        navigableMap.each { key, value ->
            if ( value instanceof NavigableMap ) {
                map.put( key, cloneNavigableMap( value ) )
            } else {
                map.put( key, value )
            }
        }
        return map
    }
}
