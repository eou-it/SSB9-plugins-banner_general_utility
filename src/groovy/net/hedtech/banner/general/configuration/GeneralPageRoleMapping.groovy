/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.hibernate.Session

import javax.persistence.*

/**
 * The persistent class for the GVQ_PAGE_ROLE_MAPPING database table.
 *
 */
@Entity
@Table(name = 'GVQ_PAGE_ROLE_MAPPING')
@NamedQueries([
        @NamedQuery(name = 'GeneralPageRoleMapping.fetchAll', query = '''FROM GeneralPageRoleMapping grm'''),
        @NamedQuery(name = 'GeneralPageRoleMapping.fetchByAppId',
                query = '''FROM GeneralPageRoleMapping grm
                                WHERE grm.applicationId = :appId''')
])
public class GeneralPageRoleMapping implements Serializable {
    private static final long serialVersionUID = 3080855838641210753L;

    @Id
    @Column(name = 'SURROGATE_ID')
    long id

    @Column(name = 'APPLICATION_ID')
    String applicationId;

    @Column(name = 'PAGE_ID')
    long pageId;

    @Column(name = 'APPLICATION_NAME')
    String applicationName;

    @Column(name = 'DISPLAY_SEQUENCE')
    int displaySequence;

    @Column(name = 'PAGE_NAME')
    String pageName;

    @Column(name = 'ROLE_CODE')
    String roleCode;

    @Version
    @Column(name = 'VERSION')
    Long version;

    GeneralPageRoleMapping(String pageName, String roleCode, String applicationName,
                      int displaySequence, long pageId, String applicationId, Long version) {
        this.pageName = pageName
        this.roleCode = roleCode
        this.applicationName = applicationName
        this.displaySequence = displaySequence
        this.pageId = pageId
        this.applicationId = applicationId
        this.version = version
    }

    GeneralPageRoleMapping() {}

    static mapping = {
        cache true
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        GeneralPageRoleMapping that = (GeneralPageRoleMapping) o

        if (id != that.id) return false
        if (applicationId != that.applicationId) return false
        if (applicationName != that.applicationName) return false
        if (displaySequence != that.displaySequence) return false
        if (pageId != that.pageId) return false
        if (pageName != that.pageName) return false
        if (roleCode != that.roleCode) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = (applicationId != null ? applicationId.hashCode() : 0)
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        result = 31 * result + (pageName != null ? pageName.hashCode() : 0)
        result = 31 * result + (roleCode != null ? roleCode.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
                GeneralPageRoleMapping{
                    id=$id,
                    applicationId=$applicationId,
                    applicationName='$applicationName',
                    displaySequence=$displaySequence,
                    pageId='$pageId',
                    pageName='$pageName',
                    roleCode='$roleCode',
                    version=$version
                }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static List fetchAll() {
        List generalReqMapList
        generalReqMapList = ConfigApplication.withSession { Session session ->
            generalReqMapList = session.getNamedQuery('GeneralPageRoleMapping.fetchAll').list()
        }
        return generalReqMapList
    }

    /**
     * Named query to fetch the data with criteria app id.
     * @param appId
     * @return
     */
    public static List fetchByAppId(appId) {
        List generalReqMapList
        generalReqMapList = ConfigApplication.withSession { Session session ->
            generalReqMapList = session.getNamedQuery('GeneralPageRoleMapping.fetchByAppId').setParameter('appId', appId).list()
        }
        return generalReqMapList
    }
}
