package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;

import javax.inject.Inject;
import java.io.File;

public class BoundariesA9Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA9Shapefile(TomtomFolder folder, CapitalProvider capitalProvider, TownTagger townTagger, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("a9.shp"), 9, capitalProvider, townTagger, nameProvider, osmLevelGenerator);
        if(new File(folder.getFile("a9.shp")).exists()) {
            nameProvider.loadAlternateCityNames("smnm.dbf");
        }
    }

    @Override
    public String getOutputFileName() {
        return "a9";
    }
}
