package com.mappy.data.batches.tomtom.shapefiles;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.helpers.BoundariesShapefile;

public class BoundariesA9Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA9Shapefile(TomtomFolder folder) {
        super(folder.getFile("___a9.shp"), 9);
    }
}
