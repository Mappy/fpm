package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.helpers.TollProvider.Toll;
import org.junit.Test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

public class TollProviderTest {

    @Test
    public void should_return_empty_with_missing_file() {
        TollProvider tollProvider = new TollProvider("src/test/resources/toll/", "missing_file.json");

        assertThat(tollProvider.byId(9999999999999L)).isEqualTo(empty());
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_with_malformed_file() {
        TollProvider tollProvider = new TollProvider("src/test/resources/toll/", "malformed_file.json");

        tollProvider.byId(12500001097987L);
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_with_missing_tomtomId_key() {
        TollProvider tollProvider = new TollProvider("src/test/resources/toll/", "without_tomtomId.json");

        tollProvider.byId(12500001097987L);
    }

    @Test
    public void should_return_empty_with_unknown_id() {
        TollProvider tollProvider = new TollProvider("src/test/resources/toll/", "tolls.json");

        assertThat(tollProvider.byId(9999999999999L)).isEqualTo(empty());
    }

    @Test
    public void should_return_tolls() {
        TollProvider tollProvider = new TollProvider("src/test/resources/toll/", "tolls.json");

        assertThat(tollProvider.byId(12500001097987L)).isEqualTo(of(new Toll(1, "Péage d'Aigrefeuille", "4556", "4557")));
    }
}
