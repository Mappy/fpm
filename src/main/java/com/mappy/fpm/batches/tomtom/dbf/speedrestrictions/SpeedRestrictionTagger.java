package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.VehicleType;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.Validity;
import com.mappy.fpm.batches.tomtom.dbf.speedtimedomains.StDbf;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;

import java.lang.RuntimeException;
import java.util.List;
import java.util.Map;
import java.util.EnumSet;

import static com.mappy.fpm.batches.tomtom.helpers.RoadTagger.isReversed;

public class SpeedRestrictionTagger {

    private final SrDbf srDbf;
    private final StDbf stDbf;

    @Inject
    public SpeedRestrictionTagger(SrDbf srDbf, StDbf stDbf) {
        this.srDbf = srDbf;
        this.stDbf = stDbf;
    }

    public Map<String, String> tag(Feature feature) {
        Map<String, String> speeds = Maps.newHashMap();
        List<SpeedRestriction> restrictions = srDbf.getSpeedRestrictions(feature.getLong("ID"));
        boolean reversed = isReversed(feature);
        EnumSet<Validity> validitiesWithPassengerCarSpeed = EnumSet.noneOf(Validity.class);
        for (SpeedRestriction restriction : restrictions) {
            TimeDomains timeDomain = stDbf.getSpeedTimeDomain(restriction.getId(), restriction.getSequenceNumber());

            if (timeDomain != null) {
                // The speed only applies in some specific cases (e.g. weather, time range)
                continue;
            }

            Validity validity = restriction.getValidity();
            switch (restriction.getVehicleType()) {
                case passengerCars:
                    validitiesWithPassengerCarSpeed.add(validity);
                    break;
                case all:
                    if (validitiesWithPassengerCarSpeed.contains(validity)) {
                        continue;
                    }
                    break;
                default:
                    continue;
            }

            switch (validity) {
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
