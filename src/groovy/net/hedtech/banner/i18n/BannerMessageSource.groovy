/*******************************************************************************
 * Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 ******************************************************************************/
package net.hedtech.banner.i18n

import grails.util.Holders as CH
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.beans.factory.NoSuchBeanDefinitionException

import java.text.MessageFormat

import grails.util.CacheEntry
import java.util.List;
import grails.util.Pair;
import org.springframework.core.io.Resource;
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.codehaus.groovy.grails.context.support.ReloadableResourceBundleMessageSource.PropertiesHolder

class BannerMessageSource extends PluginAwareResourceBundleMessageSource {

    static final String APPLICATION_PATH = 'WEB-INF/grails-app/i18n/'

    ExternalMessageSource externalMessageSource

    protected def basenamesExposed
    private ConcurrentMap<Locale, CacheEntry<PropertiesHolder>> bannerCachedMergedPluginProperties = new ConcurrentHashMap<Locale, CacheEntry<PropertiesHolder>>();

    LinkedHashMap normalizedNamesIndex

    def textManagerService

    public def setExternalMessageSource(messageSource){
        if (messageSource) {
            externalMessageSource = messageSource
        }
    }

    private def getBasenamesSuper(){
        def result = []
        // Don't like this but it is avoiding to scan the available resources again
        def listStr = super.toString()
        listStr = listStr.substring(listStr.indexOf('[')+1,listStr.lastIndexOf(']'))
        def basenamesTemp = listStr.split(",")
        basenamesTemp.each {
            if ( it.startsWith(APPLICATION_PATH) ) {
             result << it
            }
        }
        result
    }

    private def initNormalizedIndex(){

        final String APPLICATION_PATH_NORM = 'application/'
        final String PLUGINS_PATH = "/plugins/"
        final String PLUGIN_APP_PATH = "grails-app/i18n/"
        normalizedNamesIndex = [:] as LinkedHashMap
        basenamesExposed = getBasenamesSuper()
        basenamesExposed.each { basename ->
            def norm = APPLICATION_PATH_NORM + basename.minus(APPLICATION_PATH)
            normalizedNamesIndex[norm] = [source: this, basename: basename]
        }
        pluginBaseNames.each { basename ->
            def norm = basename.replace('\\','/')
            norm = norm.substring(norm.indexOf(PLUGINS_PATH)+1)
            norm = norm.minus(PLUGIN_APP_PATH)
            norm = norm.replaceFirst(/-[0-9.]+/,"")
            normalizedNamesIndex[norm.toString()] = [source: this, basename: basename]
        }
        if (externalMessageSource) {
            externalMessageSource.basenamesExposed.each { basename ->
                def norm = "${externalMessageSource.bundleName}/$basename"
                normalizedNamesIndex[norm] = [source: externalMessageSource, basename: basename]
            }
        }
    }

    public def getNormalizedNames(){
        if ( true || !normalizedNamesIndex || normalizedNamesIndex.size() ==0 ) { //
            externalMessageSource?.clearCache()
            initNormalizedIndex()
        }
        normalizedNamesIndex.collect { key, value -> key}
    }

    public def getPropertiesByNormalizedName(name, Locale locale) {
        if (!normalizedNamesIndex || normalizedNamesIndex.size() ==0 ) {
            initNormalizedIndex()
        }
        def match = normalizedNamesIndex[name]
        Properties propertiesMerged = new Properties() //Create a new instance as we do not want to merge into the PropertiesHolder
        if (!match) {
            return propertiesMerged // return empty properties
        }
        def basename = match.basename
        def fnames = match.source.calculateAllFilenames(basename, locale)
        logger.debug "getPropertiesForTM - Locale: $locale Basename: $basename"
        fnames.each{ resource ->
            // Assume the most Specific first
            if (resource.bValue!=null) {
                if (propertiesMerged.isEmpty()) {
                    propertiesMerged << match.source.getProperties(resource.aValue, resource.bValue).properties
                    logger.debug "Initialized propertiesMerged with ${propertiesMerged.size()} keys from resource ${resource.bValue.toString()}. "
                } else  {
                    def propertiesFallback = match.source.getProperties(resource.aValue, resource.bValue).properties
                    def cnt = 0
                    //Add from fallback making sure not to overwrite existing messages
                    propertiesFallback.each { key, value ->
                        if (!propertiesMerged.containsKey(key)){
                            propertiesMerged.put(key,value)
                            cnt++
                        }
                    }
                    logger.debug "Merged $cnt keys from resource ${resource.bValue.toString()}. "
                }
            }
        }
        if (!propertiesMerged) {
            logger.warn "Unable to find resources for $basename, locale $locale"
        }
        propertiesMerged
    }


    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        if (!textManagerService) {
            textManagerService = ServletContextHolder.getServletContext()?.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)?.getBean("textManagerService")
            }
        String msg = textManagerService?.findMessage(code,getLocale(locale.toString()))
        if(msg == null) {
            msg = externalMessageSource?.resolveCodeWithoutArguments(code, locale)
        }
        if(msg == null) {
            return super.resolveCodeWithoutArguments(code, getLocale(locale))    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            return  msg
        }
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        MessageFormat mf = externalMessageSource?.resolveCode(code, locale)
        if(mf == null) {
            return super.resolveCode(code, getLocale(locale))    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            return  mf
        }
    }

    private getLocale(locale) {
        if(CH.config.bannerLocaleVariant instanceof String) {
            Locale loc = new Locale(locale.language, locale.country, CH.config.bannerLocaleVariant)
            return loc
        }
        return locale
    }

    /**
     * Base method implementation taken from PluginAwareResourceBundleMessageSource in grails 2.5.x
     *
     * Added functionality to have application messages override plugin messages.
     *
     * Added functionality to have TextManager messages override all others.
     */
    @Override
    protected PropertiesHolder getMergedPluginProperties(final Locale locale) {
        log.debug "getMergedPluginProperties"
        PluginAwareResourceBundleMessageSource self = this
        def entry = CacheEntry.getValue(bannerCachedMergedPluginProperties, locale, cacheMillis, new Callable<PropertiesHolder>() {
            @Override
            public PropertiesHolder call() throws Exception {
                log.debug "PropertiesHolder call"
                Properties mergedProps = new Properties();
                PropertiesHolder mergedHolder = new PropertiesHolder(self, mergedProps);
                mergeBinaryPluginProperties(locale, mergedProps);
                log.debug "After mergeBinary: ${mergedProps.size()}"

                for (String basename : pluginBaseNames) {
                    List<Pair<String, Resource>> filenamesAndResources = calculateAllFilenames(basename, locale);
                    for (int j = filenamesAndResources.size() - 1; j >= 0; j--) {
                        Pair<String, Resource> filenameAndResource = filenamesAndResources.get(j);
                        if(filenameAndResource.getbValue() != null) {
                            PropertiesHolder propHolder = getProperties(filenameAndResource.getaValue(), filenameAndResource.getbValue());
                            mergedProps.putAll(propHolder.getProperties());
                        }
                    }
                }
                log.debug "After get resources loop: ${mergedProps.size()}"

                // override plugin messages with application messages
                getMergedProperties(locale).properties.each { key ->
                    mergedProps.put key.key, key.value
                }
                log.debug "After get application resources loop: ${mergedProps.size()}"


                mergeTextManagerProperties(locale, mergedProps);
                log.debug "After mergeTextManager: ${mergedProps.size()}}"
                return mergedHolder;
            }
        });
        return entry
    }

    /**
     * Merge all text manager properties for this locale into props
     */
    public void mergeTextManagerProperties( Locale locale, Properties props ) {
        Map entries = new LinkedHashMap()
        props.each  { key, _ ->
            String value = textManagerService.findMessage(key, locale)
            if ( value != null ) {
                entries[key] = value
            }
        }
        log.debug "mergeTextManagerProperties returning ${entries.size()}"
        props.putAll( entries )
    }
}
