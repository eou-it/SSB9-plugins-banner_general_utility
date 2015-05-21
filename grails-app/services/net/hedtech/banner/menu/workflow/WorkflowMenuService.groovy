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
        sql.eachRow("select distinct gutmenu_value,gutmenu_level,gutmenu_seq_no,gubobjs_ui_version,gutmenu_prior_obj,gutmenu_objt_code,gutmenu_desc,gubpage_name, gubmodu_url, gubmodu_plat_code  from gutmenu,gubmodu, gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND  gubobjs_name = gutmenu_value and gubpage_gubmodu_code  = gubmodu_code (+) AND  gutmenu_value = ? and rownum = 1 order by gutmenu_objt_code, gutmenu_value",[formName] ) {
            def mnu = new Menu()
            if (row?.gubmodu_url) {
                mnu.formName = it.gutmenu_value
            }else{
                mnu.formName = 'GUAINIT'
            }
            //mnu.formName = it.gutmenu_value
            mnu.name = it.gutmenu_value
            if (row?.gubmodu_url) {
                mnu.page = ((it.gubobjs_ui_version == "B") || (it.gubobjs_ui_version == "A")) ? it.gutmenu_value : it.gubpage_name
            }else{
                mnu.page = 'GUAINIT'
            }
            mnu.menu = null
            mnu.level = it.gutmenu_level
            mnu.seq = it.gutmenu_seq_no
            mnu.type = it.gutmenu_objt_code
            mnu.parent = it.gutmenu_prior_obj
            mnu.url = row?.gubmodu_url?row?.gubmodu_url:it.gubmodu_url
            if (row?.gubmodu_url) {
                mnu.uiVersion = "bannerXEadmin"
            }else {
                mnu.uiVersion = "banner8admin"
            }
            mnu.platCode = it.gubmodu_plat_code

            dataMap.add( mnu )
        }
        log.debug( "Workflow Load executed" )

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
