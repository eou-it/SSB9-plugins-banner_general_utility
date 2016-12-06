/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder

class AboutService {

    static transactional = false
    def grailsApplication
    def pluginManager
    def sessionFactory
    def menuService
    def resourceProperties
    def messageSource


    def getAbout() {
        def about = [:]

        loadResourcePropertiesFile();

        about['api.title'] = getMessage("about.banner.title")
        about[getMessage("about.banner.tab.general")] = getAppInfo()

        about[getMessage("about.banner.plugins")] = getPluginsInfo("(banner|i18nCore|sgheZkCore).*")
        about[getMessage("about.banner.other.plugins")] = getPluginsInfo("(?!(banner|i18nCore|sgheZkCore).*).*")
        about[getMessage("about.banner.copyright")] = getCopyright()
        about['api.close'] = getMessage("about.banner.close")
        return about
    }

    private void loadResourcePropertiesFile() {
        String propertyFileName = ServletContextHolder.servletContext.getRealPath('WEB-INF/classes/release.properties')
        resourceProperties = new Properties();
        InputStream input = null;
        try {

            if (new File(propertyFileName).exists()){
                input = new FileInputStream(propertyFileName);
                resourceProperties.load(input);
            }

        } catch (IOException ex) {
            log.error "IOException Occured in method loadResourcePropertiesFile" , ex
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error "IOException Occured in method loadResourcePropertiesFile", e
                }
            }
        }

    }

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
            appInfo[getMessage("about.banner.application.name")] = formatCamelCaseToEnglish(resourceProperties.getProperty("application.name"))
            appInfo[getMessage("about.banner.application.version")] = resourceProperties.getProperty("application.version")
            appInfo[getMessage("about.banner.application.build.number")] = resourceProperties.getProperty("application.build.number")
            appInfo[getMessage("about.banner.application.build.time")] = resourceProperties.getProperty("application.build.time");
            //def appName = grailsApplication.metadata['app.name']
            //appInfo[ appName ] = grailsApplication.metadata['app.version']
        } else {
            appInfo[getMessage("about.banner.application.name")] = grailsApplication.metadata['app.name']
            appInfo[getMessage("about.banner.application.version")] = grailsApplication.metadata['app.version']
        }
        appInfo[getMessage("about.banner.db.instance.name")] = getDbInstanceName()
        if (getUserName())
            appInfo[getMessage("about.banner.username")] = getUserName()

        if (getMepDescription())
            appInfo[getMessage("about.banner.mep.description")] = getMepDescription()

        return appInfo
    }

    private Map getPluginsInfo(def pattern) {
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
    }

    private String getCopyright() {
        getMessage("net.hedtech.banner.login.copyright1") + " " + getMessage("net.hedtech.banner.login.copyright2")
    }

    private String getDbInstanceName() {
        menuService.getInstitutionDBInstanceName()
    }

    private String getUserName() {
        try {
            SecurityContextHolder.context?.authentication?.principal?.username?.toUpperCase()
        } catch (Exception e) {
            log.error "Exception occured while executing getUserName method" , e
        }
    }

    private String formatCamelCaseToEnglish(value) {
        value.replaceAll(/(\B[A-Z])/, ' $1').replaceAll("banner", "Banner")
    }

    private String getMessage(String key) {
        messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
    }

}
