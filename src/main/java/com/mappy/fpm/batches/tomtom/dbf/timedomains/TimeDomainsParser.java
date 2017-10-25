package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.joining;

@Slf4j
public class TimeDomainsParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("\\[\\((\\p{Alpha})(\\d{1,2})\\)\\{(\\p{Alpha})(\\d{1,2})\\}\\]");

    private enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {
        return tomtomTimesDomains.stream().map(this::parse).collect(joining(", "));
    }

    private String parse(TimeDomains timeDomains) {

        Matcher durationMatcher = DURATION_PATTERN.matcher(timeDomains.getDomain());

        if (durationMatcher.find()) {
            return getOpeningHoursFromDuration(durationMatcher);

        } else {
            log.warn("Unable to parse '{}'", timeDomains.getDomain());
            return "";
        }
    }

    private String getOpeningHoursFromDuration(Matcher durationMatcher) {
        String beginMode = durationMatcher.group(1);
        int beginIndex = valueOf(durationMatcher.group(2));
        String durationMode = durationMatcher.group(3);
        int durationIndex = valueOf(durationMatcher.group(4));

        if("h".equals(beginMode) && "h".equals(durationMode)) {
            return String.format("%02d:00-%02d:00 off", beginIndex, (beginIndex + durationIndex) % 24);

        } else if("M".equals(beginMode) && "M".equals(durationMode)) {
            return String.format("%s-%s off", Month.values()[beginIndex - 1], Month.values()[(beginIndex + durationIndex - 2) % 12]);
        }

        log.warn("Unable to parse duration {}", durationMatcher.group(0));
        return "";
    }
}
