package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LuShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("lu.shp")).thenReturn("src/test/resources/tomtom/landuse/lu.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(10560001002272L)).thenReturn(of("name:nl", "Universitair Kinderziekenhuis", "name:fr", "Hôpital Universitaire Des Enfants"));

        LuShapefile shapefile = new LuShapefile(tomtomFolder, nameProvider);

        verify(nameProvider).loadFromFile("lxnm.dbf", "NAME", false);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/lu.osm.pbf"));
    }

    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "10560001002272")).findFirst();
        assertThat(optWay.isPresent()).isTrue();

        Tags tags = optWay.get().getTags();
        assertThat(tags.get("amenity")).isEqualTo("hospital");
        assertThat(tags.get("name")).isEqualTo("Universitair Kinderziekenhuis Koningin Fabiola");
        assertThat(tags.get("name:nl")).isEqualTo("Universitair Kinderziekenhuis");
        assertThat(tags.get("name:fr")).isEqualTo("Hôpital Universitaire Des Enfants");
    }

    @Test
    public void should_tag_national_park_according_to_type(){
        Optional<Way> park = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "10560001001680")).findFirst();
        assertThat(park.isPresent()).isTrue();
        assertThat(park.get().getTags().get("landuse")).isEqualTo("grass");

        park = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "10560001000884")).findFirst();
        assertThat(park.isPresent()).isTrue();
        assertThat(park.get().getTags().get("boundary")).isEqualTo("protected_area");
    }

    @Test
    public void should_tag_airport() {
        Optional<Way> airport = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "10560001002409")).findFirst();
        assertThat(airport.isPresent()).isTrue();
        assertThat(airport.get().getTags().get("aeroway")).isEqualTo("aerodrome");
    }
}