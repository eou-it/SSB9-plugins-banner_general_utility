package net.hedtech.banner.supplemental

import groovy.sql.Sql

import org.apache.log4j.Logger

import java.text.SimpleDateFormat
import java.text.ParseException
import net.hedtech.banner.configuration.SupplementalDataUtils
import net.hedtech.banner.exceptions.ApplicationException
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.GrailsApplication
import net.hedtech.banner.supplemental.SupplementalPropertyDiscriminatorContent
import net.hedtech.banner.supplemental.SupplementalPropertyValue

/**
 * DAO for supplemental data. This strategy works against the
 * GOVSDAV view for both reading and writing supplemental data.
 */
class SupplementalDataService {

    def dataSource               // injected by Spring
    def sessionFactory           // injected by Spring
    def grailsApplication        // injected by Spring

    private final Logger log = Logger.getLogger(getClass())

    /**
     * Returns the conditions if SDE is enabled for that UI component.
     * @param block id from UI Block Component
     */
    public boolean hasSde(id) {

        def sdeFound = false

        if (id == null || id.indexOf("Block") < 0)
            return false

        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication")

        def domainClass = grailsApplication.getArtefactByLogicalPropertyName("Domain", id.substring(0, id.indexOf("Block")))

        if (domainClass == null)
            return false

        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(domainClass?.getClazz())?.tableName.toUpperCase())

        if (tableName == null)
            return false

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.call("{$Sql.VARCHAR = call gb_sde_table.f_exists($tableName)}") { sde ->
                sdeFound = "Y".equals(sde)
            }
            return sdeFound
            //return false
        } catch (e) {
            log.error("ERROR: Could not SDE set up for table - $tableName . ${e.message}")
            throw e
        } finally {
            sql?.close()
        }
    }

    /**
     * Returns the conditions if SDE data is availablle for the model
     * @param model the model that has supplemental data to persist
     */
    public boolean hasSdeData(model) {

        def sdeDataFound

        def id = model.id

        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(model.getClass())?.tableName.toUpperCase())

        if (tableName == null)
            return false

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
       DECLARE
            l_pkey GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
        	l_ex    VARCHAR2(1):='N';
            l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${tableName},${id});
       BEGIN
              l_pkey := gp_goksdif.f_get_pk(${tableName},l_rowid);

              l_ex:=gp_goksdif.f_sd_exists(${tableName},l_pkey);

              ${Sql.VARCHAR} := l_ex;

        END ;
            """) {sdeData ->
            sdeDataFound = sdeData
        }

        return "Y".equals(sdeDataFound)
    }


    public def loadSupplementalDataForModel(model) {
        log.trace "In load: ${model}"

        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(model.getClass()).tableName.toUpperCase())
            def attributeName
            def id = model.id

            sql.call("""
	  declare
	      l_pkey 	GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
	      l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${tableName},${id});
	   begin
	       gp_goksdif.p_insert_disc(${tableName});
	       l_pkey := gp_goksdif.f_get_pk(${tableName},l_rowid);
	       gp_goksdif.p_set_current_pk(l_pkey);
	   end;
           """
            )

            def resultSetAttributesList = sessionFactory.getCurrentSession().createSQLQuery(
                    """SELECT DISTINCT govsdav_attr_name as attrName
	         FROM govsdav WHERE govsdav_table_name= :tableName
	""").setString("tableName", tableName).list()

            def supplementalProperties = [:]
            resultSetAttributesList.each() {
                loadSupplementalProperty(it, supplementalProperties, tableName)
            }

            supplementalProperties
        } catch (e) {
            log.error "Failed to load SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
            throw e
        }
    }

    /**
     * Returns the supplied model after persisting it's supplemental data.
     * @param model the model that has supplemental data to persist
     * @return def the model fully populated with supplemental data re-loaded from the database
     */
    public def persistSupplementalDataFor(model, prop) {
        log.trace "In persist: ${model}"
        def sql

        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(model.getClass()).tableName.toUpperCase())
            def sdeTableName = 'GORSDAV'

            def id
            def attributeName
            String disc
            def parentTab
            def dataType
            def value

            prop.each {

                log.debug "KEY: ${it.key} - VALUE: ${it.value}"
                def map = it.value
                attributeName = it.key

                map.each {
                    def paramMap = it.value
                    log.debug "VALUE: " + it.value

                    id = paramMap.id
                    value = paramMap.value
                    disc = paramMap.disc
                    parentTab = paramMap.pkParentTab

                    parentTab = parentTab ?: getPk(tableName, model.id)
                    dataType = paramMap.dataType

                    value = value ?: ""
                    disc = disc ?: "1"

                    if (value) {
                        validateDataType(dataType, value)
                    }

                    if (log.isDebugEnabled()) debug(id, tableName, attributeName, disc, parentTab, dataType, value)

                    // Validation Call

                    if (value && value.getAt(0) == "0" && value.getAt(1) == ".") {  // Decimal
                        value = value.substring(1)
                    }

                    sql.call("""
	                       DECLARE

	                        lv_msg varchar2(2000);
	                        p_value_as_char_out varchar2(2000);

	                        BEGIN

	                        p_value_as_char_out := ${value};

	                        lv_msg := gp_goksdif.f_validate_value(
	                            p_table_name => ${tableName},
	                            p_attr_name => ${attributeName},
	                            p_disc => ${disc},
	                            p_pk_parenttab => ${parentTab},
	                            p_attr_data_type => ${dataType},
	                            p_form_or_process => 'BANNER',
	                            p_value_as_char => p_value_as_char_out
	                        );

	                         ${Sql.VARCHAR} := lv_msg;

	                END ;
                  """
                    ) {msg ->
                        if (msg != "Y")
                            throw new ApplicationException(model, msg)
                    }

                    // End Validation

                    sql.call("""declare
					                  l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${sdeTableName},${id});
					              begin
					                  gp_goksdif.p_set_attribute( ${tableName}, ${attributeName}, ${disc},
							                                      ${parentTab}, l_rowid, ${dataType}, ${value} );
					              end;
	                           """)

                }
            }

            if (disc && disc.isNumber()) {
                sql.executeUpdate("""
                                   update GORSDAV
                                      set GORSDAV_DISC = rownum
                                    where  GORSDAV_TABLE_NAME = ${tableName}
                                      and GORSDAV_PK_PARENTTAB = ${parentTab}
                                      and GORSDAV_ATTR_NAME = ${attributeName}
                                   """
                )
            }
        } catch (e) {
            log.error "Failed to save SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
            throw e
        }
    }

    // Loads the identified attribute into the supplied supplementalProperties map

    private def loadSupplementalProperty(String attributeName, Map supplementalProperties, String tableName) {
        def session = sessionFactory.getCurrentSession()

        def resultSet = session.createSQLQuery(
                """  SELECT govsdav_attr_name,
                      govsdav_attr_reqd_ind,
                      DECODE(govsdav_attr_data_type,'DATE', TO_CHAR(x.govsdav_value.accessDATE(), g\$_date.get_nls_date_format),govsdav_value_as_char),
                      govsdav_disc,
                      govsdav_pk_parenttab,
                      govsdav_surrogate_id,
                      govsdav_attr_data_type,
                      REPLACE( govsdav_attr_prompt_disp, '%DISC%',govsdav_disc ),
                      govsdav_disc_type,
                      govsdav_disc_validation,
                      govsdav_attr_data_len,
                      govsdav_attr_data_scale,
                      govsdav_attr_info,
                      govsdav_attr_order,
                      govsdav_disc_method,
                      govsdav_GJAPDEF_VALIDATION,
                      govsdav_LOV_FORM,
                      govsdav_LOV_TABLE_OVRD,
                      govsdav_LOV_ATTR_OVRD,
                      govsdav_LOV_CODE_TITLE,
                      govsdav_LOV_DESC_TITLE
               FROM govsdav x
                   WHERE govsdav_table_name = :tableName
                   AND govsdav_attr_name = :attributeName
               """
        ).setString("tableName", tableName).
                setString("attributeName", attributeName).list()

        if (!supplementalProperties."${attributeName}") supplementalProperties."${attributeName}" = [:]
        resultSet.each() {

            if (!it[9]?.isInteger())
                it[9] = '1'

            String lovValidation = it[15]
            String lovForm = it[16]
            String lovTable = (lovForm == 'GTQSDLV') ? 'GTVSDLV' : lovForm

            def columnNames = []

            /**
             * TODO need to move this logic into SupplementalDataService's resetSDE method
             */
            if (lovValidation == 'LOV_VALIDATION') {
                log.debug("Querying for $lovForm for Table Metadata")
                Sql sql = new Sql(ApplicationHolder.getApplication().getMainContext().sessionFactory.getCurrentSession().connection())
                String query = "select * from " + lovTable
                sql.query(query) { rs ->
                    def meta = rs.metaData
                    if (meta.columnCount <= 0) return

                    log.debug("LOV Table column names ....")
                    for (i in 0..<meta.columnCount) {
                        log.debug "${i}: ${meta.getColumnLabel(i + 1)}".padRight(20)
                        columnNames << meta.getColumnLabel(i + 1)
                        log.debug "\n"
                    }
                    log.debug '-' * 40
                }

                log.debug("Querying on SDE Lookup Table executed")
                sql.connection.close()
            }

            SupplementalPropertyDiscriminatorContent discProp =
                new SupplementalPropertyDiscriminatorContent(required: it[1],
                        value: it[2],
                        disc: (it[3] != null ? it[3] : 1),
                        pkParentTab: it[4],
                        id: it[5],
                        dataType: it[6],
                        prompt: it[7],
                        discType: it[8],
                        validation: it[9] != null ? it[9].toInteger() : 1,
                        dataLength: it[10],
                        dataScale: it[11],
                        attrInfo: it[12],
                        attrOrder: it[13],
                        discMethod: it[14],
                        lovValidation: lovValidation,
                        lovProperties: [
                                lovForm: lovForm,
                                lovTableOverride: it[17],
                                lovAttributeOverride: it[18],
                                lovCodeTitle: it[19],
                                lovDescTitle: it[20],
                                columnNames: columnNames
                        ]
                )

            if (discProp.lovValidation && !(discProp.lovProperties?.lovForm)) {
                log.error "LOV_FORM is NOT mentioned for LOV $attributeName in the table GORSDAM"
            }

            SupplementalPropertyValue propValue = new SupplementalPropertyValue([(discProp.disc): discProp])
            supplementalProperties."${attributeName}" << propValue
        }
    }

    private def getPk(def table, def id) {
        def sql

        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())

            def pk
            sql.call("""declare
					          l_pkey varchar2(1000);
					          l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${table},${id});
					      begin
					          l_pkey := gp_goksdif.f_get_pk(${table},l_rowid);
			                  ${Sql.VARCHAR} := l_pkey;
			              end;
		               """) { key -> pk = key }
            return pk
        } catch (e) {
            log.error "Failed to get PK for the entity. Exception: $e "
            throw e
        }
    }

    // ---------------------------- Helper Methods -----------------------------------


    def isNumeric = {
        def formatter = java.text.NumberFormat.instance
        def pos = [0] as java.text.ParsePosition
        formatter.parse(it, pos)     // if parse position index has moved to end of string
        // them the whole string was numeric
        pos.index == it.size()
    }


    private boolean isValidDateFormats(String dateStr, String... formats) {
        for (String format : formats) {
            SimpleDateFormat sdf = new SimpleDateFormat(format)
            sdf.setLenient(false)
            try {
                sdf.parse(dateStr)
                return true
            } catch (ParseException e) {
                // Ignore because its not the right format.
            }
        }
        return false
    }


    private boolean isDateValid(dateStr) {
        def validDate = false
        if (dateStr.length() == 4) {
            validDate = isValidDateFormats(dateStr, "yyyy")
        } else if (dateStr.indexOf('/') > 0) {
            validDate = isValidDateFormats(dateStr.toLowerCase(), "MM/dd/yyyy", "MM/yyyy", "yyyy/MM/dd")
        } else if (dateStr.indexOf('-') > 0) {
            validDate = isValidDateFormats(dateStr.toLowerCase(), "MM-dd-yyyy", "MM-yyyy", "yyyy-MM-dd", "dd-MMM-yyyy")
        } else {
            validDate = isValidDateFormats(dateStr.toLowerCase(), "ddMMMyyyy", "MMMyyyy")
        }
        return validDate
    }


    private def validateDataType(dataType, String value) {
        if (dataType.equals("NUMBER") && !isNumeric(value)) {
            throw new RuntimeException("Invalid Number")
        }
        else if (dataType.equals("DATE") && value && !isDateValid(value)) {
            throw new RuntimeException("Invalid Date")
        }
    }

    private def debug(id, tableName, attributeName, String disc, parentTab, dataType, String value) {
        log.debug "*****************************"
        log.debug "id: " + id
        log.debug "tableName:" + tableName
        log.debug "attributeName:" + attributeName
        log.debug "disc: " + disc
        log.debug "parentTab: " + parentTab
        log.debug "dataType: " + dataType
        log.debug "value: " + value
        log.debug "*****************************"

        /** *****************************************************
         * This code may be enabled in order to debug Oracle ROWID
         * *****************************************************
         sql = new Sql(sessionFactory.getCurrentSession().connection())
         sql.call ("""
         declare
         l_pkey 	GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
         l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${sdeTableName},${id});
         begin
         ${Sql.VARCHAR} := l_rowid;
         end ;
         """
         ){key ->
         log.info "ROWID:" +	key}**************************************************************   */
    }


}
