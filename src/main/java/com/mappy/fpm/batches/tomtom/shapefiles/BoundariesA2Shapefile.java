package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;

import javax.inject.Inject;

public class BoundariesA2Shapefile extends BoundariesShapefile {

    private final CapitalProvider capitalProvider;

    @Inject
    public BoundariesA2Shapefile(TomtomFolder folder, CapitalProvider capitalProvider, TownTagger townTagger, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("___a2.shp"), 2, capitalProvider, townTagger, nameProvider, osmLevelGenerator);
        this.capitalProvider = capitalProvider;
    }

    @Override
    public String getOutputFileName() {
        return "a2";
    }

}
