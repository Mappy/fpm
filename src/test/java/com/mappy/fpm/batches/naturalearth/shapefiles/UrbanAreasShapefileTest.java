package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class UrbanAreasShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final UrbanAreasShapefile shp = new UrbanAreasShapefile(getClass().getResource("/naturalearth").getPath());

    @Test
    public void should_serialize_airports() {
        shp.serialize(serializer);

        assertThat(serializer.getPolygons()).filteredOn(t -> t.get("landuse").equals("residential")).hasSize(7);
    }
}