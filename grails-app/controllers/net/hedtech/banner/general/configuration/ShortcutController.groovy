/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.converters.JSON
import grails.util.Environment
import groovy.json.JsonBuilder
import org.apache.log4j.Logger
import org.springframework.context.i18n.LocaleContextHolder

class ShortcutController {

    private static final LOGGER = Logger.getLogger(ShortcutController.class.name)
    def messageSource
    def grailsApplication

    def data() {
        List jsonFiles = getShortcutJSONFiles()
        LinkedHashMap shortcutKeys = populateShortcutFromJsonFiles(jsonFiles)
        JsonBuilder outputJson = getMessageForShortcutKeys(shortcutKeys)
        println "\n ******************** \n " + outputJson
        render outputJson
    }

    private List<String> getShortcutJSONFiles() {
        List<String> jsonFiles = []
        if (Environment.current == Environment.PRODUCTION || Environment.current == Environment.TEST) {
            String absoluteDiskPath = grailsApplication.mainContext.servletContext.getRealPath('/')
            jsonFiles = new FileNameFinder().getFileNames(absoluteDiskPath, '**/*shortcut_properties.json')
        } else if (Environment.current == Environment.DEVELOPMENT) {
            String baseDirPath = System.properties['base.dir']
            jsonFiles = new FileNameFinder().getFileNames(baseDirPath, '**/*shortcut_properties.json')
        }
        jsonFiles
    }


    private LinkedHashMap populateShortcutFromJsonFiles(List<String> jsonFiles) {
        List jsonData = []
        List windowsList = []
        List macList = []
        jsonFiles.each { jsonFile ->
            jsonData << JSON.parse(new File(jsonFile).text)
            windowsList << jsonData.windows
            macList << jsonData.mac
        }
        Map shortcutKeys = new LinkedHashMap()
        shortcutKeys << [windows: windowsList]
        shortcutKeys << [mac: macList]
        shortcutKeys
    }


    private JsonBuilder getMessageForShortcutKeys(LinkedHashMap shortcutKeys) {
        JsonBuilder outputJson = new JsonBuilder()
        Map sectionHeadingWindowsMap = new LinkedHashMap()
        Map sectionHeadingMacMap = new LinkedHashMap()

        shortcutKeys.windows.each { additionalWindowsList ->
            additionalWindowsList.each{ sectionWindowsHeading ->
                        List tempList
                        sectionWindowsHeading.each { windowsShortcuts ->
                            tempList = new ArrayList()
                            windowsShortcuts.value.each { windowsShortcut ->
                                windowsShortcut.combination = getMessage(windowsShortcut.combination)
                                windowsShortcut.description = getMessage(windowsShortcut.description)
                                tempList.add(windowsShortcut)
                            }
                            sectionHeadingWindowsMap.put(getMessage(windowsShortcuts.key), tempList)
                        }

                    }
        }
        shortcutKeys.mac.each { additionalMacList ->
            additionalMacList.each {sectionMacHeading ->
                    List tempList
                    sectionMacHeading.each { macShortcuts ->
                        tempList = new ArrayList()
                        macShortcuts.value.each { macShortcut ->
                            macShortcut.combination = getMessage(macShortcut.combination)
                            macShortcut.description = getMessage(macShortcut.description)
                            tempList.add(macShortcut)
                        }
                        sectionHeadingMacMap.put(getMessage(macShortcuts.key), tempList)
                    }
            }
        }
        outputJson {
            windows sectionHeadingWindowsMap
            mac sectionHeadingMacMap
        }
        outputJson
    }


    private String getMessage(String key) {
        messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale())
    }

}
