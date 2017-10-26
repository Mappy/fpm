package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
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

    private enum WeekDay {
        Su, Mo, Tu, We, Th, Fr, Sa
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {
        return tomtomTimesDomains.stream().map(this::parse).filter(Objects::nonNull).collect(joining(", "));
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
            throw new IllegalArgumentException("Unable to parse '" + timeDomains.getDomain() + "'");
        }
    }

    private String getOpeningHoursFromDuration(Matcher matcher) {

        Elements elements = parse(matcher);

        Element begin = elements.getFirst();
        Element duration = elements.getSecond();
        if ("h".equals(begin.getMode()) && "h".equals(duration.getMode())) {
            return String.format("%02d:00-%02d:00 off", begin.getIndex(), (begin.getIndex() + duration.getIndex()) % 24);

        } else if ("M".equals(begin.getMode()) && "M".equals(duration.getMode())) {
            return String.format("%s-%s off", Month.values()[begin.getIndex() - 1], Month.values()[(begin.getIndex() + duration.getIndex() - 2) % 12]);

        } else if ("t".equals(begin.getMode())) {
            return String.format("%s 00:00-%02d:00", WeekDay.values()[begin.getIndex() - 1], duration.getIndex());

        } else if ("z".equals(begin.getMode())) {
            return null;
        }

        log.warn("Unable to parse duration {}", matcher.group(0));
        throw new IllegalArgumentException("Unable to parse duration " + matcher.group(0));
    }

    private String getOpeningHoursFromInterval(Matcher matcher) {

        Elements elements = parse(matcher);

        Element begin = elements.getFirst();
        Element end = elements.getSecond();
        if ("h".equals(begin.getMode()) && "h".equals(end.getMode())) {
            return String.format("%02d:00-%02d:00 off", begin.getIndex(), end.getIndex());

        } else if ("M".equals(begin.getMode()) && "M".equals(end.getMode())) {
            return String.format("%s-%s off", Month.values()[begin.getIndex() - 1], Month.values()[end.getIndex() - 1]);
        }

        log.warn("Unable to parse interval {}", matcher.group(0));
        throw new IllegalArgumentException("Unable to parse interval " + matcher.group(0));
    }

    private Elements parse(Matcher matcher) {
        return new Elements(parse(matcher.group(1)), parse(matcher.group(2)));
    }

    private Element parse(String group) {
        try {
            return new Element(group.substring(0, 1), valueOf(group.substring(1, group.length())));

        } catch (NumberFormatException nfe) {
            return new Element("", 0);
        }
    }

    @Data
    private static class Element {
        private final String mode;
        private final int index;
    }

    @Data
    private static class Elements {
        private final Element first;
        private final Element second;
    }
}
