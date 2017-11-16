package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;

import javax.inject.Inject;

public class LuShapefile extends LandShapefile {

    @Inject
    public LuShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lu.shp"));
    }

    @Override
    public String getOutputFileName() {
        return "lu";
    }
}
