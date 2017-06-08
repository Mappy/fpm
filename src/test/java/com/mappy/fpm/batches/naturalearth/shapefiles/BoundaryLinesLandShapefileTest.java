package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.naturalearth.discarded.BoundaryLinesLandShapefile;
import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class BoundaryLinesLandShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final BoundaryLinesLandShapefile shp = new BoundaryLinesLandShapefile(getClass().getResource("/naturalearth").getPath());

    @SuppressWarnings("unchecked")
    @Test
    public void should_serialize_airports() throws Exception {
        shp.serialize(serializer);

        assertThat(serializer.getMultilinestrings())
                .containsOnly(ImmutableMap.of("boundary", "administrative", "admin_level", "2"))
                .hasSize(3);
    }
}