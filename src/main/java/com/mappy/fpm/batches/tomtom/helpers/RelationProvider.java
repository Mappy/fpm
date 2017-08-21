package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.utils.Feature;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Singleton
public class RelationProvider {

    private final Map<Long, List<RelationMember>> relationMap = new HashMap<>();

    public void putRelation(Feature feature, List<RelationMember> relationMembers) {
        relationMap.put(feature.getLong("CITYCENTER"), relationMembers);
    }

    public Optional<List<RelationMember>> getMembers(Long id) {
        return ofNullable(relationMap.get(id));
    }
}
