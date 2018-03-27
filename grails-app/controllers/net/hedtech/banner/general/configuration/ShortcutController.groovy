package net.hedtech.banner.general.configuration

import grails.converters.JSON
import groovy.json.JsonBuilder
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.springframework.context.i18n.LocaleContextHolder

class ShortcutController {

    private static final LOGGER = Logger.getLogger(ShortcutController.class.name)
    def messageSource

    def data() {
        def pluginDir = GrailsPluginUtils.pluginInfos.find { it.name == "banner-ui-ss" }.pluginDir

        String filePath = pluginDir.path + "/shortcut-data/keyboard-shortcut-data.json"
        File jsonData = new File(filePath)
        def mainJson = new JsonBuilder()
        Map sectionHeadingWindowsMap = new HashMap();
        Map sectionHeadingMacMap = new HashMap();


        def jsonTestData = JSON.parse(jsonData.text)
        jsonTestData.windows.each
                { sectionWindowsHeading ->
                    List tempList = new ArrayList();
                    for (int i = 0; i < sectionWindowsHeading.value.size(); i++) {
                        sectionWindowsHeading.value[i].combination = getMessage(sectionWindowsHeading.value[i].combination)
                        sectionWindowsHeading.value[i].description = getMessage(sectionWindowsHeading.value[i].description)
                        tempList.add(sectionWindowsHeading.value[i]);
                    }
                    sectionHeadingWindowsMap.put(getMessage(sectionWindowsHeading.key), tempList);
                }
        jsonTestData.mac.each
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
        };

        render mainJson
    }

    private String getMessage(String key) {
        messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
    }
}
