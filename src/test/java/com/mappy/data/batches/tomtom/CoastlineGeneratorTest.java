package com.mappy.data.batches.tomtom;

import com.mappy.data.batches.utils.OsmosisSerializer;
import com.vividsolutions.jts.geom.Polygon;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CoastlineGeneratorTest {

    private final OsmosisSerializer serializer = mock(OsmosisSerializer.class);

    @Test
    public void should_generate_coastline() throws Exception {

        new CoastlineGenerator("src/test/resources/tomtom/coastline", "src/test/resources", serializer).run();

        verify(serializer, times(3999)).write(any(Polygon.class), anyMapOf(String.class, String.class));
        verify(serializer).close();
    }
}