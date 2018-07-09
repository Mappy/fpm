package com.mappy.fpm.batches;

import com.mappy.fpm.batches.merge.pbf.OsmMerger;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import net.morbz.osmonaut.osm.RelationMember;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.CountryWrapper.ALL_COUNTRIES;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class GenerateFullPbfTest {

    private final OsmMerger osmMerger = Mockito.spy(new OsmMerger());
    private final GenerateFullPbf generateFullPbf = new GenerateFullPbf(osmMerger, "src/test/resources/generateFullPbf", "target/tests", "Europe.osm.pbf", 1);

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_country_is_unknown() {
        generateFullPbf.run(newArrayList("fakeCountry"));
    }

    @Test
    public void should_get_valid_countries(){
        assertThat(GenerateFullPbf.checkAndValidCountries("Albania,Andorra"))
                .containsExactly("Albania" , "Andorra") ;
    }

    @Test
    public void should_get_all_valid_countries_when_input_country_list_is_empty(){
        assertThat(GenerateFullPbf.checkAndValidCountries(" "))
                .containsAll(ALL_COUNTRIES);
    }

    @Test
    public void should_throw_IllegalArgumentException_when_invalid_countries(){
        assertThatThrownBy(() -> GenerateFullPbf.checkAndValidCountries("invalid_country,Albania,Andorra"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid countries : invalid_country" );
    }

    @Test
    public void should_generate_Andorra() throws Exception {

        generateFullPbf.run(newArrayList("Andorre"));

        assertThat(new File("target/tests/Andorre/pbfFiles/and.osm.pbf").exists()).isTrue();
        assertThat(new File("target/tests/Andorre/pbfFiles/andand.osm.pbf").exists()).isTrue();
        assertThat(new File("target/tests/Andorre/Andorre.osm.pbf").exists()).isTrue();

        verify(osmMerger).merge(anyListOf(String.class), eq("target/tests/Andorre/Andorre.osm.pbf"));
        verify(osmMerger).merge(anyListOf(String.class), eq("target/tests/Europe.osm.pbf"));

        PbfContent pbfContent = read(new File("target/tests/Europe.osm.pbf"));

        assertAdminLevelWithNameFrIsPresent(pbfContent, "2");

        assertAdminLevelWithNameFrIsPresent(pbfContent, "8");

        assertRelationsWithRoleIsNotEmpty(pbfContent, "outer");

        assertRelationsWithRoleIsNotEmpty(pbfContent, "label");

        assertRelationsWithRoleIsNotEmpty(pbfContent, "admin_centre");
    }

    private void assertAdminLevelWithNameFrIsPresent(PbfContent pbfContent, String level) {
        Optional<RelationMember> admin_level = pbfContent.getRelations().stream()
                .filter(r -> r.getTags().hasKeyValue("admin_level", level))
                .flatMap(r -> r.getMembers().stream())
                .filter(r -> r.getEntity().getTags().hasKey("name:fr")).findFirst();

        assertThat(admin_level.isPresent()).isTrue();
    }

    private void assertRelationsWithRoleIsNotEmpty(PbfContent pbfContent, String admin_center) {
        List<RelationMember> adminCenter = pbfContent.getRelations().stream()
                .filter(r -> r.getTags().hasKeyValue("ref:tomtom", "10200000000008"))
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals(admin_center)) //
                .collect(toList());
        assertThat(adminCenter).isNotEmpty();
    }
}