package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TownTaggerTest extends AbstractTest {

    private TownTagger townTagger;

    @Before
    public void setup() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("sm.shp")).thenReturn("src/test/resources/tomtom/town/Anderlecht___________sm.shp");

        townTagger = new TownTagger(tomtomFolder);
    }

    @Test
    public void should_load_centroids() {

        Centroid centroid = townTagger.getCityCentroid(10560000388234L);
        assertCentroid(centroid, 10560000388234L, "Sint-Gillis", 8, 8, new Double[]{4.3451859, 50.8251293});

        centroid = townTagger.getCityCentroid(10560000455427L);
        assertCentroid(centroid, 10560000455427L, "Vorst", 9, 8, new Double[]{4.3134424, 50.8055758});

        centroid = townTagger.getCityCentroid(10560000718742L);
        assertCentroid(centroid, 10560000718742L, "Anderlecht", 8, 7, new Double[]{4.307077, 50.8366041});
    }

    @Test
    public void should_load_centroids_when_secondary_sherdemaal_city_hamlet() {
        Centroid hamlet = townTagger.getBuiltUpCentroid(10560001000188L);
        assertCentroid(hamlet, 10560001000188L, "Scherdemaal", 10, 12, 64, new Double[]{4.2895269, 50.8310562});
    }

    @Test
    public void should_not_load_centroids_when_name_equals_axename_leuven_city_hamlet() {
        Centroid hamlet = townTagger.getBuiltUpCentroid(10560001000335L);
        assertThat(hamlet).isNull();
    }

    @Test
    public void should_return_village_with_get_place_of_citytyp_0() {
        assertThat(townTagger.getCityCentroid(10560000379424L).getPlace()).isEqualTo("village");
    }

    @Test
    public void should_return_city_with_get_place_of_citytyp_1_and_dispclass_lower_than_8() {
        assertThat(townTagger.getCityCentroid(10560000718742L).getPlace()).isEqualTo("city");
    }

    @Test
    public void should_return_town_with_get_place_of_citytyp_1_and_dispclass_greater_than_8() {
        assertThat(townTagger.getCityCentroid(10560000388234L).getPlace()).isEqualTo("town");
    }

    @Test
    public void should_return_city_with_get_place_of_citytyp_2_and_dispclass_lower_than_8() {
        assertThat(townTagger.getCityCentroid(10560000710744L).getPlace()).isEqualTo("city");
    }

    @Test
    public void should_return_town_with_get_place_of_citytyp_2_and_dispclass_greater_than_8() {
        assertThat(townTagger.getCityCentroid(10560000712665L).getPlace()).isEqualTo("town");
    }

    @Test
    public void should_return_hamlet_with_get_place_of_citytyp_32() {
        assertThat(townTagger.getCityCentroid(10560000308734L).getPlace()).isEqualTo("hamlet");
    }

    @Test
    public void should_return_neighbourhood_with_get_place_of_citytyp_64() {
        assertThat(townTagger.getCityCentroid(10560000407632L).getPlace()).isEqualTo("neighbourhood");
    }

    private void assertCentroid(Centroid centroid, Long id, String name, Integer adminClass, Integer dispClass, Double[] point) {
        assertCentroid(centroid, id, name, adminClass, dispClass, 1, point);
    }

    private void assertCentroid(Centroid centroid, Long id, String name, Integer adminClass, Integer dispClass, Integer cityType, Double[] point) {
        assertThat(centroid.getId()).isEqualTo(id);
        assertThat(centroid.getName()).isEqualTo(name);
        assertThat(centroid.getAdminclass()).isEqualTo(adminClass);
        assertThat(centroid.getCitytyp()).isEqualTo(cityType);
        assertThat(centroid.getDispclass()).isEqualTo(dispClass);
        assertThat(centroid.getPoint().getX()).isEqualTo(point[0]);
        assertThat(centroid.getPoint().getY()).isEqualTo(point[1]);
    }
}