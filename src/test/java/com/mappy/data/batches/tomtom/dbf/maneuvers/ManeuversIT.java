package com.mappy.data.batches.tomtom.dbf.maneuvers;

import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.dbf.maneuvers.Maneuvers;
import com.mappy.data.batches.tomtom.dbf.maneuvers.MnShapefile;
import com.mappy.data.batches.tomtom.dbf.maneuvers.MpDbf;
import com.mappy.data.batches.tomtom.dbf.maneuvers.Restriction;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ManeuversIT {
    private final Maneuvers provider = new Maneuvers(new MpDbf(new TomtomFolder("src/test/resources/osmgenerator/", "andand")),
            new MnShapefile(new TomtomFolder("src/test/resources/osmgenerator/", "andand")));

    @Test
    public void should_generate_restricting_maneuver() {
        List<Restriction> restrictions = provider.getRestrictions();

        Restriction noTurnLeft = restrictions.stream().filter(restriction -> restriction.getJunctionId() == 10200000001062L).findFirst().get();
        assertThat(noTurnLeft.getSegments()).containsExactly(10200000004259L, 10200000005186L);
    }
}
