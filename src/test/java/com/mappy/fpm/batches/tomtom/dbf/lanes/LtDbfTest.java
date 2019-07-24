package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.TomtomFolder;

import org.junit.Test;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LtDbfTest {
    private final TomtomFolder folder = mock(TomtomFolder.class);

    @Test
    public void should_parse_lt_file() {
        when(folder.getFile("lt.dbf")).thenReturn(getClass().getResource("/tomtom/lt.dbf").getPath());
        LtDbf laneTimeDomains = new LtDbf(folder);
        assertThat(laneTimeDomains.getTimeDomains(
            LtDbf.RestrictionType.directionOfTrafficFlow,
            12500000942461L,
            2
        )).isEqualTo(new TimeDomains(12500000942461L, 2, "[(M11d11h10){h8}]"));
        assertThat(laneTimeDomains.getTimeDomains(
            LtDbf.RestrictionType.laneType,
            12500000942461L,
            3
        )).isEqualTo(new TimeDomains(12500000942461L, 3, "[(M11d1h10){h8}]"));
        assertThat(laneTimeDomains.getTimeDomains(
            LtDbf.RestrictionType.speedRestriction,
            12500000942461L,
            4
        )).isEqualTo(new TimeDomains(12500000942461L, 4, "[(M12d25h10){h8}]"));
    }
}

