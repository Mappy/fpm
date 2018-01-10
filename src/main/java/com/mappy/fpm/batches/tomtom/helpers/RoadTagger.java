package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TdDbf;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.utils.Feature;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.helpers.Fow.*;
import static com.mappy.fpm.batches.tomtom.helpers.Freeway.PART_OF_FREEWAY;
import static java.util.Optional.ofNullable;

@Singleton
@Slf4j
public class RoadTagger {

    public static final Integer TUNNEL = 1;
    public static final Integer BRIDGE = 2;
    private static final String FOOT = "foot";
    private static final String BICYCLE = "bicycle";
    private static final int ROAD_ELEMENT = 4110;

    private final SpeedProfiles speedProfiles;
    private final NameProvider nameProvider;
    private final SignPosts signPosts;
    private final LaneTagger lanes;
    private final SpeedRestrictionTagger speedRestriction;
    private final TollTagger tolls;
    private final TdDbf tdDbf;
    private final TimeDomainsParser timeDomainsParser;

    @Inject
    public RoadTagger(SpeedProfiles speedProfiles, NameProvider nameProvider, SignPosts signPosts, LaneTagger lanes,
                      SpeedRestrictionTagger speedRestriction, TollTagger tolls, TdDbf tdDbf, TimeDomainsParser timeDomainsParser
    ) {
        this.speedProfiles = speedProfiles;
        this.nameProvider = nameProvider;
        this.signPosts = signPosts;
        this.lanes = lanes;
        this.speedRestriction = speedRestriction;
        this.tolls = tolls;
        this.tdDbf = tdDbf;
        this.timeDomainsParser = timeDomainsParser;
        this.nameProvider.loadAlternateNames("gc.dbf");
    }

    public Map<String, String> tag(Feature feature) {
        Map<String, String> tags = newHashMap();

        Long id = feature.getLong("ID");
        tags.put("ref:tomtom", String.valueOf(id));
        tags.put("from:tomtom", String.valueOf(feature.getLong("F_JNCTID")));
        tags.put("to:tomtom", String.valueOf(feature.getLong("T_JNCTID")));
        tags.putAll(level(feature));

        addTagIf("name", feature.getString("NAME"), feature.getString("NAME") != null, tags);
        addTagIf("ref", feature.getString("SHIELDNUM"), feature.getString("SHIELDNUM") != null, tags);
        addTagIf("reversed:tomtom", "yes", isReversed(feature), tags);
        addTagIf("oneway", "yes", isOneway(feature), tags);

        tags.putAll(tolls.tag(id));

        Collection<TimeDomains> timeDomains = tdDbf.getTimeDomains(id);
        addTagIf("motor_vehicle", "no", "N".equals(feature.getString("ONEWAY")) && timeDomains.isEmpty(), tags);
        if (timeDomains != null && !timeDomains.isEmpty()){
            try {
                String openingHours = timeDomainsParser.parse(timeDomains);
                addTagIf("opening_hours", openingHours, !"".equals(openingHours), tags);
            } catch (IllegalArgumentException iae) {
                log.warn("Unable to parse opening hours from " + timeDomains);
            }
        }
        addTagIf("route", "ferry", feature.getInteger("FT").equals(1), tags);
        addTagIf("duration", () -> duration(feature), tags.containsValue("ferry"), tags);
        if (!tags.containsValue("ferry")) {
            tags.putAll(speedProfiles.getTags(feature));
            tags.putAll(speedRestriction.tag(feature));
            tags.putAll(highwayType(feature));
            tags.putAll(lanes.lanesFor(feature));
            tags.putAll(nameProvider.getAlternateNames(id));
            tags.putAll(nameProvider.getAlternateRoadSideNames(id, feature.getInteger("SOL")));

            addTagIf("tunnel", "yes", TUNNEL.equals(feature.getInteger("PARTSTRUC")), tags);
            addTagIf("bridge", "yes", BRIDGE.equals(feature.getInteger("PARTSTRUC")), tags);
            addTagIf("junction", "roundabout", ROUNDABOUT.is(feature.getInteger("FOW")), tags);
            addTagIf("access", "private", feature.getInteger("PRIVATERD") != null && feature.getInteger("PRIVATERD") > 0, tags);
            addTagIf("mappy_length", () -> String.valueOf(feature.getDouble("METERS")), feature.getDouble("METERS") != null, tags);

            tags.putAll(signPosts.getTags(id, isOneway(feature), feature.getLong("F_JNCTID"), feature.getLong("T_JNCTID")));
        }
        return tags;
    }

    private boolean isOneway(Feature feature) {
        return "TF".equals(feature.getString("ONEWAY")) || "FT".equals(feature.getString("ONEWAY"));
    }

    public static void addTagIf(String key, String value, boolean condition, Map<String, String> tags) {
        if (condition) {
            tags.put(key, value);
        }
    }

    private static void addTagIf(String key, Supplier<String> toExecute, boolean condition, Map<String, String> tags) {
        if (condition) {
            tags.put(key, toExecute.get());
        }
    }

    public static Map<String, String> level(Feature feature) {
        Map<String, String> tags = newHashMap();
        Integer fElev = feature.getInteger("F_ELEV");
        Integer tElev = feature.getInteger("T_ELEV");

        if (fElev.equals(tElev)) {
            tags.put("layer", String.valueOf(fElev));
        } else {
            if (isReversed(feature)) {
                tags.put("layer:from", String.valueOf(tElev));
                tags.put("layer:to", String.valueOf(fElev));
            } else {
                tags.put("layer:from", String.valueOf(fElev));
                tags.put("layer:to", String.valueOf(tElev));
            }
        }
        return tags;
    }

    public static boolean isReversed(Feature feature) {
        return "TF".equals(feature.getString("ONEWAY"));
    }

    private Map<String, String> highwayType(Feature feature) {
        Map<String, String> tags = newHashMap();
        Integer feattype = feature.getInteger("FEATTYP");
        if (feattype != null && feattype == ROAD_ELEMENT) {
            Integer fow = feature.getInteger("FOW");
            Fow.getFow(fow).map(Fow::getTags).ifPresent(tags::putAll);

            ofNullable(feature.getInteger("FRC"))//
                    .flatMap(f -> Frc.getFrc(f, SLIP_ROAD.is(fow), PART_OF_FREEWAY.is(feature.getInteger("FREEWAY"))))//
                    .map(Frc::getTags)//
                    .ifPresent(tags::putAll);
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
