package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;
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

public class BoundariesA1ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a1.shp")).thenReturn("src/test/resources/tomtom/boundaries/a1/belgium______________a1.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(10560000000843L)).thenReturn(of("name", "Brussel", "name:fr", "Bruxelles"));

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("belgium", "1")).thenReturn("4");

        BoundariesA1Shapefile shapefile = new BoundariesA1Shapefile(tomtomFolder, nameProvider, osmLevelGenerator);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/a1.osm.pbf"));
    }

    @Test
    public void should_have_members_with_tags() throws Exception {

        Relation wallonie = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000851")).findFirst().get();
        assertThat(wallonie.getTags().size()).isEqualTo(7);
        assertThat(wallonie.getTags().get("name")).isEqualTo("Région Wallonne");
        assertThat(wallonie.getTags().get("boundary")).isEqualTo("administrative");
        assertThat(wallonie.getTags().get("ref:INSEE")).isEqualTo("03000");
        assertThat(wallonie.getTags().get("type")).isEqualTo("boundary");
        assertThat(wallonie.getTags().get("admin_level")).isEqualTo("4");
        assertThat(wallonie.getTags().get("layer")).isEqualTo("4");

        Relation brussel = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000843")).findFirst().get();
        assertThat(brussel.getTags().size()).isEqualTo(8);
        assertThat(brussel.getTags().get("name")).isEqualTo("Brussel Hoofdstedelijk Gewest");
        assertThat(brussel.getTags().get("name:fr")).isEqualTo("Bruxelles");
        assertThat(brussel.getTags().get("boundary")).isEqualTo("administrative");
        assertThat(brussel.getTags().get("ref:INSEE")).isEqualTo("04000");
        assertThat(brussel.getTags().get("type")).isEqualTo("boundary");
        assertThat(brussel.getTags().get("admin_level")).isEqualTo("4");
        assertThat(brussel.getTags().get("layer")).isEqualTo("4");

        Relation vlaamsGewest = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000849")).findFirst().get();
        assertThat(vlaamsGewest.getTags().size()).isEqualTo(7);
        assertThat(vlaamsGewest.getTags().get("name")).isEqualTo("Vlaams Gewest");
        assertThat(vlaamsGewest.getTags().get("boundary")).isEqualTo("administrative");
        assertThat(vlaamsGewest.getTags().get("ref:INSEE")).isEqualTo("02000");
        assertThat(vlaamsGewest.getTags().get("type")).isEqualTo("boundary");
        assertThat(vlaamsGewest.getTags().get("admin_level")).isEqualTo("4");
        assertThat(vlaamsGewest.getTags().get("layer")).isEqualTo("4");
    }

    @Test
    public void should_have_relation_with_role_label_and_tags() throws Exception {
        List<RelationMember> labels = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals("label"))//
                .collect(toList());

        assertThat(labels).hasSize(3);

        RelationMember wallonie = labels.stream().filter(member -> member.getEntity().getTags().hasKeyValue("ref:tomtom", "10560000000851")).findFirst().get();
        assertThat(wallonie.getEntity().getTags().size()).isEqualTo(3);
        assertThat(wallonie.getEntity().getTags().get("name")).isEqualTo("Région Wallonne");
        assertThat(wallonie.getEntity().getTags().get("ref:INSEE")).isEqualTo("03000");

        RelationMember brussel = labels.stream().filter(member -> member.getEntity().getTags().hasKeyValue("ref:tomtom", "10560000000843")).findFirst().get();
        assertThat(brussel.getEntity().getTags().size()).isEqualTo(4);
        assertThat(brussel.getEntity().getTags().get("name")).isEqualTo("Brussel Hoofdstedelijk Gewest");
        assertThat(brussel.getEntity().getTags().get("name:fr")).isEqualTo("Bruxelles");
        assertThat(brussel.getEntity().getTags().get("ref:INSEE")).isEqualTo("04000");

        RelationMember vlaamsGewest = labels.stream().filter(member -> member.getEntity().getTags().hasKeyValue("ref:tomtom", "10560000000849")).findFirst().get();
        assertThat(vlaamsGewest.getEntity().getTags().size()).isEqualTo(3);
        assertThat(vlaamsGewest.getEntity().getTags().get("name")).isEqualTo("Vlaams Gewest");
        assertThat(vlaamsGewest.getEntity().getTags().get("ref:INSEE")).isEqualTo("02000");
    }

    @Test
    public void should_have_inner_boundaries_in_vlaams_gewest() throws Exception {
        Relation vlaamsGewest = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000849")).findFirst().get();
        assertThat(vlaamsGewest.getMembers().stream().filter(relationMember -> relationMember.getRole().equals("inner")).collect(toList())).isNotEmpty();
    }

    @Test
    public void should_have_some_outer_boundaries_in_all_3_relations() throws Exception {
        Relation wallonie = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000851")).findFirst().get();
        assertThat(wallonie.getMembers().stream().filter(relationMember -> relationMember.getRole().equals("outer")).collect(toList())).isNotEmpty();

        Relation brussel = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000843")).findFirst().get();
        assertThat(brussel.getMembers().stream().filter(relationMember -> relationMember.getRole().equals("outer")).collect(toList())).isNotEmpty();

        Relation vlaamsGewest = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000849")).findFirst().get();
        assertThat(vlaamsGewest.getMembers().stream().filter(relationMember -> relationMember.getRole().equals("outer")).collect(toList())).isNotEmpty();
    }
}