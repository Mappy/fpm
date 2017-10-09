package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger.Centroid;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TownTaggerTest extends AbstractTest {

    private TownTagger townTagger;

    @Before
    public void setup() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("sm.shp")).thenReturn("src/test/resources/tomtom/town/Anderlecht___________sm.shp");

        townTagger = new TownTagger(tomtomFolder);
    }

    @Test
    public void should_load_centroids() {

        Centroid centroid = townTagger.get(10560000388234L);
        Double[] doubles = {4.3451859, 50.8251293};
        assertCentroid(centroid, 10560000388234L, "Sint-Gillis", 8, 1, 8, doubles);

        centroid = townTagger.get(10560000455427L);
        Double[] doubles2 = {4.3134424, 50.8055758};
        assertCentroid(centroid, 10560000455427L, "Vorst", 9, 1, 8, doubles2);

        centroid = townTagger.get(10560000718742L);
        Double[] doubles3 = {4.307077, 50.8366041};
        assertCentroid(centroid, 10560000718742L, "Anderlecht", 8, 1, 7, doubles3);
    }

    private void assertCentroid(Centroid centroid, Long id, String name, Integer adminclass, Integer citytyp, Integer dispclass, Double[] point) {
        assertThat(centroid.getId()).isEqualTo(id);
        assertThat(centroid.getName()).isEqualTo(name);
        assertThat(centroid.getAdminclass()).isEqualTo(adminclass);
        assertThat(centroid.getCitytyp()).isEqualTo(citytyp);
        assertThat(centroid.getDispclass()).isEqualTo(dispclass);
        assertThat(centroid.getPoint().getX()).isEqualTo(point[0]);
        assertThat(centroid.getPoint().getY()).isEqualTo(point[1]);
    }
}