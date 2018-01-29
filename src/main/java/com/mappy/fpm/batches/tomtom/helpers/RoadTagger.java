package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsData;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.utils.Feature;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.helpers.FormOfWay.ROUNDABOUT;
import static com.mappy.fpm.batches.tomtom.helpers.FormOfWay.SLIP_ROAD;
import static com.mappy.fpm.batches.tomtom.helpers.Freeway.PART_OF_FREEWAY;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;

@Singleton
@Slf4j
public class RoadTagger {

    public static final Integer TUNNEL = 1;
    public static final Integer BRIDGE = 2;
    private static final int ROAD_ELEMENT = 4110;

    private final SpeedProfiles speedProfiles;
    private final NameProvider nameProvider;
    private final SignPosts signPosts;
    private final LaneTagger lanes;
    private final SpeedRestrictionTagger speedRestriction;
    private final TollTagger tolls;
    private final TimeDomainsData timeDomainsData;
    private final TimeDomainsParser timeDomainsParser;

    @Inject
    public RoadTagger(SpeedProfiles speedProfiles, NameProvider nameProvider, SignPosts signPosts, LaneTagger lanes,
                      SpeedRestrictionTagger speedRestriction, TollTagger tolls, TimeDomainsData timeDomainsData, TimeDomainsParser timeDomainsParser
    ) {
        this.speedProfiles = speedProfiles;
        this.nameProvider = nameProvider;
        this.signPosts = signPosts;
        this.lanes = lanes;
        this.speedRestriction = speedRestriction;
        this.tolls = tolls;
        this.timeDomainsData = timeDomainsData;
        this.timeDomainsParser = timeDomainsParser;
        this.nameProvider.loadAlternateNames("gc.dbf");
    }

    public Map<String, String> tag(Feature feature) {
        Map<String, String> tags = newHashMap();

        Long id = feature.getLong("ID");

        tagTomtomSpecial(feature, tags, id);

        tags.putAll(level(feature));

        addTagIf("name", feature.getString("NAME"), ofNullable(feature.getString("NAME")).isPresent(), tags);
        addTagIf("ref", feature.getString("SHIELDNUM"), ofNullable(feature.getString("SHIELDNUM")).isPresent(), tags);
        addTagIf("oneway", "yes", isOneway(feature), tags);

        tags.putAll(tolls.tag(id));

        Collection<TimeDomains> timeDomains = timeDomainsData.getTimeDomains(id);
        addTagIf("motor_vehicle", "no", "N".equals(feature.getString("ONEWAY")) && timeDomains.isEmpty(), tags);

        if (timeDomains != null && !timeDomains.isEmpty()) {
            tagsTimeDomains(tags, timeDomains);
        }

        addTagIf("route", "ferry", feature.getInteger("FT").equals(1), tags);
        addTagIf("duration", () -> duration(feature), tags.containsValue("ferry"), tags);

        if (tags.containsValue("ferry")) {
            return tags;
        }

        tagRoute(feature, tags, id);

        return tags;
    }

    public static Map<String, String> level(Feature feature) {
        Map<String, String> tags = newHashMap();
        Integer fElev = feature.getInteger("F_ELEV");
        Integer tElev = feature.getInteger("T_ELEV");

        if (fElev.equals(tElev)) {
            tags.put("layer", valueOf(fElev));
        } else {
            if (isReversed(feature)) {
                tags.put("layer:from", valueOf(tElev));
                tags.put("layer:to", valueOf(fElev));
            } else {
                tags.put("layer:from", valueOf(fElev));
                tags.put("layer:to", valueOf(tElev));
            }
        }
        return tags;
    }

    public static boolean isReversed(Feature feature) {
        return "TF".equals(feature.getString("ONEWAY"));
    }

    public static void addTagIf(String key, String value, boolean condition, Map<String, String> tags) {
        if (condition) {
            tags.put(key, value);
        }
    }

    private void tagRoute(Feature feature, Map<String, String> tags, Long id) {
        tags.putAll(speedProfiles.getTags(feature));
        tags.putAll(speedRestriction.tag(feature));
        tags.putAll(highwayType(feature));
        tags.putAll(lanes.lanesFor(feature));
        tags.putAll(nameProvider.getAlternateNames(id));

        Integer sol = ofNullable(feature.getInteger("SOL")).orElse(0);
        tags.putAll(nameProvider.getAlternateRoadSideNames(id, sol));

        addTagIf("tunnel", "yes", TUNNEL.equals(feature.getInteger("PARTSTRUC")), tags);
        addTagIf("bridge", "yes", BRIDGE.equals(feature.getInteger("PARTSTRUC")), tags);
        addTagIf("junction", "roundabout", ROUNDABOUT.is(feature.getInteger("FOW")), tags);
        addTagIf("access", "private", ofNullable(feature.getInteger("PRIVATERD")).isPresent() && feature.getInteger("PRIVATERD") > 0, tags);
        addTagIf("mappy_length", () -> valueOf(feature.getDouble("METERS")), ofNullable(feature.getDouble("METERS")).isPresent(), tags);

        tags.putAll(signPosts.getTags(id, isOneway(feature), feature.getLong("F_JNCTID"), feature.getLong("T_JNCTID")));
    }

    private void tagTomtomSpecial(Feature feature, Map<String, String> tags, Long id) {
        tags.put("ref:tomtom", valueOf(id));
        tags.put("from:tomtom", valueOf(feature.getLong("F_JNCTID")));
        tags.put("to:tomtom", valueOf(feature.getLong("T_JNCTID")));
        addTagIf("reversed:tomtom", "yes", isReversed(feature), tags);
    }

    private void tagsTimeDomains(Map<String, String> tags, Collection<TimeDomains> timeDomains) {
        try {
            String openingHours = timeDomainsParser.parse(timeDomains);
            addTagIf("opening_hours", openingHours, !"".equals(openingHours), tags);
        } catch (IllegalArgumentException iae) {
            log.warn("Unable to parse opening hours from " + timeDomains);
        }
    }

    private boolean isOneway(Feature feature) {
        return "TF".equals(feature.getString("ONEWAY")) || "FT".equals(feature.getString("ONEWAY"));
    }

    private static void addTagIf(String key, Supplier<String> toExecute, boolean condition, Map<String, String> tags) {
        if (condition) {
            tags.put(key, toExecute.get());
        }
    }

    private Map<String, String> highwayType(Feature feature) {
        Map<String, String> tags = newHashMap();
        Optional<Integer> feattype = ofNullable(feature.getInteger("FEATTYP"));

        if (feattype.isPresent() && feattype.get() == ROAD_ELEMENT) {
            Integer fow = feature.getInteger("FOW");

            ofNullable(feature.getInteger("FRC"))//
                    .flatMap(f -> FunctionalRoadClass.getFonctionalRoadClass(f, SLIP_ROAD.is(fow), PART_OF_FREEWAY.is(feature.getInteger("FREEWAY"))))//
                    .map(FunctionalRoadClass::getTags)//
                    .ifPresent(tags::putAll);

            FormOfWay.getFormOfWay(feature.getInteger("FOW")).map(FormOfWay::getTags).ifPresent(tags::putAll);
        }
        return tags;
    }

    private static String duration(Feature feature) {
        Double minutes = feature.getDouble("MINUTES");
        Double hours = minutes / 60 % 24;
        Double mins = minutes % 60;
        Double sec = minutes * 60 % 60;
        return String.format("%02d:%02d:%02d", hours.intValue(), mins.intValue(), sec.intValue());
    }
}
