package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Test;

import static com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.Validity.*;
import static com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.VehicleType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SrDbfTest {
    @Test
    public void should_parse_sr() {
        TomtomFolder folder = mock(TomtomFolder.class);
        when(folder.getFile("sr.dbf")).thenReturn(getClass().getResource("/tomtom/sr2.dbf").getPath());

        SrDbf srDbf = new SrDbf(folder);

        assertThat(srDbf.getSpeedRestrictions(12500067305696L)).containsExactly(
                new SpeedRestriction(12500067305696L, 1, 30, positive, VehicleType.passengerCars),
                new SpeedRestriction(12500067305696L, 2, 50, negative, VehicleType.passengerCars));

        assertThat(srDbf.getSpeedRestrictions(12500067332646L)).containsExactly(
                new SpeedRestriction(12500067332646L, 1, 30, both, VehicleType.passengerCars));
    }
}
