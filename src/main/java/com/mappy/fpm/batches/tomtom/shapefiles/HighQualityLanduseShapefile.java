package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class HighQualityLanduseShapefile extends TomtomShapefile {
    private static final int PARK = 23;
    private static final int GREEN_URBAN_AREA = 14;
    private static final int GRASS = 13;
    private static final int FOREST = 10;

    @Inject
    public HighQualityLanduseShapefile(TomtomFolder folder) {
        super(folder.getFile("2dtb.shp"));
    }

    @Override
    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
        Map<String, String> tags = tags(feature.getInteger("BLOCKCLASS"));
        if (!tags.isEmpty()) {
            tags.put("source", "Tomtom - Citymap");
            geometrySerializer.write(feature.getMultiPolygon(), tags);
        }
    }

    private static Map<String, String> tags(Integer blockclass) {

        Map<String, String> result = newHashMap();
        switch (blockclass) {
            case FOREST:
                result.put("landuse", "forest");
                break;
            case GRASS:
                result.put("landuse", "grass");
                break;
            case GREEN_URBAN_AREA:
                result.put("landuse", "grass");
                break;
            case PARK:
                result.put("leisure", "park");
                break;
        }
        return result;
    }
}
