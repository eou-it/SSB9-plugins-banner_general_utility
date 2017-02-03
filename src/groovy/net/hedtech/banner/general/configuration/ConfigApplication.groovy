/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBAPPL database table.
 *
 */
@Entity
@Table(name = 'GUBAPPL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigApplication.fetchAll',
                query = '''FROM ConfigApplication capp''')
])


public class ConfigApplication implements Serializable {

    private static final long serialVersionUID = 1000L

    /*
    * Surrogate ID for GUBAPPL
    */
    @Id
    @SequenceGenerator(name = 'GUBAPPL_SEQ_GENERATOR', sequenceName = 'GUBAPPL_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUBAPPL_SEQ_GENERATOR')
    @Column(name = 'GUBAPPL_SURROGATE_ID')
    Long id

    /**
     * Date that record was created or last updated.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GUBAPPL_ACTIVITY_DATE')
    Date lastModified

    /**
     * Generated unique numeric identifier for this entity.
     */
    @SequenceGenerator(name = 'GUBAPPL_APP_SEQ_GENERATOR', sequenceName = 'GUBAPPL_APP_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUBAPPL_APP_SEQ_GENERATOR')
    @Column(name = 'GUBAPPL_APP_ID')
    Long appId

    /**
     *  Name of the application.
     */
    @Column(name = 'GUBAPPL_APP_NAME')
    String appName

    /**
     *  Data origin column for GUBAPPL
     */
    @Column(name = 'GUBAPPL_DATA_ORIGIN')
    String dataOrigin

    /**
     * Last modified by column for GUBAPPL
     */
    @Column(name = 'GUBAPPL_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GUBAPPL_VERSION')
    Long version


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
        result = 31 * result + (appId != null ? appId.hashCode() : 0)
        result = 31 * result + (appName != null ? appName.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            Gubappl{
                id=$id,
                appId=$appId,
                appName='$appName',
                dataOrigin='$dataOrigin',
                userId='$lastModifiedBy',
                activityDate=$lastModified,
                version=$version
            }"""
    }


    static constraints = {
        appId(nullable: true)
        appName(nullable: false, maxSize: 255)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def fetchAll() {
        def configApplication
        configApplication = ConfigApplication.withSession { session ->
            configApplication = session.getNamedQuery('ConfigApplication.fetchAll').list()
        }
        return configApplication
    }
}
