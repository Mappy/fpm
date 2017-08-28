package com.mappy.fpm.batches.toll;

import com.mappy.fpm.batches.toll.TollReader.Toll;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Test;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TollReaderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);

    @Test
    public void should_return_tolls() throws Exception {
        when(tomtomFolder.getTollsFile()).thenReturn("src/test/resources/osmgenerator/tolls.json");

        TollReader tollReader = new TollReader(tomtomFolder);

        assertThat(tollReader.tollForTomtomId(12500001097987L)).isEqualTo(of(new Toll("1", "Péage d'Aigrefeuille", "4556", "4557")));
    }
}
