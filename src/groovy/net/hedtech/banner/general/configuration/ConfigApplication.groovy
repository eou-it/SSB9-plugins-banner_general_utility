/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBAPPL database table.
 *
 */
@Entity
@Table(name = 'GUBAPPL', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigApplication.findAll', query = '''FROM ConfigApplication capp''')
])
public class ConfigApplication implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @SequenceGenerator(name = 'GUBAPPL_SEQ_GENERATOR', sequenceName = 'GUBAPPL_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUBAPPL_SEQ_GENERATOR')
    @Column(name = 'GUBAPPL_SURROGATE_ID', precision = 19)
    Long id

    @Temporal(TemporalType.DATE)
    @Column(name = 'GUBAPPL_ACTIVITY_DATE', nullable = false)
    Date lastModified

    @Column(name = 'GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long appId

    @Column(name = 'GUBAPPL_APP_NAME', length = 255)
    String appName

    @Column(name = 'GUBAPPL_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GUBAPPL_USER_ID', length = 30)
    String lastModifiedBy

    @Version
    @Column(name = 'GUBAPPL_VERSION', precision = 19)
    Long version

    public ConfigApplication() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigApplication gubappl = (ConfigApplication) o

        if (lastModified != gubappl.lastModified) return false
        if (appId != gubappl.appId) return false
        if (appName != gubappl.appName) return false
        if (dataOrigin != gubappl.dataOrigin) return false
        if (id != gubappl.id) return false
        if (lastModifiedBy != gubappl.lastModifiedBy) return false
        if (version != gubappl.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (appId != null ? appId.hashCode() : 0)
        result = 31 * result + (appName != null ? appName.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            Gubappl{
                id=$id,
                activityDate=$lastModified,
                appId=$appId,
                appName='$appName',
                dataOrigin='$dataOrigin',
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configApplication
        configApplication = ConfigApplication.withSession { session ->
            configApplication = session.getNamedQuery('ConfigApplication.findAll').list()
        }
        return configApplication
    }
}
