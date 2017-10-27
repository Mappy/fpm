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
import static java.util.stream.Collectors.joining;

@Slf4j
public class TimeDomainsParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("\\[\\((.*)\\)\\{(.*)\\}\\]");
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("\\[\\((.*)\\)\\((.*)\\)\\]");

    private static final Pattern BEGIN_PATTERN = Pattern.compile("(\\p{Alpha}\\d{1,2})");

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

        List<Element> begin = elements.getFirst();
        List<Element> duration = elements.getSecond();
        if ("h".equals(begin.get(0).getMode()) && "h".equals(duration.get(0).getMode())) {
            return generateWithHours(begin, duration);

        } else if ("M".equals(begin.get(0).getMode()) && "M".equals(duration.get(0).getMode())) {
            return String.format("%s-%s off", Month.values()[begin.get(0).getIndex() - 1], Month.values()[(begin.get(0).getIndex() + duration.get(0).getIndex() - 2) % 12]);

        } else if ("t".equals(begin.get(0).getMode())) {
            return generateWithWeekDay(begin, duration);

        } else if ("z".equals(begin.get(0).getMode())) {
            return null;
        }

        log.warn("Unable to parse duration {}", matcher.group(0));
        throw new IllegalArgumentException("Unable to parse duration " + matcher.group(0));
    }

    private String generateWithHours(List<Element> begin, List<Element> duration) {
        int beginMinute = begin.stream().filter(e -> "m".equals(e.getMode())).map(Element::getIndex).findFirst().orElse(0);
        int durationMinute = duration.stream().filter(e -> "m".equals(e.getMode())).map(Element::getIndex).findFirst().orElse(0);

        return String.format("%02d:%02d-%02d:%02d off", begin.get(0).getIndex(), beginMinute, (begin.get(0).getIndex() + duration.get(0).getIndex()) % 24, beginMinute + durationMinute);
    }

    private String generateWithWeekDay(List<Element> begin, List<Element> duration) {
        int beginHour = 0;
        String days = begin.stream().filter(e -> "t".equals(e.mode)).map(e -> WeekDay.values()[e.index -1].name()).collect(joining(","));

        Element last = begin.get(begin.size() - 1);
        if ("h".equals(last.mode)) {
            beginHour = last.getIndex();
        }

        return String.format("%s %02d:00-%02d:00 off", days, beginHour, (beginHour + duration.get(0).getIndex()) % 24);
    }

    private String getOpeningHoursFromInterval(Matcher matcher) {

        Elements elements = parse(matcher);

        List<Element> begin = elements.getFirst();
        List<Element> end = elements.getSecond();
        if ("h".equals(begin.get(0).getMode()) && "h".equals(end.get(0).getMode())) {
            return String.format("%02d:00-%02d:00 off", begin.get(0).getIndex(), end.get(0).getIndex());

        } else if ("M".equals(begin.get(0).getMode()) && "M".equals(end.get(0).getMode())) {
            return String.format("%s-%s off", Month.values()[begin.get(0).getIndex() - 1], Month.values()[end.get(0).getIndex() - 1]);
        }

        log.warn("Unable to parse interval {}", matcher.group(0));
        throw new IllegalArgumentException("Unable to parse interval " + matcher.group(0));
    }

    private Elements parse(Matcher matcher) {
        return new Elements(parse(matcher.group(1)), parse(matcher.group(2)));
    }

    private List<Element> parse(String group) {
        List<Element> list = newArrayList();
        try {
            Matcher matcher = BEGIN_PATTERN.matcher(group);

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
