package com.mappy.data.batches.tomtom.shapefiles;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.helpers.BoundariesShapefile;

public class BoundariesA8Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder) {
        super(folder.getFile("___a8.shp"), 8);
    }
}
