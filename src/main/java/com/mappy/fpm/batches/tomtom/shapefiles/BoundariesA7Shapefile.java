package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;

import javax.inject.Inject;

public class BoundariesA7Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA7Shapefile(TomtomFolder folder) {
        super(folder.getFile("___a7.shp"), 6);
    }
}
