package com.mappy.fpm.batches.tomtom.shapefiles;


import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;

import javax.inject.Inject;


public class BoundariesA0Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA0Shapefile(TomtomFolder folder) {
        super(folder.getFile("___a0.shp"), 2);
    }
}
