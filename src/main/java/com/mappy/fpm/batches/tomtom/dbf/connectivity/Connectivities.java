package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Aggregate connectivity path and connectivity information data to be easier to manipulate
 */
@Slf4j
@Singleton
public class Connectivities {

    private final ArrayListMultimap<Long, Connectivity> sectionConnectivities = ArrayListMultimap.create();

    @Inject
    public Connectivities(LnDbf lnDbf, LpDbf lpDbf) {
        buildConnectivityItems(lnDbf, lpDbf);
        log.info("{} connectivities found", sectionConnectivities.size());
    }

    public List<Connectivity> getConnectivitiesStartingAtSection(long originSectionId) {
        return sectionConnectivities.get(originSectionId);
    }

    private void buildConnectivityItems(LnDbf lnDbf, LpDbf lpDbf) {
        ArrayListMultimap<Long, ConnectivityPath> connectivityPathMapping = lpDbf.getConnectivityPaths();
        for (long connectivityId: connectivityPathMapping.keySet()) {
            List<ConnectivityPath> connectivityPaths = connectivityPathMapping.get(connectivityId);
            if (!lnDbf.containsKey(connectivityId)) {
                String message = "Connectivity path with no info associated: %d";
                throw new RuntimeException(String.format(message, connectivityId));
            }
            List<ConnectivityInformation> connectivityInfoList = lnDbf.get(connectivityId);
            long junctionId = connectivityInfoList.get(0).getJunctionId();
            ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
            for (ConnectivityInformation info: connectivityInfoList) {
                laneMapping.put(info.getOriginLane(), info.getDestinationLane());
            }

            long originSectionId = -1;
            List<Long> destinations = newArrayList();
            for (ConnectivityPath path: connectivityPaths) {
                if (path.getSequenceNumber() == 1) {
                    originSectionId = path.getSectionId();
                } else {
                    int destinationIndex = path.getSequenceNumber() - 2;
                    while (destinations.size() < destinationIndex + 1) {
                        destinations.add(-1L);
                    }
                    destinations.set(destinationIndex, path.getSectionId());
                }
            }
            if (originSectionId == -1) {
                String message = "Could not find origin section for connectivity %d";
                throw new RuntimeException(String.format(message, connectivityId));
            }
            if (destinations.size() == 0 || destinations.stream().anyMatch(sectionId -> sectionId == -1)) {
                String message = "Could not find all destination sections for connectivity %d";
                throw new RuntimeException(String.format(message, connectivityId));
            }

            Connectivity connectivity = new Connectivity(connectivityId, junctionId, originSectionId, destinations, laneMapping);
            sectionConnectivities.put(originSectionId, connectivity);
        }
    }
}
