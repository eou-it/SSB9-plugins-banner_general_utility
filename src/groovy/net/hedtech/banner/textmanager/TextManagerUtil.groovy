/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import org.apache.log4j.Logger

class TextManagerUtil {
    public static dbValues = [:]

    private static final def log = Logger.getLogger(TextManagerUtil.class.name)

    private void logError(String msg) {
        String message = msg + "\n" +
                "Arguments: mo=<mode> ba=<batch> lo=<db logon> pc=<TranMan Project> sl=<source language>" +
                " tl=<target language>  sf=<source file> tf=<target file>\n" +
                "  mode: s (extract) | r (reverse extract) | t (translate) | q (quick translate - no check)\n" +
                "  batch: [y|n]. n (No) is default. If y (Yes), the module record will be updated with file locations etc."
        log.error(message)
    }

    public void parseArgs(String[] args) {
        //loop through the arguments and parse key=value pairs
        log.debug("Arguments:")
        args.each{ item ->
            int pos = item.indexOf("=")
            if (pos >= 0) {
                String key = item.substring(0, pos)
                String val = item.substring(pos + 1)
                dbValues.put(key, val)
                log.debug(key + "=" + val)
            }
        }
        if (dbValues.srcIndicator == null) {
            dbValues << [srcIndicator:"s"]
        } else if (dbValues.srcIndicator.equals("t")) {
            if (dbValues.tgtFile == null) {
                logError("No target file specified (tgtFile=...)")
            }
            if (dbValues.tgtLocale == null) {
                logError("No target language specified (tgtLocale=...)")
            }
        } else if (dbValues.srcIndicator.equals("r")) {
            if (dbValues.tgtLocale == null) {
                logError("No target language specified (tgtLocale=...)")
            }
        }
    }

    static String smartQuotesReplace(String s) {
        StringBuffer res = new StringBuffer()
        char c
        s.eachWithIndex{ item, index ->
            c = item
            if (c == '\'') {
                // look ahead
                if (index + 1 < s.length() && s[index + 1] == '\'') {
                    res.append(c)
                } else {
                    res.append("\u2019")
                }
            } else {
                res.append(c)
            }
        }
        return res.toString()
    }
}
