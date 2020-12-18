/*******************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.hibernate.Session
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * The persistent class for the GVQ_PAGE_ROLE_MAPPING database table.
 *
 */
@Entity
@Table(name = 'GVQ_PAGE_ROLE_MAPPING')
@NamedQueries([
        @NamedQuery(name = 'GeneralPageRoleMapping.fetchAll', query = '''FROM GeneralPageRoleMapping grm'''),
        @NamedQuery(name = 'GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator',
                query = '''FROM GeneralPageRoleMapping grm
                                WHERE grm.applicationId = :appId
                                and grm.statusIndicator = :statusIndicator''')
])
public class GeneralPageRoleMapping implements Serializable {
    private static final long serialVersionUID = 3080855838641210753L;

    @Id
    @Column(name = 'SURROGATE_ID')
    long id


    @Column(name = 'APPLICATION_ID')
    String applicationId


    @Column(name = 'PAGE_ID')
    String pageId


    @Column(name = 'APPLICATION_NAME')
    String applicationName


    @Type(type = "yes_no")
    @Column(name = 'STATUS_INDICATOR')
    Boolean statusIndicator


    @Column(name = 'DISPLAY_SEQUENCE')
    Integer displaySequence

    @Column(name = 'PAGE_URL')
    String pageUrl


    @Column(name = 'ROLE_CODE')
    String roleCode


    @Version
    @Column(name = 'VERSION')
    Long version

    GeneralPageRoleMapping(String pageUrl, String roleCode, String applicationName,
                      int displaySequence, String pageId, String applicationId, Long version) {
        this.pageUrl = pageUrl
        this.roleCode = roleCode
        this.applicationName = applicationName
        this.displaySequence = displaySequence
        this.pageId = pageId
        this.applicationId = applicationId
        this.version = version
    }

    GeneralPageRoleMapping() {}


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        GeneralPageRoleMapping that = (GeneralPageRoleMapping) o

        if (id != that.id) return false
        if (applicationId != that.applicationId) return false
        if (applicationName != that.applicationName) return false
        if (statusIndicator != that.statusIndicator) return false
        if (displaySequence != that.displaySequence) return false
        if (pageId != that.pageId) return false
        if (pageUrl != that.pageUrl) return false
        if (roleCode != that.roleCode) return false
        if (version != that.version) return false

        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = (applicationId != null ? applicationId.hashCode() : 0)
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0)
        result = 31 * result + (statusIndicator != null ? statusIndicator.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        result = 31 * result + (pageUrl != null ? pageUrl.hashCode() : 0)
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
                    statusIndicator='$statusIndicator',
                    displaySequence=$displaySequence,
                    pageId='$pageId',
                    pageUrl='$pageUrl',
                    roleCode='$roleCode',
                    version=$version
                }"""
    }


    public static List fetchAll() {
        List generalReqMapList
        generalReqMapList = GeneralPageRoleMapping.withSession { Session session ->
            generalReqMapList = session.getNamedQuery('GeneralPageRoleMapping.fetchAll').list()
        }
        return generalReqMapList
    }


    public static List fetchByAppIdAndStatusIndicator(appId, statusIndicator = true) {
        List generalReqMapList
        generalReqMapList = GeneralPageRoleMapping.withSession { Session session ->
            generalReqMapList = session.getNamedQuery('GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator').setParameter('appId', appId).setParameter('statusIndicator', statusIndicator).list()
        }
        return generalReqMapList
    }
}
