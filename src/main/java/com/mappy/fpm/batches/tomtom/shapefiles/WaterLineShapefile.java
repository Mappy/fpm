package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class WaterLineShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;

    @Inject
    public WaterLineShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(folder.getFile("wl.shp"));
        this.nameProvider = nameProvider;
        if(new File(folder.getFile("wl.shp")).exists()) {
            this.nameProvider.loadFromFile("wxnm.dbf", "NAME", false);
        }
    }

    @Override
    public String getOutputFileName() {
        return "wl";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        Map<String, String> tags = newHashMap();
        String name = feature.getString("NAME");
        if (name != null) {
            tags.put("name", name);
        }
        switch (feature.getInteger("DISPCLASS")) {
            case 1:
            case 2:
                tags.put("waterway", "river");
                break;
            default:
                tags.put("waterway", "stream");
                break;
        }
        tags.put("natural", "water");
        tags.put("ref:tomtom", String.valueOf(feature.getLong("ID")));
        tags.putAll(nameProvider.getAlternateNames(feature.getLong("ID")));
        serializer.write(feature.getMultiLineString(), tags);
    }
}
