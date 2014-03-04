/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.session;

import java.util.List;
import java.util.Map;

public interface IBannerUserSessionContributor {

    /**
     * Extract the specific shared info to be persisted
     */
    public Map<String, Object> publish();

    /**
     * Update the app with the DB-retrieved
     * other-app shared info.
     *
     * @param bannerUserSession
     */
    public void consume(List<BannerUserSession> bannerUserSession);

}
