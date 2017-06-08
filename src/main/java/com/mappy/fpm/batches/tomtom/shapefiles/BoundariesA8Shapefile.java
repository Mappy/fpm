package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;

import javax.inject.Inject;

public class BoundariesA8Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder) {
        super(folder.getFile("___a8.shp"), 8);
    }
}
