/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import org.hibernate.Session

import javax.persistence.*

/**
 * The persistent class for the GVQ_GURVCTAP database table.
 *
 */
@Entity
@Table(name = 'GVQ_GURVCTAP')
@NamedQueries([
        @NamedQuery(name = 'GeneralRequestMap.fetchAll', query = '''FROM GeneralRequestMap grm'''),
        @NamedQuery(name = 'GeneralRequestMap.fetchByApp',
                query = '''FROM GeneralRequestMap grm
                    WHERE grm.applicationId = :appId''')
])
public class GeneralRequestMap implements Serializable {
    private static final long serialVersionUID = 3080855838641210753L;

    @Id
    @Column(name = 'APPLICATION_ID')
    long applicationId;

    @Id
    @Column(name = 'APPLICATION_NAME')
    String applicationName;

    @Column(name = 'DISPLAY_SEQUENCE')
    int displaySequence;

    @Id
    @Column(name = 'PAGE_ID')
    long pageId;

    @Column(name = 'PAGE_NAME')
    String pageName;

    @Column(name = 'ROLE_CODE_LIST')
    String roleCodeList;

    @Version
    @Column(name = 'VERSION')
    Long version;

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        GeneralRequestMap that = (GeneralRequestMap) o

        if (applicationId != that.applicationId) return false
        if (applicationName != that.applicationName) return false
        if (displaySequence != that.displaySequence) return false
        if (pageId != that.pageId) return false
        if (pageName != that.pageName) return false
        if (roleCodeList != that.roleCodeList) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (applicationId != null ? applicationId.hashCode() : 0)
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        result = 31 * result + (pageName != null ? pageName.hashCode() : 0)
        result = 31 * result + (roleCodeList != null ? roleCodeList.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
                GeneralRequestMap{
                    applicationId=$applicationId,
                    applicationName='$applicationName',
                    displaySequence=$displaySequence,
                    pageId='$pageId',
                    pageName='$pageName',
                    roleCodeList='$roleCodeList',
                    version=$version
                }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def fetchAll() {
        def generalReqMapList
        generalReqMapList = ConfigApplication.withSession { session ->
            generalReqMapList = session.getNamedQuery('GeneralRequestMap.fetchAll').list()
        }
        return generalReqMapList
    }

    /**
     * Named query to fetch the data with criteria app id.
     * @param appId
     * @return
     */
    public static def fetchByApp(appId) {
        def generalReqMapList
        generalReqMapList = ConfigApplication.withSession { Session session ->
            generalReqMapList = session.getNamedQuery('GeneralRequestMap.fetchByApp').setParameter('appId', appId).list()
        }
        return generalReqMapList
    }
}
