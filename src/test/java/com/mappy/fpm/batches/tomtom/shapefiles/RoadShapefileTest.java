package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.maneuvers.Maneuvers;
import com.mappy.fpm.batches.tomtom.dbf.maneuvers.RestrictionsAccumulator;
import com.mappy.fpm.batches.tomtom.helpers.RoadTagger;
import com.mappy.fpm.batches.utils.Feature;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RoadShapefileTest {

    private static final RoadTagger roadTagger = mock(RoadTagger.class);
    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("nw.shp")).thenReturn("src/test/resources/tomtom/road/nw.shp");
        when(tomtomFolder.getZone()).thenReturn("mockzone");
        when(tomtomFolder.getInputFolder()).thenReturn("/this/is/a/path/mockinputfolder");

        Map<String, String> mockedMap = newHashMap();
        mockedMap.put("ref:tomtom", "12500001097987");
        when(roadTagger.tag(any(Feature.class))).thenReturn(mockedMap);

        RestrictionsAccumulator restrictionsAccumulator = new RestrictionsAccumulator(mock(Maneuvers.class));

        RoadShapefile shapefile = new RoadShapefile(tomtomFolder, roadTagger, restrictionsAccumulator);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/nw.osm.pbf"));
    }

    @Test
    public void should_read_road() {
        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "12500001097987")).findFirst();
        assertThat(optWay.isPresent()).isTrue();

        verify(roadTagger, times(6)).tag(any(Feature.class));
    }
}
