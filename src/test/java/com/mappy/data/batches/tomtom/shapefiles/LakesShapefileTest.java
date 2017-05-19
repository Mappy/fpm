package com.mappy.data.batches.tomtom.shapefiles;

import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class LakesShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final HighQualityLanduseShapefile shp = new HighQualityLanduseShapefile(new TomtomFolder(getClass().getResource("/osmgenerator/").getPath(), "citylights"));

    @Test
    public void should_serialize_lakes() throws Exception {
        shp.serialize(serializer);

        assertThat(serializer.getMultipolygons()).extracting(m -> m.get("landuse")).containsOnly("grass").hasSize(10);
    }

}