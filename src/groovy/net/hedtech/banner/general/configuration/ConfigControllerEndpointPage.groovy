/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURCTLEPP database table.
 *
 */
@Entity
@Table(name = 'GURCTLEPP')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigControllerEndpointPage.fetchAll',
                query = '''FROM ConfigControllerEndpointPage ccep'''),
/*        @NamedQuery(name = ConfigControllerEndpointPage.GET_ALL_CONFIG_BY_APP_NAME,
                query = '''SELECT new net.hedtech.banner.general.configuration.RequestURLMap(ccep.pageName, crpm.roleCode, capp.appName,
                                    ccep.displaySequence, ccep.pageId, ccep.gubapplAppId, ccep.version)
                                FROM ConfigControllerEndpointPage ccep, ConfigRolePageMapping crpm,
                                   ConfigApplication capp
                                WHERE (ccep.gubapplAppId = crpm.gubapplAppId AND ccep.pageId = crpm.pageId)
                                AND (ccep.gubapplAppId = capp.appId)
                                AND capp.appName = :appName''')*/
])

public class ConfigControllerEndpointPage implements Serializable {

    private static final long serialVersionUID = 90000L


    @Id
    @SequenceGenerator(name = 'GURCTLEPP_SEQ_GENERATOR', sequenceName = 'GURCTLEPP_SURROGATE_ID_SEQ')
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'GURCTLEPP_SEQ_GENERATOR')
    @Column(name = 'GURCTLEPP_SURROGATE_ID')
    Long id


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURCTLEPP_ACTIVITY_DATE')
    Date lastModified

    /**
     * Data origin column for GURCTLEPP
     */

    @Column(name = 'GURCTLEPP_DATA_ORIGIN')
    String dataOrigin


    @Column(name = 'GURCTLEPP_DESCRIPTION')
    String description


    @Column(name = 'GURCTLEPP_DISPLAY_SEQUENCE')
    Long displaySequence


    @Column(name = 'GURCTLEPP_ENABLE_DISABLE')
    String enableDisable


   /**
    * Foreign Key : FK_GURCTLEPP_INV_GUBAPPL
    */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name = "GURCTLEPP_GUBAPPL_APP_ID", referencedColumnName = "GUBAPPL_APP_ID")
    ])
    ConfigApplication gubapplAppId


    @Column(name = 'GURCTLEPP_PAGE_NAME')
    String pageName


    @Column(name = 'GURCTLEPP_USER_ID')
    String lastModifiedBy


    @Version
    @Column(name = 'GURCTLEPP_VERSION')
    Long version


    @Column(name = 'GURCTLEPP_PAGE_ID')
    Long pageId


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigControllerEndpointPage gurctlepp = (ConfigControllerEndpointPage) o

        if (lastModified != gurctlepp.lastModified) return false
        if (gubapplAppId != gurctlepp.gubapplAppId) return false
        if (dataOrigin != gurctlepp.dataOrigin) return false
        if (description != gurctlepp.description) return false
        if (displaySequence != gurctlepp.displaySequence) return false
        if (enableDisable != gurctlepp.enableDisable) return false
        if (id != gurctlepp.id) return false
        if (pageId != gurctlepp.pageId) return false
        if (pageName != gurctlepp.pageName) return false
        if (lastModifiedBy != gurctlepp.lastModifiedBy) return false
        if (version != gurctlepp.version) return false

        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (enableDisable != null ? enableDisable.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (pageName != null ? pageName.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """
            ConfigControllerEndpointPage{
                id=$id,
                activityDate=$lastModified,
                dataOrigin='$dataOrigin',
                description='$description',
                displaySequence=$displaySequence,
                enableDisable='$enableDisable',
                gubapplAppId=$gubapplAppId,
                pageName='$pageName',
                userId='$lastModifiedBy',
                version=$version,
                pageId=$pageId
            }"""
    }


    static constraints = {
        pageId(nullable: false, maxSize: 256)
        pageName(nullable: true, maxSize: 256)
        gubapplAppId(nullable: false)
        enableDisable(nullable: true)
        description(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def fetchAll() {
        def configRolePageMapping
        configRolePageMapping = ConfigControllerEndpointPage.withSession { session ->
            configRolePageMapping = session.getNamedQuery('ConfigControllerEndpointPage.fetchAll').list()
        }
        return configRolePageMapping
    }

    /**
     * Named query to fetch all data from this domain by appName.
     * @param appName String
     * @return list of RequestURLMap.
     */
    /*public static def getAllConfigByAppName(def appName) {
        def configRolePageMapping
        configRolePageMapping = ConfigControllerEndpointPage.withSession { session ->
            configRolePageMapping = session.getNamedQuery(GET_ALL_CONFIG_BY_APP_NAME).setString('appName', appName).list()
        }
        return configRolePageMapping
    }*/
}
