/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * The persistent class for the GURCTLEP database table.
 *
 */
@Entity
@Table(name = 'GURCTLEP')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigControllerEndpointPage.fetchAll',
                query = '''FROM ConfigControllerEndpointPage ccep'''),

])

public class ConfigControllerEndpointPage implements Serializable {

    private static final long serialVersionUID = 90000L


    @Id
    @SequenceGenerator(name = 'GURCTLEP_SEQ_GENERATOR', allocationSize = 1, sequenceName = 'GURCTLEP_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GURCTLEP_SEQ_GENERATOR')
    @Column(name = 'GURCTLEP_SURROGATE_ID')
    Long id


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURCTLEP_ACTIVITY_DATE')
    Date lastModified

    /**
     * Data origin column for GURCTLEP
     */
    @Column(name = 'GURCTLEP_DATA_ORIGIN')
    String dataOrigin


    @Column(name = 'GURCTLEP_DESCRIPTION')
    String description


    @Column(name = 'GURCTLEP_DISPLAY_SEQUENCE')
    Long displaySequence


    @Type(type = "yes_no")
    @Column(name = 'GURCTLEP_STATUS_INDICATOR')
    Boolean statusIndicator = true


   /**
    * Foreign Key : FK_GURCTLEP_INV_GUBAPPL
    */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GURCTLEP_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication configApplication


    @Column(name = 'GURCTLEP_PAGE_URL')
    String pageUrl


    @Column(name = 'GURCTLEP_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GURCTLEP_VERSION')
    Long version


    @Column(name = 'GURCTLEP_PAGE_ID')
    String pageId


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigControllerEndpointPage gurctlep = (ConfigControllerEndpointPage) o

        if (id != gurctlep.id) return false
        if (lastModified != gurctlep.lastModified) return false
        if (configApplication != gurctlep.configApplication) return false
        if (dataOrigin != gurctlep.dataOrigin) return false
        if (description != gurctlep.description) return false
        if (displaySequence != gurctlep.displaySequence) return false
        if (statusIndicator != gurctlep.statusIndicator) return false
        if (pageId != gurctlep.pageId) return false
        if (pageUrl != gurctlep.pageUrl) return false
        if (lastModifiedBy != gurctlep.lastModifiedBy) return false
        if (version != gurctlep.version) return false

        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (statusIndicator != null ? statusIndicator.hashCode() : 0)
        result = 31 * result + (configApplication != null ? configApplication.hashCode() : 0)
        result = 31 * result + (pageUrl != null ? pageUrl.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """
            ConfigControllerEndpointPage{
                id=$id,
                activityDate=$lastModified,
                dataOrigin='$dataOrigin',
                description='$description',
                displaySequence=$displaySequence,
                statusIndicator='$statusIndicator',
                configApplication=$configApplication,
                pageUrl='$pageUrl',
                userId='$lastModifiedBy',
                version=$version,
                pageId=$pageId
            }"""
    }


    static constraints = {
        pageId(nullable: false, maxSize: 60)
        pageUrl(nullable: true, maxSize: 256)
        configApplication(nullable: false)
        statusIndicator( nullable: false, maxSize:1)
        displaySequence(nullable: true)
        description(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def fetchAll() {
        def controllerEndpointPages
        controllerEndpointPages = ConfigControllerEndpointPage.withSession { session ->
            controllerEndpointPages = session.getNamedQuery('ConfigControllerEndpointPage.fetchAll').list()
        }
        return controllerEndpointPages
    }

}
