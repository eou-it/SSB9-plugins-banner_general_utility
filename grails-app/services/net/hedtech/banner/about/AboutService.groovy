/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import org.apache.log4j.Logger
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

    private final log = Logger.getLogger(getClass())

    def getAbout() {
        def about = [:]

        loadResourcePropertiesFile();

        about['api'] = getMessage("about.banner.title")
        about[getMessage("about.banner.application")] = getAppInfo()
        about[getMessage("about.banner.copyright")] = getCopyright()
        about[getMessage("about.banner.db.instance.name")] = getDbInstanceName()
        //about << getReleaseInfo()
        if (getUserName())
            about[getMessage("about.banner.username")] = getUserName()

        if (getMepDescription())
            about[getMessage("about.banner.mep.description")] = getMepDescription()

        about[getMessage("about.banner.plugins")] = getPluginsInfo()
        return about
    }

    private void loadResourcePropertiesFile() {
        String propertyFileName = ServletContextHolder.servletContext.getRealPath('WEB-INF/classes/release.properties')
        resourceProperties = new Properties();
        InputStream input = null;
        try {

            input = new FileInputStream(propertyFileName);

            resourceProperties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
            // ignore
        }

        return mepDescription

    }

    private Map getAppInfo() {
        def appInfo = [:]
        if (resourceProperties) {
            appInfo[getMessage("about.banner.name")] = formatCamelCaseToEnglish(resourceProperties.getProperty("application.name"))
            appInfo[getMessage("about.banner.version")] = resourceProperties.getProperty("application.version")
            appInfo[getMessage("about.banner.build.number")] = resourceProperties.getProperty("application.build.number")
            appInfo[getMessage("about.banner.build.time")] = resourceProperties.getProperty("application.build.time");
            //def appName = grailsApplication.metadata['app.name']
            //appInfo[ appName ] = grailsApplication.metadata['app.version']
        } else {
            appInfo[getMessage("about.banner.name")] = grailsApplication.metadata['app.name']
            appInfo[getMessage("about.banner.version")] = grailsApplication.metadata['app.version']
        }
        return appInfo
    }

    private Map getPluginsInfo() {
        def pluginInfo = [:]
        // plugin details
        def plugins = pluginManager.allPlugins
        plugins.each {
            String name = formatCamelCaseToEnglish(it.name)
            String version = it.version
            pluginInfo[name] = version
        }
        return pluginInfo.sort { it.key }
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
            // ignore
        }
    }

    private String formatCamelCaseToEnglish(value) {
        value.replaceAll(/(\B[A-Z])/, ' $1').replaceAll("banner", "Banner")
    }

    private String getMessage(String key) {
        messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
    }

}
