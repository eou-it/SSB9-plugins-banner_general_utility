/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import net.hedtech.banner.security.XssSanitizer

class KeepAliveController {

    static defaultAction = "data"

    def data = {
        
        String callback = XssSanitizer.sanitize(params.callback)

        if( callback ) {
            render text: "$callback && $callback({'result':'I am Alive'});", contentType: "text/javascript"
        } else {
            render "I am Alive"
        }
    }
}
