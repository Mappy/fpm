package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.tomtom.helpers.VehicleType;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Lists.newArrayList;

public class LaneTagger {

    private final LdDbf ldDbf;
    private final LfDbf lfDbf;
    private final LtDbf ltDbf;

    @Inject
    public LaneTagger(LdDbf ldDbf, LfDbf lfDbf, LtDbf ltDbf) {
        this.ldDbf = ldDbf;
        this.lfDbf = lfDbf;
        this.ltDbf = ltDbf;
    }

    public Map<String, String> lanesFor(Feature feature, Boolean isReversed) {
        Map<String, String> tags = newHashMap();

        if (ldDbf.containsKey(feature.getLong("ID"))) {
            tags.put("turn:lanes", on("|").join(reorder(feature, isReversed)));
        }

        Integer lanes = feature.getInteger("LANES");
        if (lanes > 0) {
            tags.put("lanes", String.valueOf(lanes));
        }

        tags.putAll(tagTrafficFlow(feature));

        return tags;
    }

    private List<String> reorder(Feature feature, Boolean isReversed) {
        List<String> parts = ldDbf.get(feature.getLong("ID"));
        if (isReversed) {
            return parts;
        }
        return reverse(parts);
    }

    /**
     * List, for each lane and each direction, which vehicle is prohibited from circulating
     */
    private List<Map<LaneTrafficFlow.Direction, EnumSet<VehicleType.Vehicle>>> computeTrafficFlowRestrictions(Feature feature, Collection<LaneTrafficFlow> trafficFlow) {
        List<Map<LaneTrafficFlow.Direction, EnumSet<VehicleType.Vehicle>>> restrictions = newArrayList();
        int laneCount = feature.getInteger("LANES");
        for (int lane = 0; lane < laneCount; ++lane) {
            Map<LaneTrafficFlow.Direction, EnumSet<VehicleType.Vehicle>> laneRestrictions = newHashMap();
            laneRestrictions.put(LaneTrafficFlow.Direction.forward, EnumSet.noneOf(VehicleType.Vehicle.class));
            laneRestrictions.put(LaneTrafficFlow.Direction.backward, EnumSet.noneOf(VehicleType.Vehicle.class));
            restrictions.add(laneRestrictions);
        }
        for (LaneTrafficFlow laneRestriction: trafficFlow) {
            for (int lane: laneRestriction.getLaneValidity()) {
                for (LaneTrafficFlow.Direction direction: LaneTrafficFlow.Direction.values()) {
                    if (laneRestriction.getDirection().directions.contains(direction)) {
                        restrictions.get(lane).get(direction).addAll(laneRestriction.getVehicleType().vehicles);
                    }
                }
            }
        }
        return restrictions;
    }

    private Map<String, String> tagTrafficFlow(Feature feature) {
        Map<String, String> tags = newHashMap();
        Collection<LaneTrafficFlow> trafficFlow = lfDbf.get(feature.getLong("ID"));
        if (trafficFlow.isEmpty()) {
            return tags;
        }
        List<Map<LaneTrafficFlow.Direction, EnumSet<VehicleType.Vehicle>>> restrictions = computeTrafficFlowRestrictions(feature, trafficFlow);
        Integer laneCount = feature.getInteger("LANES");
        for (LaneTrafficFlow.Direction direction: LaneTrafficFlow.Direction.values()) {
            // for each lane, an OSM qualification of the access condition for each vehicle type
            Map<VehicleType.Vehicle, List<String>> vehicleAccess = newHashMap();
            for (VehicleType.Vehicle vehicle: VehicleType.Vehicle.values()) {
                // Tomtom does not specify rules for bicycles
                if (vehicle == VehicleType.Vehicle.bicycle) {
                    continue;
                }
                vehicleAccess.put(vehicle, newArrayList());
            }

            for (int lane = 0; lane < laneCount; ++lane) {
                EnumSet<VehicleType.Vehicle> allowedVehicles = restrictions.get(lane).get(direction);
                for (VehicleType.Vehicle vehicle: VehicleType.Vehicle.values()) {
                    // Tomtom does not specify rules for bicycles
                    if (vehicle == VehicleType.Vehicle.bicycle) {
                        continue;
                    }
                    String value;
                    if (restrictions.get(lane).get(direction).contains(vehicle)) {
                        value = "no";
                    // All except bicycle and itself
                    } else if (allowedVehicles.size() == VehicleType.Vehicle.values().length - 2) {
                        value = "designated";
                    } else {
                        value = "yes";
                    }
                    vehicleAccess.get(vehicle).add(value);
                }
            }
            // determine private access for passenger vehicles
            List<String> residentialAccess = vehicleAccess.remove(VehicleType.Vehicle.residentialVehicle);
            List<String> publicAccess = vehicleAccess.get(VehicleType.Vehicle.passengerCar);
            for (int lane = 0; lane < laneCount; ++lane) {
                if (publicAccess.get(lane) == "no" && residentialAccess.get(lane) != "no") {
                    publicAccess.set(lane, "private");
                }
            }
            for (Map.Entry<VehicleType.Vehicle, List<String>> entry: vehicleAccess.entrySet()) {
                String tag = entry.getKey().osmLabel + ":lanes:" + direction.osmLabel;
                tags.put(tag, String.join("|", entry.getValue()));
            }
        }
        return tags;
    }

    static void incrementCounter(Map<LaneTrafficFlow.Direction, Integer> counter, LaneTrafficFlow.Direction direction) {
        counter.put(direction, counter.getOrDefault(direction, 0) + 1);
    }
}
