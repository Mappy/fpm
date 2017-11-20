package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class LuShapefile extends LandShapefile {

    @Inject
    public LuShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(nameProvider, folder.getFile("lu.shp"));
    }

    @Override
    public String getOutputFileName() {
        return "lu";
    }

    @Override
    protected Map<String, String> getSpecificTags(Feature feature) {
        Map<String, String> tags = newHashMap();
        if (feature.getInteger("FEATTYP") == 7120) {
            tags.put("landuse", "forest");
        }
        return tags;
    }
}
