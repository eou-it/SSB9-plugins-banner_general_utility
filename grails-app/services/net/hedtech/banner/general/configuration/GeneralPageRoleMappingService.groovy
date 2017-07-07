/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.web.access.intercept.RequestmapFilterInvocationDefinition
import grails.transaction.Transactional
import grails.util.Holders
import org.apache.commons.lang3.text.WordUtils
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

    public static boolean isDataIsSeededForInterceptUrlMap = false

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

        if (!isDataIsSeededForInterceptUrlMap) {
            if (originalInterceptUrlMap == null) {
                originalInterceptUrlMap = ReflectionUtils.getConfigProperty("interceptUrlMap").clone()
            }
            interceptedUrlMapFromConfig = originalInterceptUrlMap.clone()

            interceptedUrlMapFromConfig.putAll(interceptedUrlMapFromDB)
        } else {
            interceptedUrlMapFromConfig = interceptedUrlMapFromDB.clone()
        }

        if (interceptedUrlMapFromConfig.get(wildcardKey)) {
            def wildcardValue = interceptedUrlMapFromConfig.get(wildcardKey)
            interceptedUrlMapFromConfig.remove(wildcardKey)
            interceptedUrlMapFromConfig << [(wildcardKey): wildcardValue]
        }

        // Prepare List of interceptedUrlMap from the Merged data.
        Holders.config.grails.plugin.springsecurity.interceptUrlMap = [:]
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
                                                      AND generalPageRoleMapping.statusIndicator = :statusIndicator''')
                                .setParameter('appId', appId)
                                .setParameter('statusIndicator', true)
                                .list()
                    }
                    catch (e) {
                        logger.error('Exception while executing the query with new Hibernate session;', e)
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

    private Map prepareMap(List list, Map generalPageRoleMapping) {
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
        generalPageRoleMapping
    }

    /**
     * Method is used to seed the data when server starts,
     * The data will be seeded in to three tables
     * 1) GUBAPPL - If application is starting for 1st time (This call will be before
     *              ConfigPropertiesService.seedDataToDBFromConfig()
     * 2) GURCTLEP
     * 3) GURAPPR
     */
    public void seedInterceptUrlMapAtServerStartup() {
        try {
            String appName = Holders.grailsApplication.metadata['app.name']
            String appId = Holders.grailsApplication.metadata['app.appId']

            ConfigApplication application = ConfigApplication.fetchByAppName(appName)
            if (!application) {
                ConfigApplication newConfigApp = new ConfigApplication()
                newConfigApp.setAppId(appId)
                newConfigApp.setAppName(appName)
                newConfigApp.setLastModifiedBy('BANNER')
                application = newConfigApp.save(failOnError: true, flush: true)
            }

            LinkedHashMap<String, ArrayList<String>> interceptUrlMap = ReflectionUtils.getConfigProperty("interceptUrlMap").clone()
            List list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(appId)
            Map<String, ArrayList<String>> interceptUrlMapFromDB = new HashMap<String, ArrayList<String>>()
            interceptUrlMapFromDB = prepareMap(list, interceptUrlMapFromDB)

            interceptUrlMap.each { String url, List<String> roles ->
                if (!interceptUrlMapFromDB.containsKey(url)) {
                    // Start
                    // Save GURCTLEP
                    ConfigControllerEndpointPage ccep = new ConfigControllerEndpointPage()
                    ccep.setLastModifiedBy('BANNER')
                    ccep.setLastModified(new Date())
                    ccep.setDisplaySequence(interceptUrlMap.findIndexOf { it.key == url })
                    ccep.setPageUrl(url)
                    ccep.setStatusIndicator(true)
                    ConfigApplication ca = new ConfigApplication()
                    ca.setId(application.getId())

                    ccep.setConfigApplication(ca)

                    // Preparing the pageId
                    String appIdCamelCase = WordUtils.capitalizeFully(application.getAppId())
                    String pageId = ''
                    if (url == '/' || url == '/**') {
                        pageId = appIdCamelCase + ' ' + url
                    } else {
                        pageId = getStringForPageId(url, application.getAppId())
                    }
                    ccep.setPageId(pageId)
                    ccep.save(failOnError: true, flush: true)

                    // Save GURAPPR - multile roles
                    roles.each { roleCode ->
                        ConfigRolePageMapping crpm = new ConfigRolePageMapping()
                        crpm.setConfigApplication(ca)
                        crpm.setLastModifiedBy('BANNER')
                        crpm.setLastModified(new Date())
                        crpm.setEndpointPage(ccep)
                        crpm.setRoleCode(roleCode)
                        crpm.save(failOnError: true, flush: true)
                    }
                    // End
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage())
        }

        isDataIsSeededForInterceptUrlMap = true
    }

    /**
     * Method is used to identify the duplicate pageId and appId
     *
     * @param pageId page id of String type.
     * @param appId application id of String typel.
     * @return return value is true or false type if boolean.
     */
    public boolean isDuplicatePageId(String pageId, String appId) {
        def ccep = ConfigControllerEndpointPage.findByPageIdAndConfigApplication(pageId, ConfigApplication.fetchByAppId(appId))
        return (ccep != null)
    }

    /**
     * The method is to prepare the pageId (PK) for GURCTLEP table, method will accept the
     * GURCTLEP_PAGE_URL and will return the end point with last two '/' char.
     * Eg. url = '/ssb/themeEditor/**' then the prepared pageId = 'Psa Themeeditor'
     * from the 2nd last '/' char if the same url exists for the same application then we will append the previous
     * page name Eg. 'Psa Ssb Themeeditor'.
     * @param url GURCTLEP_PAGE_URL value from GURCTLEP table.
     * @return pageId prepared pageId from the pageUrl.
     */
    private String getStringForPageId(String url) {
        List<String> list = new ArrayList<String>(Arrays.asList(url.split("/")))
        list.removeAll(Arrays.asList(null, ""))

        int lastIndex = (list.size() - 1)

        if (list?.get(lastIndex) == '**') {
            list.remove(lastIndex)
            lastIndex = (list.size() - 1)
        } else if (list?.get(lastIndex)?.contains('**')) {
            list?.set(lastIndex, list?.get(lastIndex)?.minus('**'))
        }

        String preparedPageId = ''
        boolean endLoop = false
        list.eachWithIndex { String str, int i ->
            if (!endLoop) {
                preparedPageId = preparedPageId + str.capitalize()
                int initIndex = 0
                if (preparedPageId?.size() > 60) {
                    while (preparedPageId?.size() > 60) {
                        logger.warn('Prepared PageId to seed data for intercepturl is exceed the size, ' +
                                'the full length of prepared url is : ' + preparedPageId)
                        preparedPageId = preparedPageId?.minus(list?.get(initIndex)?.capitalize())
                        initIndex++
                    }
                    endLoop = true
                }
            }
        }
        return preparedPageId
    }

}
