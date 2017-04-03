package net.hedtech.banner.general.configuration

import net.sf.ehcache.CacheException
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.event.CacheEventListener

class ConfigCacheEventListener implements CacheEventListener {

    public static final CacheEventListener INSTANCE = new ConfigCacheEventListener();

    @Override
    void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        log.warn("................put..............")
    }

    @Override
    void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    void notifyElementExpired(Ehcache ehcache, Element element) {
        log.warn(".................expired..................")

    }

    @Override
    void notifyElementEvicted(Ehcache ehcache, Element element) {
        log.warn(".................evicted................")
    }

    @Override
    void notifyRemoveAll(Ehcache ehcache) {

    }

    @Override
    void dispose() {

    }
}
