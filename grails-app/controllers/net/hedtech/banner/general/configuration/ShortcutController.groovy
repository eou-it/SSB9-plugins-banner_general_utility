package net.hedtech.banner.general.configuration

import grails.util.Environment
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.log4j.Logger
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder

import javax.servlet.ServletContext

class ShortcutController {

    private static final LOGGER = Logger.getLogger(ShortcutController.class.name)
    def messageSource
    def grailsApplication

    def data() {
        String filePath="";
        if (Environment.current == Environment.PRODUCTION || Environment.current ==Environment.TEST) {
            String relativeWebPath = "/js/shortcut-data/platform_shortcut_properties.json"
            String absoluteDiskPath = servletContext.getRealPath(relativeWebPath);
            println "absoluteDiskPath "+absoluteDiskPath
            println "grailsApplication.mainContext.servletContext.getRealPath('/')"+grailsApplication.mainContext.servletContext.getRealPath('/')
          /*  def basePath = grailsApplication.mainContext.servletContext.getRealPath('/')+relativeWebPath
            println "BasePAth "+basePath
            String filePath1 = baseDirPath + "/web-app/js/shortcut-data/platform_shortcut_properties.json"
            def baseDirPath = System.properties['base.dir']
           println "BASE DIR PATH " + baseDirPath
            println "BASE DIR PATH  FILE PATH " + filePath1
            filePath = baseDirPath+absoluteDiskPath*/
            filepath= absoluteDiskPath
        } else if (Environment.current == Environment.DEVELOPMENT) {
            def baseDirPath = System.properties['base.dir']
            filePath = baseDirPath + "/web-app/js/shortcut-data/platform_shortcut_properties.json"
        }
        def jsonSlurper = new JsonSlurper()
        def jsonData = jsonSlurper.parseText(new File(filePath).text)
        def mainJson = new JsonBuilder()
        Map sectionHeadingWindowsMap = new HashMap();
        Map sectionHeadingMacMap = new HashMap();
        try {
            jsonData.windows.each
                    { sectionWindowsHeading ->
                        List tempList = new ArrayList();
                        for (int i = 0; i < sectionWindowsHeading.value.size(); i++) {
                            sectionWindowsHeading.value[i].combination = getMessage(sectionWindowsHeading.value[i].combination)
                            sectionWindowsHeading.value[i].description = getMessage(sectionWindowsHeading.value[i].description)
                            tempList.add(sectionWindowsHeading.value[i]);
                        }
                        sectionHeadingWindowsMap.put(getMessage(sectionWindowsHeading.key), tempList);
                    }
            jsonData.mac.each
                    { sectionMacHeading ->
                        List tempList = new ArrayList();
                        for (int i = 0; i < sectionMacHeading.value.size(); i++) {
                            sectionMacHeading.value[i].combination = getMessage(sectionMacHeading.value[i].combination)
                            sectionMacHeading.value[i].description = getMessage(sectionMacHeading.value[i].description)
                            tempList.add(sectionMacHeading.value[i]);
                        }
                        sectionHeadingMacMap.put(getMessage(sectionMacHeading.key), tempList);
                    }
            mainJson {
                windows sectionHeadingWindowsMap
                mac sectionHeadingMacMap
            }
        } catch (NoSuchMessageException exception) {
            LOGGER.error("Couldn't get the message properties" + exception)
        }

        render mainJson
    }

    private String getMessage(String key) {
        messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
    }
}
