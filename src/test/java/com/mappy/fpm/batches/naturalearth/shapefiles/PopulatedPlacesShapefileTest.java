package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.geonames.*;
import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PopulatedPlacesShapefileTest {
    private final Geonames geonames = mock(Geonames.class);
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final PopulatedPlacesShapefile shp = new PopulatedPlacesShapefile(getClass().getResource("/naturalearth").getPath(), geonames);

    @Test
    public void should_serialize_place() {
        shp.serialize(serializer);

        assertThat(serializer.getPoints())
                .filteredOn(m -> "city".equals(m.get("place")) || "town".equals(m.get("place")))
                .extracting(m -> m.get("name"))
                .containsOnly("Dieppe", "Le Mans", "Rouen", "Caen", "Le Havre", "Paris", "Versailles", "Hania");
    }

    @Test
    public void should_serialize_capital() {
        shp.serialize(serializer);

        assertThat(serializer.getPoints())
                .filteredOn(m -> "yes".equals(m.get("capital")))
                .extracting(m -> m.get("name"))
                .containsOnly("Paris");
    }

    @Test
    public void should_add_geonames_names() {
        when(geonames.frenchNames(260114)).thenReturn(newArrayList(new AlternateName("La Canée", false, false, false, false)));

        shp.serialize(serializer);

        assertThat(serializer.getPoints()).contains(ImmutableMap.of("name", "Hania", "name:fr", "La Canée", "place", "town", "population", "78728"));
    }
}