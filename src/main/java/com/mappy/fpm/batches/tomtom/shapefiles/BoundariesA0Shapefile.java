package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;

import javax.inject.Inject;

public class BoundariesA0Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA0Shapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(folder.getFile("___a0.shp"), 2, 0, nameProvider);
    }
}
