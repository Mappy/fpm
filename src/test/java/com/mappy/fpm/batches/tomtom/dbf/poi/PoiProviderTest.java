package com.mappy.fpm.batches.tomtom.dbf.poi;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static com.mappy.fpm.batches.tomtom.dbf.poi.FeatureType.MOUNTAIN_PASS;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PoiProviderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);
    private PoiProvider poiProvider;

    @Before
    public void setUp() {
        when(tomtomFolder.getFile("pi.dbf")).thenReturn("src/test/resources/tomtom/poi/andand___________pi.dbf");
        when(tomtomFolder.getFile("piea.dbf")).thenReturn("src/test/resources/tomtom/poi/andand___________piea.dbf");
        poiProvider = new PoiProvider(tomtomFolder);
    }

    @Test
    public void should_have_a_mountain_pass() {
        assertThat(poiProvider.getPoiNameByType(10200000000496L, MOUNTAIN_PASS.getValue())).isEqualTo(of("Ordino"));
    }

    @Test
    public void should_not_have_a_mountain_pass() {
        assertThat(poiProvider.getPoiNameByType(10200000002151L, MOUNTAIN_PASS.getValue())).isEqualTo(empty());
    }

    @Test
    public void should_not_have_a_mountain_pass_when_id_not_exist() {
        assertThat(poiProvider.getPoiNameByType(123L, MOUNTAIN_PASS.getValue())).isEqualTo(empty());
    }


}