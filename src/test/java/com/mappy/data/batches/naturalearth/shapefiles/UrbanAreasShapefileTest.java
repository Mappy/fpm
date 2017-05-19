package com.mappy.data.batches.naturalearth.shapefiles;

import com.mappy.data.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class UrbanAreasShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final UrbanAreasShapefile shp = new UrbanAreasShapefile(getClass().getResource("/naturalearth").getPath());

    @Test
    public void should_serialize_airports() throws Exception {
        shp.serialize(serializer);

        assertThat(serializer.getPolygons()).filteredOn(t -> t.get("landuse").equals("residential")).hasSize(7);
    }
}