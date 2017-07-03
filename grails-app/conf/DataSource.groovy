/** *******************************************************************************
 Copyright 2009-2015 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */


// Support Hibernate annotations
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration


// Note: Most of the dataSource configuration resides in resources.groovy and in the
// installation-specific configuration file (see Config.groovy for the include).

dataSource {
    configClass = GrailsAnnotationConfiguration.class
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    loggingSql = false

}


hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
    config.location = [
            "classpath:hibernate-banner-general-utility.cfg.xml",
            "classpath:hibernate-banner-core.cfg.xml"
    ]
}
// environment specific settings
environments {
    development {
        dataSource {
        }
    }
    test {
        dataSource {
        }
    }
    production {
        dataSource {
        }
    }
}
