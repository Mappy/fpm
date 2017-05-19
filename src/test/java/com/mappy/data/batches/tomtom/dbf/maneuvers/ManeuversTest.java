package com.mappy.data.batches.tomtom.dbf.maneuvers;

import com.mappy.data.batches.tomtom.dbf.maneuvers.Maneuver;
import com.mappy.data.batches.tomtom.dbf.maneuvers.ManeuverPath;
import com.mappy.data.batches.tomtom.dbf.maneuvers.Maneuvers;
import com.mappy.data.batches.tomtom.dbf.maneuvers.MnShapefile;
import com.mappy.data.batches.tomtom.dbf.maneuvers.MpDbf;
import com.mappy.data.batches.tomtom.dbf.maneuvers.Restriction;

import java.util.List;

import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ManeuversTest {
    private final MpDbf mp = mock(MpDbf.class);
    private final MnShapefile mn = mock(MnShapefile.class);

    @Test
    public void should_generate_restricting_maneuver() {
        when(mp.paths()).thenReturn(newArrayList(
                new ManeuverPath(10200000005187L, 2L, 10200000005186L, 4110L),
                new ManeuverPath(10200000005187L, 1L, 10200000004259L, 4110L)
                ));
        when(mn.maneuvers()).thenReturn(newArrayList(
                new Maneuver(10200000001062L, 10200000005187L, 2101)));

        Maneuvers provider = new Maneuvers(mp, mn);
        List<Restriction> restrictions = provider.getRestrictions();

        assertThat(restrictions).containsExactly(new Restriction(newArrayList(10200000004259L, 10200000005186L), 10200000001062L));
    }

    @Test
    public void should_not_fail_when_no_mp_file_exists() throws Exception {
        when(mp.paths()).thenReturn(newArrayList());
        when(mn.maneuvers()).thenReturn(newArrayList());

        Maneuvers provider = new Maneuvers(mp, mn);
        assertThat(provider.getRestrictions()).isEmpty();
    }

    @Test
    public void should_create_maneuver_with_multiple_ways() throws Exception {
        when(mp.paths()).thenReturn(newArrayList(
                new ManeuverPath(14420000070072L, 1L, 14420000006953L, 4110L),
                new ManeuverPath(14420000070072L, 3L, 14420000051735L, 4110L),
                new ManeuverPath(14420000070072L, 2L, 14420000049330L, 4110L)
                ));
        when(mn.maneuvers()).thenReturn(newArrayList(
                new Maneuver(14420000019386L, 14420000070072L, 2103)));

        Maneuvers provider = new Maneuvers(mp, mn);
        List<Restriction> restrictions = provider.getRestrictions();

        assertThat(restrictions).containsExactly(new Restriction(newArrayList(14420000006953L, 14420000049330L, 14420000051735L), 14420000019386L));
    }
}
