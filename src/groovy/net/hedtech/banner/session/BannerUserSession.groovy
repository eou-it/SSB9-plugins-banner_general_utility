/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.session

import javax.persistence.*

@Entity
@Table(name="GURSESS")
class BannerUserSession implements Serializable {

    @Id
    @Column(name = "GURSESS_SURROGATE_ID")
    @SequenceGenerator(name = "GURSESS_SEQ_GEN", allocationSize = 1, sequenceName = "GURSESS_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURSESS_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GURSESS
     */
    @Version
    @Column(name = "GURSESS_VERSION", nullable = false, precision = 19)
    Long version

    /**
	 * UserID
	 */
	@Column(name="GURSESS_USER_ID", length=30)
	String lastModifiedBy

	/**
	 * Activity Date of the last change
	 */
	@Column(name="GURSESS_ACTIVITY_DATE")
	Date lastModified

	/**
	 * Data Origin column for GURSESS
	 */
	@Column(name="GURSESS_DATA_ORIGIN", length=30)
	String dataOrigin

    @Column(name="GURSESS_USER", length=30)
    String sessionToken

    @Column(name="GURSESS_NAME")
    String infoType

    @Transient
    Object info

    @Column(name="GURSESS_VALUE")
    String infoPersisted

    @Column(name="GURSESS_VALUE_TYPE")
    String infoDataType

    static constraints = {
        lastModifiedBy(nullable:true, maxSize:30)
		lastModified(nullable:true)
		dataOrigin(nullable:true, maxSize:30)
        sessionToken(nullable:false, maxSize:150)
        infoType(nullable:false, maxSize:1000)
        info(nullable:false)
        infoDataType(nullable:false)
        infoPersisted(nullable:false)
    }

    Object getInfo () {
        dbDecodeInfo(infoPersisted, infoDataType)
    }

    void setInfo (Object info) {
        infoPersisted = dbEncodeInfo (info, info.class.name)
        this.infoDataType = info.class.name
    }

    private def dbEncodeInfo (info, dataType) {
        if (dataType == "java.util.Date") {
            ""+((Date)info)?.getTime()
        } else {
            info
        }
    }

    private def dbDecodeInfo (info, dataType) {
        if (dataType == "java.util.Date") {
            new Date(Long.parseLong(info))
        } else {
            info
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        BannerUserSession that = (BannerUserSession) o;

        if (dataOrigin != that.dataOrigin) return false;
        if (id != that.id) return false;
        if (info != that.info) return false;
        if (infoType != that.infoType) return false;
        if (lastModified != that.lastModified) return false;
        if (lastModifiedBy != that.lastModifiedBy) return false;
        if (sessionToken != that.sessionToken) return false;
        if (version != that.version) return false;

        return true;
    }

    int hashCode() {
        int result = 0
        if (id) result = id.hashCode();
        if (version) result = 31 * result + version.hashCode();
        if (lastModifiedBy) result = 31 * result + lastModifiedBy.hashCode();
        if (lastModified) result = 31 * result + lastModified.hashCode();
        if (dataOrigin) result = 31 * result + dataOrigin.hashCode();
        if (sessionToken) result = 31 * result + sessionToken.hashCode();
        if (infoType) result = 31 * result + infoType.hashCode();
        if (infoPersisted) result = 31 * result + getInfo().hashCode();
        return result;
    }


    public String toString () {
        def s = getInfo()
        """BannerUserSession[
                id=$id,
                sessionToken=$sessionToken,
                infoType=$infoType,
                info=$s,
                version=$version,
                lastModifiedBy=$lastModifiedBy,
                lastModified=$lastModified,
                dataOrigin=$dataOrigin]"""
    }
}
