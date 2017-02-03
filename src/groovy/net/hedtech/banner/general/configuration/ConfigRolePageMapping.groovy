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
    private static final long serialVersionUID = 0L

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

    @Column(name = 'GURAPPR_GUBAPPL_APP_ID')
    Long gubapplAppId

    @Column(name = 'GURAPPR_USER_ID')
    String lastModifiedBy

    @Version
    @Column(name = 'GURAPPR_VERSION')
    Long version

    @Column(name = 'GURAPPR_PAGE_ID')
    Long pageId

    @Column(name = 'GURAPPR_TWTVROLE_CODE')
    String roleCode


    static constraints = {
        gubapplAppId(nullable: false)
        pageId(nullable: false)
        roleCode(nullable: false, maxSize: 30)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigRolePageMapping gurappr = (ConfigRolePageMapping) o

        if (lastModified != gurappr.lastModified) return false
        if (gubapplAppId != gurappr.gubapplAppId) return false
        if (roleCode != gurappr.roleCode) return false
        if (dataOrigin != gurappr.dataOrigin) return false
        if (id != gurappr.id) return false
        if (pageId != gurappr.pageId) return false
        if (lastModifiedBy != gurappr.lastModifiedBy) return false
        if (version != gurappr.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
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
                gubapplAppId=$gubapplAppId,
                userId='$lastModifiedBy',
                version=$version,
                pageId=$pageId,
                code='$roleCode'
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def fetchAll() {
        ConfigRolePageMapping configRolePageMapping
        configRolePageMapping = ConfigRolePageMapping.withSession { session ->
            configRolePageMapping = session.getNamedQuery('ConfigRolePageMapping.fetchAll').list()
        }
        return configRolePageMapping
    }
}
