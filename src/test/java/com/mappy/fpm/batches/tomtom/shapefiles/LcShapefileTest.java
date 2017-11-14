package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LcShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("lc.shp")).thenReturn("src/test/resources/tomtom/landcover/lc.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(10560001002712L)).thenReturn(of("name:nl", "Belle"));

        LcShapefile shapefile = new LcShapefile(tomtomFolder, nameProvider);

        verify(nameProvider).loadFromFile("lxnm.dbf", "NAME", false);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/lc.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/lc.osm.pbf"));
    }

    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Optional<Relation> optRelation = pbfContent.getRelations().stream().filter(relation -> relation.getTags().hasKeyValue("ref:tomtom", "10560001002712")).findFirst();
        assertThat(optRelation.isPresent()).isTrue();

        Tags tags = optRelation.get().getTags();
        assertThat(tags.get("landuse")).contains("forest");
        assertThat(tags.get("name")).contains("Belle-Vuebos");
        assertThat(tags.get("name:nl")).contains("Belle");
    }
}