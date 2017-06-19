/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class TextManagerDBIntegrationTests extends BaseIntegrationTestCase {
    def textManagerService
    def textManagerDB = new TextManagerDB()

    Map dbValues = [:]
    String name = 'APPLICATION/MESSAGES'
    String srcLocale = 'es_MX'
    String locale = 'es_MX'

    @Before
    public void setUp(){
        formContext= ['GUAGMNU']
        super.setUp()
        initializeProject()
    }

    @After
    public void cleanUp(){
        textManagerService.deleteProjectforApp()
        textManagerDB.closeConnection()
    }

    public void initializeProject(){
        textManagerService.createProjectForApp('UNITTEST', 'Integration Test Banner General Utility')
        textManagerDB.createConnection()
    }

    @Test
    public void testTextManagerDB(){
        def properties = new Properties()
        properties.put("dummy.label1", "Dummy English text1")
        properties.put("dummy.label2", "Dummy English text2")
        TextManagerDB.ObjectProperty op = new TextManagerDB.ObjectProperty()
        dbValues.projectCode = 'UNITTEST'
        dbValues.moduleName =  name.toUpperCase()
        dbValues.srcLocale =  'root'
        dbValues.srcFile = locale == 'en' ? "${name}.properties" : "${name}_${locale}.properties"
        dbValues.srcIndicator = locale == srcLocale ? 's' : 'r'
        dbValues.tgtLocale = locale == srcLocale ? '' : "${locale.replace('_','')}"

        textManagerDB.setDBContext(dbValues)
        textManagerDB.setDefaultProp(dbValues)

        if(dbValues.srcIndicator == 's')
            assertEquals(dbValues.srcLocale.toString(), textManagerDB.defaultProp.langCode)
        else
            assertEquals(dbValues.tgtLocale.toString(), textManagerDB.defaultProp.langCode)

        properties.each { property ->
            int sepLoc = 0
            final String sep = '.'
            String key = property.key
            String value = property.value
            sepLoc = key.lastIndexOf(sep)
            if (sepLoc == -1) {
                sepLoc = 0
            }
            op.parentName = sep + key.substring(0, sepLoc) //. plus expression between brackets in [x.y...].z
            op.objectName = key.substring(sepLoc)       // expression between brackets in x.y....[z]
            op.string = textManagerService.smartQuotesReplace(value)
            op.langCode = 'root'
            textManagerDB.setPropString(op)
        }

        textManagerDB.setModuleRecord(dbValues)
        textManagerDB.invalidateStrings()
    }
}
