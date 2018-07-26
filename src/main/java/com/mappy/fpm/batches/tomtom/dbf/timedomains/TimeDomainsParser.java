package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Slf4j
public class TimeDomainsParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\[\\((.*)\\)\\{(.*)\\}\\]");
    private static final Pattern COMPOUND_DURATION_PATTERN = Pattern.compile("^\\[\\[\\((.*)\\)\\{(.*)\\}\\]\\*\\[\\((.*)\\)\\{(.*)\\}\\]\\]");
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("^\\[\\((.*)\\)\\((.*)\\)\\]");

    private static final Pattern MOTIF_PATTERN = Pattern.compile("(\\p{Alpha}\\d{1,2})");

    private enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    }

    private enum WeekDay {
        Su, Mo, Tu, We, Th, Fr, Sa
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {
        String osmOpeningHours = tomtomTimesDomains.stream().map(this::parse).filter(Objects::nonNull).collect(joining(", "));

        if (!"".equals(osmOpeningHours)) {
            osmOpeningHours = "1970-9999; " + osmOpeningHours;
        }

        return osmOpeningHours;
    }

    private String parse(TimeDomains timeDomains) {

        String domain = timeDomains.getDomain();
        if (domain.matches(".*[+zfl].*") || domain.split("\\*").length > 2) {
            return "";

        } else if(!domain.matches("[Mdhtm\\d{1,2}*\\[\\]\\(\\)\\{\\}]*")) {
            log.warn("Unable to parse unknown char '{}'", domain);
            throw new IllegalArgumentException("Unable to parse unknown char '" + domain + "'");
        }

        Matcher durationMatcher = DURATION_PATTERN.matcher(domain);
        Matcher intervalMatcher = INTERVAL_PATTERN.matcher(domain);
        Matcher compoundDurationMatcher = COMPOUND_DURATION_PATTERN.matcher(domain);

        if (durationMatcher.find()) {
            return getOpeningHours(durationMatcher, true);

        } else if (intervalMatcher.find()) {
            return getOpeningHours(intervalMatcher, false);

        } else if (compoundDurationMatcher.find()) {
            return getOpeningHours(compoundDurationMatcher, true);

        } else {
            throw new IllegalArgumentException("Unable to parse '" + domain + "'");
        }
    }

    private String getOpeningHours(Matcher matcher, boolean isDuration) {
        String beginMatch = matcher.group(1);
        String secondMatch = matcher.group(2);

        if (matcher.groupCount() == 4) {
            beginMatch += matcher.group(3);
            secondMatch += matcher.group(4);
        }

        List<Element> begin = parse(beginMatch);
        List<Element> second = parse(secondMatch);

        return generate(begin, second, isDuration);
    }

    private String generate(List<Element> begins, List<Element> seconds, boolean isDuration) {
        int beginMonth = getIndex(begins, "M");
        int secondMonth = getIndex(seconds, "M");

        String month = "";
        int sumMonth = beginMonth + secondMonth;
        if (sumMonth > 0) {
            sumMonth = sumMonth > 12 ? sumMonth % 12 : sumMonth;
            int endMonth = isDuration ? sumMonth : secondMonth;
            month = format("%s-%s", Month.values()[beginMonth - 1], Month.values()[endMonth - 1]);
        }

        String days = begins.stream().filter(e -> "t".equals(e.mode)).map(e -> WeekDay.values()[e.index - 1].name()).collect(joining(","));

        int beginHour = getIndex(begins, "h");
        int beginMinute = getIndex(begins, "m");

        int secondHour = getIndex(seconds, "h");
        int secondMinute = getIndex(seconds, "m");
        String hours = "";
        if (beginHour + beginMinute + secondHour + secondMinute > 0) {
            int endHour = isDuration ? (beginHour + secondHour) % 24 + (beginMinute + secondMinute >= 60 ? 1 : 0) : secondHour;
            int endMinute = isDuration ? (beginMinute + secondMinute) % 60 : secondMinute;
            hours = format("%02d:%02d-%02d:%02d", beginHour, beginMinute, endHour, endMinute);
        }

        return format("%s %s %s off", month, days, hours).trim().replaceAll("\\s+", " ");
    }

    private Integer getIndex(List<Element> elements, String mode) {
        return elements.stream().filter(e -> mode.equals(e.mode)).map(Element::getIndex).findFirst().orElse(0);
    }

    private List<Element> parse(String group) {
        List<Element> list = newArrayList();
        Matcher matcher = MOTIF_PATTERN.matcher(group);

        int index = 0;
        while (matcher.find(index)) {
            String firstBegin = matcher.group(1);
            Element element = new Element(firstBegin.substring(0, 1), valueOf(firstBegin.substring(1, firstBegin.length())));
            list.add(element);
            index += firstBegin.length();
        }

        return list;
    }

    @Data
    private static class Element {
        private final String mode;
        private final int index;
    }
}
