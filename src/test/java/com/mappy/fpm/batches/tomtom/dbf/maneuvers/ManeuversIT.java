package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ManeuversIT {
    private final Maneuvers provider = new Maneuvers(new MpDbf(new TomtomFolder("src/test/resources/osmgenerator/", "andand")),
            new MnDbf(new TomtomFolder("src/test/resources/osmgenerator/", "andand")));

    @Test
    public void should_generate_restricting_maneuver() {
        List<Restriction> restrictions = provider.getRestrictions();

        Restriction noTurnLeft = restrictions.stream().filter(restriction -> restriction.getJunctionId() == 10200000001062L).findFirst().get();
        assertThat(noTurnLeft.getSegments()).containsExactly(10200000004259L, 10200000005186L);
    }
}
