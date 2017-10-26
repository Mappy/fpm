package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.joining;

@Slf4j
public class TimeDomainsParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("\\[\\((.*)\\)\\{(.*)\\}\\]");
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("\\[\\((.*)\\)\\((.*)\\)\\]");

    private enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {
        return tomtomTimesDomains.stream().map(this::parse).collect(joining(", "));
    }

    private String parse(TimeDomains timeDomains) {

        Matcher durationMatcher = DURATION_PATTERN.matcher(timeDomains.getDomain());
        Matcher intervalMatcher = INTERVAL_PATTERN.matcher(timeDomains.getDomain());

        if (durationMatcher.find()) {
            return getOpeningHoursFromDuration(durationMatcher);

        } else if (intervalMatcher.find()) {
            return getOpeningHoursFromInterval(intervalMatcher);

        } else {
            log.warn("Unable to parse '{}'", timeDomains.getDomain());
            return "";
        }
    }

    private String getOpeningHoursFromDuration(Matcher matcher) {

        Element begin;
        Element duration;
        try {
            begin = parse(matcher.group(1));
            duration = parse(matcher.group(2));
        } catch (NumberFormatException mfe) {
            log.warn("Unable to parse duration {}", matcher.group(0));
            return "";
        }

        if ("h".equals(begin.getMode()) && "h".equals(duration.getMode())) {
            return String.format("%02d:00-%02d:00 off", begin.getIndex(), (begin.getIndex() + duration.getIndex()) % 24);

        } else if ("M".equals(begin.getMode()) && "M".equals(duration.getMode())) {
            return String.format("%s-%s off", Month.values()[begin.getIndex() - 1], Month.values()[(begin.getIndex() + duration.getIndex() - 2) % 12]);
        }

        log.warn("Unable to parse duration {}", matcher.group(0));
        return "";
    }

    private String getOpeningHoursFromInterval(Matcher matcher) {

        Element begin;
        Element end;
        try {
            begin = parse(matcher.group(1));
            end = parse(matcher.group(2));
        } catch (NumberFormatException mfe) {
            log.warn("Unable to parse interval {}", matcher.group(0));
            return "";
        }

        if ("M".equals(begin.getMode()) && "M".equals(end.getMode())) {
            return String.format("%s-%s off", Month.values()[begin.getIndex() - 1], Month.values()[end.getIndex() - 1]);
        }

        log.warn("Unable to parse interval {}", matcher.group(0));
        return "";
    }

    private Element parse(String group) {
        return new Element(group.substring(0, 1), valueOf(group.substring(1, group.length())));
    }

    @Data
    private static class Element {
        private final String mode;
        private final int index;
    }
}
