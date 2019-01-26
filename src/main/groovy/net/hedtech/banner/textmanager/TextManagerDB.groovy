/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import grails.util.Holders as CH
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.grails.web.util.GrailsApplicationAttributes
import java.sql.SQLException

@Slf4j
class TextManagerDB {
    def dataSource
    Sql sql
    ObjectProperty defaultProp

    /*String projectCode
    String langCodeSrc
    String langCodeTgt
    String moduleType
    String moduleName*/


    static class ObjectProperty {
        String langCode
        int parentType
        String parentName
        int objectType
        String objectName
        int propCode
        int status
        String string
        int match
        String statusX

        public ObjectProperty() {
            parentType = 10  //Module
            objectType = 26  //Property Class
            propCode = 438  //D2FP_TEXT
            status = 0
        }
    }

    public createConnection (sessionFactory){
        /*ApplicationContext ctx= Holders.getGrailsApplication().getMainContext() -- for future reference*/
        sql = new Sql(sessionFactory.getCurrentSession().connection())
    }

    public void closeConnection() throws SQLException {
        if ( sql) {
            sql.commit();
            sql.close();
        }
    }

    public ObjectProperty getDefaultObjectProp() {
        return defaultProp
    }

    void setDefaultProp(Map dbValues){
        defaultProp = new ObjectProperty()
        if(dbValues.srcIndicator.equals("s"))
            defaultProp.langCode = dbValues.srcLocale
        else
            defaultProp.langCode = dbValues.tgtLocale
    }

    void setDBContext(Map dbValues) throws SQLException {
        int defStatus = 1 //set to 1 for properties - assume translatable by default
        int sqlTrace = 0
        long timestamp = System.currentTimeMillis()
        String projectCode   = dbValues.projectCode
        String langCodeSrc  = dbValues.srcLocale
        String langCodeTgt  = dbValues.tgtLocale
        String moduleType = 'J'
        String moduleName = dbValues.moduleName

        //Reverse extract.
        if (dbValues.srcIndicator.equals("r")) {
            defStatus = 7 //set to Reverse extracted
        }
        try {
            def params = [sqlTrace, projectCode, langCodeSrc, langCodeTgt, moduleType, moduleName, defStatus]
            def stmt = """
                          Begin
                            if :1>0 then
                                DBMS_SESSION.SET_SQL_TRACE(TRUE);
                            end if;
                            GMKOBJI.P_SETCONTEXT(:2, :3, :4, :5, :6, :7);
                          End;
                       """
            sql.call(stmt, params)
        } catch (SQLException e) {
            log.error("Error in SetDBContext", e)
        }
        timestamp = System.currentTimeMillis() - timestamp
        log.debug("SetDBContext done in " + timestamp + " ms")
    }


    void setModuleRecord(Map dbValues) throws SQLException {
        String dataSrc
        String langCode
        String modDesc
        String projectCode   = dbValues.projectCode
        String langCodeSrc  = dbValues.srcLocale
        String langCodeTgt  = dbValues.tgtLocale
        String moduleType = 'J'
        String moduleName = dbValues.moduleName

        switch (dbValues.srcIndicator.charAt(0) ) {
            case 's':
                dataSrc = dbValues.srcFile
                langCode = dbValues.srcLocale
                modDesc = "Properties batch extract"
                break
            case 'r':
                dataSrc = dbValues.srcFile
                langCode = dbValues.tgtLocale
                modDesc = "Properties batch reverse extract"
                break
            default: //q and t both translate
                dataSrc = dbValues.tgtFile
                langCode = dbValues.tgtLocale
                modDesc = "Properties batch translate"
        }
        try{
            def params = [projectCode, moduleName, moduleType, langCode, langCodeSrc, modDesc, dataSrc]
            def stmt = """
                          Declare
                            b1 GMRMDUL.GMRMDUL_PROJECT%type :=:1;
                            b2 GMRMDUL.GMRMDUL_MODULE_NAME%type  :=:2;
                            b3 GMRMDUL.GMRMDUL_MODULE_TYPE%type  :=:3;
                            b4 GMRMDUL.GMRMDUL_LANG_CODE%type    :=:4;
                            b5 GMRMDUL.GMRMDUL_SRC_LANG_CODE%type:=:5;
                            b6 GMRMDUL.GMRMDUL_MOD_DESC%type     :=:6;
                            b7 GMRMDUL.GMRMDUL_DATASOURCE%type   :=:7;
                          Begin
                            insert into
                            GMRMDUL (GMRMDUL_PROJECT,GMRMDUL_MODULE_NAME,GMRMDUL_MODULE_TYPE,
                                     GMRMDUL_LANG_CODE,GMRMDUL_SRC_LANG_CODE,GMRMDUL_MOD_DESC,GMRMDUL_DATASOURCE,
                                     GMRMDUL_USER_ID,GMRMDUL_ACTIVITY_DATE
                                    )
                            values (b1,b2,b3,b4,b5,b6,b7,user,sysdate);
                            Exception when dup_val_on_index then
                            update GMRMDUL
                            set GMRMDUL_ACTIVITY_DATE = sysdate,
                                GMRMDUL_MOD_DESC  = b6,
                                GMRMDUL_DATASOURCE= b7,
                                GMRMDUL_USER_ID   = user
                            where GMRMDUL_PROJECT = b1 and
                                  GMRMDUL_MODULE_NAME  = b2 and
                                  GMRMDUL_MODULE_TYPE  = b3 and
                                  GMRMDUL_LANG_CODE    = b4;
                          End;
                       """
            sql.call(stmt, params)
            log.debug "setModuleRecord project: $projectCode, sourceName: $moduleName, langCode: $langCode, langCodeSrc: $langCodeSrc, modDesc: $modDesc, dataSource: $dataSrc"
        } catch (SQLException e) {
            log.error("Error in setModuleRecord", e)
        }
    }

    void setPropString(ObjectProperty op) throws SQLException {
        try {
            def params = [Sql.VARCHAR, op.langCode, op.parentType, op.parentName, op.objectType, op.objectName, op.propCode, Sql.inout(Sql.INTEGER(op.status)), op.string]
            def stmt = """
                          Begin
                            :1  := GMKOBJI.F_SETPROPSTRINGX(
                                    pLang_Code    => :2,
                                    pParent_type  => :3,
                                    pParent_name  => :4,
                                    pObject_type  => :5,
                                    pObject_name  => :6,
                                    pObject_prop  => :7,
                                    pTransl_stat  => :8,
                                    pProp_string  => :9
                                   );
                          End;
                       """
            sql.call(stmt, params){statusX, status ->
                op.statusX = statusX
                op.status = status
                log.debug "setPropString $op.statusX Text: $op.string"
            }
        } catch (SQLException e) {
            log.error("Error in setPropString", e)
        }
    }

    void invalidateStrings(Map dbValues) throws SQLException {
        String projectCode   = dbValues.projectCode
        String langCodeSrc  = dbValues.srcLocale
        String moduleType = 'J'
        String moduleName = dbValues.moduleName
        long timestamp = System.currentTimeMillis()
        try {
            def params = [projectCode, langCodeSrc,moduleType, moduleName, Sql.INTEGER]
            def stmt = """
                          Begin
                            update GMRSPRP set GMRSPRP_STAT_CODE=-5
                            where GMRSPRP_PROJECT=:1
                                and GMRSPRP_LANG_CODE   =:2
                                and GMRSPRP_MODULE_TYPE =:3
                                and GMRSPRP_MODULE_NAME =:4
                                and GMRSPRP_ACTIVITY_DATE<GMKOBJI.g_session_time;
                            :5:=GMKOBJI.f_CleanUp(-5);
                          End;
                       """
            sql.call(stmt, params){
                timestamp = System.currentTimeMillis() - timestamp
                log.debug "Obsoleted " + it + " properties in " + timestamp + " ms"
            }
        } catch (SQLException e) {
            log.error("Error in dbif.invalidateStrings", e)
        }
    }
}