package net.hedtech.banner.menu.jobs

import grails.transaction.Transactional
import groovy.sql.Sql

@Transactional
class JobsMenuService {

    def sessionFactory

    def getPlatCodeJavaFormsUrl(){

        def sql = new Sql( sessionFactory.getCurrentSession().connection() )

        def row = sql.firstRow("""select gubmodu_url
                from gubmodu, gubpage where gubpage_gubmodu_code  = gubmodu_code AND  gubpage_code = ?
                AND gubmodu_plat_code = 'ADMJF'
                """,['GJAPCTL'])

        return row?.gubmodu_url

    }
}
