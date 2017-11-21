/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURUCFG database table.
 *
 */
@Entity
@Table(name = 'GURUCFG')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigUserPreference.fetchAll',
                query = ''' FROM ConfigUserPreference configUsrPref '''),

        @NamedQuery(name = 'ConfigUserPreference.fetchByPidm',
                query = """ FROM ConfigUserPreference configUsrPref
                WHERE configUsrPref.pidm = :pidm """),

        @NamedQuery(name = 'ConfigUserPreference.fetchByConfigNamePidmAndAppId',
                query = """ FROM ConfigUserPreference configUsrPref
                WHERE configUsrPref.configName = :configName
                and configUsrPref.configApplication = :appId
                and configUsrPref.pidm = :pidm
                """)
])

public class ConfigUserPreference implements Serializable {
    private static final long serialVersionUID = 190099L

    @Id
    @SequenceGenerator(name = 'GURUCFG_SEQ_GENERATOR', allocationSize = 1, sequenceName = 'GURUCFG_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GURUCFG_SEQ_GENERATOR')
    @Column(name = 'GURUCFG_SURROGATE_ID')
    Long id

    @Column(name = 'GURUCFG_CONFIG_NAME')
    String configName

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURUCFG_ACTIVITY_DATE')
    Date lastModified

    @Column(name = 'GURUCFG_CONFIG_TYPE')
    String configType

    @Lob
    @Column(name = 'GURUCFG_CONFIG_VALUE')
    String configValue

    @Column(name = 'GURUCFG_DATA_ORIGIN')
    String dataOrigin

    /**
     * Foreign Key : FK_GURUCFG_INV_GUBAPPL
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GURUCFG_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication configApplication


    @Column(name = 'GURUCFG_PIDM')
    Integer pidm


    @Column(name = 'GURUCFG_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GURUCFG_VERSION')
    Long version


    static constraints = {
        configName(maxSize: 256)
        lastModified(nullable: true)
        configType(maxSize: 30)
        dataOrigin(maxSize: 30, nullable: true)
        configApplication(nullable: false)
        lastModifiedBy(maxSize: 30, nullable: true)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigUserPreference gurucfg = (ConfigUserPreference) o

        if (lastModified != gurucfg.lastModified) return false
        if (configName != gurucfg.configName) return false
        if (configType != gurucfg.configType) return false
        if (configValue != gurucfg.configValue) return false
        if (dataOrigin != gurucfg.dataOrigin) return false
        if (configApplication != gurucfg.configApplication) return false
        if (id != gurucfg.id) return false
        if (pidm != gurucfg.pidm) return false
        if (lastModifiedBy != gurucfg.lastModifiedBy) return false
        if (version != gurucfg.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (configName != null ? configName.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (configType != null ? configType.hashCode() : 0)
        result = 31 * result + (configValue != null ? configValue.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (configApplication != null ? configApplication.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigUserPreference{
                id=$id,
                configName='$configName',
                activityDate=$lastModified,
                configType='$configType',
                configValue='$configValue',
                dataOrigin='$dataOrigin',
                configApplication=$configApplication,
                pidm=$pidm,
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static List fetchAll() {
        List configUserPreferences = []
        configUserPreferences = ConfigUserPreference.withSession { session ->
            configUserPreferences = session.getNamedQuery('ConfigUserPreference.fetchAll').list()
        }
        return configUserPreferences
    }


    public static List fetchByPidm(Integer pidm) {
        List configUserPreferences = []
        if(pidm) {
            configUserPreferences = ConfigUserPreference.withSession { session ->
                configUserPreferences = session.getNamedQuery('ConfigUserPreference.fetchByPidm')
                        .setInteger('pidm', pidm).list()
            }
        }
        return configUserPreferences
    }


    public static ConfigUserPreference fetchByConfigNamePidmAndAppId(String configName, Integer pidm, String appId) {
        ConfigUserPreference configUserPreference
        if(pidm && appId && configName) {
            configUserPreference = ConfigUserPreference.withSession { session ->
                configUserPreference = session.getNamedQuery('ConfigUserPreference.fetchByConfigNamePidmAndAppId')
                        .setString('configName', configName)
                        .setInteger('pidm', pidm)
                        .setString('appId', appId)
                        .uniqueResult()
            }
        }
        return configUserPreference
    }



}
