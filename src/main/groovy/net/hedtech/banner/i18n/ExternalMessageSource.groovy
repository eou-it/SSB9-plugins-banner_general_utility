/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.i18n

import org.codehaus.groovy.grails.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import grails.util.Holders as CH
// Implements an external message source, which is used by BannerMessageSource
class ExternalMessageSource extends ReloadableResourceBundleMessageSource {

    String bundleLocation
    String bundleName
    protected def basenamesExposed = [] // will go into basenames in superclass, since that cannot be accessed, we keep a copy

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        def rloader = new DefaultResourceLoader() {
            public Resource getResource(String location) {
                def path=bundleLocation
                if ( ! (path.endsWith("/") || path.endsWith("\\")) ) {
                    path += "/"
                }
                new FileSystemResource(new File(path + location))
            }
        }
        super.setResourceLoader(rloader);
    }

    /* constructor + methods added to support an external MessageSource */
    public ExternalMessageSource(bundleLocation, bundleName, info = "" ) {
        super()
        logger.debug "Initialize ExternalMessageSource $bundleName"
        try {
            this.bundleLocation = bundleLocation
            this.bundleName = bundleName
            logger.debug "External Bundle location : $bundleLocation ($info)"
            //Get the basenames from the external root properties files (assume filename has no underscore)
            new File(bundleLocation).eachFileMatch(~/[^_]*.properties/) {   file ->
                String fileName = file.name
                fileName-= ".properties"  // remove the extension
                basenamesExposed.add(fileName)
                logger.debug "added resource ${file.name}"
            }
            this.setResourceLoader() // make sure that the super  class uses the resource loader above
            setBasenames( (String []) basenamesExposed)
        } catch (FileNotFoundException ex) {
            logger.error "Unable to load external resources from configured location. Not found: ${ex.getMessage()}"
        }
    }

    public void addBasename(String baseName)  {
        logger.debug "Adding to $bundleName resources: " + baseName
        if (baseName && !basenamesExposed.contains(baseName)) {
            basenamesExposed.add(baseName)
        }
        setBasenames((String []) basenamesExposed)
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return super.resolveCodeWithoutArguments(code, getLocale(locale))
    }

    private getLocale(locale) {
        if(CH.config.bannerLocaleVariant instanceof String) {
            Locale loc = new Locale(locale.language, locale.country, CH.config.bannerLocaleVariant)
            return loc
        }
        return locale
    }
}
