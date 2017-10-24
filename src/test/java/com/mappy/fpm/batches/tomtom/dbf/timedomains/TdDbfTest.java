package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TdDbfTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);

    private TdDbf tdDbf;

    @Before
    public void setup() {
        when(tomtomFolder.getFile("td.dbf")).thenReturn("src/test/resources/tomtom/road/td.dbf");
        tdDbf = new TdDbf(tomtomFolder);
    }

    @Test
    public void should_read_time_domain_restriction() {
        assertThat(tdDbf.getTimeDomains(14420000000590L)).containsExactly(
                new TimeDomains(14420000000590L, "[(h11){h7}]"),
                new TimeDomains(14420000000590L, "[(h22){h8}]"));
    }
}