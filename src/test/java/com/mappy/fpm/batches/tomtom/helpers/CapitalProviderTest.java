package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CapitalProviderTest {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    private static final Centroid BRUSSEL = new Centroid(null, "Brussel", "1000", 0, 1, 2, new Point(new PackedCoordinateSequence.Double(new double[]{4.3557069, 50.8455362}, 2), FACTORY));
    private static final Centroid NAMUR = new Centroid(null, "Namur", "5000", 1, 1, 7, new Point(new PackedCoordinateSequence.Double(new double[]{4.8650627, 50.4649232}, 2), FACTORY));
    private static final Centroid LEUVEN = new Centroid(null, "Leuven", "3000", 2, 1, 8, new Point(new PackedCoordinateSequence.Double(new double[]{4.7005373, 50.8798309}, 2), FACTORY));
    private static final Centroid LIEGE = new Centroid(null, "Liège", "4000", 2, 1, 7, new Point(new PackedCoordinateSequence.Double(new double[]{5.5715583, 50.6439083}, 2), FACTORY));
    private static final Centroid MECHELEN = new Centroid(null, "Mechelen", "2800", 7, 1, 8, new Point(new PackedCoordinateSequence.Double(new double[]{4.477941, 51.0289371}, 2), FACTORY));
    private static final Centroid BASTOGNE = new Centroid(null, "Bastogne", "6600", 7, 1, 10, new Point(new PackedCoordinateSequence.Double(new double[]{5.7214881, 50.0055343}, 2), FACTORY));

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);

    private CapitalProvider capitalProvider;

    @Before
    public void setUp() {
        when(tomtomFolder.getSMFiles()).thenReturn(newArrayList("src/test/resources/tomtom/town/country1_sm.shp", "src/test/resources/tomtom/town/country2_sm.shp"));

        capitalProvider = new CapitalProvider(tomtomFolder);
    }

    @Test
    public void should_get_national_capital_with_level_0() {

        List<Centroid> capitals = capitalProvider.get(0);

        assertThat(capitals).containsOnly(BRUSSEL);
    }

    @Test
    public void should_get_departmental_capital_with_level_7() {

        List<Centroid> capitals = capitalProvider.get(7);

        assertThat(capitals).containsOnly(BRUSSEL, NAMUR, LEUVEN, LIEGE, MECHELEN, BASTOGNE);
    }
}