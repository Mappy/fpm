package com.mappy.data.batches.naturalearth.shapefiles;

import com.mappy.data.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class OceansLabelsShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final OceansLabelsShapefile shp = new OceansLabelsShapefile(getClass().getResource("/naturalearth").getPath());

    @Test
    public void should_serialize_ocean_labels() throws Exception {
        shp.serialize(serializer);

        assertThat(serializer.getPoints()).extracting(m -> m.get("place")).contains("ocean");
        assertThat(serializer.getPoints()).extracting(m -> m.get("place")).contains("sea");
        assertThat(serializer.getPoints()).extracting(m -> m.get("place")).contains("locality");

    }
}