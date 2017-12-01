package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class LakesShapefileTest {

    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final LakesShapefile shp = new LakesShapefile(getClass().getResource("/naturalearth").getPath());

    @Test
    public void should_serialize_lakes() {
        shp.serialize(serializer);

        assertThat(serializer.getMultipolygons()).extracting(m -> m.get("natural")).containsOnly("water").hasSize(7);
    }

}