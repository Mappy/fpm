package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;

import javax.inject.Inject;

public class BuiltUpShapefile extends LandShapefile {

    @Inject
    public BuiltUpShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("bu.shp"));
    }
}