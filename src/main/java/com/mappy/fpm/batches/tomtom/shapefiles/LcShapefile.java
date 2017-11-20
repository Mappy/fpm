package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class LcShapefile extends LandShapefile {

    @Inject
    public LcShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lc.shp"));
    }

    @Override
    public String getOutputFileName() {
        return "lc";
    }

    @Override
    protected Map<String, String> getSpecificTags(Feature feature) {
        Map<String, String> tags = newHashMap();
        if (feature.getInteger("FEATTYP") == 7120) {
            tags.put("natural", "wood");
        }
        return tags;
    }
}
