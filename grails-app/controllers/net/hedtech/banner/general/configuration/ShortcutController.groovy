/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.converters.JSON
import grails.util.Environment
import groovy.json.JsonBuilder
import org.apache.log4j.Logger
import org.springframework.context.i18n.LocaleContextHolder
import static groovy.io.FileType.FILES

class ShortcutController {

    private static final LOGGER = Logger.getLogger(ShortcutController.class.name)
    private static List<File> JSONFILELIST = []
    private static JsonBuilder OUTPUTJSON
    def messageSource
    def grailsApplication

    def data() {
        if (JSONFILELIST.isEmpty() || OUTPUTJSON == null) {
            JSONFILELIST = getShortcutJSONFiles()
            LinkedHashMap shortcutKeys = populateOSSpecificShortcutFromJsonFiles()
            OUTPUTJSON = getMessageForShortcutKeys(shortcutKeys)
        }
        render OUTPUTJSON
    }


    private List<File> getShortcutJSONFiles() {
        String dirPath = ''
        if (Environment.current == Environment.PRODUCTION || Environment.current == Environment.TEST) {
            dirPath = grailsApplication.mainContext.servletContext.getRealPath('/')
        } else if (Environment.current == Environment.DEVELOPMENT) {
            dirPath = System.properties['base.dir']
        }
        List<File> jsonFiles = getJSONFilesFromDirectory(dirPath)
        return jsonFiles
    }


    private static List<File> getJSONFilesFromDirectory(String dirPath){
        List<File> jsonFiles = []
        new File(dirPath).eachFileRecurse(FILES) {
            if(it.name.endsWith('shortcut_properties.json')) {
                jsonFiles << it
            }
        }
        return jsonFiles
    }


    private static LinkedHashMap populateOSSpecificShortcutFromJsonFiles() {
        List jsonData = []
        List windowsList = []
        List macList = []
        JSONFILELIST?.each { jsonFile ->
            jsonData << JSON.parse(jsonFile.text)
            windowsList << jsonData.windows
            macList << jsonData.mac
        }
        LinkedHashMap shortcutKeys = new LinkedHashMap()
        shortcutKeys << [windows: windowsList]
        shortcutKeys << [mac: macList]
        return shortcutKeys
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
