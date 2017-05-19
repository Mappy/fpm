package com.mappy.data.batches.tomtom.shapefiles;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.dbf.names.NameProvider;
import com.mappy.data.batches.tomtom.helpers.LandShapefile;

public class LuShapefile extends LandShapefile {

    @Inject
    public LuShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lu.shp"));
    }
}
