package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MpDbfTest {
    private final MpDbf mp = new MpDbf(new TomtomFolder("src/test/resources/osmgenerator/", "andand"));

    @Test
    public void should_generate_restricting_maneuver() {
        assertThat(mp.paths()).contains(
                new ManeuverPath(10200000006274L, 1L, 10200000002653L, 4110L),
                new ManeuverPath(10200000006274L, 2L, 10200000004142L, 4110L),
                new ManeuverPath(10200000006275L, 1L, 10200000005070L, 4110L),
                new ManeuverPath(10200000006275L, 2L, 10200000001673L, 4110L));
    }

    @Test
    public void should_not_fail_when_no_mp_file_exists() throws Exception {
        MpDbf mp = new MpDbf(new TomtomFolder("src/test/resources/osmgenerator/", "other_country"));
        assertThat(mp.paths()).isEmpty();
    }
}
