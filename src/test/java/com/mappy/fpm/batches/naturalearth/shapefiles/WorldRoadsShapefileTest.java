package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static com.mappy.fpm.utils.MemoryFeature.*;
import static org.assertj.core.api.Assertions.*;

public class WorldRoadsShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final WorldRoadsShapefile shp = new WorldRoadsShapefile(getClass().getResource("/naturalearth").getPath());

    @Test
    public void should_serialize_roads() {
        shp.serialize(serializer);

        assertThat(serializer.getMultilinestrings()).extracting(m -> m.get("highway")).containsOnly("motorway", "trunk", "unclassified");
    }

    @Test
    public void should_serialize_ferry() {
        shp.serialize(serializer, multilinestring(ImmutableMap.of("type", "Ferry Route"), 42.0, 3.0, 42.1, 3.1));

        assertThat(serializer.getMultilinestrings().get(0)).containsEntry("route", "ferry");
    }

    @Test
    public void should_discard_north_american_roads() {
        shp.serialize(serializer, multilinestring(ImmutableMap.of("type", "Road", "sov_a3", "USA"), 42.0, 3.0, 42.1, 3.1));

        assertThat(serializer.getMultilinestrings()).isEmpty();
    }
}