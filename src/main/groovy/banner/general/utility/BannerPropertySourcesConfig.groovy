package banner.general.utility

import org.grails.config.NavigableMap
import org.grails.config.PropertySourcesConfig
import org.springframework.web.context.request.RequestContextHolder

class BannerPropertySourcesConfig extends PropertySourcesConfig {
    @Override
    Object get(Object key) {
        final boolean isWebRequest = ( RequestContextHolder.getRequestAttributes() != null )
        Object result = super.get( key )
        if ( isWebRequest ) {
            String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
            if ( sessionMepCode ) {
                if ( !( super.get( """${sessionMepCode}.${key}""" ) instanceof NavigableMap.NullSafeNavigator ) ) {
                    result = super.get( """${sessionMepCode}.${key}""" )
                }
            }
        }
        return result
    }
}
