package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;


public class BoundariesA0Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA0Shapefile(TomtomFolder folder, CapitalProvider capitalProvider, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("___a0.shp"), 0, capitalProvider, null, nameProvider, osmLevelGenerator);
    }

    @Override
    public String getOutputFileName() {
        return "a0";
    }

    @Override
    protected String getNationalCode(Feature feature) {
        return getIso3166Numeric(feature);
    }

    @Override
    protected String getIso3166Numeric(Feature feature) {
        String id = feature.getLong("ID").toString();
        if (id != null) {
            return id.substring(1, 4);
        }
        return null;
    }

    @Override
    protected String getIso3166Alpha3(Feature feature) {
        return feature.getString("ORDER00");
    }
}
