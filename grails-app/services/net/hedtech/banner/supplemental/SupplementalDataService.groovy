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
    // Sql sql
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

}
