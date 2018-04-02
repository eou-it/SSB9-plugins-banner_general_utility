package net.hedtech.banner.general.configuration

import grails.converters.JSON
import grails.util.Environment
import groovy.json.JsonBuilder
import org.apache.log4j.Logger
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder

class ShortcutController {

    private static final LOGGER = Logger.getLogger(ShortcutController.class.name)
    def messageSource
    def grailsApplication

    def data() {
        def jsonFiles
        def jsonData = []

        if (Environment.current == Environment.PRODUCTION || Environment.current == Environment.TEST) {
            String absoluteDiskPath = grailsApplication.mainContext.servletContext.getRealPath('/')
            jsonFiles = new FileNameByRegexFinder().getFileNames(absoluteDiskPath, /.*shortcut_properties.json/)
        } else if (Environment.current == Environment.DEVELOPMENT) {
            def baseDirPath = System.properties['base.dir']
            jsonFiles = new FileNameByRegexFinder().getFileNames(baseDirPath, /.*shortcut_properties.json/)
        }

        jsonFiles.each { jsonFile ->
            jsonData << JSON.parse(new File(jsonFile).text)
        }

        List windowsList = new ArrayList();
        List macList = new ArrayList();
        jsonData.each { jsonItem ->
            jsonItem.each { eachjson ->
                if (eachjson.key == "windows") {
                    windowsList.add(eachjson.value)
                } else if (eachjson.key == "mac") {
                    macList.add(eachjson.value)
                }
            }
        }
        def concatKeys = new HashMap()
        concatKeys.put("windows", windowsList)
        concatKeys.put("mac", macList)
        def mainJson = new JsonBuilder()
        Map sectionHeadingWindowsMap = new HashMap();
        Map sectionHeadingMacMap = new HashMap();
        try {
            concatKeys.windows.each { additionalWindowsList ->
                additionalWindowsList.each
                        { sectionWindowsHeading ->
                            List tempList = new ArrayList();
                            sectionWindowsHeading.value.each { windowsShortcut ->
                                windowsShortcut.combination = getMessage(windowsShortcut.combination)
                                windowsShortcut.description = getMessage(windowsShortcut.description)
                                tempList.add(windowsShortcut);
                            }
                            sectionHeadingWindowsMap.put(getMessage(sectionWindowsHeading.key), tempList);
                        }
            }
            concatKeys.mac.each { additionalMacList ->
                additionalMacList.each {
                    sectionMacHeading ->
                        List tempList = new ArrayList();
                        sectionMacHeading.value.each { macShortcut ->
                            macShortcut.combination = getMessage(macShortcut.combination)
                            macShortcut.description = getMessage(macShortcut.description)
                            tempList.add(macShortcut);
                        }
                        sectionHeadingMacMap.put(getMessage(sectionMacHeading.key), tempList);
                }
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
