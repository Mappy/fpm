package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.joining;

public class TimeDomainsParser {

    private static final Pattern PATTERN = Pattern.compile("\\[\\(h(\\d{1,2})\\)\\{h(\\d{1,2})\\}\\]"); // [(h11){h7}]

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {

        return tomtomTimesDomains.stream().map(this::parse).collect(joining(", "));
    }

    private String parse(TimeDomains next) {
        String domain = next.getDomain();

        String begin_hour = null;
        String duration_hour = null;
        Matcher matcher = PATTERN.matcher(domain);
        if (matcher.find()) {
            begin_hour = matcher.group(1);
            duration_hour = matcher.group(2);
        }

        return String.format("%02d:00-%02d:00 off", valueOf(begin_hour), (valueOf(begin_hour) + valueOf(duration_hour)) % 24);
    }
}
