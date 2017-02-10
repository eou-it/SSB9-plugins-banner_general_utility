/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBAIR database table.
 *
 */
@Entity
@Table(name = 'GUBAIR')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigInstance.fetchAll',
                    query = '''FROM ConfigInstance configInstance''')
])

public class ConfigInstance implements Serializable {

    private static final long serialVersionUID = 99999L

    @Id
    @SequenceGenerator(name = 'GUBAIR_SEQ_GENERATOR', sequenceName = 'GUBAIR_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUBAIR_SEQ_GENERATOR')
    @Column(name = 'GUBAIR_SURROGATE_ID', precision = 19)
    Long id


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GUBAIR_ACTIVITY_DATE')
    Date lastModified


    @Column(name = 'GUBAIR_DATA_ORIGIN')
    String dataOrigin


    @Column(name = 'GUBAIR_ENV')
    Long env

    /**
     * Foreign Key : FK_GUBAIR_INV_GUBAPPL
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GUBAIR_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication configApplication


    @Column(name = 'GUBAIR_URL')
    String url


    @Column(name = 'GUBAIR_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GUBAIR_VERSION')
    Long version


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigInstance gubair = (ConfigInstance) o

        if (lastModified != gubair.lastModified) return false
        if (dataOrigin != gubair.dataOrigin) return false
        if (env != gubair.env) return false
        if (configApplication != gubair.configApplication) return false
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
        result = 31 * result + (configApplication != null ? configApplication.hashCode() : 0)
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
                configApplication=$configApplication,
                url='$url',
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    static constraints = {
        env(nullable: true)
        url(nullable: true, maxSize: 256)
        configApplication(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static List fetchAll() {
        List configInstances = []
        configInstances = ConfigInstance.withSession { session ->
            configInstances = session.getNamedQuery('ConfigInstance.fetchAll').list()
        }
        return configInstances
    }
}
