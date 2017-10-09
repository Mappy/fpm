package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BoundariesShapefileTest {

    @Test
    public void should_correctly_determine_zone() {
        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        new BoundariesShapefile("le_mans_2dbd.shp", 7, mock(NameProvider.class), osmLevelGenerator);
        verify(osmLevelGenerator).getOsmLevel("le_mans", "7");

        new BoundariesShapefile("fraf24___________mn.shp", 7, mock(NameProvider.class), osmLevelGenerator);
        verify(osmLevelGenerator).getOsmLevel("fraf24", "7");
    }
}