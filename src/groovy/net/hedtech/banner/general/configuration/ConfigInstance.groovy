/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBCEUR database table.
 *
 */
@Entity
@Table(name = 'GUBCEUR')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigInstance.fetchAll',
                    query = '''FROM ConfigInstance configInstance''')
])

public class ConfigInstance implements Serializable {

    private static final long serialVersionUID = 99999L

    @Id
    @SequenceGenerator(name = 'GUBCEUR_SEQ_GENERATOR', sequenceName = 'GUBCEUR_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUBCEUR_SEQ_GENERATOR')
    @Column(name = 'GUBCEUR_SURROGATE_ID', precision = 19)
    Long id


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GUBCEUR_ACTIVITY_DATE')
    Date lastModified


    @Column(name = 'GUBCEUR_DATA_ORIGIN')
    String dataOrigin


    @Column(name = 'GUBCEUR_ENVIRNOMENT_SEQ')
    Long env

    /**
     * Foreign Key : FK_GUBCEUR_INV_GUBAPPL
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GUBCEUR_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication configApplication


    @Column(name = 'GUBCEUR_URL')
    String url


    @Column(name = 'GUBCEUR_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GUBCEUR_VERSION')
    Long version


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigInstance gubceur = (ConfigInstance) o

        if (lastModified != gubceur.lastModified) return false
        if (dataOrigin != gubceur.dataOrigin) return false
        if (env != gubceur.env) return false
        if (configApplication != gubceur.configApplication) return false
        if (id != gubceur.id) return false
        if (url != gubceur.url) return false
        if (lastModifiedBy != gubceur.lastModifiedBy) return false
        if (version != gubceur.version) return false

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
            ConfigInstance{
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
