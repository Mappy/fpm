package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import net.morbz.osmonaut.osm.Node;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

public class TownShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;
    private GeometrySerializer serializer;

    @Before
    public void setup() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("sm.shp")).thenReturn("src/test/resources/tomtom/town/Anderlecht___________sm.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateCityNames(356376363L)) //
                .thenReturn(of("name", "Hamlet", "name:nl", "gehuchten", "name:fr", "hameaux"));
        when(nameProvider.getAlternateCityNames(10560000407632L)) //
                .thenReturn(of("name", "Scherdemaal", "name:nl", "neighbourhood", "name:fr", "quartier"));

        TownShapefile townShapefile = new TownShapefile(tomtomFolder, nameProvider);
        serializer = Mockito.spy(new OsmosisSerializer("target/tests/AnderlechtSM.osm.pbf", "Test_TU"));

        townShapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/AnderlechtSM.osm.pbf"));
    }

    @Test
    public void should_not_serialize_admin_class_lower_than_10() {
        verify(serializer, times(2)).writePoint(any(), anyMap());
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