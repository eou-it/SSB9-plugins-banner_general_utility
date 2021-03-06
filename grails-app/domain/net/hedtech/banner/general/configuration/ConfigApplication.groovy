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
        @NamedQuery(name = "ConfigApplication.fetchByAppName",
                    query = """ FROM ConfigApplication capp WHERE capp.appName = :appName """),
        @NamedQuery(name = "ConfigApplication.fetchByAppId",
                query = """ FROM ConfigApplication capp WHERE capp.appId = :appId """)
])


public class ConfigApplication implements Serializable {

    private static final long serialVersionUID = 1000L

    /*
    * Surrogate ID for GUBAPPL
    */
    @Id
    @SequenceGenerator(name = 'GUBAPPL_SEQ_GENERATOR', allocationSize = 1, sequenceName = 'GUBAPPL_SURROGATE_ID_SEQUENCE')
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
    @Column(name = 'GUBAPPL_APP_ID')
    String appId


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
            ConfigApplication{
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
        appId(nullable: false, maxSize: 10)
        appName(nullable: false, maxSize: 255)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    public static ConfigApplication fetchByAppName(String appName) {
        ConfigApplication configApplication
        if (appName) {
            configApplication = ConfigApplication.withSession { session ->
                configApplication = session.getNamedQuery('ConfigApplication.fetchByAppName').setString('appName', appName).uniqueResult()
            }
        }
        return configApplication
    }


    public static ConfigApplication fetchByAppId(String appId) {
        ConfigApplication configApplication
        if (appId) {
            configApplication = ConfigApplication.withSession { session ->
                configApplication = session.getNamedQuery('ConfigApplication.fetchByAppId').setString('appId', appId).uniqueResult()
            }
        }
        return configApplication
    }
}
