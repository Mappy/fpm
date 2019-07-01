package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.dbf.geocodes.GeocodeProvider;
import com.mappy.fpm.batches.tomtom.dbf.intersection.RouteIntersectionProvider;
import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.poi.FeatureType;
import com.mappy.fpm.batches.tomtom.dbf.poi.PoiProvider;
import com.mappy.fpm.batches.tomtom.dbf.routenumbers.RouteNumbersProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsProvider;
import com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationAreaProvider;
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
    private static final Integer ROAD_ELEMENT = 4110;
    private final SpeedProfiles speedProfiles;
    private final GeocodeProvider geocodeProvider;
    private final SignPosts signPosts;
    private final LaneTagger lanes;
    private final SpeedRestrictionTagger speedRestriction;
    private final TollTagger tolls;
    private final TimeDomainsProvider timeDomainsProvider;
    private final TimeDomainsParser timeDomainsParser;
    private final TransportationAreaProvider transportationAreaProvider;
    private final RouteNumbersProvider routeNumbersProvider;
    private final RouteIntersectionProvider intersectionProvider;
    private final PoiProvider poiProvider;

    @Inject
    public RoadTagger(SpeedProfiles speedProfiles, GeocodeProvider geocodeProvider, SignPosts signPosts, LaneTagger lanes,
                      SpeedRestrictionTagger speedRestriction, TollTagger tolls, TimeDomainsProvider timeDomainsProvider, TimeDomainsParser timeDomainsParser,
                      TransportationAreaProvider transportationAreaProvider, RouteNumbersProvider routeNumbersProvider, RouteIntersectionProvider intersectionProvider, PoiProvider poiProvider) {
        this.speedProfiles = speedProfiles;
        this.geocodeProvider = geocodeProvider;
        this.signPosts = signPosts;
        this.lanes = lanes;
        this.speedRestriction = speedRestriction;
        this.tolls = tolls;
        this.timeDomainsProvider = timeDomainsProvider;
        this.timeDomainsParser = timeDomainsParser;
        this.transportationAreaProvider = transportationAreaProvider;
        this.routeNumbersProvider = routeNumbersProvider;
        this.intersectionProvider = intersectionProvider;
        this.poiProvider = poiProvider;
        this.intersectionProvider.loadIntersectionById();
    }

    public Map<String, String> tag(Feature feature) {
        Map<String, String> tags = newHashMap();

        Long id = feature.getLong("ID");

        tagTomtomSpecial(feature, tags, id);

        tags.putAll(level(feature));

        addTagIf("name", feature.getString("NAME"), ofNullable(feature.getString("NAME")).isPresent(), tags);
        addTagIf("int_ref", feature.getString("SHIELDNUM"), ofNullable(feature.getString("SHIELDNUM")).isPresent(), tags);
        routeNumbersProvider.getInternationalRouteNumbers(id).ifPresent(s -> tags.put("int_ref", s));
        routeNumbersProvider.getNationalRouteNumbers(id).ifPresent(s -> tags.put("ref", s));
        addTagIf("oneway", "yes", isOneway(feature), tags);

        tags.putAll(tolls.tag(id));

        Collection<TimeDomains> timeDomains = timeDomainsProvider.getTimeDomains(id);
        addTagIf("motor_vehicle", "no", "N".equals(feature.getString("ONEWAY")) && timeDomains.isEmpty(), tags);

        if (timeDomains != null && !timeDomains.isEmpty()) {
            tagsTimeDomains(tags, timeDomains);
        }

        addTagIf("route", "ferry", feature.getInteger("FT").equals(1), tags);
        addTagIf("duration", () -> duration(feature), tags.containsValue("ferry"), tags);

        tags.putAll(geocodeProvider.getNamesAndAlternateNamesWithSide(id));

        if (tags.containsValue("ferry")) {
            return tags;
        }

        tagRoute(feature, tags, id);

        tags.putAll(geocodeProvider.getInterpolations(id));

        geocodeProvider.getLeftPostalCode(id).ifPresent(postcodes -> tags.put("is_in:left", postcodes));
        geocodeProvider.getRightPostalCode(id).ifPresent(postcodes -> tags.put("is_in:right", postcodes));
        geocodeProvider.getInterpolationsAddressLeft(id).ifPresent(interpolations -> tags.put("addr:interpolation:left", interpolations));
        geocodeProvider.getInterpolationsAddressRight(id).ifPresent(interpolations -> tags.put("addr:interpolation:right", interpolations));

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

        addTagIf("tunnel", "yes", TUNNEL.equals(feature.getInteger("PARTSTRUC")), tags);
        addTagIf("bridge", "yes", BRIDGE.equals(feature.getInteger("PARTSTRUC")), tags);
        addTagIf("junction", "roundabout", ROUNDABOUT.is(feature.getInteger("FOW")), tags);
        addTagIf("access", "private", ofNullable(feature.getInteger("PRIVATERD")).isPresent() && feature.getInteger("PRIVATERD") > 0, tags);

        addTagIf("mappy_length", () -> valueOf(feature.getDouble("METERS")), ofNullable(feature.getDouble("METERS")).isPresent(), tags);

        if (SLIP_ROAD.is(feature.getInteger("FOW")) && intersectionProvider.getIntersectionById().containsKey(id)) {
            tags.put("junction:ref", intersectionProvider.getIntersectionById().get(id));
        }

        tags.putAll(signPosts.getTags(id, isOneway(feature), feature.getLong("F_JNCTID"), feature.getLong("T_JNCTID")));

        poiProvider.getPoiNameByType(id, FeatureType.MOUNTAIN_PASS.getValue()).ifPresent(value -> {
            tags.put("mountain_pass", value);
        });
    }

    private void tagTomtomSpecial(Feature feature, Map<String, String> tags, Long id) {
        tags.put("ref:tomtom", valueOf(id));
        tags.put("from:tomtom", valueOf(feature.getLong("F_JNCTID")));
        tags.put("to:tomtom", valueOf(feature.getLong("T_JNCTID")));
        addTagIf("reversed:tomtom", "yes", isReversed(feature), tags);
        addTagIf("global_importance:tomtom", valueOf(feature.getInteger("NET2CLASS")), ofNullable(feature.getInteger("NET2CLASS")).isPresent(), tags);
        transportationAreaProvider.getSmallestAreasLeft(id).ifPresent(ids -> tags.put("admin:tomtom:left", ids));
        transportationAreaProvider.geSmallestAreasRight(id).ifPresent(ids -> tags.put("admin:tomtom:right", ids));
        transportationAreaProvider.getBuiltUpLeft(id).ifPresent(ids -> tags.put("bua:tomtom:left", ids));
        transportationAreaProvider.getBuiltUpRight(id).ifPresent(ids -> tags.put("bua:tomtom:right", ids));
        routeNumbersProvider.getRouteTypeOrderByPriority(id).ifPresent(type -> tags.put("route_type:tomtom", type));
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

        if (feattype.isPresent() && feattype.get().equals(ROAD_ELEMENT)) {
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
