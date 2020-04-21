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
                if (sessionMepCode) {
                    List<String> configList = super.get( "banner.mep.configurations" )
                    if ( configList && configList.get(0)?.toLowerCase() == 'all' ) {
                        if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                            if ( result instanceof NavigableMap){
                                //result.merge( super.get("${sessionMepCode}.${key}") )
                                result =  getInnerPropertyConfigMap(sessionMepCode, result, key, key)
                            } else {
                                result = super.get("${sessionMepCode}.${key}")
                            }
                        }
                    } else if ( configList ) {
                        Config configDB = BannerHolders.getMeppedConfigObjs().get('mepConfigList')
                        if (!(super.get("${sessionMepCode}.${key}") instanceof NavigableMap.NullSafeNavigator)) {
                            if ( configDB.containsKey(key) ) {
                                if ( result instanceof NavigableMap  ) {
                                    configList.each { def mepKey ->
                                        if ( configDB.containsProperty("${mepKey}") ) {
                                            ConfigSlurper configSlurper = new ConfigSlurper()
                                            Properties properties = new Properties()
                                            properties.put("${mepKey}".substring("${mepKey}".indexOf('.') + 1), super.get("${sessionMepCode}.${mepKey}"))
                                            result.merge( configSlurper.parse(properties) )
                                        }
                                    }
                                } else {
                                    result = super.get("${sessionMepCode}.${key}")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e) {
            log.error( "Error in BannerPropertySorucesConfig key = ${key}", e.printStackTrace() )
        } finally {
            return result
        }
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
                def configValue = super.get("${sessionMepCode}.${parameterKey}.${entrykey}") ?: grails.util.Holders.grailsApplication.config.get("${parameterKey}.${entrykey}")
                String configKey = "${parameterKey}.${entrykey}".minus("${parentkey}.")
                property.put(configKey, configValue)
                propertyMap = configSlurper.parse(property)
            }
        }
        return propertyMap
    }
}
