package banner.general.utility

import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import net.hedtech.banner.general.configuration.GeneralPageRoleMappingService
import net.hedtech.banner.i18n.BannerMessageSource

class BannerGeneralUtilityGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.11 > *"
    def dependsOn = [
            bannerCore: '9.28.1 => *'
           /* springSecurityCore: '3.2.3 => *'*/
    ]
    def loadAfter = ["bannerCore"]

    def author = "ellucian"
    def authorEmail = ""
    def title = "Banner Core Framework Plugin"
    def description = '''This plugin adds Spring Security (aka Acegi) and a custom
                         |DataSource implementation (BannerDataSource) that together
                         |provide for authentication and authorization based upon
                         |Banner Security configuration. In addition, this plugin provides
                         |additional framework support (e.g., injecting CRUD methods into
                         |services, providing base test classes) to facilitate development of
                         |Banner web applications.'''.stripMargin()
    // def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = ""



    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]


    Closure doWithSpring() { {->
            // Reconfigure the messageSource to use BannerMessageSource
            GrailsApplication application = grailsApplication
            messageSource(BannerMessageSource) { bean ->
                application = application
                pluginManager = pluginManager
            }

            /**
             * If the securityConfigType = 'Requestmap' then the "GeneralPageRoleMappingService" will be get injected
             * which extends "RequestmapFilterInvocationDefinition", this service will fetch the Requestmap from the
             * DB and Config.groovy.
             */
            if (SpringSecurityUtils.securityConfigType == 'Requestmap') {
                objectDefinitionSource(GeneralPageRoleMappingService) {
                    if (SpringSecurityUtils.securityConfig.rejectIfNoRule instanceof Boolean) {
                        rejectIfNoRule = SpringSecurityUtils.securityConfig.rejectIfNoRule
                    }
                }
            }
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }


}
