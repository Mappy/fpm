package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import it.unimi.dsi.fastutil.longs.Long2LongAVLTreeMap;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.helpers.RoadTagger.isReversed;
import static java.util.stream.Collectors.joining;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

@Slf4j
@Singleton
public class RestrictionsAccumulator {

    private final Maneuvers maneuvers;
    private final Map<Long, Long> wayByTomtomId = new Long2LongAVLTreeMap();
    private final Map<Long, Long> nodeByJunctionId = new Long2LongAVLTreeMap();
    private int viaCounter = 0;

    @Inject
    public RestrictionsAccumulator(Maneuvers maneuvers) {
        this.maneuvers = maneuvers;
    }

    public void register(Feature feature, Way way) {
        long tomtomId = feature.getLong("ID");
        if (maneuvers.getRestrictionRoadIds().contains(tomtomId)) {
            boolean reversed = isReversed(feature);
            Long from = feature.getLong("F_JNCTID");
            Long to = feature.getLong("T_JNCTID");
            wayByTomtomId.put(tomtomId, way.getId());
            long firstNode = way.getWayNodes().get(0).getNodeId();
            long lastNode = way.getWayNodes().get(way.getWayNodes().size() - 1).getNodeId();
            add(reversed ? to : from, firstNode);
            add(reversed ? from : to, lastNode);
        }
    }

    public void complete(GeometrySerializer serializer) {
        int count = 0;
        int ignored = 0;
        for (Restriction restriction : maneuvers.getRestrictions()) {
            try {
                serializer.write(members(restriction), tomtomMembers(restriction));
            }
            catch (IllegalArgumentException e) {
                log.error("Ignoring {}: {}", restriction, e);
                ignored++;
            }
            count++;
        }
        if (count > 0) {
            log.info("{} restrictions written. Ignored {} ({}%)", count, ignored, 100.0 * ignored / count);
        }
        log.info("{} restrictions with more than 2 segments ({}%)", viaCounter, 100.0 * viaCounter / count);
    }

    private void add(Long junctionId, long nodeId) {
        if (nodeByJunctionId.containsKey(junctionId)) {
            checkState(nodeByJunctionId.get(junctionId).equals(nodeId), "Trying to override nodeId of a junctionId by another nodeId");
        }
        else {
            nodeByJunctionId.put(junctionId, nodeId);
        }
    }

    private List<RelationMember> members(Restriction restriction) {
        List<Long> segments = restriction.getSegments();
        checkSegment(restriction, segments);

        List<RelationMember> members = newArrayList();
        members.add(new RelationMember(wayByTomtomId.get(segments.get(0)), Way, "from"));
        List<Long> viaWays = segments.subList(1, segments.size() - 1);
        if (viaWays.isEmpty()) {
            members.add(new RelationMember(nodeByJunctionId.get(restriction.getJunctionId()), Node, "via"));
        } else {
            viaCounter++;
            for (Long segment : viaWays) {
                members.add(new RelationMember(wayByTomtomId.get(segment), Way, "via"));
            }
        }
        members.add(new RelationMember(wayByTomtomId.get(segments.get(segments.size() - 1)), Way, "to"));
        return members;
    }

    private static Map<String, String> tomtomMembers(Restriction restriction) {
        Map<String, String> tags = newHashMap(ImmutableMap.<String, String> builder().put("type", "restriction").put("restriction", "no_left_turn").build());
        List<Long> segments = restriction.getSegments();

        tags.put("from:tomtom", segments.get(0).toString());

        List<Long> viaWays = segments.subList(1, segments.size() - 1);
        if (viaWays.isEmpty()) {
            tags.put("via:tomtom", restriction.getJunctionId().toString());
        } else {
            tags.put("vias:tomtom", viaWays.stream().map(Object::toString).collect(joining("-")));
        }
        tags.put("to:tomtom", segments.get(segments.size() - 1).toString());
        return tags;
    }

    private void checkSegment(Restriction restriction, List<Long> segments) {
        for (Long segment : segments) {
            checkArgument(wayByTomtomId.containsKey(segment), "Restriction: from way " + segment + "  not found");
        }
        checkArgument(nodeByJunctionId.containsKey(restriction.getJunctionId()), "Restriction: via node  " + restriction.getJunctionId() + "  not found");
    }
}