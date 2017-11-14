package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
@Getter
public class Maneuvers {

    private final List<Restriction> restrictions = newArrayList();
    private final Set<Long> restrictionRoadIds = newHashSet();

    @Inject
    public Maneuvers(MpDbf mpDbf, MnDbf mnDbf) {
        Map<Long, List<ManeuverPath>> mpById = mpDbf.paths().stream().sorted(Comparator.comparing(ManeuverPath::getSeqnr)).collect(groupingBy(ManeuverPath::getId));
        for (Maneuver maneuver : mnDbf.maneuvers()) {
            List<ManeuverPath> path = mpById.get(maneuver.getId());
            List<Long> segments = path.stream().map(ManeuverPath::getTrpelId).collect(toList());
            checkState(segments.size() > 1);
            restrictionRoadIds.addAll(segments);
            restrictions.add(new Restriction(segments, maneuver.getJunctionId()));
        }
        log.info("{} maneuvers found", restrictions.size());
    }
}
