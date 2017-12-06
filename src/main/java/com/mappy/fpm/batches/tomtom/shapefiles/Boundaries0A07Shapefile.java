package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;

import javax.inject.Inject;

public class Boundaries0A07Shapefile extends BoundariesShapefile {

    @Inject
    public Boundaries0A07Shapefile(TomtomFolder folder, CapitalProvider capitalProvider, TownTagger townTagger, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator){
        super(folder.getFile("___oa07.shp"), 6, capitalProvider, townTagger, nameProvider, osmLevelGenerator);
    }

    @Override
    public String getOutputFileName() {
        return "oa07";
    }
}
