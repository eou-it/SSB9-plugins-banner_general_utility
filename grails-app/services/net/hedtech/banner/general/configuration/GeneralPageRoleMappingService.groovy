/*******************************************************************************
 Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.configuration

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.InterceptedUrl
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.web.access.intercept.RequestmapFilterInvocationDefinition
import grails.util.Holders
import org.apache.commons.lang.WordUtils
import org.hibernate.Session
import org.hibernate.SessionBuilder
import org.springframework.http.HttpMethod
import org.springframework.util.StringUtils

/**
 * The service class extends the InterceptUrlMapFilterInvocationDefinition to override the required methods to do the
 * CRUD operation for interceptUrlMap which is set in the Config.groovy.
 * If the grails.plugin.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap in the Cofig.groovy then this
 * service will get injected by the spring from BannerGeneralUtilityGrailsPlugin.groovy.
 */
@Transactional
class GeneralPageRoleMappingService extends RequestmapFilterInvocationDefinition {
    def sessionFactory

    private static List originalInterceptUrlMap

    private String wildcardKey = '/**'
    private static final String INTERCEPT_URLMAP_PATTERN = 'pattern'
    private static final String INTERCEPT_URLMAP_ACCESS = 'access'

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
            log.error("Exception initializing; Error message is: { " + e.getMessage() + " }", e)
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

        log.debug("configs: $data")
    }

    /**
     * The service method to get the intercept url from DB and Config.groovy to assign the values to compiled map.
     */
    protected List<InterceptedUrl> pageRoleMappingListFromDBAndConfig() {
        List<InterceptedUrl> data = new ArrayList<InterceptedUrl>()
        LinkedHashSet interceptedUrlMapFromDB = new LinkedHashSet<>()
        interceptedUrlMapFromDB = getPageRoleMappingList()
        LinkedHashSet interceptedUrlMapFromConfig

        if (!isDataIsSeededForInterceptUrlMap) {
            if (originalInterceptUrlMap == null) {
                originalInterceptUrlMap = ReflectionUtils.getConfigProperty("interceptUrlMap").clone()
            }
            interceptedUrlMapFromConfig = originalInterceptUrlMap.clone()

            interceptedUrlMapFromConfig.addAll(interceptedUrlMapFromDB)
            //List OF MAP
        } else {
            interceptedUrlMapFromConfig = interceptedUrlMapFromDB.clone()
        }

        if (interceptedUrlMapFromConfig.pattern.contains(wildcardKey)) {
            def wildcardValue
            interceptedUrlMapFromConfig.find{ url ->
                if(url.pattern == wildcardKey){
                    wildcardValue =  url.access
                }
            }
            HashMap wildCardMap = new HashMap()
            wildCardMap.put(INTERCEPT_URLMAP_PATTERN,wildcardKey)
            wildCardMap.put(INTERCEPT_URLMAP_ACCESS,wildcardValue)
            interceptedUrlMapFromConfig.remove(wildCardMap)
            interceptedUrlMapFromConfig << wildCardMap
        }

        // Prepare List of interceptedUrlMap from the Merged data.
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.clear()

        interceptedUrlMapFromConfig.each { interceptMapping ->
            HttpMethod method = null
            if(StringUtils.hasText(interceptMapping.pattern) && interceptMapping.access?.size() > 0) {
                InterceptedUrl iu = new InterceptedUrl(interceptMapping.pattern, interceptMapping.access,method)
                Holders.config.grails.plugin.springsecurity.interceptUrlMap?.add(iu)
                data.add(iu)
            }else {
                log.error("Key is =${interceptMapping?.pattern} and Value is =${interceptMapping?.access} in invalid for interceptUrlMap.")
            }
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
            SessionBuilder sb = hibernateSessionFactory.withOptions()
            Boolean ssbEnabled= Holders?.config?.ssbEnabled instanceof Boolean ? Holders?.config?.ssbEnabled : false
            if(ssbEnabled) {
                session = sb.connection(dataSource.getSsbConnection()).openSession()
            }
            else{
                session = sb.connection(dataSource.getConnection()).openSession()
            }
        } catch (e) {
            log.error('Exception creating Hibernate session;', e)
        }
        session
    }

    /**
     * This method will be get the list of all intercepted url map from DB and Config.groovy.
     * @return list
     */
    @Transactional(readOnly = true)
    public LinkedHashSet getPageRoleMappingList() {
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
    private LinkedHashSet fetchGeneralPageRoleMappingByAppId() {
        def list
        Session session
        def generalPageRoleMapping = new LinkedHashMap<String, ArrayList<GeneralPageRoleMapping>>()
        LinkedHashSet<Map<String, ?>> interceptUrlMapFromDB = new LinkedHashSet<Map<String, ?>>()
        try {
            String appId = Holders.config.app.appId
            if (appId) {
                if (!sessionFactory) {
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
                        log.error('Exception while executing the query with new Hibernate session;')
                    }
                    finally {
                      // session?.close()
                    }
                } else {
                    list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(appId, true)
                }
                interceptUrlMapFromDB= prepareMap(list)
            }
        } catch (e) {
            log.error("Exception in get list of GeneralPageRoleMapping", e)
        }
        interceptUrlMapFromDB
    }

    /**
     *  Return the intercept URL Hashset having pattern as key (page url) and access as key (list of roles)
     * @param list
     * @return Set
     */
    private LinkedHashSet prepareMap(List list) {
        def pageRoleInterceptURLList = new LinkedHashSet<GeneralPageRoleMapping>()
        list.each { GeneralPageRoleMapping grm ->
            HashMap urlSetMap = new HashMap()
            List pageRoleMappingList = new ArrayList()
            pageRoleMappingList = getRolesList(list,grm.pageUrl)
            urlSetMap.put(INTERCEPT_URLMAP_ACCESS ,pageRoleMappingList)
            urlSetMap.put(INTERCEPT_URLMAP_PATTERN, grm.pageUrl)
            pageRoleInterceptURLList.add(urlSetMap)
        }
        pageRoleInterceptURLList
    }

    /**
     * To Create the List of Roles associated for each pageURL
     * @param roleList
     * @param pageUrl
     * @return
     */
    private List getRolesList(List roleList,String pageUrl){
        List<String> roleCodeList = new ArrayList<String>()
        roleList.each { pageRoleMapping ->
            if (pageRoleMapping.pageUrl == pageUrl) {
                roleCodeList << pageRoleMapping.roleCode
            }
        }
        return roleCodeList
    }

    /**
     * Method is used to seed the data when server starts,
     * The data will be seeded in to three tables
     * <p><ul>
     * <li> GUBAPPL - If application is starting for 1st time (This call will be before
     *              ConfigPropertiesService.seedDataToDBFromConfig()
     * <li> GURCTLEP - Page id, app id and url will be seeded.
     * <li> GURAPPR - Roles for the page id of GURCTLEP will be seeded.
     * </ul><p>
     */
    public void seedInterceptUrlMapAtServerStartup() {
        try {
            String appName = Holders.config.app.name
            String appId = Holders.config.app.appId

            ConfigApplication application = ConfigApplication.fetchByAppId(appId)
            if (!application) {
                ConfigApplication newConfigApp = new ConfigApplication()
                newConfigApp.setAppId(appId)
                newConfigApp.setAppName(appName)
                newConfigApp.setLastModifiedBy('BANNER')
                application = newConfigApp.save(failOnError: true, flush: true)
            }

            List<Map<String, ?>> interceptUrlMap = ReflectionUtils.getConfigProperty("interceptUrlMap").clone()
            List list = GeneralPageRoleMapping.fetchByAppIdAndStatusIndicator(appId)
            LinkedHashSet<Map<String, ?>> interceptUrlMapFromDB = new LinkedHashSet<Map<String, ?>>()
            interceptUrlMapFromDB = prepareMap(list)

            //int pageIdMaxSizepageId
            int pageIdMaxSize = ConfigControllerEndpointPage.getConstrainedProperties().get('pageId').getMaxSize()
            pageIdMaxSize = (pageIdMaxSize ? pageIdMaxSize : 60)

            Long maxOfDisplaySequence = ConfigControllerEndpointPage.createCriteria()?.get {
                projections {
                    max 'displaySequence'
                }
            } as Long
            maxOfDisplaySequence = (maxOfDisplaySequence ? maxOfDisplaySequence : 0)
            interceptUrlMap.each { roles ->
                if (!interceptUrlMapFromDB?.pattern?.contains(roles?.pattern)) {
                    // Start
                    // Save GURCTLEP
                    ConfigControllerEndpointPage ccep = new ConfigControllerEndpointPage()
                    ccep.setLastModifiedBy('BANNER')
                    ccep.setLastModified(new Date())
                    ccep.setDisplaySequence(maxOfDisplaySequence + 1)
                    ccep.setPageUrl(roles.pattern)
                    ccep.setStatusIndicator(true)
                    ccep.setConfigApplication(application)

                    // Preparing the pageId
                    String appIdCamelCase = WordUtils.capitalizeFully(application.getAppId())
                    String pageId = ''
                    if (roles.pattern == '/' || roles.pattern == '/**') {
                        pageId = appIdCamelCase + ' ' + roles.pattern
                    } else {
                        pageId = getStringForPageId(roles.pattern, pageIdMaxSize)
                    }
                    ccep.setPageId(pageId)
                    ccep.save(failOnError: true, flush: true)

                    // Save GURAPPR - multile roles
                    roles.configAttributes.each { roleCode ->
                        ConfigRolePageMapping crpm = new ConfigRolePageMapping()
                        crpm.setConfigApplication(application)
                        crpm.setLastModifiedBy('BANNER')
                        crpm.setLastModified(new Date())
                        crpm.setEndpointPage(ccep)
                        crpm.setRoleCode(roleCode.toString())
                        crpm.save(failOnError: true, flush: true)
                    }
                    // End
                    maxOfDisplaySequence++
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage())
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
     * The method is to prepare the pageId (PK) for GURCTLEP table.
     * <p><ul>
     * <li> GURCTLEP_PAGE_URL and will return the url without the '/' string.
     *    Eg. url = '/ssb/themeEditor/**' then the prepared pageId = 'SsbThemeeditor'
     * <li> If the '**' will be removed if url has it at the end.
     *    eg: /ssb/home/**  ---> SsbHome
     *        /ssb/home/test**  ---> SsbHomeTest
     * <li> If the prepared page id is greater than the size of the column length
     *    then method will remove the page id string from the beginning.
     *    eg: /ssb/AuthenticationTesting/testingEndPoint1/testingEndPoint2/homePage
     *        the page id will be SsbAuthenticationTestingTestingEndPoint1TestingEndPoint2HomePage
     *        prepared page id for above string is - EndPoint1TestingEndPoint2HomePage
     * </ul><p>
     * @param url GURCTLEP_PAGE_URL value from GURCTLEP table.
     * @param pageIdMaxSize Size of the table column - GURCTLEP_PAGE_ID
     * @return pageId - @String type, prepared pageId from the pageUrl.
     */
    private static String getStringForPageId(String url, int pageIdMaxSize) {
        List<String> list = new ArrayList<String>(Arrays.asList(url.split("/")))
        list.removeAll(Arrays.asList(null, ""))

        int lastIndex = (list.size() - 1)

        if (list?.get(lastIndex) == '**') {
            list.remove(lastIndex)
        } else if (list?.get(lastIndex)?.contains('**')) {
            list?.set(lastIndex, list?.get(lastIndex)?.minus('**'))
        }

        String preparedPageId = ''
        boolean endLoop = false

        list.eachWithIndex { String str, int i ->
            if (!endLoop) {
                preparedPageId = preparedPageId + str.capitalize()
                int initIndex = 0
                if (preparedPageId?.size() > pageIdMaxSize) {
                    while (preparedPageId?.size() > pageIdMaxSize) {
                        log.warn("Prepared PageId to seed data for intercepturl is exceed the size(maxSize=${pageIdMaxSize}), " +
                                "the full length of prepared url is : " + preparedPageId)
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
