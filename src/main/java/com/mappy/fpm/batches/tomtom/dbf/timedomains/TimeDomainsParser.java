package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import spark.utils.StringUtils;
import sun.management.counter.StringCounter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Slf4j
public class TimeDomainsParser {

    private static final Pattern INTERVAL_PATTERN = Pattern.compile("^\\[\\((\\w+)\\)\\((\\w+)\\)\\]$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\[\\((\\w+)\\)\\{(\\w+)\\}\\]$");
    private static final Pattern COMPOUND_DURATION_PATTERN = Pattern.compile("^\\[\\[\\((\\w+)\\)\\{(\\w+)\\}\\]\\*\\[\\((\\w+)\\)\\{(\\w+)\\}\\]\\]$");

    // week:w, particular day:f/l, seconds:s, fuzzy:z, are not supported by now
    private static final Pattern MOTIF_PATTERN = Pattern.compile("(y\\d{4}|[M,d,t,h,m]\\d{1,2})");

    private enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    }

    private enum WeekDay {
        Su, Mo, Tu, We, Th, Fr, Sa
    }

    @Data
    private static class Element {
        private final String mode;
        private final int index;
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {
        return tomtomTimesDomains.stream().map(this::parse).filter(StringUtils::isNotEmpty).collect(joining(", "));
    }

    private String parse(TimeDomains timeDomains) {
        String domain = timeDomains.getDomain();

        // Check for known unsupported formats
        if (domain.matches(".*[+wzfl].*") || domain.split("\\*").length > 2) {
            return "";
        }

        Map<Pattern, Boolean> patterns = new HashMap<Pattern, Boolean>()
        {{
            put(INTERVAL_PATTERN, false);
            put(DURATION_PATTERN, true);
            put(COMPOUND_DURATION_PATTERN, true);
        }};

        for (Map.Entry<Pattern, Boolean> entry : patterns.entrySet()) {
            Matcher matcher = entry.getKey().matcher(domain);
            if (matcher.matches()) {
                return getOpeningHours(matcher, entry.getValue());
            }
        }

        throw new IllegalArgumentException("Unable to parse '" + domain + "'");
    }

    private String getOpeningHours(Matcher matcher, Boolean isDuration) {
        String firstMatch = matcher.group(1);
        String secondMatch = matcher.group(2);

        if (matcher.groupCount() == 4) {
            firstMatch += matcher.group(3);
            secondMatch += matcher.group(4);
        }

        List<Element> begin = parseDate(firstMatch);
        List<Element> second = parseDate(secondMatch);
        return generate(begin, second, isDuration);
    }

    private List<Element> parseDate(String group) {
        List<Element> list = newArrayList();
        Matcher matcher = MOTIF_PATTERN.matcher(group);

        int index = 0;
        while (matcher.find(index)) {
            String firstBegin = matcher.group(1);
            Element element = new Element(firstBegin.substring(0, 1), valueOf(firstBegin.substring(1, firstBegin.length())));
            list.add(element);
            index += firstBegin.length();
        }

        if (list.isEmpty()) {
            throw new IllegalArgumentException("Unsupported date syntax: " + group);
        }

        return list;
    }

    private String generate(List<Element> begins, List<Element> seconds, Boolean isDuration) {
        // Gather date information
        int beginYear = getIndex(begins, "y");
        int secondYear = getIndex(seconds, "y");
        int endYear = isDuration ? beginYear + secondYear : secondYear;

        int beginMonth = getIndex(begins, "M");
        int secondMonth = getIndex(seconds, "M");
        int sumMonth = beginMonth + secondMonth;
        sumMonth = sumMonth > 12 ? sumMonth % 12 : sumMonth;
        int endMonth = isDuration ? sumMonth : secondMonth;

        int beginDay = getIndex(begins, "d");
        int endDay = getIndex(seconds, "d");

        String days = begins.stream().filter(e -> "t".equals(e.mode)).map(e -> WeekDay.values()[e.index - 1].name()).collect(joining(","));

        int beginHour = getIndex(begins, "h");
        int beginMinute = getIndex(begins, "m");
        int secondHour = getIndex(seconds, "h");
        int secondMinute = getIndex(seconds, "m");
        int endHour = isDuration ? (beginHour + secondHour) % 24 + (beginMinute + secondMinute >= 60 ? 1 : 0) : secondHour;
        int endMinute = isDuration ? (beginMinute + secondMinute) % 60 : secondMinute;

        // Format the closing hours
        String hours = "";
        if (Math.abs(endHour - beginHour) > 0 || Math.abs(endMinute - beginMinute) > 0) {
            hours = format("%02d:%02d-%02d:%02d", beginHour, beginMinute, endHour, endMinute);
        }

        String month = "";
        if (beginMonth > 0) {
            month = format("%s", Month.values()[beginMonth - 1]);
            if (endMonth > 0 && endMonth != beginMonth) {
                month += format("-%s", Month.values()[endMonth - 1]);
            }
        }

        String year = "";
        if (beginYear > 0) {
            year = format("%s", beginYear);
            if (endYear > 0 && endYear != beginYear) {
                year += format("-%s", endYear);
                if (!isDuration) {
                    return format(
                        "%s %s %s-%s %s %s off",
                        beginYear,
                        Month.values()[beginMonth - 1],
                        beginDay,
                        endYear,
                        Month.values()[endMonth - 1],
                        endDay
                    ).trim().replaceAll("\\s+", " ");
                }
            }
        }

        return format("%s %s %s %s off", year, month, days, hours).trim().replaceAll("\\s+", " ");
    }

    private Integer getIndex(List<Element> elements, String mode) {
        return elements.stream().filter(e -> mode.equals(e.mode)).map(Element::getIndex).findFirst().orElse(0);
    }
}
