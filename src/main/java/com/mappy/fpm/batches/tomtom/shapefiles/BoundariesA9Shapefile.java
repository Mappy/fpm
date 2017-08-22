package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;

import javax.inject.Inject;

public class BoundariesA9Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA9Shapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(folder.getFile("___a9.shp"), 9, nameProvider);
    }
}
