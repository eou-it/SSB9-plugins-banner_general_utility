/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUROCFG database table.
 *
 */
@Entity
@Table(name = 'GUROCFG', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigurationProperties.findAll',
                query = '''FROM ConfigurationProperties cp'''),
        @NamedQuery(name = 'ConfigurationProperties.findByAppName',
                query = '''FROM ConfigurationProperties cp
                                WHERE cp.gubapplAppId = (SELECT capp.appId
                                                            FROM ConfigApplication capp
                                                            WHERE capp.appName = :appName)''')
])
public class ConfigurationProperties implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @SequenceGenerator(name = 'GUROCFG_SEQ_GENERATOR', sequenceName = 'GUROCFG_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUROCFG_SEQ_GENERATOR')
    @Column(name = 'GUROCFG_SURROGATE_ID', precision = 19)
    Long id

    @Column(name = 'CONFIG_NAME', length = 50)
    String configName

    @Temporal(TemporalType.DATE)
    @Column(name = 'GUROCFG_ACTIVITY_DATE', nullable = false)
    Date lastModified

    @Column(name = 'GUROCFG_CONFIG_TYPE', length = 30)
    String configType

    @Lob
    @Column(name = 'GUROCFG_CONFIG_VALUE')
    String configValue

    @Column(name = 'GUROCFG_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GUROCFG_GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GUROCFG_USER_ID', length = 30)
    String lastModifiedBy

    @Version
    @Column(name = 'GUROCFG_VERSION', precision = 19)
    Long version

    public ConfigurationProperties() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigurationProperties gurocfg = (ConfigurationProperties) o

        if (lastModified != gurocfg.lastModified) return false
        if (configName != gurocfg.configName) return false
        if (configType != gurocfg.configType) return false
        if (configValue != gurocfg.configValue) return false
        if (dataOrigin != gurocfg.dataOrigin) return false
        if (gubapplAppId != gurocfg.gubapplAppId) return false
        if (id != gurocfg.id) return false
        if (lastModifiedBy != gurocfg.lastModifiedBy) return false
        if (version != gurocfg.version) return false

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
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigurationProperties{
                id=$id,
                configName='$configName',
                activityDate=$lastModified,
                configType='$configType',
                configValue='$configValue',
                dataOrigin='$dataOrigin',
                gubapplAppId=$gubapplAppId,
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configurationProperties
        configurationProperties = ConfigurationProperties.withSession { session ->
            configurationProperties = session.getNamedQuery('ConfigurationProperties.findAll').list()
        }
        return configurationProperties
    }

    /**
     * Named query to fetch all data from this domain by app name.
     * @return List
     */
    public static def findByAppName(def appName) {
        def configurationProperties
        configurationProperties = ConfigurationProperties.withSession { session ->
            configurationProperties = session.getNamedQuery('ConfigurationProperties.findByAppName')
                    .setString('appName', appName).list()
        }
        return configurationProperties
    }
}
