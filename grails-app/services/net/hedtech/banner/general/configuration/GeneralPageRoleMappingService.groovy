/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.web.access.intercept.RequestmapFilterInvocationDefinition
import grails.transaction.Transactional
import grails.util.Holders
import org.apache.log4j.Logger
import org.hibernate.classic.Session
import org.springframework.http.HttpMethod

/**
 * The service class extends the InterceptUrlMapFilterInvocationDefinition to override the required methods to do the
 * CRUD operation for interceptUrlMap which is set in the Config.groovy.
 * If the grails.plugin.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap in the Cofig.groovy then this
 * service will get injected by the spring from BannerGeneralUtilityGrailsPlugin.groovy.
 */
class GeneralPageRoleMappingService extends RequestmapFilterInvocationDefinition {
    private static Logger logger = Logger.getLogger(GeneralPageRoleMappingService.class.name)

    def sessionFactory

    private static Map originalInterceptUrlMap

    private String wildcardKey = '/**'

    /**
     * Overriden because this method calls reset().
     */
    @Override
    protected void initialize() {
        if (initialized) {
            return
        }
        try {
            reset()
            initialized = true
        } catch (Exception e) {
            logger.error("Exception initializing; Error message is: { " + e.getMessage() + " }", e)
        }
    }

    /**
     * Overriden method will get all the IntercetedUrlMap from the Config.groovy and DB.
     */
    @Override
    public void reset() {
        resetConfigs()

        List<InterceptedUrl> data = pageRoleMappingListFromDBAndConfig()

        for (InterceptedUrl iu : data) {
            compileAndStoreMapping(iu)
        }

        logger.debug("configs: $data")
    }

    /**
     * The service method to get the intercept url from DB and Config.groovy to assign the values to compiled map.
     */
    protected List<InterceptedUrl> pageRoleMappingListFromDBAndConfig() {
        List<InterceptedUrl> data = new ArrayList<InterceptedUrl>()
        def pageRoleMappingList = getPageRoleMappingList()
        LinkedHashMap<String, List<String>> interceptedUrlMapFromDB = new LinkedHashMap<String, List<String>>()
        pageRoleMappingList.each { String key, ArrayList<GeneralPageRoleMapping> grmList ->
            def roleList = []
            grmList.each { GeneralPageRoleMapping grm ->
                roleList << grm.roleCode
            }
            grmList.each {
                interceptedUrlMapFromDB.put(key, super.split(roleList?.join(',')))
            }
        }
        Map interceptedUrlMapFromConfig

        if (originalInterceptUrlMap == null) {
            originalInterceptUrlMap = ReflectionUtils.getConfigProperty("interceptUrlMap").clone()
        }
        interceptedUrlMapFromConfig = originalInterceptUrlMap.clone()

        interceptedUrlMapFromConfig.putAll(interceptedUrlMapFromDB)
        Holders.config.grails.plugin.springsecurity.interceptUrlMap = [:]

        if (interceptedUrlMapFromConfig.get(wildcardKey)) {
            def wildcardValue = interceptedUrlMapFromConfig.get(wildcardKey)
            interceptedUrlMapFromConfig.remove(wildcardKey)
            interceptedUrlMapFromConfig << [(wildcardKey): wildcardValue]
        }

        // Prepare List of interceptedUrlMap from the Merged data.
        interceptedUrlMapFromConfig.each { k, v ->
            HttpMethod method = null
            InterceptedUrl iu = new InterceptedUrl(k, super.split(v?.join(',')), method)
            Holders.config.grails.plugin.springsecurity.interceptUrlMap?.put(k, super.split(v?.join(',')))
            data.add(iu)
        }
        data
    }

    /**
     * Method is used to get the Hibernate session from the main context by the help of datasource.
     * @return Session     Classic hibernate session.
     */
    protected Session getHibernateSession() {
        Session session
        try {
            def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
            def ctx = Holders.grailsApplication.mainContext
            sessionFactory = Holders.grailsApplication.getMainContext().sessionFactory
            def hibernateSessionFactory = (!sessionFactory ? ctx.sessionFactory : sessionFactory)
            session = hibernateSessionFactory.openSession(dataSource.getSsbConnection())
        } catch (e) {
            logger.error('Exception creating Hibernate session;', e)
        }
        session
    }

    /**
     * This method will be get the list of all intercepted url map from DB and Config.groovy.
     * @return list
     */
    @Transactional(readOnly = true)
    private def getPageRoleMappingList() {
        fetchGeneralPageRoleMappingByAppId()
    }

    /**
     * Return the compiled intercepted url map.
     * @return compiled map.
     */
    public def fetchCompiledValue() {
        return compiled
    }

    /**
     * The private method and this common method will be called with Hibernate Session passed as a param.
     * @param session
     */
    private LinkedHashMap fetchGeneralPageRoleMappingByAppId() {
        def list
        def generalPageRoleMapping = new LinkedHashMap<String, ArrayList<GeneralPageRoleMapping>>()
        try {
            String appId = Holders.grailsApplication.metadata['app.appId']
            if (appId) {
                if (!sessionFactory) {
                    Session session
                    try {
                        session = getHibernateSession()
                        list = session.createQuery('''SELECT new GeneralPageRoleMapping(generalPageRoleMapping.pageUrl,
                                                                                generalPageRoleMapping.roleCode,
                                                                                generalPageRoleMapping.applicationName,
                                                                                generalPageRoleMapping.displaySequence,
                                                                                generalPageRoleMapping.pageId,
                                                                                generalPageRoleMapping.applicationId,
                                                                                generalPageRoleMapping.version)
                                                                          FROM GeneralPageRoleMapping generalPageRoleMapping
                                                                          WHERE generalPageRoleMapping.applicationId = :appId
                                                                          AND generalPageRoleMapping.statusIndicator = true ''')
                                .setParameter('appId', appId).list()
                    }
                    catch (e) {
                        logger.error('Exception while executing the query with new Hibernate session;')
                    }
                    finally {
                        session?.close()
                    }
                } else {
                    list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(appId, true)
                }

                def urlSet = new LinkedHashSet<String>()
                list.each { GeneralPageRoleMapping grm ->
                    urlSet.add(grm.pageUrl)
                }

                urlSet.each { String pageUrl ->
                    def pageRoleMappingList = new ArrayList<GeneralPageRoleMapping>()
                    list.each { GeneralPageRoleMapping pageRoleMapping ->
                        if (pageRoleMapping.pageUrl == pageUrl) {
                            pageRoleMappingList << pageRoleMapping
                        }
                    }
                    generalPageRoleMapping.put(pageUrl, pageRoleMappingList)
                }
            }
        } catch (e) {
            logger.error("Exception in get list of GeneralPageRoleMapping", e)
        }
        generalPageRoleMapping
    }


}
