package net.hedtech.banner.menu.workflow

import grails.transaction.Transactional
import groovy.sql.Sql
import net.hedtech.banner.menu.Menu

@Transactional
class WorkflowMenuService {

    def sessionFactory

    def processWorkflowRequest(def clientId){

        def sql = new Sql( sessionFactory.getCurrentSession().connection() )

        def formName
        def dataMap = []

        sql.call '{call WFIKWIBC.get_workitem_queue_read_only(?, ?, ?, ?)}', [clientId, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR], { a,b,c ->
            formName = b
        }

        def row = sql.firstRow("""select gubmodu_url
                from gubmodu, gubpage where gubpage_gubmodu_code  = gubmodu_code AND  gubpage_code = ?
                AND gubmodu_plat_code = 'ADMJF'
                """,[formName])

        //select only one row
        sql.call("{call gb_common.p_set_context( ?, ?, ?, ?)}", ['GUKMENU', 'OBJ_SECURITY', 'OFF', 'N'])

        sql.eachRow("""select gubobjs_name, gubobjs_ui_version
                       gubpage_name,
                       gubmodu_url, gubmodu_plat_code
                       from gubmodu, gubpage,gubobjs
                       where gubobjs_name  = gubpage_code (+)
                       AND  gubpage_gubmodu_code  = gubmodu_code (+)
                       and gubobjs_name = ? and rownum = 1""", [formName]) {

            def mnu = new Menu()
            if (row?.gubmodu_url) {
                mnu.formName = it.gubobjs_name
            } else {
                mnu.formName = 'GUAINIT'
            }
            mnu.formName = it.gubobjs_name
            mnu.name = it.gubobjs_name
            if (row?.gubmodu_url) {
                mnu.page = it.gubobjs_name
            } else {
                mnu.page = 'GUAINIT'
            }

            mnu.type = 'FORM'

            mnu.url = row?.gubmodu_url ? row?.gubmodu_url : it.gubmodu_url
            if (row?.gubmodu_url) {
                mnu.uiVersion = "bannerXEadmin"
            } else {
                mnu.uiVersion = "banner8admin"
            }
            mnu.platCode = it.gubmodu_plat_code

            dataMap.add(mnu)

        }

        log.debug( "Workflow Load executed" )

        sql.call("{call gb_common.p_set_context( ?, ?, ?, ?)}", ['GUKMENU', 'OBJ_SECURITY', 'ON', 'N'])

        return dataMap
    }


    def isPlatCodeJavaForms(def clientId){

        def sql = new Sql( sessionFactory.getCurrentSession().connection() )

        def formName

        sql.call '{call WFIKWIBC.get_workitem_queue_read_only(?, ?, ?, ?)}', [clientId, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR], { a,b,c ->
            formName = b
        }

        def row = sql.firstRow("""select gubmodu_url
                from gubmodu, gubpage where gubpage_gubmodu_code  = gubmodu_code AND  gubpage_code = ?
                AND gubmodu_plat_code = 'ADMJF'
                """,[formName])

        return row?.gubmodu_url?true:false

    }
}
