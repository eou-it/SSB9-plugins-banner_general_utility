/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.web.access.intercept.InterceptUrlMapFilterInvocationDefinition
import grails.transaction.Transactional
import grails.util.Holders
import org.hibernate.classic.Session
import org.springframework.http.HttpMethod

/**
 * The service class extends the InterceptUrlMapFilterInvocationDefinition to override the required methods to do the
 * CRUD operation for interceptUrlMap which is set in the Config.groovy.
 * If the grails.plugin.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap in the Cofig.groovy then this
 * service will get injected by the spring from BannerGeneralUtilityGrailsPlugin.groovy.
 */

class GeneralPageRoleMappingService extends InterceptUrlMapFilterInvocationDefinition {

    /**
     * Grails Application name from the Grails context holder.
     */
    final private static def APP_NAME = Holders.grailsApplication.metadata['app.name']

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
            return;
        }

        try {
            reset();
            initialized = true;
        }
        catch (RuntimeException e) {
            log.warn("Exception initializing; this is ok if it's at startup and due " +
                    "to GORM not being initialized yet since the first web request will " +
                    "re-initialize. Error message is: {}", e.getMessage());
        }
    }

    /**
     * Overriden method will get all the IntercetedUrlMap from the Config.groovy and DB.
     */
    @Override
    public synchronized void reset() {
        resetConfigs();

        List<InterceptedUrl> data = pageRoleMappingListFromDBAndConfig()

        for (InterceptedUrl iu : data) {
            compileAndStoreMapping(iu)
        }

        if (log.isTraceEnabled()) {
            log.trace("configs: {}", getConfigAttributeMap());
        }
    }

    /**
     * The service method to get the intercept url from DB and Config.groovy to assign the values to compiled map.
     */
    private List<InterceptedUrl> pageRoleMappingListFromDBAndConfig() {
        List<InterceptedUrl> data = new ArrayList<InterceptedUrl>()
        def map = list()
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
        LinkedHashMap<String, ArrayList<String>> interceptedUrlMapFromConfig = ReflectionUtils.getConfigProperty("interceptUrlMap")
        LinkedHashMap<String, List<String>> mergedData = new LinkedHashMap<String, List<String>>()

        mergedData.putAll(interceptedUrlMapFromDB);

        // Merge the InterceptedUrlMap from DB and Config.groovy
        for (String key : interceptedUrlMapFromConfig?.keySet()) {
            List<String> list2 = interceptedUrlMapFromConfig?.get(key);
            List<String> list3 = mergedData?.get(key);
            if (list3 != null) {
                list3.addAll(list2);
                list3 = list3.unique {x, y -> x <=> y}
            } else {
                list2 = list2.unique {x, y -> x <=> y}
                mergedData.put(key, list2);
            }
        }

        // Prepare List of interceptedUrlMap from the Merged data.
        mergedData.each { k, v ->
            HttpMethod method = null
            data.add(new InterceptedUrl(k, super.split(v?.join(',')), method))
        }
        data
    }

    /**
     * Method is used to prepare the values.
     * @param role TWTVROLE_CODE value from database.
     * @return String prepared role as a string.
     */
    private static def pushValuesToPrepareSelfServiceRoles(role) {
        if (role != 'IS_AUTHENTICATED_ANONYMOUSLY') {
            return "ROLE_${"SELFSERVICE-$role".toUpperCase()}_${"BAN_DEFAULT_M".toUpperCase()}".toString()
        }
        return role
    }

    /**
     * This private method will be called when this filter is invoked before grails config,
     */
    @Transactional(readOnly = true)
    private def list() {
        Session session = getHibernateSession()
        fetchGeneralPageRoleMappingByAppId(session)
    }

    /**
     * Method is used to get the Hibernate session from the main context by the help of datasource.
     * @return Session     Classic hibernate session.
     */
    private Session getHibernateSession() {
        def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
        def ctx = Holders.grailsApplication.mainContext
        def hibernateSessionFactory = (!sessionFactory ? ctx.sessionFactory : sessionFactory)
        Session session = hibernateSessionFactory.openSession(dataSource.getSsbConnection())
        session
    }

    /**
     * This method will be called from the GeneralPageRoleMappingController.
     * @return list
     */
    @Transactional(readOnly = true)
    public def getList() {
        fetchGeneralPageRoleMappingByAppId(sessionFactory.getCurrentSession())
    }

    /**
     * The private method and this common method will be called with Hibernate Session passed as a param.
     * @param session
     */
    private LinkedHashMap fetchGeneralPageRoleMappingByAppId(Session session) {
        def list
        def generalPageRoleMapping = new LinkedHashMap<String, ArrayList<GeneralPageRoleMapping>>()
        try {
            List appList = getAppIdByAppName(session)
            if (appList) {
                def appId = appList.get(0)
                list = session.createQuery('''SELECT new GeneralPageRoleMapping(generalPageRoleMapping.pageName,
                                                        generalPageRoleMapping.roleCode,
                                                        generalPageRoleMapping.applicationName,
                                                        generalPageRoleMapping.displaySequence,
                                                        generalPageRoleMapping.pageId,
                                                        generalPageRoleMapping.applicationId,
                                                        generalPageRoleMapping.version)
                                                from GeneralPageRoleMapping generalPageRoleMapping
                                                WHERE generalPageRoleMapping.applicationId = :appId''')
                        .setParameter('appId', appId).list()
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
            log.warn("Exception in get list of GeneralPageRoleMapping", e.getMessage());
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
    private List getAppIdByAppName(Session session) {
        def appList = session.createQuery('''SELECT capp.appId FROM ConfigApplication capp
                                                        WHERE capp.appName = :appName''').setString('appName', APP_NAME).list()
        appList
    }

    /**
     * Save the GeneralPageRoleMapping.
     * @param generalPageRoleMapping
     * @return savedGeneralPageRoleMapping
     * TODO - Still this method is dummy and need to implement.
     */
    @Transactional
    public def save(def generalPageRoleMapping) {
        // Save the GeneralPageRoleMapping and return the saved obj.
        def saved
        saved
    }

    /**
     * Update the GeneralPageRoleMapping map.
     * TODO - Still this method is dummy and need to implement.
     * @param generalPageRoleMapping
     * @return updatedGeneralPageRoleMapping
     */
    @Transactional
    public def update(def generalPageRoleMapping) {
        // Update the GeneralPageRoleMapping and return the updated obj.
        def updated
        updated
    }

    /**
     * Delete the GeneralPageRoleMapping map.
     * TODO - Still this method is dummy and need to implement.
     * @param generalPageRoleMapping
     */
    @Transactional
    public void delete(def generalPageRoleMapping) {
        // Delete the GeneralPageRoleMapping.
    }
}
