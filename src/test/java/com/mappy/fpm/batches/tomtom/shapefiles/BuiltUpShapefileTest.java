package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.tomtom.helpers.Centroid;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.RelationMember;
import net.morbz.osmonaut.osm.Tags;
import org.geotools.geometry.jts.LiteCoordinateSequence;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuiltUpShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {
        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(12500001063055L)).thenReturn(newHashMap(of("name:fr", "Rougnat_fr")));
        when(nameProvider.getAlternateNames(12500001060481L)).thenReturn(newHashMap(of("name:fr", "Auzances_fr")));
        when(nameProvider.getAlternateNames(12500001067545L)).thenReturn(newHashMap(of("name:fr", "La Chaux-Bourdue_fr")));
        when(nameProvider.getAlternateNames(112500001060737L)).thenReturn(newHashMap(of("name:fr", "Le Montely_fr")));

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("bu.shp")).thenReturn("src/test/resources/tomtom/boundaries/bu/rougnat___________bu.shp");

        TownTagger townTagger = mock(TownTagger.class);
        GeometryFactory factory = mock(GeometryFactory.class);

        Point point = new Point(new LiteCoordinateSequence(new double[]{2.5027452, 46.0514552}, 2), factory);
        when(townTagger.getBuiltUpCentroid(12500001063055L)).thenReturn(new Centroid(12500001063055L, "Rougnat", null, 8, 32, 7, point));

        Point point2 = new Point(new LiteCoordinateSequence(new double[]{4.601984, 51.181340}, 2), factory);
        when(townTagger.getBuiltUpCentroid(12500001060481L)).thenReturn(new Centroid(12500001060481L, "Auzances", "456", 8, 32, 8, point2));

        Point point3 = new Point(new LiteCoordinateSequence(new double[]{4.606374, 51.162370}, 2), factory);
        when(townTagger.getBuiltUpCentroid(12500001067545L)).thenReturn(new Centroid(12500001067545L, "La Chaux-Bourdue", "123", 8, 32, 8, point3));

        Point point4 = new Point(new LiteCoordinateSequence(new double[]{4.596975, 51.210989}, 2), factory);
        when(townTagger.getBuiltUpCentroid(12500001060737L)).thenReturn(new Centroid(112500001060737L, "Le Montely", "1011", 8, 32, 8, point4));

        BuiltUpShapefile shapefile = new BuiltUpShapefile(tomtomFolder, nameProvider, townTagger);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/bu.osm.pbf"));
        assertThat(pbfContent.getRelations()).hasSize(4);
    }

    @Test
    public void should_add_city_center_on_polygon() {
        List<RelationMember> members = pbfContent.getRelations().stream()
                .flatMap(r -> r.getMembers().stream())
                .filter(m -> "admin_centre".equals(m.getRole()))
                .collect(toList());

        assertThat(members).hasSize(4);
        List<Tags> collect = members.stream().map(m -> m.getEntity().getTags()).collect(toList());
        assertThat(collect).extracting(t -> t.get("addr:postcode")).containsOnly(null, "456", "123", "1011");
        assertThat(collect).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
        assertThat(collect).extracting(t -> t.get("name:fr")).containsOnly("Rougnat_fr", "Auzances_fr", "La Chaux-Bourdue_fr", "Le Montely_fr");
    }

    @Test
    public void should_have_relations_with_all_tags() {
        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("landuse")).containsOnly("residential");
        assertThat(tags).extracting(t -> t.get("type")).containsOnly("multipolygon");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("12500001063055", "12500001060481", "12500001067545", "12500001060737");
    }

    @Test
    public void should_have_relations_with_tags_and_role_outer() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "outer".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
        assertThat(tags).extracting(t -> t.get("place")).containsOnly("hamlet");
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "label".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("12500001063055", "12500001060481", "12500001067545", "12500001060737");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
    }
}