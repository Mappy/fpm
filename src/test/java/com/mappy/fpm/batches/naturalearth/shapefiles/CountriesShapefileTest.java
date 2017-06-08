package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.geonames.*;
import com.mappy.fpm.batches.naturalearth.discarded.CountriesShapefile;
import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CountriesShapefileTest {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final Geonames geonames = mock(Geonames.class);
    private final CountriesShapefile shp = new CountriesShapefile(getClass().getResource("/naturalearth").getPath(), geonames);

    @Test
    public void should_serialize_countries() throws Exception {
        shp.serialize(serializer);

        assertThat(serializer.getPoints())
                .extracting(m -> m.get("name"))
                .containsOnly("Luxembourg", "Belgium", "Netherlands");
    }

    @Test
    public void should_add_french_names() throws Exception {
        when(geonames.frenchNames("NLD")).thenReturn(newArrayList(new AlternateName("Pays-Bas", false, false, false, false)));

        shp.serialize(serializer);

        assertThat(serializer.getPoints()).contains(ImmutableMap.of("name", "Netherlands", "place", "country", "name:fr", "Pays-Bas", "ISO3166-1:alpha3", "NLD"));
    }
}