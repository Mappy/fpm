package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.ImmutableMap;
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

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LcShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("lc.shp")).thenReturn("src/test/resources/tomtom/landcover/bel_lc.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(anyLong())).thenReturn(ImmutableMap.of("name:nl", "Belle-Vuebos"));

        LcShapefile shapefile = new LcShapefile(tomtomFolder, nameProvider);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/bel_lc.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/bel_lc.osm.pbf"));
    }

    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Optional<Relation> optRelation = pbfContent.getRelations().stream().filter(relation -> relation.getTags().hasKeyValue("name", "Belle-Vuebos")).findFirst();
        assertThat(optRelation.isPresent()).isTrue();

        Tags tags = optRelation.get().getTags();
        assertThat(tags.get("name:nl")).contains("Belle-Vuebos");
    }
}