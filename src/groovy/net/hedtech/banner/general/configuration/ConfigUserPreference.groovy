/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURUCFG database table.
 *
 */
@Entity
@Table(name = 'GURUCFG', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigUserPreference.findAll',
                query = '''FROM ConfigUserPreference configUsrPref''')
])
public class ConfigUserPreference implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @SequenceGenerator(name = 'GURUCFG_SEQ_GENERATOR', sequenceName = 'GURUCFG_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GURUCFG_SEQ_GENERATOR')
    @Column(name = 'GURUCFG_SURROGATE_ID', precision = 19)
    Long id

    @Column(name = 'CONFIG_NAME', length = 50)
    String configName

    @Temporal(TemporalType.DATE)
    @Column(name = 'GURUCFG_ACTIVITY_DATE', nullable = false)
    Date lastModified

    @Column(name = 'GURUCFG_CONFIG_TYPE', length = 30)
    String configType

    @Lob
    @Column(name = 'GURUCFG_CONFIG_VALUE')
    String configValue

    @Column(name = 'GURUCFG_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GURUCFG_GUBAPPL_APP_ID', unique = true, nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GURUCFG_PIDM', precision = 19)
    Long pidm

    @Column(name = 'GURUCFG_USER_ID', length = 30)
    String lastModifiedBy

    @Version
    @Column(name = 'GURUCFG_VERSION', precision = 19)
    Long version

    public ConfigUserPreference() {
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
        if (gubapplAppId != gurucfg.gubapplAppId) return false
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
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
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
                gubapplAppId=$gubapplAppId,
                pidm=$pidm,
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configUserPreference
        configUserPreference = ConfigUserPreference.withSession { session ->
            configUserPreference = session.getNamedQuery('ConfigUserPreference.findAll').list()
        }
        return configUserPreference
    }
}
