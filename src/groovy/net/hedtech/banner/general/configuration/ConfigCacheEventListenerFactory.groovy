package net.hedtech.banner.general.configuration

import net.sf.ehcache.event.CacheEventListener
import net.sf.ehcache.event.CacheEventListenerFactory

public class ConfigCacheEventListenerFactory extends  CacheEventListenerFactory {

    @Override
    CacheEventListener createCacheEventListener(Properties properties) {
        return ConfigCacheEventListener.INSTANCE
    }
}
