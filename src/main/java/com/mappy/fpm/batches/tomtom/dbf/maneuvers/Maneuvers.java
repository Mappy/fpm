package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
public class Maneuvers {
    private final List<Restriction> restrictions = Lists.newArrayList();
    private final Set<Long> restrictionRoadIds = Sets.newHashSet();

    @Inject
    public Maneuvers(MpDbf mpDbf, MnShapefile mnShapefile) {
        load(mpDbf.paths(), mnShapefile.maneuvers());
    }

    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public Set<Long> getRestrictionRoadIds() {
        return restrictionRoadIds;
    }

    private void load(List<ManeuverPath> maneuverPaths, List<Maneuver> maneuvers) {
        Map<Long, List<ManeuverPath>> mpById = maneuverPaths.stream().sorted(Comparator.comparing(ManeuverPath::getSeqnr)).collect(groupingBy(ManeuverPath::getId));
        for (Maneuver maneuver : maneuvers) {
            List<ManeuverPath> path = mpById.get(maneuver.getId());
            List<Long> segments = path.stream().map(ManeuverPath::getTrpelId).collect(toList());
            checkState(segments.size() > 1);
            restrictionRoadIds.addAll(segments);
            restrictions.add(new Restriction(segments, maneuver.getJunctionId()));
        }
        log.info("{} maneuvers found", restrictions.size());
    }
}
