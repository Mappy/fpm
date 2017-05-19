package com.mappy.data.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.data.batches.naturalearth.discarded.BoundaryLinesLandShapefile;
import com.mappy.data.utils.MemoryGeometrySerializer;

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