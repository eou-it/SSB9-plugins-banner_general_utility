/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURAPPR database table.
 *
 */
@Entity
@Table(name = 'GURAPPR')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigRolePageMapping.fetchAll',
                    query = '''FROM ConfigRolePageMapping crpm''')
])
public class ConfigRolePageMapping implements Serializable {

    private static final long serialVersionUID = 100L

    @Id
    @SequenceGenerator(name = 'GURAPPR_SEQ_GENERATOR', sequenceName = 'GURAPPR_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GURAPPR_SEQ_GENERATOR')
    @Column(name = 'GURAPPR_SURROGATE_ID')
    Long id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURAPPR_ACTIVITY_DATE')
    Date lastModified


    @Column(name = 'GURAPPR_DATA_ORIGIN')
    String dataOrigin


    /**
     * Foreign Key : FK_GURAPPR_INV_GURCTLEP
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GURAPPR_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication configApplication


    @Column(name = 'GURAPPR_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GURAPPR_VERSION',  nullable = false, precision = 19)
    Long version


    /**
     * Foreign Key : FK_GURAPPR_INV_GURCTLEP
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GURAPPR_PAGE_ID", referencedColumnName = "GURCTLEP_PAGE_ID")
    ])
    ConfigControllerEndpointPage endpointPage


    @Column(name = 'GURAPPR_ROLE_CODE')
    String roleCode



    static constraints = {
        endpointPage(nullable: false , maxSize: 60)
        roleCode(nullable: false, maxSize: 256)
        configApplication(nullable: false)
        lastModified(nullable: true)
        dataOrigin(nullable:true, maxSize: 30)
        lastModifiedBy(maxSize: 30, nullable: true)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigRolePageMapping gurappr = (ConfigRolePageMapping) o

        if (lastModified != gurappr.lastModified) return false
        if (configApplication != gurappr.configApplication) return false
        if (roleCode != gurappr.roleCode) return false
        if (dataOrigin != gurappr.dataOrigin) return false
        if (id != gurappr.id) return false
        if (endpointPage != gurappr.endpointPage) return false
        if (lastModifiedBy != gurappr.lastModifiedBy) return false
        if (version != gurappr.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (configApplication != null ? configApplication.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (endpointPage != null ? endpointPage.hashCode() : 0)
        result = 31 * result + (roleCode != null ? roleCode.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigRolePageMapping{
                id=$id,
                activityDate=$lastModified,
                dataOrigin='$dataOrigin',
                gubapplAppId=$configApplication,
                userId='$lastModifiedBy',
                version=$version,
                endpointPage=$endpointPage,
                code='$roleCode'
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def fetchAll() {
        def configRolePageMappings
        configRolePageMappings = ConfigRolePageMapping.withSession { session ->
            configRolePageMappings = session.getNamedQuery('ConfigRolePageMapping.fetchAll').list()
        }
        return configRolePageMappings
    }
}
