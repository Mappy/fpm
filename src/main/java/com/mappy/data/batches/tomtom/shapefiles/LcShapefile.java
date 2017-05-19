package com.mappy.data.batches.tomtom.shapefiles;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.dbf.names.NameProvider;
import com.mappy.data.batches.tomtom.helpers.LandShapefile;

public class LcShapefile extends LandShapefile {

    @Inject
    public LcShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lc.shp"));
    }
}
