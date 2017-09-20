package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.Maps;
import com.mappy.fpm.batches.tomtom.TomtomStats;
import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.function.Supplier;

import static com.mappy.fpm.batches.tomtom.helpers.Fow.*;
import static com.mappy.fpm.batches.tomtom.helpers.Freeway.PART_OF_FREEWAY;

@Singleton
public class RoadTagger {

    public static final Integer TUNNEL = 1;
    public static final Integer BRIDGE = 2;
    private static final String HIGHWAY = "highway";
    private static final String FOOT = "foot";
    private static final String BICYCLE = "bicycle";
    private static final String SERVICE = "service";
    private static final int ROAD_ELEMENT = 4110;
    private final SpeedProfiles speedProfiles;
    private final TomtomStats stats;
    private final NameProvider nameProvider;
    private final SignPosts signPosts;
    private final LaneTagger lanes;
    private final SpeedRestrictionTagger speedRestriction;

    @Inject
    public RoadTagger(SpeedProfiles speedProfiles, TomtomStats tomtomStats, NameProvider nameProvider, SignPosts signPosts, LaneTagger lanes,
                      SpeedRestrictionTagger speedRestriction) {
        this.speedProfiles = speedProfiles;
        this.stats = tomtomStats;
        this.nameProvider = nameProvider;
        this.signPosts = signPosts;
        this.lanes = lanes;
        this.speedRestriction = speedRestriction;
        this.nameProvider.loadFromFile("gc.dbf", "FULLNAME", true);
    }

    public Map<String, String> tag(Feature feature) {
        stats.increment("totalRoads");
        Map<String, String> tags = Maps.newHashMap();

        Long id = feature.getLong("ID");
        tags.put("ref:tomtom", String.valueOf(id));
        tags.put("from:tomtom", String.valueOf(feature.getLong("F_JNCTID")));
        tags.put("to:tomtom", String.valueOf(feature.getLong("T_JNCTID")));
        tags.putAll(level(feature));

        addTagIf("name", feature.getString("NAME"), feature.getString("NAME") != null, tags);
        addTagIf("ref", feature.getString("SHIELDNUM"), feature.getString("SHIELDNUM") != null, tags);
        addTagIf("reversed:tomtom", "yes", isReversed(feature), tags);
        addTagIf("oneway", "yes", isOneway(feature), tags);
        Map<String, String> restrictionTags = speedRestriction.tag(feature);

        addTagIf("vehicle", "no", "N".equals(feature.getString("ONEWAY")) && restrictionTags.isEmpty(), tags);
        addTagIf("route", "ferry", feature.getInteger("FT").equals(1), tags);
        addTagIf("duration", () -> duration(feature), tags.containsValue("ferry"), tags);
        if (!tags.containsValue("ferry")) {
            tags.putAll(speedProfiles.extracted(feature));
            tags.putAll(restrictionTags);
            tags.putAll(highwayType(feature));
            tags.putAll(lanes.lanesFor(feature));
            tags.putAll(nameProvider.getAlternateNames(id));
            tags.putAll(nameProvider.getSideNames(id, feature.getInteger("SOL")));

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

    public static void addTagIf(String key, Supplier<String> toExecute, boolean condition, Map<String, String> tags) {
        if (condition) {
            tags.put(key, toExecute.get());
        }
    }

    public static Map<String, String> level(Feature feature) {
        Map<String, String> tags = Maps.newHashMap();
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
        Map<String, String> tags = Maps.newHashMap();
        Integer feattype = feature.getInteger("FEATTYP");
        if (feattype != null && feattype == ROAD_ELEMENT) {
            Integer fow = feature.getInteger("FOW");
            Integer frc = feature.getInteger("FRC");
            if (PEDESTRIAN.is(fow)) {
                tags.put(HIGHWAY, "pedestrian");
                stats.increment("highway_pedestrian");
            } else if (STAIRS.is(fow)) {
                tags.put(HIGHWAY, "steps");
                stats.increment("highway_steps");
            } else if (WALKWAY.is(fow)) {
                tags.put(HIGHWAY, "footway");
                stats.increment("highway_footway");
            } else if (PARKING_PLACE.is(fow) || ENTRANCE_EXIT_CAR_PARK.is(fow)) {
                tags.put(HIGHWAY, "service");
                tags.put(SERVICE, "parking_aisle");
                stats.increment("parking");
            } else if (frc != null) {
                boolean isFreeway = PART_OF_FREEWAY.is(feature.getInteger("FREEWAY"));
                tags.put(HIGHWAY, functionalRoadClass(frc, SLIP_ROAD.is(fow), isFreeway));
                if (frc == 0 || PART_OF_MOTORWAY.is(fow) || isFreeway) {
                    tags.put(FOOT, "no");
                    tags.put(BICYCLE, "no");
                }
            }
        }
        return tags;
    }

    private String functionalRoadClass(int frc, boolean link, boolean isFreeway) {
        switch (Frc.valueOf(frc)) {
            case MAJOR_ROAD:
                stats.increment("highway_motorway");
                return link ? "motorway_link" : "motorway";

            case LESS_THAN_MOTORWAY:
                if (isFreeway) {
                    stats.increment("highway_trunk");
                    return link ? "trunk_link" : "trunk";
                } else {
                    stats.increment("highway_primary");
                    return link ? "primary_link" : "primary";
                }

            case OTHER_MAJOR_ROAD:
                stats.increment("highway_primary");
                return link ? "primary_link" : "primary";

            case SECONDARY_ROAD:
            case LOCAL_CONNECTING_ROAD:
            case LOCAL_ROAD_OF_HIGH_IMPORTANCE:
                stats.increment("highway_secondary");
                return link ? "secondary_link" : "secondary";

            case LOCAL_ROAD:
            case LOCAL_ROAD_OF_MINOR_IMPORTANCE:
                stats.increment("highway_residential");
                return link ? "tertiary_link" : "residential";

            default:
                stats.increment("highway_unclassified");
                return link ? "tertiary_link" : "unclassified";
        }
    }

    private static String duration(Feature feature) {
        Double minutes = feature.getDouble("MINUTES");
        Double hours = minutes / 60 % 24;
        Double mins = minutes % 60;
        Double sec = minutes * 60 % 60;
        return String.format("%02d:%02d:%02d", hours.intValue(), mins.intValue(), sec.intValue());
    }
}
