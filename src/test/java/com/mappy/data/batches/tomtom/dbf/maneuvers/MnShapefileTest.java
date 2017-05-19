package com.mappy.data.batches.tomtom.dbf.maneuvers;

import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.dbf.maneuvers.Maneuver;
import com.mappy.data.batches.tomtom.dbf.maneuvers.MnShapefile;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class MnShapefileTest {
    private final MnShapefile mn = new MnShapefile(new TomtomFolder("src/test/resources/osmgenerator/", "andand"));

    @Test
    public void should_load_maneuvers() {
        assertThat(mn.maneuvers()).contains(
                new Maneuver(10200000000009L, 10200000006712L, 2101),
                new Maneuver(10200000000009L, 10200000006970L, 2101),
                new Maneuver(10200000000008L, 10200000005061L, 2101));
    }

    @Test
    public void should_not_fail_when_no_mn_file_exists() throws Exception {
        MnShapefile mn = new MnShapefile(new TomtomFolder("src/test/resources/osmgenerator/", "other_country"));

        assertThat(mn.maneuvers()).isEmpty();
    }
}
