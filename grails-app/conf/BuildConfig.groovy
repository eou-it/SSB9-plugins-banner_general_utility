grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.plugin.location.'spring-security-cas' = "../spring_security_cas.git"
grails.plugin.location.'banner-core'="../banner_core.git"
grails.project.dependency.resolution = {

    inherits( "global" ) {
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    plugins {
        runtime  ":hibernate:3.6.10.10"
        compile ":spring-security-core:1.2.7.3"
        compile ':functional-test:1.2.7'
        compile ':resources:1.1.6'
        compile ':markdown:1.0.0.RC1'
		runtime ":webxml:1.4.1"
    }

    distribution = {
    }

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        } else
        {
            grailsPlugins()
            grailsHome()
            grailsCentral()
            mavenCentral()
            mavenRepo "http://repository.jboss.org/maven2/"
            mavenRepo "http://repository.codehaus.org"
        }
    }

    dependencies {
    }

}

// CodeNarc rulesets
codenarc.ruleSetFiles="rulesets/banner.groovy"
codenarc.reportName="target/CodeNarcReport.html"
codenarc.propertiesFile="grails-app/conf/codenarc.properties"

