package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.VehicleType;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;

import java.lang.RuntimeException;
import java.util.List;
import java.util.Map;

import static com.mappy.fpm.batches.tomtom.helpers.RoadTagger.isReversed;

public class SpeedRestrictionTagger {

    private final SrDbf dbf;

    @Inject
    public SpeedRestrictionTagger(SrDbf dbf) {
        this.dbf = dbf;
    }

    public Map<String, String> tag(Feature feature) {
        Map<String, String> speeds = Maps.newHashMap();
        List<SpeedRestriction> restrictions = dbf.getSpeedRestrictions(feature.getLong("ID"));
        boolean reversed = isReversed(feature);
        boolean gotPassengerCarSpeed = false;
        for (SpeedRestriction restriction : restrictions) {
            switch (restriction.getVehicleType()) {
                case passengerCars:
                    gotPassengerCarSpeed = true;
                    break;
                case all:
                    if (gotPassengerCarSpeed) {
                        continue;
                    }
                    break;
                default:
                    continue;
            }
            switch (restriction.getValidity()) {
                case positive:
                    speeds.put(reversed ? "maxspeed:backward" : "maxspeed:forward", String.valueOf(restriction.getSpeed()));
                    break;
                case negative:
                    speeds.put(reversed ? "maxspeed:forward" : "maxspeed:backward", String.valueOf(restriction.getSpeed()));
                    break;
                case both:
                    speeds.put("maxspeed", String.valueOf(restriction.getSpeed()));
                    break;
            }
        }
        return speeds;
    }
}
