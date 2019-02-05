/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import grails.web.context.ServletContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.i18n.MessageHelper

class AboutService {

    static transactional = false
    def grailsApplication
    def pluginManager
    def sessionFactory
    def resourceProperties
    def messageSource


    def getAbout() {
        def about = [:]

        loadResourcePropertiesFile();

        about['api.title'] = getMessage("about.banner.title")
        about['api.close'] = getMessage("about.banner.close")
        about['about.banner.application.name'] = getApplicationName()
        about['about.banner.application.version'] = getVersion()

        /* Commented for now because we need only application name & version number.
         For specific role we have to show all the details but still not decided for which role to show all details.

         about['about.banner.tab.general'] = getAppInfo()

        about['about.banner.plugins'] = getPluginsInfo("(banner|i18nCore|sgheZkCore).*")
        about['about.banner.other.plugins'] = getPluginsInfo("(?!(banner|i18nCore|sgheZkCore).*).*")
        about['api.close'] = getMessage("about.banner.close") */
        about['about.banner.copyright'] = getCopyright()
        about['about.banner.copyrightLegalNotice'] = getCopyrightLegalNotice()
        return about
    }

    private String getApplicationName(){
        String aboutApplicationName = MessageHelper.message("about.application.name")
        if(!aboutApplicationName.equalsIgnoreCase("about.application.name"))
        {
            aboutApplicationName
        }
        else {
            if(resourceProperties){
                formatCamelCaseToEnglish(resourceProperties.getProperty("application.name"))
            } else {
                //grailsApplication.metadata['app.name']
                grailsApplication.config.info.app.name
            }
        }
    }
    private void loadResourcePropertiesFile() {
        String propertyFiletext = Thread.currentThread().getContextClassLoader().getResource( "release.properties" )?.text
        String propertyFilePath = Thread.currentThread().getContextClassLoader().getResource( "release.properties" )?.path
        resourceProperties = new Properties()
        try {
            if (propertyFiletext != null && propertyFilePath.endsWith('release.properties')){
                resourceProperties.load(new StringReader(propertyFiletext))
            }
        } catch (IOException ex) {
            log.error "IOException Occured in method loadResourcePropertiesFile" , ex
        }

    }

/*
    private String getMepDescription() {

        String mepDescription
        try {
            def user = SecurityContextHolder.context.authentication?.user

            if (user && user.mepHomeContext) {
                mepDescription = user?.mepHomeContextDescription
            }
        } catch (Exception e) {
            log.error "Exception Occured in method getMepDescription", e
        }

        return mepDescription

    }


    private Map getAppInfo() {
        def appInfo = [:]
        if (resourceProperties) {
            appInfo[getMessage("about.banner.application.build.number")] = resourceProperties.getProperty("application.build.number")
            appInfo[getMessage("about.banner.application.build.time")] = resourceProperties.getProperty("application.build.time");
        } else {
            appInfo[getMessage("about.banner.application.name")] = grailsApplication.metadata['app.name']
            appInfo[getMessage("about.banner.application.version")] = grailsApplication.metadata['app.version']
        }
        //appInfo[getMessage("about.banner.db.instance.name")] = getDbInstanceName()
        if (getUserName())
            appInfo[getMessage("about.banner.username")] = getUserName()

        return appInfo
    }
 */

    private String getVersion(){
        if (resourceProperties) {
            getMessage("about.banner.application.version") + " " + resourceProperties.getProperty("application.version")
        } else {
            getMessage("about.banner.application.version") + " " + grailsApplication.config.info.app.version
        }
    }

/*    private Map getPluginsInfo(def pattern) {
        def pluginInfo = [:]
        // plugin details
        def plugins = pluginManager.allPlugins.findAll { plugin -> plugin.name ==~ pattern  }
        //plugins.collect { def key = it.name; [key: it.value]}
        plugins.each {
            String name = formatCamelCaseToEnglish(it.name)
            String version = it.version
            pluginInfo[name] = version
        }
        return pluginInfo.sort { formatCamelCaseToEnglish(it.key) }
    }*/

    private String getCopyright() {
        getMessage("default.copyright.startyear")
                .concat(getMessage("default.copyright.endyear")
                .concat(" ")
                .concat(getMessage("default.copyright.message")))
    }

    private String getCopyrightLegalNotice() {
        getMessage("net.hedtech.banner.login.copyright2")
    }

/*    private String getUserName() {
        String userName = ""
        try {
            userName = SecurityContextHolder.context?.authentication?.principal?.username?.toUpperCase()
        } catch (Exception e) {
            log.error "Exception occured while executing getUserName method" , e
        }
        if("__grails.anonymous.user__".toUpperCase().equals(userName)){
            userName = "N/A"
        }
        return userName
    }*/

    private String formatCamelCaseToEnglish(value) {
        if(value) {
            value.replaceAll(/(\B[A-Z])/, ' $1').replaceAll("banner", "Banner")
        } else{
            value
        }
    }

    private String getMessage(String key) {
        messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
    }

}
