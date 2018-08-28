/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.converters.JSON
import grails.util.Environment
import groovy.io.FileVisitResult
import groovy.json.JsonBuilder
import grails.core.GrailsApplication
import org.springframework.context.i18n.LocaleContextHolder

import static groovy.io.FileType.FILES

class ShortcutController {
    GrailsApplication grailsApplication
    private static List<File> JSONFILELIST = []
    def messageSource

    def data() {
        LinkedHashMap shortcutKeysMap = new LinkedHashMap()
        if (JSONFILELIST.isEmpty()) {
            JSONFILELIST = getShortcutJSONFiles()
        }
        shortcutKeysMap = populateOSSpecificShortcutFromJsonFiles()
        JsonBuilder outputJson = getMessageForShortcutKeys(shortcutKeysMap)
        render outputJson
    }


    private List<File> getShortcutJSONFiles() {
        String dirPath = ''
        if (Environment.current == Environment.PRODUCTION || Environment.current == Environment.TEST) {
            dirPath = grailsApplication.mainContext.servletContext.getRealPath('/')
        } else if (Environment.current == Environment.DEVELOPMENT) {
            dirPath = System.properties['user.dir']
        }
        List<File> jsonFiles = getJSONFilesFromDirectory(dirPath)
        log.debug("{} shortcut JSON Files found.", jsonFiles?.size())
        log.debug("Json Files = {}", jsonFiles)
        return jsonFiles
    }


    private static List<File> getJSONFilesFromDirectory(String dirPath){
        final excludedDirs = ['.git', 'gradle', '.idea', 'node_modules', '.gradle', 'build']
        List<File> jsonFiles = []
       /* new File(dirPath).eachFileRecurse(FILES) {
            if(it.name.endsWith('shortcut_properties.json')) {
                jsonFiles << it
            }
        }*/

        new File(dirPath).traverse(
                type                : FILES,
                preDir              : { if (it.name in excludedDirs) return FileVisitResult.SKIP_SUBTREE }, // excludes children of excluded dirs
                excludeNameFilter   : { it in excludedDirs }, // excludes the excluded dirs as well
                nameFilter          :  ~/.*shortcut_properties.json/, //{ it.name?.endsWith('shortcut_properties.json')} // matched only given names
                //nameFilter          :  ~/.*shortcut_properties.*\.json/, //{ it.name?.endsWith('shortcut_properties.json')} // matched only given names
        ) {jsonFiles << it }
        
        return jsonFiles
    }


    private static LinkedHashMap populateOSSpecificShortcutFromJsonFiles() {
        def jsonData
        List windowsList = []
        List macList = []
        JSONFILELIST?.each { jsonFile ->
            jsonData = JSON.parse(jsonFile.text)
            windowsList << jsonData?.windows
            macList << jsonData?.mac
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
                    if(sectionHeadingWindowsMap.get(getMessage(windowsShortcuts.key))){
                        List existingKeys = sectionHeadingWindowsMap.get(getMessage(windowsShortcuts.key)) as List
                        existingKeys.addAll(tempList)
                        tempList = existingKeys
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
                    if(sectionHeadingMacMap.get(getMessage(macShortcuts.key))){
                        List existingKeys = sectionHeadingMacMap.get(getMessage(macShortcuts.key)) as List
                        existingKeys.addAll(tempList)
                        tempList = existingKeys
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
