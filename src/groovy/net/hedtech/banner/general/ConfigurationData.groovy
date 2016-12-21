/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import javax.persistence.*

@Entity
@Table(name = "GUROCFG")
@NamedQueries(value = [
        @NamedQuery(name = "ConfigurationData.fetchByNameandType",
                query = """FROM   ConfigurationData a
		   WHERE  a.name = :name
		   AND    a.type = :type
	      """),
        @NamedQuery(name = "ConfigurationData.fetchThemes",
                query = """FROM   ConfigurationData a
		   WHERE  a.type = :type
	      """)
])
class ConfigurationData implements Serializable{

    @Id
    @Column(name="GUROCFG_SURROGATE_ID")
    @SequenceGenerator(name = "GUROCFG_SEQ_GEN", sequenceName = "GUROCFG_SURROGATE_ID_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GUROCFG_SEQ_GEN")
    Long id;

    @Column(name="GUROCFG_NAME", nullable = false, length = 50)
    String name;

    @Column(name="GUROCFG_TYPE", nullable = false, length = 30)
    String type

    @Column(name="GUROCFG_VALUE", nullable = true)
    @Lob
    String value;

    @Version
    @Column(name="GUROCFG_VERSION" , nullable = false, precision = 19)
    Long version

    @Column(name="GUROCFG_USER_ID", length = 30)
    String lastModifiedBy

    @Column(name="GUROCFG_DATA_ORIGIN", length = 30)
    String dataOrigin

    @Column(name = "GUROCFG_ACTIVITY_DATE" )
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigurationData that = (ConfigurationData) o

        if (dataOrigin != that.dataOrigin) return false
        if (id != that.id) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (name != that.name) return false
        if (type != that.type) return false
        if (value != that.value) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (type != null ? type.hashCode() : 0)
        result = 31 * result + (value != null ? value.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return "ConfigurationData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", version=" + version +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", dataOrigin='" + dataOrigin + '\'' +
                ", lastModified=" + lastModified +
                '}';
    }

    public static ConfigurationData fetchThemebyNameandType(String name, String type) {
        List<ConfigurationData> configurationData
        configurationData = ConfigurationData.withSession {session ->
            configurationData = session.getNamedQuery('ConfigurationData.fetchByNameandType').setString('name',name).setString('type',type).list()}
        ConfigurationData result = configurationData?.size()>0?configurationData.get(0):null
        return result
    }

    public static List fetchThemes(String type) {
        List configurationData
        configurationData = ConfigurationData.withSession {session ->
            configurationData = session.getNamedQuery('ConfigurationData.fetchThemes').setString('type',type).list()}
        return configurationData
    }
}
