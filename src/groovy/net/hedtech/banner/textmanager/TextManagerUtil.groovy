/******************************************************************************
 *  Copyright 2017 Ellucian Company L.P. and its affiliates.                  *
 ******************************************************************************/
package net.hedtech.banner.textmanager

import org.apache.log4j.Logger

class TextManagerUtil {
    private def dbValues = [:]
    public static final String logon = "lo"
    public static final String pc = "pc"
    public static final String sl = "sl"
    public static final String tl = "tl"
    public static final String mo = "mo"
    public static final String ba = "ba"
    public static final String sourceFile = "sf"
    public static final String moduleName = "mn"
    public static final String targetFile = "tf"

    private static final def log = Logger.getLogger(TextManagerUtil.class)

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
                String key = item.substring(0, pos).toLowerCase()
                String val = item.substring(pos + 1)
                dbValues[key] = val
                log.debug(key + "=" + val)
            }
        }
        if (dbValues[logon] == null) {
            logError("No log on specified(lo=user/passwd@connect [tns or jdbc connection])")
        }
        if (dbValues[sourceFile] == null) {
            logError("No source file specified (sf=...)")
        }
        if (dbValues[mo] == null) {
            dbValues[mo]= "s"
        } else if (dbValues[mo].equals("t")) {
            if (dbValues[targetFile] == null) {
                logError("No target file specified (tf=...)")
            }
            if (dbValues[tl] == null) {
                logError("No target language specified (tl=...)")
            }
        } else if (dbValues[mo].equals("r")) {
            if (dbValues[tl] == null) {
                logError("No target language specified (tl=...)")
            }
        }
    }

    def getValue(key) {
        dbValues[key]
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
