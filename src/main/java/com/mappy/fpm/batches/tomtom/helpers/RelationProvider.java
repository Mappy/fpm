package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.utils.Feature;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Singleton
public class RelationProvider {

    private final Map<Long, RelationMemberTags> relationMap = new HashMap<>();

    public void putRelation(Feature feature, List<RelationMember> relationMembers) {
        relationMap.put(feature.getLong("CITYCENTER"), new RelationMemberTags().withPopulation(ofNullable(valueOf(feature.getLong("POP")))).withName(feature.getString("NAME")).withRelationMembers(relationMembers));
    }

    public Optional<RelationMemberTags> getMembers(Long id) {
        return ofNullable(relationMap.get(id));
    }

    public Optional<String> getPop(Long id) {
        return relationMap.get(id) == null ? empty() : relationMap.get(id).getPopulation();
    }


    @Data
    @Wither
    @RequiredArgsConstructor
    public class RelationMemberTags {
        private final String name;
        private final Optional<String> population;
        private final List<RelationMember> relationMembers;

        public RelationMemberTags() {
            this(null, empty(), null);
        }
    }
}
