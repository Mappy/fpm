package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;

import javax.inject.Inject;

public class LcShapefile extends LandShapefile {

    @Inject
    public LcShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lc.shp"));
    }
}
