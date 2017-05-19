package com.mappy.data.batches.tomtom.shapefiles;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.helpers.BoundariesShapefile;

public class BoundariesA1Shapefile extends BoundariesShapefile {
    @Inject
    public BoundariesA1Shapefile(TomtomFolder folder) {
        super(folder.getFile("___a1.shp"), 4);
    }
}
