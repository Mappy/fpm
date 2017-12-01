package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import net.morbz.osmonaut.osm.Relation;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildingShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("2dbd.shp")).thenReturn("src/test/resources/tomtom/building/2dbd.shp");

        BuildingShapefile shapefile = new BuildingShapefile(tomtomFolder);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/2dbd.osm.pbf"));
    }

    @Test
    public void should_generate_building_file() {

        assertThat(pbfContent.getRelations()).hasSize(12);
        assertThat(pbfContent.getWays()).hasSize(2140);

        Optional<Relation> optRelation = pbfContent.getRelations().stream().filter(relation -> relation.getTags().hasKeyValue("ref:tomtom", "57281214290")).findFirst();
        assertThat(optRelation.isPresent()).isTrue();
        assertThat(optRelation.get().getTags().get("building")).contains("school");
    }
}