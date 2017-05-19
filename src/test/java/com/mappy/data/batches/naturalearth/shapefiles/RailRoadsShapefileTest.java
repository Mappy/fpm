package com.mappy.data.batches.naturalearth.shapefiles;

import com.mappy.data.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class RailRoadsShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final RailRoadsShapefile shp = new RailRoadsShapefile(getClass().getResource("/naturalearth").getPath());

    @Test
    public void should_serialize_roads() throws Exception {
        shp.serialize(serializer);

        assertThat(serializer.getMultilinestrings()).extracting(m -> m.get("railway")).containsOnly("rail");
    }
}