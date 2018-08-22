package banner.general.utility

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.*
import grails.util.Holders
import net.hedtech.banner.general.configuration.GeneralPageRoleMappingService
import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource
import net.hedtech.banner.i18n.BannerMessageSource
import grails.plugin.springsecurity.SecurityConfigType

class BannerGeneralUtilityGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"
    def dependsOn = [
            bannerCore: '9.28.1 => *'
    ]
    def loadAfter = ["bannerCore"]
    // resources that are excluded from plugin packaging
    /*def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]*/
    //String securityConfigType = grails.plugin.springsecurity.SpringSecurityUtils.securityConfigType
    //ConfigObject conf = SpringSecurityUtils.securityConfig

    // TODO Fill in these fields
    def title = "Banner General Utility" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    // def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/banner-general-utility"

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
        /*def beanConf = springConfig.getBeanConfig('messageSource')
        def beanDef = beanConf ? beanConf.beanDefinition : springConfig.getBeanDefinition('messageSource')
        if (beanDef?.beanClassName == PluginAwareResourceBundleMessageSource.class.canonicalName) {
            //just change the target class of the bean, maintaining all configurations.
            beanDef.beanClassName = BannerMessageSource.class.canonicalName
        }
*/
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
