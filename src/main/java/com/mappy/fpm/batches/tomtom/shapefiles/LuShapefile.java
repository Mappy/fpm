package com.mappy.fpm.batches.tomtom.shapefiles;

import javax.inject.Inject;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;

public class LuShapefile extends LandShapefile {

    @Inject
    public LuShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lu.shp"));
    }
}
