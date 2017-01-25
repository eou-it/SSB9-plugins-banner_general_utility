/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBAIR database table.
 *
 */
@Entity
@Table(name = 'GUBAIR', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigInstance.findAll', query = '''FROM ConfigInstance configInstance''')
])
public class ConfigInstance implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @SequenceGenerator(name = 'GUBAIR_SEQ_GENERATOR', sequenceName = 'GUBAIR_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUBAIR_SEQ_GENERATOR')
    @Column(name = 'GUBAIR_SURROGATE_ID', precision = 19)
    Long id

    @Temporal(TemporalType.DATE)
    @Column(name = 'GUBAIR_ACTIVITY_DATE', nullable = false)
    Date lastModified

    @Column(name = 'GUBAIR_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GUBAIR_ENV', nullable = false, precision = 19)
    Long env

    @Column(name = 'GUBAIR_GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GUBAIR_URL', length = 256)
    String url

    @Column(name = 'GUBAIR_USER_ID', length = 30)
    String lastModifiedBy

    @Version
    @Column(name = 'GUBAIR_VERSION', precision = 19)
    Long version

    public ConfigInstance() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigInstance gubair = (ConfigInstance) o

        if (lastModified != gubair.lastModified) return false
        if (dataOrigin != gubair.dataOrigin) return false
        if (env != gubair.env) return false
        if (gubapplAppId != gubair.gubapplAppId) return false
        if (id != gubair.id) return false
        if (url != gubair.url) return false
        if (lastModifiedBy != gubair.lastModifiedBy) return false
        if (version != gubair.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (env != null ? env.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (url != null ? url.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            Gubair{
                id=$id,
                activityDate=$lastModified,
                dateOrigin='$dataOrigin',
                env=$env,
                gubapplAppId=$gubapplAppId,
                url='$url',
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configInstance
        configInstance = ConfigInstance.withSession { session ->
            configInstance = session.getNamedQuery('ConfigInstance.findAll').list()
        }
        return configInstance
    }
}
