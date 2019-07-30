package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class ConnectivityTagger {

    private final Connectivities connectivities;

    @Inject
    public ConnectivityTagger(Connectivities connectivities) {
        this.connectivities = connectivities;
    }

    public Map<String, String> tagConnectivities(Feature feature) {
        Map<String, String> tags = newHashMap();

        long sectionId = feature.getLong("ID");
        List<Connectivity> sectionConnectivities = connectivities.getConnectivitiesStartingAtSection(sectionId);

        if (sectionConnectivities == null) {
            return tags;
        }

        Map<Direction, List<List<Connectivity>>> destinations = listConnectivitiesPerLane(feature, sectionConnectivities);

        for (Direction direction : Direction.values()) {
            String osmValue = connectivityOSMValue(destinations.get(direction));
            if (osmValue != null) {
                tags.put(String.format("lanes:connectivity:%s:tomtom", direction.osmValue), osmValue);
            }
        }

        return tags;
    }

    private Map<Direction, List<List<Connectivity>>> listConnectivitiesPerLane(Feature feature, List<Connectivity> sectionConnectivities) {
        Integer laneCount = feature.getInteger("LANES");
        Long nodeTo = feature.getLong("T_JNCTID");
        // destinations are a sequence of sections which can be made using this lane
        // we store a list of destinations per lane, per direction
        Map<Direction, List<List<Connectivity>>> destinations = newHashMap();
        for (Direction direction : Direction.values()) {
            destinations.put(direction, newArrayList());
            for (int laneIndex = 0; laneIndex < laneCount; ++laneIndex) {
                destinations.get(direction).add(newArrayList());
            }
        }

        for (Connectivity connectivity: sectionConnectivities) {
            boolean happensAtNodeTo = connectivity.getJunctionId() == nodeTo;
            Direction direction = Direction.fromBoolean(happensAtNodeTo);
            for (int tomtomLaneIndex: connectivity.getLaneMapping().keySet()) {
                // Tomtom lists lanes right to left, OSM does it left to right
                int osmLaneIndex = laneCount - tomtomLaneIndex;
                destinations.get(direction).get(osmLaneIndex).add(connectivity);
            }
        }
        return destinations;
    }

    private String connectivityOSMValue(List<List<Connectivity>> destinations) {
        List<String> excerpts = newArrayList();
        boolean foundDestination = false;
        for (List<Connectivity> laneDestinations: destinations) {
            if (laneDestinations.size() > 0) {
                foundDestination = true;
            }
            excerpts.add(
                laneDestinations.stream().map(
                    connectivity -> connectivity.getDestinationSections().stream().map(
                        sectionId -> Long.toString(sectionId)
                    ).collect(Collectors.joining("_"))
                ).collect(Collectors.joining(";"))
            );
        }
        if (foundDestination) {
            return String.join("|", excerpts);
        }
        return null;
    }

    private enum Direction {
        forward("forward"), backward("backward");

        public final String osmValue;

        private Direction(String osmValue) {
            this.osmValue = osmValue;
        }

        public static Direction fromBoolean(boolean isForward) {
            return isForward ? forward : backward;
        }
    }
}
