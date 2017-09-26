package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;

import javax.inject.Inject;

public class BoundariesA1Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA1Shapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(folder.getFile("___a1.shp"), 4, 1, nameProvider);
    }
}
