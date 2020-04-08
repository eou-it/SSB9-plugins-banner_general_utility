/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.utility

import grails.config.Config
import org.grails.config.NavigableMap
import org.grails.config.PropertySourcesConfig
import org.springframework.web.context.request.RequestContextHolder

/**
 * This class is extends the PropertySourcesConfig to customize for getting the "MEPed" configurations.
 */
class BannerPropertySourcesConfig extends PropertySourcesConfig {
    @Override
    Object get(Object key) {
        final boolean isWebRequest = (RequestContextHolder.getRequestAttributes() != null)
        Object result = super.get(key)
        if (isWebRequest) {
            String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
            if (sessionMepCode) {
                List<String> configList = super.get( "banner.mep.configurations" )
                if ( configList && configList.get(0)?.toLowerCase() == 'all' ) {
                    if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                        result = super.get("${sessionMepCode}.${key}")
                    }
                } else if ( configList ) {
                    Config configDB = BannerHolders.getMeppedConfigObjs().get('mepConfigList')
                    if ( !(configDB."${key}" instanceof NavigableMap.NullSafeNavigator) ) {
                        if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                            result = super.get("${sessionMepCode}.${key}")
                        }
                    }
                }
            }
        }
        return result
    }
}
