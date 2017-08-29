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

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Singleton
public class RelationProvider {

    private final Map<Long, RelationMemberTags> relationMap = new HashMap<>();

    public void putRelation(Feature feature, List<RelationMember> relationMembers, Map<String, String> tags) {
        relationMap.put(feature.getLong("CITYCENTER"), new RelationMemberTags().withTags(tags).withRelationMembers(relationMembers));
    }

    public Optional<RelationMemberTags> getMembers(Long id) {
        return ofNullable(relationMap.get(id));
    }

    public Optional<String> getPop(Long id) {
        return relationMap.get(id) == null ? empty() : ofNullable(relationMap.get(id).getTags().get("population"));
    }


    @Data
    @Wither
    @RequiredArgsConstructor
    public class RelationMemberTags {
        private final Map<String, String> tags;
        private final List<RelationMember> relationMembers;

        public RelationMemberTags() {
            this(newHashMap(), null);
        }
    }
}
