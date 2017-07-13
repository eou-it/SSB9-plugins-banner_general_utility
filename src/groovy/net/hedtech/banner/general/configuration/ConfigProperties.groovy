/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.apache.log4j.Logger

import javax.persistence.*

/**
 * The persistent class for the GUROCFG database table.
 *
 */

@Entity
@Table(name = 'GUROCFG')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigProperties.fetchByAppId',
                query = '''FROM ConfigProperties cp WHERE cp.configApplication = :appId'''),
        @NamedQuery(name = 'ConfigProperties.fetchSimpleConfigByAppId',
                query = '''FROM ConfigProperties cp WHERE cp.configApplication = :appId
                           and cp.configType in ('boolean','string','integer')''')
])
public class ConfigProperties implements Serializable {
    private static final long serialVersionUID = 10009L

    private static Logger logger = Logger.getLogger(ConfigProperties.getClass().getName())

    @Id
    @SequenceGenerator(name = 'GUROCFG_SEQ_GENERATOR', sequenceName = 'GUROCFG_SURROGATE_ID_SEQUENCE')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GUROCFG_SEQ_GENERATOR')
    @Column(name = 'GUROCFG_SURROGATE_ID')
    Long id


    @Column(name = 'GUROCFG_NAME')
    String configName


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GUROCFG_ACTIVITY_DATE')
    Date lastModified


    @Column(name = 'GUROCFG_TYPE')
    String configType


    @Lob
    @Column(name = 'GUROCFG_VALUE')
    String configValue


    @Column(name = 'GUROCFG_DATA_ORIGIN')
    String dataOrigin

    /**
     * Foreign Key : FK_GUROCFG_INV_GUBAPPL
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GUROCFG_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication configApplication


    @Column(name = 'GUROCFG_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GUROCFG_VERSION')
    Long version


    static constraints = {
        configName(maxSize: 50)
        lastModified(nullable: true)
        configType(maxSize: 30)
        configValue(nullable: true)
        dataOrigin(maxSize: 30, nullable: true)
        configApplication(nullable: true)
        lastModifiedBy(maxSize: 30, nullable: true)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigProperties gurocfg = (ConfigProperties) o

        if (lastModified != gurocfg.lastModified) return false
        if (configName != gurocfg.configName) return false
        if (configType != gurocfg.configType) return false
        if (configValue != gurocfg.configValue) return false
        if (dataOrigin != gurocfg.dataOrigin) return false
        if (configApplication != gurocfg.configApplication) return false
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
        result = 31 * result + (configApplication != null ? configApplication.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigProperties{
                id=$id,
                configName='$configName',
                activityDate=$lastModified,
                configType='$configType',
                configValue='$configValue',
                dataOrigin='$dataOrigin',
                configApplication=$configApplication,
                userId='$lastModifiedBy',
                version=$version
            }"""
    }

    /**
     * Named query to fetch data from this domain based on App Id.
     * @return List
     */
    public static List fetchByAppId(String appId) {
        List configProperties = []
        if (appId) {
            configProperties = ConfigProperties.withSession { session ->
                configProperties = session.getNamedQuery('ConfigProperties.fetchByAppId')
                        .setString('appId', appId).list()
            }
        }
        return configProperties
    }


    public static List fetchSimpleConfigByAppId(String appId) {
        List configProperties = []

        configProperties = ConfigProperties.withSession { session ->
            configProperties = session.getNamedQuery('ConfigProperties.fetchSimpleConfigByAppId')
                    .setString('appId', appId).list()
        }
        return configProperties
    }
}
