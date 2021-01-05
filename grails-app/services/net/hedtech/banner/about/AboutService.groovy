/*******************************************************************************
 Copyright 2009-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder
import net.hedtech.banner.i18n.MessageHelper

class AboutService {

    static transactional = false
    def grailsApplication
    def sessionFactory
    def resourceProperties
    def messageSource
    def springSecurityService

    def getAbout() {
        def about = [:]

        loadResourcePropertiesFile();

        about['api.title'] = getMessage("about.banner.title")
        about['api.close'] = getMessage("about.banner.close")
        about['about.banner.application.name'] = getApplicationName()
        about['about.banner.application.version'] = getVersion()

        if (displayPlatformVersion()) {
            about['about.banner.platform.version'] = getPlatformVersion()
        }

        /* Commented for now because we need only application name & version number.
         For specific role we have to show all the details but still not decided for which role to show all details.

         about['about.banner.tab.general'] = getAppInfo()

        about['about.banner.plugins'] = getPluginsInfo("(banner|i18nCore|sgheZkCore).*")
        about['about.banner.other.plugins'] = getPluginsInfo("(?!(banner|i18nCore|sgheZkCore).*).*")
        about['api.close'] = getMessage("about.banner.close") */
        about['about.banner.copyright'] = getCopyright()
        about['about.banner.copyrightLegalNotice'] = getCopyrightLegalNotice()
        about['about.banner.ellucianPrivacyNotice'] = getEllucianPrivacyNotice()
        about['about.banner.ellucianPrivacyNoticeLink'] = getEllucianPrivacyNoticeLink()
        return about
    }

    private boolean displayPlatformVersion(){
        boolean displayPlatformVersion = false
        if (springSecurityService?.isLoggedIn()) {
            ArrayList  userLoggedRoles = springSecurityService?.getAuthentication()?.getAuthorities()?.authority?.asList()
            ArrayList  roles = Holders?.config?.aboutInfoAccessRoles as ArrayList
            roles = (roles == null) ? new ArrayList() : roles
            if (!Collections.disjoint(userLoggedRoles , roles)) {
                displayPlatformVersion = true
            }
        }
        return displayPlatformVersion
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
                grailsApplication.metadata['info.app.name']
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


    private String getVersion(){
        if (resourceProperties) {
            getMessage("about.banner.application.version") + " " + resourceProperties.getProperty("application.version")
        } else {
            getMessage("about.banner.application.version") + " " + grailsApplication.metadata['info.app.version']
        }
    }

    private String getPlatformVersion(){
        getMessage("about.banner.platform.version") + " " + Holders.config.app.platform.version
    }


    private String getCopyright() {
        String startYear = getMessage("default.copyright.startyear")
        String endYear = getMessage("default.copyright.endyear")
        Object[] args = [startYear,endYear]
        getMessage("default.copyright.message",args)

    }

    private String getCopyrightLegalNotice() {
        getMessage("net.hedtech.banner.login.copyright2")
    }

    private String getEllucianPrivacyNotice() {
        getMessage("net.hedtech.banner.ellucianPrivacyNotice")
    }

    private String getEllucianPrivacyNoticeLink() {
        Holders.config.banner.ellucianPrivacyNotice
    }


    private String formatCamelCaseToEnglish(value) {
        if(value) {
            value.replaceAll(/(\B[A-Z])/, ' $1').replaceAll("banner", "Banner")
        } else{
            value
        }
    }

    private String getMessage(String key,args= null) {
        messageSource.getMessage(key, args, LocaleContextHolder.getLocale())
    }
}
