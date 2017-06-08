package com.mappy.fpm.batches.merge.pbf;

import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityMergeResultTest {

    @Test
    public void should_merge_bounds() {
        EntityMergeResult result = new EntityMergeResult(header(20170200, new Tag("k1", "v1")), header(20170210, new Tag("k2", "v2")), null, false).merge();

        assertThat(toMap(result.getResult().getEntity().getTags())).containsOnly(entry("k1", "v1"), entry("k2", "v2"));
        assertThat(result.isMerged()).isTrue();
    }

    @Test
    public void should_merge_tags_on_nodes_with_same_ids() {
        EntityMergeResult result = new EntityMergeResult(node(20170200, new Tag("k1", "v1")), node(20170200, new Tag("k2", "v2")), null, false).merge();

        assertThat(toMap(result.getResult().getEntity().getTags())).containsOnly(entry("k1", "v1"), entry("k2", "v2"));
        assertThat(result.isMerged()).isTrue();
    }

    @Test
    public void should_not_merge_tags_on_nodes_with_same_ids() {
        EntityMergeResult result = new EntityMergeResult(node(20170200, new Tag("k1", "v1")), node(20170201, new Tag("k2", "v2")), null, false).merge();

        assertThat(toMap(result.getResult().getEntity().getTags())).contains(entry("k1", "v1"));
        assertThat(result.isMerged()).isFalse();
    }

    private Map<String, String> toMap(Collection<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue()));
    }

    private EntityContainer header(int id, Tag... tags) {
        return container(withData(id, newArrayList(tags), new Bound("")));
    }

    private EntityContainer node(int id, Tag... tags) {
        return container(withData(id, newArrayList(tags), new Node(new CommonEntityData(id, id, (Date) null, null, id), 0.0, 0.0)));
    }

    private EntityContainer container(Entity entity) {
        EntityContainer container = mock(EntityContainer.class);
        when(container.getEntity()).thenReturn(entity);
        return container;
    }

    private Entity withData(int version, Collection<Tag> tags, Entity node) {
        node.setId(version);
        node.getTags().addAll(tags);
        return node;
    }
}
