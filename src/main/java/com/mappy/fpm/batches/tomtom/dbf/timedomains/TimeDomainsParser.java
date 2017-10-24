package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.joining;

@Slf4j
public class TimeDomainsParser {

    private static final Pattern HOUR_PATTERN = Pattern.compile("\\[\\(h(\\d{1,2})\\)\\{h(\\d{1,2})\\}\\]"); // [(h11){h7}]
    private static final Pattern MONTH_PATTERN = Pattern.compile("\\[\\(M(\\d{1,2})\\)\\{M(\\d{1,2})\\}\\]"); // [(M3){M5}]

    private enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {

        return tomtomTimesDomains.stream().map(this::parse).collect(joining(", "));
    }

    private String parse(TimeDomains timeDomains) {

        Matcher hourMatcher = HOUR_PATTERN.matcher(timeDomains.getDomain());
        Matcher monthMatcher = MONTH_PATTERN.matcher(timeDomains.getDomain());

        if (hourMatcher.find()) {
            String begin_hour = hourMatcher.group(1);
            String duration_hour = hourMatcher.group(2);
            return String.format("%02d:00-%02d:00 off", valueOf(begin_hour), (valueOf(begin_hour) + valueOf(duration_hour)) % 24);

        } else if (monthMatcher.find()) {
            String begin_month = monthMatcher.group(1);
            String duration_month = monthMatcher.group(2);
            return String.format("%s-%s off", Month.values()[valueOf(begin_month) - 1], Month.values()[(valueOf(begin_month) + valueOf(duration_month) - 2) % 12]);

        } else {
            log.warn("Unable to parse '{}'", timeDomains.getDomain());
            return "";
        }
    }
}
