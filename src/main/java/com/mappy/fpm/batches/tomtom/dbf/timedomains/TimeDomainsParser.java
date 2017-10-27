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
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("^\\[\\((.*)\\)\\((.*)\\)\\]");

    private static final Pattern MOTIF_PATTERN = Pattern.compile("(\\p{Alpha}\\d{1,2})");

    private enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    }

    private enum WeekDay {
        Su, Mo, Tu, We, Th, Fr, Sa
    }

    public String parse(Collection<TimeDomains> tomtomTimesDomains) {
        String result = tomtomTimesDomains.stream().map(this::parse).filter(Objects::nonNull).collect(joining(", "));
//        log.info("Parsing time domain from {} to {}", tomtomTimesDomains, result);
        return result;
    }

    private String parse(TimeDomains timeDomains) {

        Matcher durationMatcher = DURATION_PATTERN.matcher(timeDomains.getDomain());
        Matcher intervalMatcher = INTERVAL_PATTERN.matcher(timeDomains.getDomain());

        if (durationMatcher.find()) {
            return getOpeningHours(durationMatcher, true);

        } else if (intervalMatcher.find()) {
            return getOpeningHours(intervalMatcher, false);

        } else {
            log.warn("Unable to parse '{}'", timeDomains.getDomain());
            throw new IllegalArgumentException("Unable to parse '" + timeDomains.getDomain() + "'");
        }
    }

    private String getOpeningHours(Matcher matcher, boolean isDuration) {

        Elements elements = new Elements(parse(matcher.group(1)), parse(matcher.group(2)));

        List<Element> begin = elements.getFirst();
        List<Element> duration = elements.getSecond();

        if (begin.stream().anyMatch(e -> newArrayList("h", "M", "t").contains(e.mode))) {
            return generate(begin, duration, isDuration);

        } else if (begin.stream().anyMatch(e -> newArrayList("z").contains(e.mode))) {
            return null;
        }

        log.warn("Unable to parse duration {}", matcher.group(0));
        throw new IllegalArgumentException("Unable to parse duration " + matcher.group(0));
    }

    private String generate(List<Element> begin, List<Element> duration, boolean isDuration) {
        int beginMonth = getIndex(begin, "M");
        int durationMonth = getIndex(duration, "M");

        String month = "";
        if (beginMonth + durationMonth > 0) {
            int endMonth = isDuration ? (beginMonth + durationMonth - 1) % 12 : durationMonth;
            month = format("%s-%s", Month.values()[beginMonth - 1], Month.values()[endMonth - 1]);
        }

        String days = begin.stream().filter(e -> "t".equals(e.mode)).map(e -> WeekDay.values()[e.index - 1].name()).collect(joining(","));

        int beginHour = getIndex(begin, "h");
        int beginMinute = getIndex(begin, "m");

        int durationHour = getIndex(duration, "h");
        int durationMinute = getIndex(duration, "m");
        String hours = "";
        if (beginHour + beginMinute + durationHour + durationMinute > 0) {
            int endHour = isDuration ? (beginHour + durationHour) % 24 + (beginMinute + durationMinute >= 60 ? 1 : 0) : durationHour;
            int endMinute = isDuration ? (beginMinute + durationMinute) % 60 : durationMinute;
            hours = format("%02d:%02d-%02d:%02d", beginHour, beginMinute, endHour, endMinute);
        }

        return format("%s%s %s", month, days, hours).trim() + " off";
    }

    private Integer getIndex(List<Element> begin, String mode) {
        return begin.stream().filter(e -> mode.equals(e.mode)).map(Element::getIndex).findFirst().orElse(0);
    }

    private List<Element> parse(String group) {
        List<Element> list = newArrayList();
        try {
            Matcher matcher = MOTIF_PATTERN.matcher(group);

            int index = 0;
            while (matcher.find(index)) {
                String firstBegin = matcher.group(1);
                Element element = new Element(firstBegin.substring(0, 1), valueOf(firstBegin.substring(1, firstBegin.length())));
                list.add(element);
                index += firstBegin.length();
            }

            return list;

        } catch (NumberFormatException nfe) {
            log.warn("Unable to parse {}", group);
            throw new IllegalArgumentException("Unable to parse " + group);
        }
    }

    @Data
    private static class Element {
        private final String mode;
        private final int index;
    }

    @Data
    private static class Elements {
        private final List<Element> first;
        private final List<Element> second;
    }
}
