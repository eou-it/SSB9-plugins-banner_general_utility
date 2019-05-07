package net.hedtech.banner.security

import java.util.regex.Pattern

class XssSanitizer {

    public static Pattern XSS_PATTERN = Pattern.compile("((\\%3C)|<)[^\\n]+((\\%3E)|>)", Pattern.CASE_INSENSITIVE)

    public static String sanitize(def input) {

        if (input != null && input instanceof String) {

            // remove known XSS input patterns
            input = XSS_PATTERN.matcher(input).replaceAll("");
        }

        return input;
    }
}
