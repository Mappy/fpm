package com.mappy.fpm.batches.tomtom.download;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.jsoup.parser.Parser.xmlParser;

public class MetalinkParser {

    private static final Pattern PATTERN = Pattern.compile("^(.*?)-shp(.?)-(.*?)-(.*?)-(.*?)\\.7z\\.001");

    public static Metalink parse(InputStream xml) {
        try {
            Document doc = Jsoup.parse(IOUtils.toString(xml, UTF_8.name()), "", xmlParser());
            return new Metalink(doc.select("files file").stream()
                    .map(MetalinkParser::tomtomFileDescriptor)
                    .filter(Objects::nonNull)
                    .filter(MetalinkUrl::isShapefile)
                    .filter(MetalinkUrl::needed)
                    .collect(toList()));
        }
        catch (IOException e) {
            throw propagate(e);
        }
    }

    private static MetalinkUrl tomtomFileDescriptor(Element e) {
        return MetalinkUrl.parse(e.attr("name"), e.select("resources url").text());
    }

    @Data
    public static class Metalink {
        private final List<MetalinkUrl> urls;

        public int size() {
            return urls.size();
        }

        public boolean isEmpty() {
            return urls.isEmpty();
        }

        public List<String> zones() {
            return urls.stream().map(MetalinkUrl::getZone).distinct().collect(toList());
        }

        public List<String> types() {
            return urls.stream().map(MetalinkUrl::getType).distinct().collect(toList());
        }

        public Metalink forCountry(String country) {
            return new Metalink(urls.stream().filter(t -> t.getCountry().equals(country)).collect(toList()));
        }

        public Metalink forZone(String zone) {
            return new Metalink(urls.stream().filter(t -> t.getZone().equals(zone)).collect(toList()));
        }
    }

    @Data
    public static class MetalinkUrl {
        private static final Set<String> NEEDED = newHashSet(newArrayList("2dcmnb", "mn", "sp"));

        private final String name;
        private final String country;
        private final String format;
        private final String type;
        private final String zone;
        private final String url;

        public boolean isShapefile() {
            return newArrayList("d", "").contains(format);
        }

        public boolean needed() {
            return NEEDED.contains(type);
        }

        public static MetalinkUrl parse(String name, String url) {
            Matcher matcher = PATTERN.matcher(name);
            return matcher.matches() ? new MetalinkUrl(name, matcher.group(4), matcher.group(2), matcher.group(3), matcher.group(5), url) : null;
        }
    }
}