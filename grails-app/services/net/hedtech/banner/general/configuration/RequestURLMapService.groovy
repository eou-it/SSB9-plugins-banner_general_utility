/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.web.access.intercept.RequestmapFilterInvocationDefinition
import grails.transaction.Transactional
import grails.util.Holders
import org.hibernate.classic.Session
import org.springframework.http.HttpMethod
import org.springframework.security.core.context.SecurityContextHolder

/**
 * The service class extends the RequestmapFilterInvocationDefinition to override the required methods to do the
 * CRUD operation for Requestmap which is set in the Config.groovy.
 * If the grails.plugin.springsecurity.securityConfigType = SecurityConfigType.Requestmap in the Cofig.groovy then this
 * service will get injected by the spring from resources.groovy.
 */

class RequestURLMapService extends RequestmapFilterInvocationDefinition {

    def configApplicationService
    def configControllerEndpointPageService
    def configRolePageMappingService

    /**
     * Grails Application name from the Grails context holder.
     */
    final private static def APP_NAME = Holders.grailsApplication.metadata['app.name']

    /**
     * This will be injected by the spring only when making this service call from the RequestmapController.
     */
    def sessionFactory

    /**
     * Overriden because this method calls reset() method which is calling loadRequestmaps().
     */
    @Override
    protected void initialize() {
        if (initialized) {
            if (compiled.size() == 0) {
                reset()
            }
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
     * Overriden because this method calls loadRequestmaps() method which is having our custom implematation to
     * get the Request map from the database.
     */
    @Override
    public synchronized void reset() {
        resetConfigs();

        def list = loadRequestmaps()

        for (InterceptedUrl iu : list) {
            compileAndStoreMapping(iu);
        }

        if (log.isTraceEnabled()) {
            log.trace("configs: {}", getConfigAttributeMap());
        }
    }

    /**
     * Overriding this method to fetch the RequestURLMap from the database.
     * @return List < InterceptedUrl >  the list of InterceptedUrl data from the DB.
     */
    @Override
    protected List<InterceptedUrl> loadRequestmaps() {
        List<InterceptedUrl> data = getRequestURLMap()
        return data
    }

    /**
     * The service method to get the intercept url from DB and assigning the values to context.
     */
    private def getRequestURLMap() {
        List<InterceptedUrl> data = new ArrayList<InterceptedUrl>()
        def map = list()
        def dataMap = [:]
        map.each { String key, ArrayList<GeneralRequestMap> grmList ->
            def roleList = []
            def grmListFinal = []
            grmList.each { GeneralRequestMap grm ->
                //int count = 0
                grmListFinal << grm
                int displaySeq = grm.displaySequence
                def roleCode = (grm.roleCode == 'WEBUSER' ? 'IS_AUTHENTICATED_ANONYMOUSLY' : grm.roleCode)
//                if (displaySeq == 5 && count == 0) {
//                    GeneralRequestMap generalRequestMap = new GeneralRequestMap()
//                    generalRequestMap.roleCode = 'GUEST'
//                    generalRequestMap.pageName = key
//                    grmListFinal << generalRequestMap
//                    count++
//                }
                roleList << pushValuesToPrepareSelfServiceRoles(roleCode)
            }
            grmListFinal.each {
                HttpMethod method = null
                data.add(new InterceptedUrl(key, super.split(roleList?.join(',')), method))
            }
        }
        data
    }

    /**
     * Method is used to prepare the values.
     * @param role TWTVROLE_CODE value from database.
     * @return String prepared role as a string.
     */
    private def pushValuesToPrepareSelfServiceRoles(role) {
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
        getAllRequestMap(session)
    }

    /**
     * Method is used to get the Hibernate session from the main context by the help of datasource.
     * @return Session     Classic hibernate session.
     */
    private Session getHibernateSession() {
        def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
        def ctx = Holders.grailsApplication.mainContext
        def sessionFactory = ctx.sessionFactory
        Session session = sessionFactory.openSession(dataSource.getSsbConnection())
        session
    }

    /**
     * This method will be called from the RequestmapController.
     * @return list
     */
    @Transactional(readOnly = true)
    public def getList() {
        getAllRequestMap(sessionFactory.getCurrentSession())
    }

    /**
     * The private method and this common method will be called with Hibernate Session passed as a param.
     * @param session
     */
    private LinkedHashMap getAllRequestMap(Session session) {
        def list
        def requestMap = new LinkedHashMap<String, ArrayList<GeneralRequestMap>>()
        try {
            List appList = getAppIdByAppName(session)
            if (appList) {
                def appId = appList.get(0)
                list = session.createQuery('''SELECT new GeneralRequestMap(requestMap.pageName,
                                                        requestMap.roleCode,
                                                        requestMap.applicationName,
                                                        requestMap.displaySequence,
                                                        requestMap.pageId,
                                                        requestMap.applicationId,
                                                        requestMap.version)
                                                from GeneralRequestMap requestMap
                                                WHERE requestMap.applicationId = :appId''')
                        .setParameter('appId', appId).list()
                def urlSet = new LinkedHashSet<String>()
                list.each { GeneralRequestMap grm ->
                    urlSet.add(grm.pageName)
                }

                urlSet.each { String pageName ->
                    def patternList = new ArrayList<GeneralRequestMap>()
                    list.each { GeneralRequestMap generalRequestMap ->
                        if (generalRequestMap.pageName == pageName) {
                            patternList << generalRequestMap
                        }
                    }
                    requestMap.put(pageName, patternList)
                }
            }
        } catch (e) {
            log.warn("Exception in get list of Requestmap", e.getMessage());
        } finally {
            session.close()
        }
        requestMap
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
     * This method will get called from BootStrap.groovy to save the static url and mapped roles for it.
     * This will called only once at the time of server startup.
     */
    @Transactional
    public void saveMapFromConfig() {
        LinkedHashMap<String, List> map = Holders.config.selfService.staticRequestMap
        Session session = getHibernateSession()
        List appList = getAppIdByAppName(session)
        def appId = appList ? appList.get(0) : null
        def lastModifiedBy = SecurityContextHolder.context?.authentication?.principal?.username
        lastModifiedBy = (lastModifiedBy == null ? 'GRAILS' : lastModifiedBy)
        ConfigApplication configApplication = configApplicationService.get(appId)
        map.each { pageName, roleCodeList ->
            ConfigControllerEndpointPage controllerEndpointPage
            def findEndPointPageQuery = ConfigControllerEndpointPage.where {
                pageName == pageName
                configApplication == configApplication
                displaySequence == map.findIndexOf { it.key == pageName }
            }
            def existingEndpointPage = findEndPointPageQuery.find()
            if (!existingEndpointPage) {
                ConfigControllerEndpointPage endpointPage = new ConfigControllerEndpointPage(
                        pageName: pageName,
                        configApplication: configApplication,
                        displaySequence: map.findIndexOf { it.key == pageName },
                        lastModified: new Date(),
                        lastModifiedBy: lastModifiedBy
                )
                controllerEndpointPage = configControllerEndpointPageService.create(endpointPage)
                controllerEndpointPage = controllerEndpointPage.refresh()
            } else {
                controllerEndpointPage = existingEndpointPage.refresh()
            }
            roleCodeList.each { String roleCode ->
                def findRoleMappingQuery = ConfigRolePageMapping.where {
                    configApplication == configApplication
                    endpointPage == controllerEndpointPage
                    roleCode == (roleCode == 'IS_AUTHENTICATED_ANONYMOUSLY' ? 'WEBUSER' : roleCode)
                }
                def existingRolePageMapping = findRoleMappingQuery.find()
                if (!existingRolePageMapping) {
                    existingRolePageMapping = new ConfigRolePageMapping(
                            configApplication: configApplication,
                            endpointPage: controllerEndpointPage,
                            roleCode: (roleCode == 'IS_AUTHENTICATED_ANONYMOUSLY' ? 'WEBUSER' : roleCode),
                            lastModified: new Date(),
                            lastModifiedBy: lastModifiedBy
                    )
                    configRolePageMappingService.create(existingRolePageMapping)
                }
            }
        }
    }

    /**
     * Save the request map.
     * @param requestMap
     * @return savedRequestMap
     * TODO - Still this method is dummy and need to implement.
     */
    @Transactional
    public def save(def requestMap) {
        // Save the requestmap and return the saved obj.
        def saved
        saved
    }

    /**
     * Update the request map.
     * TODO - Still this method is dummy and need to implement.
     * @param requestMap
     * @return updatedRequestMap
     */
    @Transactional
    public def update(def requestMap) {
        // Update the requestmap and return the updated obj.
        def updated
        updated
    }

    /**
     * Delete the request map.
     * TODO - Still this method is dummy and need to implement.
     * @param requestMap
     */
    @Transactional
    public void delete(def requestMap) {
        // Delete the requestmap.
    }
}
