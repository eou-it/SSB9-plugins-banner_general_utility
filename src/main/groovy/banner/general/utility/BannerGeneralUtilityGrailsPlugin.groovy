package banner.general.utility

import grails.plugins.*
import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource
import net.hedtech.banner.i18n.BannerMessageSource
import net.hedtech.banner.general.configuration.GeneralPageRoleMappingService
import grails.plugin.springsecurity.SpringSecurityUtils

class BannerGeneralUtilityGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"
    // resources that are excluded from plugin packaging

    // TODO Fill in these fields
    def title = "Banner General Utility" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    //String securityConfigType = SpringSecurityUtils.securityConfigType
    //ConfigObject conf = SpringSecurityUtils.securityConfig

    Closure doWithSpring() { {->
             // Reconfigure the messageSource to use BannerMessageSource
       /* def beanConf = springConfig.getBeanConfig('messageSource')
        def beanDef = beanConf ? beanConf.beanDefinition : springConfig.getBeanDefinition('messageSource')
        if (beanDef?.beanClassName == PluginAwareResourceBundleMessageSource.class.canonicalName) {
            //just change the target class of the bean, maintaining all configurations.
            beanDef.beanClassName = BannerMessageSource.class.canonicalName
        }

        *//**
         * If the securityConfigType = 'Requestmap' then the "GeneralPageRoleMappingService" will be get injected
         * which extends "RequestmapFilterInvocationDefinition", this service will fetch the Requestmap from the
         * DB and Config.groovy.
         *//*
        if (securityConfigType == 'Requestmap') {
            objectDefinitionSource(GeneralPageRoleMappingService) {
                if (conf.rejectIfNoRule instanceof Boolean) {
                    rejectIfNoRule = conf.rejectIfNoRule
                }
            }
        }*/
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
