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
        try {
            if (isWebRequest) {
                String sessionMepCode = RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")
                if ( sessionMepCode && !(super.get( "banner.mep.configurations" ) instanceof NavigableMap.NullSafeNavigator) ) {
                    List<String> configList = super.get( "banner.mep.configurations" )
                    if ( configList && configList.get(0)?.toLowerCase() == 'all' && !( configList instanceof NavigableMap.NullSafeNavigator ) ) {
                        if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                            result = getResultForMep( result, key, sessionMepCode )
                        }
                    } else if ( configList && !( configList instanceof NavigableMap.NullSafeNavigator ) ) {
                        Config configDB = BannerHolders.getMeppedConfigObjs().get('mepConfigList')
                        if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                            if ( configDB.containsKey(key) ) {
                                result = getResultForMep( result, key, sessionMepCode )
                            }
                        }
                    }
                }
            }
        } catch (e) {
            log.error( "Error in BannerPropertySorucesConfig key = ${key}" )
        } finally {
            return result
        }
    }

    private Object getResultForMep ( result, key, sessionMepCode ) {
        if ( result instanceof NavigableMap){
            result =  getInnerPropertyConfigMap(sessionMepCode, result, key, key)
        } else {
            result = super.get("${sessionMepCode}.${key}")
        }
        return result
    }

    private def getInnerPropertyConfigMap(sessionMepCode, propertyConfigkey, parameterKey, parentkey){
        NavigableMap propertyMap = new NavigableMap()
        ConfigObject innerPropertyMap = new ConfigObject()
        ConfigSlurper configSlurper = new ConfigSlurper()
        Properties property = new Properties()
        propertyConfigkey.each{entrykey, entryvalue ->
            if (entryvalue instanceof NavigableMap){
                innerPropertyMap << getInnerPropertyConfigMap(sessionMepCode, entryvalue, "${parameterKey}.${entrykey}", parentkey)
                propertyMap.merge(innerPropertyMap)
            } else {
                def value = super.get("${sessionMepCode}.${parameterKey}.${entrykey}")
                def configValue = !(value instanceof NavigableMap.NullSafeNavigator) ?: grails.util.Holders.grailsApplication.config.get("${parameterKey}.${entrykey}")
                String configKey = "${parameterKey}.${entrykey}".minus("${parentkey}.")
                property.put(configKey, configValue)
                propertyMap = configSlurper.parse(property)
            }
        }
        return propertyMap
    }
}
