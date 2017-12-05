package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Boundaries0A07ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___oa07.shp")).thenReturn("src/test/resources/tomtom/boundaries/oa07/aquitaine___________oa07.shp");


        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(12500007163496L)).thenReturn(of("name", "Poitiers", "name:en", "Potter"));

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("aquitaine", 6)).thenReturn("7");

        Boundaries0A07Shapefile shapefile = new Boundaries0A07Shapefile(tomtomFolder, nameProvider, osmLevelGenerator);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/oa07.osm.pbf"));
    }

    @Test
    public void should_have_relations_with_all_tags() {
        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).hasSize(7);
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("7");
        assertThat(tags).extracting(t -> t.get("type")).containsOnly("boundary");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Limoges", "Angoulême", "Tulle", "Guéret", "Poitiers", "Niort", "La Rochelle");
        assertThat(tags).extracting(t -> t.get("name:en")).containsOnly(null, null, null, null, "Potter", null, null);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("12500007107679", "12500007108056", "12500007212748", "12500007167661", "12500007163496", "12500007067720", "12500007212747");
    }


    @Test
    public void should_have_relations_with_tags_and_role_outer() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "outer".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(329);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Limoges", "Angoulême", "Tulle", "Guéret", "Poitiers", "Niort", "La Rochelle");
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("7");
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "label".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(7);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("12500007107679", "12500007108056", "12500007212748", "12500007167661", "12500007163496", "12500007067720", "12500007212747");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Limoges", "Angoulême", "Tulle", "Guéret", "Poitiers", "Niort", "La Rochelle");
        assertThat(tags).extracting(t -> t.get("name:en")).containsOnly(null, null, null, null, "Potter", null, null);
    }
}