/*******************************************************************************
 Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
grails.project.groupId = "net.hedtech" // used when deploying to a maven repo

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml', 'application/vnd.sungardhe.student.v0.01+xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data',
        jpg: 'image/jpeg',
        png: 'image/png',
        gif: 'image/gif',
        bmp: 'image/bmp',
        svg:'image/svg+xml',
        svgz:'image/svg+xml'
]

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64  **** note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

grails.converters.domain.include.version = true
//grails.converters.json.date = "default"

grails.converters.json.pretty.print = true
grails.converters.json.default.deep = true

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true



// ******************************************************************************
//
//                       +++ DATA ORIGIN CONFIGURATION +++
//
// ******************************************************************************
// This field is a Banner standard, along with 'lastModifiedBy' and lastModified.
// These properties are populated automatically before an entity is inserted or updated
// within the database. The lastModifiedBy uses the username of the logged in user,
// the lastModified uses the current timestamp, and the dataOrigin uses the value
// specified here:
dataOrigin = "Banner"

// ******************************************************************************
//
//                       +++ FORM-CONTROLLER MAP +++
//
// ******************************************************************************
// This map relates controllers to the Banner forms that it replaces.  This map
// supports 1:1 and 1:M (where a controller supports the functionality of more than
// one Banner form.  This map is critical, as it is used by the security framework to
// set appropriate Banner security role(s) on a database connection. For example, if a
// logged in user navigates to the 'medicalInformation' controller, when a database
// connection is attained and the user has the necessary role, the role is enabled
// for that user and Banner object.
formControllerMap = [
        '/':['GUAGMNU'],
        'uiCatalog' : ['SELFSERVICE'],
        'home' : ['SELFSERVICE']
]
grails {
    plugin {
        springsecurity {
            logout {
                afterLogoutUrl = "/"
                mepErrorLogoutUrl='/logout/logoutPage'
            }
            useRequestMapDomainClass = false
            securityConfigType = grails.plugin.springsecurity.SecurityConfigType.InterceptUrlMap
            interceptUrlMap = [
                    [pattern:'/', access:['IS_AUTHENTICATED_ANONYMOUSLY']],
                    [pattern:'/login/**', access:['IS_AUTHENTICATED_ANONYMOUSLY']],
                    [pattern:'/logout/**', access:['IS_AUTHENTICATED_ANONYMOUSLY']],
                    [pattern:'/index/', access:['IS_AUTHENTICATED_ANONYMOUSLY']],
                    [pattern:'/**', access:['IS_AUTHENTICATED_ANONYMOUSLY']],
            ]
        }
    }
}


// Note: Most of the dataSource configuration resides in resources.groovy and in the
// installation-specific configuration file (see Config.groovy for the include).
//
dataSource {
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    loggingSql = false
}


hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    //hbm2ddl.auto = null
    show_sql = false
    packagesToScan="net.hedtech.**.*"
    flush.mode = AUTO
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
    config.location = [
            "classpath:hibernate-banner-core.cfg.xml",
            "classpath:hibernate-banner-core.testing.cfg.xml"
    ]
}

//Added for integration tests to run in plugin level
grails.config.locations = [
        BANNER_APP_CONFIG: "banner_configuration.groovy"
]

//Added for integration tests to test with role
environments {
    test {
        aboutInfoAccessRoles = ["ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M"]
    }
}

