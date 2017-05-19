/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import oracle.jdbc.*
import org.apache.log4j.Logger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import net.hedtech.banner.textmanager.TextManagerUtil

class TextManagerDB {
    def dataSource
    ObjectProperty defaultProp

    String projectCode
    String langCodeSrc
    String langCodeTgt
    String moduleType
    String moduleName

    private static final def log = Logger.getLogger(TextManagerDB.class.name)

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

    public Connection conn

    public createConnection (){
        DriverManager.registerDriver(new OracleDriver())
        conn = DriverManager.getConnection(dataSource.underlyingSsbDataSource.url,
                dataSource.underlyingSsbDataSource.username, dataSource.underlyingSsbDataSource.password)
    }

    public void closeConnection() throws SQLException {
        if ( !conn.isClosed()) {
            conn.commit();
            conn.close();
        }
    }

    public ObjectProperty getDefaultObjectProp() {
        return defaultProp
    }

    void setDefaultProp(tmUtil){
        defaultProp = new ObjectProperty()
        if(tmUtil.dbValues.srcIndicator.equals("s"))
            defaultProp.langCode = tmUtil.dbValues.srcLocale
        else
            defaultProp.langCode = tmUtil.dbValues.tgtLocale
    }

    String getModuleName(String fileName, String moduleName) {
        int begin, end
        if (moduleName != null)
            return moduleName
        begin = fileName.lastIndexOf("/")
        end = fileName.lastIndexOf("\\")
        if (end >= 0 && begin < end)
            begin = end
        if (begin < 0)
            begin = 0
        else
            begin++
        end = fileName.lastIndexOf(".")
        return fileName.substring(begin, end).toUpperCase()
    }

    void setDBContext(TextManagerUtil tmUtil) throws SQLException {
        int defStatus = 1 //set to 1 for properties - assume translatable by default
        int sqlTrace = 0
        long timestamp = System.currentTimeMillis()
        projectCode   = TextManagerUtil.dbValues.projectCode
        langCodeSrc  = TextManagerUtil.dbValues.srcLocale
        langCodeTgt  = TextManagerUtil.dbValues.tgtLocale
        moduleType = "J"
        moduleName = getModuleName(TextManagerUtil.dbValues.srcFile, TextManagerUtil.dbValues.moduleName)

        //Reverse extract.
        if (TextManagerUtil.dbValues.srcIndicator.equals("r")) {
            defStatus = 7 //set to Reverse extracted
        }
        try {
            OracleCallableStatement stmt = null
            stmt = conn.prepareCall(
                    "Begin \n" +
                            "   if :1>0 then \n" +
                            "         DBMS_SESSION.SET_SQL_TRACE(TRUE); \n" +
                            "   end if; \n" +
                            "   GMKOBJI.P_SETCONTEXT( \n" +
                            "             :2,\n" +
                            "             :3,\n" +
                            "             :4,\n" +
                            "             :5,\n" +
                            "             :6,\n" +
                            "             :7);\n" +
                            "End;")
            stmt.setInt(1, sqlTrace)
            stmt.setString(2, projectCode)
            stmt.setString(3, langCodeSrc)
            stmt.setString(4, langCodeTgt)
            stmt.setString(5, moduleType)
            stmt.setString(6, moduleName)
            stmt.setInt(7, defStatus)
            stmt.execute()
        } catch (SQLException e) {
            log.error("Error in SetDBContext", e)
        }
        timestamp = System.currentTimeMillis() - timestamp
        log.debug("SetDBContext done in " + timestamp + " ms")
    }


    void setModuleRecord(TextManagerUtil tmUtil) throws SQLException {
        String dataSrc=TextManagerUtil.dbValues.srcFile
        String langCode=langCodeSrc
        String modDesc


        switch ( TextManagerUtil.dbValues.srcIndicator.charAt(0) ) {
            case 's':
                dataSrc=TextManagerUtil.dbValues.srcFile
                langCode=langCodeSrc
                modDesc="Properties batch extract"
                break
            case 'r':
                dataSrc=TextManagerUtil.dbValues.srcFile
                langCode=langCodeTgt
                modDesc="Properties batch reverse extract"
                break
            default: //q and t both translate
                dataSrc=TextManagerUtil.dbValues.tgtFile
                langCode=langCodeTgt
                modDesc="Properties batch translate"
        }
        try{
            OracleCallableStatement stmt = null
            stmt = conn.prepareCall(
                    "Declare \n"+
                            "   b1 GMRMDUL.GMRMDUL_PROJECT%type :=:1;\n"+
                            "   b2 GMRMDUL.GMRMDUL_MODULE_NAME%type  :=:2;\n"+
                            "   b3 GMRMDUL.GMRMDUL_MODULE_TYPE%type  :=:3;\n"+
                            "   b4 GMRMDUL.GMRMDUL_LANG_CODE%type    :=:4;\n"+
                            "   b5 GMRMDUL.GMRMDUL_SRC_LANG_CODE%type:=:5;\n"+
                            "   b6 GMRMDUL.GMRMDUL_MOD_DESC%type     :=:6;\n"+
                            "   b7 GMRMDUL.GMRMDUL_DATASOURCE%type   :=:7;\n"+
                            "Begin \n" +
                            "   insert into \n" +
                            "   GMRMDUL (GMRMDUL_PROJECT,GMRMDUL_MODULE_NAME,GMRMDUL_MODULE_TYPE, \n" +
                            "          GMRMDUL_LANG_CODE,GMRMDUL_SRC_LANG_CODE,GMRMDUL_MOD_DESC,GMRMDUL_DATASOURCE,\n" +
                            "        GMRMDUL_USER_ID,GMRMDUL_ACTIVITY_DATE)\n" +
                            "   values (b1,b2,b3,b4,b5,b6,b7,user,sysdate);\n"+
                            "Exception when dup_val_on_index then\n"+
                            "   update GMRMDUL \n"+
                            "      set GMRMDUL_ACTIVITY_DATE = sysdate\n"+
                            "         ,GMRMDUL_MOD_DESC  = b6 \n"+
                            "         ,GMRMDUL_DATASOURCE= b7 \n"+
                            "         ,GMRMDUL_USER_ID   = user\n"+
                            "   where GMRMDUL_PROJECT = b1 \n"+
                            "     and GMRMDUL_MODULE_NAME  = b2 \n"+
                            "     and GMRMDUL_MODULE_TYPE  = b3 \n"+
                            "     and GMRMDUL_LANG_CODE    = b4;\n"+
                            "End;"   )
            stmt.setString(1,projectCode)
            stmt.setString(2,moduleName)
            stmt.setString(3,moduleType)
            stmt.setString(4,langCode)
            stmt.setString(5,langCodeSrc)
            stmt.setString(6,modDesc)
            stmt.setString(7,dataSrc)
            stmt.execute()
            log.debug("setModuleRecord project: $project_code sourceName: $module_name langCode: $lang_code modDesc: $mod_desc dataSource: $data_source")
        } catch (SQLException e) {
            log.error("Error in setModuleRecord", e)
        }
    }

    void setPropString(ObjectProperty op) throws SQLException {
        try {
            OracleCallableStatement stmt = null
            stmt = conn.prepareCall(
                    "Begin\n" +
                            "   :1  := GMKOBJI.F_SETPROPSTRINGX(\n" +
                            "      pLang_Code    => :2,   \n" +
                            "      pParent_type  => :3,   \n" +
                            "      pParent_name  => :4,   \n" +
                            "      pObject_type  => :5,   \n" +
                            "      pObject_name  => :6,   \n" +
                            "      pObject_prop  => :7,   \n" +
                            "      pTransl_stat  => :8,   \n" +
                            "      pProp_string  => :9    \n" +
                            "   );\n" +
                            "End;")
            stmt.registerOutParameter(1, OracleTypes.VARCHAR)
            stmt.registerOutParameter(8, OracleTypes.INTEGER)
            stmt.setString(2, op.langCode)
            stmt.setInt(3, op.parentType)
            stmt.setString(4, op.parentName)
            stmt.setInt(5, op.objectType)
            stmt.setString(6, op.objectName)
            stmt.setInt(7, op.propCode)
            stmt.setInt(8, op.status)
            stmt.setString(9, op.string)
            stmt.execute()
            op.statusX = stmt.getString(1)
            op.status = stmt.getInt(8)
            log.debug("  setPropString $op.statusX Text: $op.string")
        } catch (SQLException e) {
                log.error("Error in setPropString string=", e)
        }
    }

    void invalidateStrings() throws SQLException {
        long timestamp = System.currentTimeMillis()
        try {
            OracleCallableStatement stmt = null
            stmt = conn.prepareCall(
                    "Begin\n" +
                            "  update GMRSPRP set GMRSPRP_STAT_CODE=-5\n" +
                            "  where GMRSPRP_PROJECT=:1\n" +
                            "    and GMRSPRP_LANG_CODE   =:2\n" +
                            "    and GMRSPRP_MODULE_TYPE =:3\n" +
                            "    and GMRSPRP_MODULE_NAME =:4\n" +
                            "    and GMRSPRP_ACTIVITY_DATE<GMKOBJI.g_session_time;\n" +
                            "  :5:=GMKOBJI.f_CleanUp(-5);\n" +
                            "End;"
            )

            stmt.setString(1, projectCode)
            stmt.setString(2, langCodeSrc)
            stmt.setString(3, moduleType)
            stmt.setString(4, moduleName)
            stmt.registerOutParameter(5, OracleTypes.INTEGER)
            stmt.execute()
            timestamp = System.currentTimeMillis() - timestamp
            log.debug("Obsoleted " + stmt.getString(5) + " properties in " + timestamp + " ms")
        } catch (SQLException e) {
            log.error("Error in dbif.invalidateStrings", e)
        }
    }
}
