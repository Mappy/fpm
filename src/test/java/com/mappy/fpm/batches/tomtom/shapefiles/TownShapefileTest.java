package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import net.morbz.osmonaut.osm.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TownShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("sm.shp")).thenReturn("src/test/resources/tomtom/town/Anderlecht___________sm.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateCityNames(356376363L)) //
                .thenReturn(of("name", "Hamlet", "name:nl", "gehuchten", "name:fr", "hameaux"));
        when(nameProvider.getAlternateCityNames(10560000407632L)) //
                .thenReturn(of("name", "Scherdemaal", "name:nl", "neighbourhood", "name:fr", "quartier"));

        TownShapefile shapefile = new TownShapefile(tomtomFolder, nameProvider);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/sm.osm.pbf"));
    }

    @Test
    public void should_load_neighbourhood_as_point_with_all_tags() {
        Node hamlet = pbfContent.getNodes().stream().filter(node -> node.getTags().hasKeyValue("place", "neighbourhood")).findFirst().get();

        assertThat(hamlet.getTags().get("name")).isEqualTo("Scherdemaal");
        assertThat(hamlet.getTags().get("name:fr")).isEqualTo("quartier");
        assertThat(hamlet.getTags().get("name:nl")).isEqualTo("neighbourhood");
    }

    @Test
    public void should_load_hamlet_as_point_with_all_tags() {
        Node hamlet = pbfContent.getNodes().stream().filter(node -> node.getTags().hasKeyValue("place", "hamlet")).findFirst().get();

        assertThat(hamlet.getTags().get("name")).isEqualTo("Hamlet");
        assertThat(hamlet.getTags().get("name:fr")).isEqualTo("hameaux");
        assertThat(hamlet.getTags().get("name:nl")).isEqualTo("gehuchten");
    }
}