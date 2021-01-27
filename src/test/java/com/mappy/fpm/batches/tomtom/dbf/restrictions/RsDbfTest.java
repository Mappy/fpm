package com.mappy.fpm.batches.tomtom.dbf.restrictions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;

import org.junit.Test;

public class RsDbfTest {
    @Test
    public void should_parse_rs() {
        TomtomFolder folder = mock(TomtomFolder.class);
        when(folder.getFile("rs.dbf")).thenReturn(getClass().getResource("/tomtom/rs.dbf").getPath());

        RsDbf rsDbf = new RsDbf(folder);

        assertThat(rsDbf.getRestrictions(17240000002822L)).containsExactly(
            new Restriction(17240000002822L, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.passengerCars),
            new Restriction(17240000002822L, 2, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.taxi),
            new Restriction(17240000002822L, 3, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.publicBus)
        );

        assertThat(rsDbf.getRestrictions(17240000005566L)).containsExactly(
            new Restriction(17240000005566L, 1, Restriction.Validity.notApplicable, Restriction.Type.notApplicable, 0, VehicleType.all)
        );

        assertThat(rsDbf.getRestrictions(17240000000902L)).containsExactly(
            new Restriction(17240000000902L, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.constructionStatus, 0, VehicleType.all)
        );

        assertThat(rsDbf.getRestrictions(17240000000903L)).containsExactly(
            new Restriction(17240000000903L, 1, Restriction.Validity.inPositiveLineDirection, Restriction.Type.constructionStatus, 0, VehicleType.all)
        );

        assertThat(rsDbf.getRestrictions(17240000001376L)).containsExactly(
            new Restriction(17240000001376L, 1, Restriction.Validity.inNegativeLineDirection, Restriction.Type.constructionStatus, 0, VehicleType.all)
        );
    }
}
