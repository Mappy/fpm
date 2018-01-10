package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;

import javax.inject.Inject;

public class BoundariesA2Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA2Shapefile(TomtomFolder folder, CapitalProvider capitalProvider, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("a2.shp"), 2, capitalProvider, null, nameProvider, osmLevelGenerator);
    }

    @Override
    public String getOutputFileName() {
        return "a2";
    }

}
