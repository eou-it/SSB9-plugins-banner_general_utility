/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.about

import groovy.sql.Sql
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder

import java.text.SimpleDateFormat

class AboutService {

    static transactional = false
    def grailsApplication
    def pluginManager
    def sessionFactory
    def menuService
    def resourceProperties

    private final log = Logger.getLogger(getClass())

    def getAbout() {
        def about = [:]

        loadResourcePropertiesFile();

        about["Application"] = getAppInfo()
        about["Copyright"] = getCopyright()
        about["DB Instance Name"] = getDbInstanceName()
        //about << getReleaseInfo()
        if(getUserName())
            about["User Name"] = getUserName()

        if(getMepDescription())
            about["MEP Description"] = getMepDescription()

        about["Plugins"] = getPluginsInfo()
        return about
    }

    private void loadResourcePropertiesFile() {
        String propertyFileName =  ServletContextHolder.servletContext.getRealPath('WEB-INF/classes/release.properties')
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
        if(resourceProperties){
            appInfo['Name'] =  formatCamelCaseToEnglish(resourceProperties.getProperty("application.name"))
            appInfo['Version'] =  resourceProperties.getProperty("application.version")
            appInfo['Build Number'] =  resourceProperties.getProperty("application.build.number")
            appInfo['Build Time'] =  resourceProperties.getProperty("application.build.time");
            //def appName = grailsApplication.metadata['app.name']
            //appInfo[ appName ] = grailsApplication.metadata['app.version']
        } else {
            appInfo['Name'] = grailsApplication.metadata['app.name']
            appInfo['Version'] = grailsApplication.metadata['app.version']
        }
        return appInfo
    }

    private Map getPluginsInfo() {
        def pluginInfo = [:]
        // plugin details
        def plugins = pluginManager.allPlugins
        plugins.each{
            String name = formatCamelCaseToEnglish(it.name)
            String version = it.version
            pluginInfo[name] = version
        }
        return pluginInfo.sort { it.key }
    }

    private String getCopyright() {
        grailsApplication.getMainContext().getBean('messageSource').getMessage("net.hedtech.banner.login.copyright1", null, LocaleContextHolder.getLocale()) + " " +  grailsApplication.getMainContext().getBean('messageSource').getMessage("net.hedtech.banner.login.copyright2", null, LocaleContextHolder.getLocale())
    }

    private String getDbInstanceName(){
        menuService.getInstitutionDBInstanceName()
    }

    private String getUserName(){
        try {
            SecurityContextHolder.context?.authentication?.principal?.username?.toUpperCase()
        } catch (Exception e){
            // ignore
        }
    }

    private Map getReleaseInfo(){
        def releaseInfo = [:]
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def row = sql.firstRow("select GURVERS_RELEASE, GURVERS_STAGE_DATE from GURVERS ORDER BY GURVERS_STAGE_DATE DESC")
        releaseInfo["Release"] = row.GURVERS_RELEASE
        def stageDate = row.GURVERS_STAGE_DATE

        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy")
        releaseInfo["Stage Date"] = format.format(stageDate)

        return releaseInfo
    }

    private String formatCamelCaseToEnglish(value){
        value.replaceAll(/(\B[A-Z])/,' $1').replaceAll("banner","Banner")
    }

}
