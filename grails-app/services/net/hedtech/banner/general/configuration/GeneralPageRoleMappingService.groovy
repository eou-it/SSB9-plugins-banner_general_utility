/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.web.access.intercept.InterceptUrlMapFilterInvocationDefinition
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
class GeneralPageRoleMappingService extends InterceptUrlMapFilterInvocationDefinition {
    private static Logger logger = Logger.getLogger(GeneralPageRoleMappingService.getClass().getName())
    /**
     * This will be injected by the spring only when making this service call from the GeneralPageRoleMappingController.
     */
    def sessionFactory

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
    public synchronized void reset() {
        resetConfigs()

        List<InterceptedUrl> data = pageRoleMappingListFromDBAndConfig()

        for (InterceptedUrl iu : data) {
            compileAndStoreMapping(iu)
        }

        if (logger.isTraceEnabled()) {
            logger.trace("configs: {}", getConfigAttributeMap())
        }
    }

    /**
     * The service method to get the intercept url from DB and Config.groovy to assign the values to compiled map.
     */
    protected List<InterceptedUrl> pageRoleMappingListFromDBAndConfig() {
        List<InterceptedUrl> data = new ArrayList<InterceptedUrl>()
        def map = getList()
        LinkedHashMap<String, List<String>> interceptedUrlMapFromDB = new LinkedHashMap<String, List<String>>()
        map.each { String key, ArrayList<GeneralPageRoleMapping> grmList ->
            def roleList = []
            def grmListFinal = []
            grmList.each { GeneralPageRoleMapping grm ->
                grmListFinal << grm
                def roleCode = (grm.roleCode == 'WEBUSER' ? 'IS_AUTHENTICATED_ANONYMOUSLY' : grm.roleCode)
                roleList << pushValuesToPrepareSelfServiceRoles(roleCode)
            }
            grmListFinal.each {
                interceptedUrlMapFromDB.put(key, super.split(roleList?.join(',')))
            }
        }
        Map interceptedUrlMapFromConfig = ReflectionUtils.getConfigProperty("interceptUrlMap")
        Map mergedData = mergeMap(interceptedUrlMapFromConfig, interceptedUrlMapFromDB)

        // Prepare List of interceptedUrlMap from the Merged data.
        mergedData.each { k, v ->
            HttpMethod method = null
            data.add(new InterceptedUrl(k, super.split(v?.join(',')), method))
        }
        data
    }

    /**
     * Method is used to merging two maps.
     * @param firstMap Map
     * @param secondMap Map
     * @return Map
     */
    protected Map mergeMap(Map firstMap, Map secondMap) {
        def resultMap = [:]
        resultMap.putAll(firstMap)
        resultMap.putAll(secondMap)

        resultMap.each { key, value ->
            if (firstMap[key] && secondMap[key]) {
                resultMap[key] = firstMap[key] + secondMap[key]
            }
            if (resultMap[key] instanceof List) {
                resultMap[key].unique { x, y -> x <=> y }
            }
        }

        return resultMap
    }

    /**
     * Method is used to prepare the values.
     * @param role TWTVROLE_CODE value from database.
     * @return String prepared role as a string.
     */
    protected String pushValuesToPrepareSelfServiceRoles(String role) {
        if (role != 'IS_AUTHENTICATED_ANONYMOUSLY') {
            return "ROLE_${"SELFSERVICE-$role".toUpperCase()}_${"BAN_DEFAULT_M".toUpperCase()}".toString()
        }
        return role
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
    private def getList() {
        fetchGeneralPageRoleMappingByAppId(getHibernateSession())
    }

    /**
     * This method can be called from the controller to get merged InterceptedUrlMap
     * from DB and Config.groovy.
     * @return
     */
    public def fetchListOfInterceptURLMap() {
        return getList()
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
    private LinkedHashMap fetchGeneralPageRoleMappingByAppId(Session session) {
        def list
        def generalPageRoleMapping = new LinkedHashMap<String, ArrayList<GeneralPageRoleMapping>>()
        try {
            String appId = getAppIdByAppName(session)
            if (appId) {
                if (!sessionFactory) {
                    list = session.createQuery('''SELECT new GeneralPageRoleMapping(generalPageRoleMapping.pageName,
                                                        generalPageRoleMapping.roleCode,
                                                        generalPageRoleMapping.applicationName,
                                                        generalPageRoleMapping.displaySequence,
                                                        generalPageRoleMapping.pageId,
                                                        generalPageRoleMapping.applicationId,
                                                        generalPageRoleMapping.version)
                                                  FROM GeneralPageRoleMapping generalPageRoleMapping
                                                  WHERE generalPageRoleMapping.applicationId = :appId''')
                            .setParameter('appId', appId).list()
                } else {
                    list = GeneralPageRoleMapping.fetchByAppId(appId)
                }

                def urlSet = new LinkedHashSet<String>()
                list.each { GeneralPageRoleMapping grm ->
                    urlSet.add(grm.pageName)
                }

                urlSet.each { String pageName ->
                    def pageRoleMappingList = new ArrayList<GeneralPageRoleMapping>()
                    list.each { GeneralPageRoleMapping pageRoleMapping ->
                        if (pageRoleMapping.pageName == pageName) {
                            pageRoleMappingList << pageRoleMapping
                        }
                    }
                    generalPageRoleMapping.put(pageName, pageRoleMappingList)
                }
            }
        } catch (e) {
            logger.error("Exception in get list of GeneralPageRoleMapping", e)
        } finally {
            session.close()
        }
        generalPageRoleMapping
    }

    /**
     * Method is used to get the applicationId by applicationName.
     * @param session Classic Hibernate session.
     * @return List    List for application id's.
     */
    private String getAppIdByAppName(Session session) {
        final def APP_NAME = Holders.grailsApplication.metadata['app.name']
        String appId
        try {
            if (!sessionFactory) {
                appId = session.createQuery('''SELECT capp.appId FROM ConfigApplication capp
                                                         WHERE capp.appName = :appName''').setString('appName', APP_NAME).uniqueResult()
            } else {
                appId = ConfigApplication.fetchByAppName(APP_NAME)?.appId
            }
        } catch (e) {
            logger.error("Exception in get Application Id", e)
        }
        appId
    }

}
