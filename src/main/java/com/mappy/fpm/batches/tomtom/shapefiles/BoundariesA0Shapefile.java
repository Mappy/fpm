package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;

import javax.inject.Inject;

public class BoundariesA0Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA0Shapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("___a0.shp"), 0, nameProvider, osmLevelGenerator);
    }

    @Override
    public String getOutputFileName() {
        return "a0";
    }
}
