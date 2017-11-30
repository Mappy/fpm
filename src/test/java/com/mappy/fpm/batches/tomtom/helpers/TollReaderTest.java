package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.TollReader.Toll;
import org.junit.Test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TollReaderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);

    @Test
    public void should_return_empty_with_missing_file() {
        when(tomtomFolder.getTollsFile()).thenReturn("src/test/resources/toll/missing_file.json");

        TollReader tollReader = new TollReader(tomtomFolder);

        assertThat(tollReader.tollForTomtomId(9999999999999L)).isEqualTo(empty());
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_with_malformed_file() throws Exception {
        when(tomtomFolder.getTollsFile()).thenReturn("src/test/resources/toll/malformed_file.json");

        TollReader tollReader = new TollReader(tomtomFolder);

        tollReader.tollForTomtomId(12500001097987L);
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_with_missing_tomtomId_key() throws Exception {
        when(tomtomFolder.getTollsFile()).thenReturn("src/test/resources/toll/without_tomtomId.json");

        TollReader tollReader = new TollReader(tomtomFolder);

        tollReader.tollForTomtomId(12500001097987L);
    }

    @Test
    public void should_return_empty_with_unknown_id() {
        when(tomtomFolder.getTollsFile()).thenReturn("src/test/resources/toll/tolls.json");

        TollReader tollReader = new TollReader(tomtomFolder);

        assertThat(tollReader.tollForTomtomId(9999999999999L)).isEqualTo(empty());
    }

    @Test
    public void should_return_tolls() throws Exception {
        when(tomtomFolder.getTollsFile()).thenReturn("src/test/resources/toll/tolls.json");

        TollReader tollReader = new TollReader(tomtomFolder);

        assertThat(tollReader.tollForTomtomId(12500001097987L)).isEqualTo(of(new Toll(1, "Péage d'Aigrefeuille", "4556", "4557")));
    }
}
