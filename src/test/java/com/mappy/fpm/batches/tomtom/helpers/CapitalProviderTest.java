package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.shapefiles.BoundariesA7Shapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import net.morbz.osmonaut.osm.Node;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.io.File;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CapitalProviderTest extends AbstractTest {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    private static final Centroid BRUSSEL = new Centroid(10560000430948L, "Brussel", "1000", 0, 1, 2, new Point(new PackedCoordinateSequence.Double(new double[]{4.3557069, 50.8455362}, 2), FACTORY));
    private static final Centroid NAMUR = new Centroid(10560000987220L, "Namur", "5000", 1, 1, 7, new Point(new PackedCoordinateSequence.Double(new double[]{4.8650627, 50.4649232}, 2), FACTORY));
    private static final Centroid LEUVEN = new Centroid(10560000308734L, "Leuven", "3000", 2, 1, 8, new Point(new PackedCoordinateSequence.Double(new double[]{4.7005373, 50.8798309}, 2), FACTORY));
    private static final Centroid LIEGE = new Centroid(10560001750756L, "Liège", "4000", 2, 1, 7, new Point(new PackedCoordinateSequence.Double(new double[]{5.5715583, 50.6439083}, 2), FACTORY));
    private static final Centroid MECHELEN = new Centroid(10560000378932L, "Mechelen", "2800", 7, 1, 8, new Point(new PackedCoordinateSequence.Double(new double[]{4.477941, 51.0289371}, 2), FACTORY));
    private static final Centroid BASTOGNE = new Centroid(10560001070563L, "Bastogne", "6600", 7, 1, 10, new Point(new PackedCoordinateSequence.Double(new double[]{5.7214881, 50.0055343}, 2), FACTORY));
    private static final Centroid PULYNY = new Centroid(18040000766718L, "Pulyny", null, 7, 9, 10, new Point(new PackedCoordinateSequence.Double(new double[]{28.260929, 50.466215}, 2), FACTORY));

    private static CapitalProvider capitalProvider;
    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {
        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getSMFiles()).thenReturn(newArrayList("src/test/resources/tomtom/town/country1_sm.shp", "src/test/resources/tomtom/town/country2_sm.shp", "src/test/resources/tomtom/town/Pulyny___________sm.shp"));
        when(tomtomFolder.getFile("smnm.dbf")).thenReturn("src/test/resources/tomtom/town/Pulyny___________smnm.dbf");
        when(tomtomFolder.getFile("a7.shp")).thenReturn("src/test/resources/tomtom/town/Pulyny___________a7.shp");
        when(tomtomFolder.getFile("an.dbf")).thenReturn("src/test/resources/tomtom/town/Pulyny___________an.dbf");

        capitalProvider = new CapitalProvider(tomtomFolder);

        BoundariesA7Shapefile shapefile = new BoundariesA7Shapefile(tomtomFolder, capitalProvider, new NameProvider(tomtomFolder), new OsmLevelGenerator());
        shapefile.serialize("target/tests/");
        pbfContent = read(new File("target/tests/a7.osm.pbf"));
    }

    @Test
    public void should_get_national_capital_with_level_0() {

        List<Centroid> capitals = capitalProvider.get(0);

        assertThat(capitals).containsOnly(BRUSSEL);
    }

    @Test
    public void should_get_departmental_capital_with_level_7() {

        List<Centroid> capitals = capitalProvider.get(7);

        assertThat(capitals).containsOnly(BRUSSEL, NAMUR, LEUVEN, LIEGE, MECHELEN, BASTOGNE, PULYNY);
    }

    @Test
    public void should_load_capital_a7_as_point_with_all_translations() {
        Node capital = pbfContent.getNodes().stream().filter(n -> n.getTags().size() > 0).findFirst().get();

        assertThat(capital.getTags().get("name")).isEqualTo("Pulyny");
        assertThat(capital.getTags().get("name:uk")).isEqualTo("Пулини");
        assertThat(capital.getTags().get("name:uk-Latn")).isEqualTo("Pulyny");
    }
}