/*******************************************************************************
 * Copyright 2013-2019 Ellucian Company L.P. and its affiliates.
 ******************************************************************************/
package net.hedtech.banner.i18n


import grails.plugins.GrailsPlugin
import grails.util.CacheEntry
import grails.util.Environment
import grails.util.Holders
import grails.util.Holders as CH
import grails.util.Pair
import groovy.util.logging.Slf4j
import org.grails.io.support.Resource
import org.grails.plugins.BinaryGrailsPlugin
import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource
import org.grails.spring.context.support.ReloadableResourceBundleMessageSource.PropertiesHolder
import org.grails.web.json.JSONArray

import java.text.MessageFormat
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Slf4j
class BannerMessageSource extends PluginAwareResourceBundleMessageSource {


    static final String APPLICATION_PATH_DEV = "grails-app/i18n/"
    static final String PLUGINS_PATH_DEV = "/plugins/"

    static final String APPLICATION_PATH_PROD = "/WEB-INF/classes/"
    static final String PLUGIN_PATH_PROD = "/WEB-INF/lib/"

    private String messageBundleLocationPattern = "classpath*:messages.properties"

    ExternalMessageSource externalMessageSource

    Map propertiesMap = new HashMap()


    protected List basenamesExposed = []
    protected List pluginBaseNames = []
    private ConcurrentMap<Locale, CacheEntry<PropertiesHolder>> bannerCachedMergedPluginProperties = new ConcurrentHashMap<Locale, CacheEntry<PropertiesHolder>>()
    private ConcurrentMap<Locale, CacheEntry<PropertiesHolder>> bannerCachedMergedBinaryPluginProperties = new ConcurrentHashMap<Locale, CacheEntry<PropertiesHolder>>()

    LinkedHashMap normalizedNamesIndex

    def textManagerService


    public def setExternalMessageSource(messageSource){
        if (messageSource) {
            externalMessageSource = messageSource
        }
    }

    private def setBaseNamesSuper(){
        Resource[] resources
        resources  = new org.grails.io.support.PathMatchingResourcePatternResolver(this.class.getClassLoader()).getResources(messageBundleLocationPattern)
        println('################## this classloader start ####################')
        for (Resource resource : resources) {
            String fileStr = resource.getURL().file.toString()
            println(fileStr)
        }
        println('################## this classloader end ####################')

        Resource[] resources1  = new org.grails.io.support.PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader()).getResources(messageBundleLocationPattern)

        println('################## thread context classloader start ####################')
        for (Resource resource : resources1) {
            String fileStr = resource.getURL().file.toString()
            println(fileStr)
        }
        println('################## thread context classloader end ####################')
        for (Resource resource : resources) {
            String fileStr = resource.getURL().file.toString()
            if(Environment.isDevelopmentEnvironmentAvailable()){
                if(fileStr.contains(APPLICATION_PATH_DEV)) {
                    basenamesExposed.add(fileStr)
                } else {
                    pluginBaseNames.add(fileStr)
                }
            } else {
                if(fileStr.contains(APPLICATION_PATH_PROD)){
                    basenamesExposed.add(fileStr)
                } else {
                    pluginBaseNames.add(fileStr)
                }
            }
        }

    }

    private def initNormalizedIndex(){

        final String APPLICATION_PATH_NORM = 'application/'
        normalizedNamesIndex = [:] as LinkedHashMap

        setBaseNamesSuper()
        synchronized (basenamesExposed) {
            basenamesExposed.each { basename ->
                def norm
                if (Environment.isDevelopmentEnvironmentAvailable()) {
                    norm = APPLICATION_PATH_NORM + basename.minus(APPLICATION_PATH_DEV)
                } else {
                    norm = APPLICATION_PATH_NORM + basename.minus(APPLICATION_PATH_PROD)
                }
                normalizedNamesIndex[norm] = [source: this, basename: basename]
            }
        }
        synchronized (pluginBaseNames) {
            pluginBaseNames.each { basename ->
                def norm = basename.replace('\\', '/')
                if (Environment.isDevelopmentEnvironmentAvailable()) {
                    norm = norm.substring(norm.indexOf(PLUGINS_PATH_DEV) + 1)
                    norm = norm.minus(PLUGINS_PATH_DEV)
                    norm = norm.replaceFirst(/-[0-9.]+/, "")
                } else {
                    norm = norm.substring(norm.indexOf(PLUGIN_PATH_PROD) + 1)
                    norm = norm.minus(PLUGIN_PATH_PROD)
                    norm = norm.replaceFirst(/-[0-9.]+/, "")
                }
                normalizedNamesIndex[norm.toString()] = [source: this, basename: basename]
            }
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
             textManagerService = Holders.grailsApplication.mainContext.getBean("textManagerService")
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
        if (!textManagerService) {
             textManagerService = Holders.grailsApplication.mainContext.getBean("textManagerService")
        }
        String msg = textManagerService?.findMessage(code,getLocale(locale.toString()))
        if(msg != null) {
            return new MessageFormat( msg )
        }
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
                Properties mergedProps = new Properties()
                PropertiesHolder mergedHolder = new PropertiesHolder(self, mergedProps)
                mergeBinaryPluginProperties(locale, mergedProps)
                log.debug "After mergeBinary: ${mergedProps.size()}"

                for (String basename : pluginBaseNames) {
                    List<Pair<String, Resource>> filenamesAndResources = calculateAllFilenames(basename, locale)
                    for (int j = filenamesAndResources.size() - 1; j >= 0; j--) {
                        Pair<String, Resource> filenameAndResource = filenamesAndResources.get(j)
                        if(filenameAndResource.getbValue() != null) {
                            PropertiesHolder propHolder = getProperties(filenameAndResource.getaValue(), filenameAndResource.getbValue())
                            mergedProps.putAll(propHolder.getProperties())
                        }
                    }
                }
                log.debug "After get resources loop: ${mergedProps.size()}"

                // override plugin messages with application messages
                getMergedProperties(locale).properties.each { key ->
                    mergedProps.put key.key, key.value
                }
                log.debug "After get application resources loop: ${mergedProps.size()}"


                mergeTextManagerProperties(locale, mergedProps)
                log.debug "After mergeTextManager: ${mergedProps.size()}}"
                return mergedHolder
            }
        })
        return entry
    }
    /**
     * Base method implementation taken from PluginAwareResourceBundleMessageSource in grails 3.x
     *
     * Added functionality to have application messages override plugin messages.
     *
     * Added functionality to have TextManager messages override all others.
     */
    @Override
    protected PropertiesHolder getMergedBinaryPluginProperties(final Locale locale) {
        PluginAwareResourceBundleMessageSource self = this
        if(bannerCachedMergedBinaryPluginProperties.size()>0){
            bannerCachedMergedBinaryPluginProperties.clear()
        }
        return CacheEntry.getValue(bannerCachedMergedBinaryPluginProperties, locale, cacheMillis, new Callable<PropertiesHolder>() {
            @Override
            public PropertiesHolder call() throws Exception {
                Properties mergedProps = new Properties();
                PropertiesHolder mergedHolder = new PropertiesHolder(self, mergedProps);
                mergeBinaryPluginProperties(locale, mergedProps);

                log.debug "After get resources loop: ${mergedProps.size()}"
                for (String basename : pluginBaseNames) {
                    List<Pair<String, Resource>> filenamesAndResources = calculateAllFilenames(basename, locale)
                    for (int j = filenamesAndResources.size() - 1; j >= 0; j--) {
                        Pair<String, Resource> filenameAndResource = filenamesAndResources.get(j)
                        if(filenameAndResource.getbValue() != null) {
                            PropertiesHolder propHolder = getProperties(filenameAndResource.getaValue(), filenameAndResource.getbValue())
                            mergedProps.putAll(propHolder.getProperties())
                        }
                    }
                }
                // override plugin messages with application messages
                getMergedProperties(locale).properties.each { key ->
                    mergedProps.put key.key, key.value
                }
                log.debug "After get application resources loop: ${mergedProps.size()}"


                mergeTextManagerProperties(locale, mergedProps)
                log.debug "After mergeTextManager: ${mergedProps.size()}}"
                return mergedHolder;
            }

        });
    }

    /**
     * Merge all text manager properties for this locale into props
     */
    public void mergeTextManagerProperties( Locale locale, Properties props ) {
        Map entries = new LinkedHashMap()
        if (!textManagerService) {
              textManagerService = Holders.grailsApplication.mainContext.getBean("textManagerService")
        }
        props.each  { key, _ ->
            String value = textManagerService?.findMessage(key, locale)
            if ( value != null ) {
                entries[key] = value
            }
        }
        log.debug "mergeTextManagerProperties returning ${entries.size()}"
        props.putAll( entries )
    }


    public def mergeBinaryUploadPluginProperties(final Locale locale ) {
        final GrailsPlugin[] allPlugins = pluginManager.getAllPlugins()
        for (GrailsPlugin plugin : allPlugins) {
            if (plugin instanceof BinaryGrailsPlugin) {
                BinaryGrailsPlugin binaryPlugin = (BinaryGrailsPlugin) plugin
                final Properties binaryPluginProperties = binaryPlugin.getProperties(locale)
                if (binaryPluginProperties != null) {
                    String path = plugin.getPluginPath()+"/messages"
                    propertiesMap.put(path,binaryPluginProperties)
                }
            }
        }
        setBaseNamesSuper()
        if(basenamesExposed.size()>0){
            String file = basenamesExposed.get(0)
            def loc = locale.toString()
            def langSuffix = ( loc.contains("en") || loc == "root" ) ? "" : "_${loc}"
            Properties properties = new Properties()
            def fileName = "messages${langSuffix}.properties"
            file = file.substring(0,file.lastIndexOf('/'))+"/${fileName}"
            File propertiesFile=checkFileExists(file)
            if(propertiesFile) {
                propertiesFile.withInputStream {
                    properties.load(it)
                }
                propertiesMap.put('i18n/messages', properties)
            }
        }
        return propertiesMap
    }

    private checkFileExists(file) {
        File defaultFile = new File(file)
        if(defaultFile.exists()){
            return defaultFile
        }else{
            file = file.substring(0,file.lastIndexOf('/'))+"/messages.properties"
            defaultFile = new File(file)
            if(defaultFile.exists()){
                return defaultFile
            }else
                return null
        }
    }
}
