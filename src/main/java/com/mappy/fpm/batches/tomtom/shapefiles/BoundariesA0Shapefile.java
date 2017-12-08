package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.neovisionaries.i18n.CountryCode;

import javax.inject.Inject;

import static java.lang.String.valueOf;

public class BoundariesA0Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA0Shapefile(TomtomFolder folder, CapitalProvider capitalProvider, TownTagger townTagger, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("___a0.shp"), 0, capitalProvider, townTagger, nameProvider, osmLevelGenerator);
    }

    @Override
    public String getOutputFileName() {
        return "a0";
    }

    @Override
    protected String getInseeWithAlpha3(String alpha3) {
        String alpha32 = alpha3;
        if (CountryCode.getByCode(alpha3) == null) {
            alpha32 = alpha3.substring(0, alpha3.length() - 1);
        }
        return CountryCode.getByCode(alpha32) == null ? alpha3 : valueOf(CountryCode.getByCode(alpha32).getNumeric());
    }
}
