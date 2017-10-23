package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeDomainsParser {

    private static final Pattern HOUR_PATTERN = Pattern.compile("\\(h(.*?)\\)");

    public String parse(TimeDomains tomtomTimesDomains) {
        String domain = tomtomTimesDomains.getDomain(); // [(h11){h7}]

        String hour = null;
        Matcher matcher = HOUR_PATTERN.matcher(domain);
        if (matcher.find()) {
            hour = matcher.group(1);
        }

        String duration = null; // 7
        return hour + " off";
    }
}
