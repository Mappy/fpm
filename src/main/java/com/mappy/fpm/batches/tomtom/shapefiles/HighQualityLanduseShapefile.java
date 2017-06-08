package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.util.Map;

public class HighQualityLanduseShapefile extends TomtomShapefile {
    private static final int PARK = 23;
    private static final int GREEEN_URBAN_AREA = 14;
    private static final int GRASS = 13;
    private static final int FOREST = 10;

    @Inject
    public HighQualityLanduseShapefile(TomtomFolder folder) {
        super(folder.getFile("2dtb.shp"));
    }

    @Override
    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
        Map<String, String> tags = tags(feature.getInteger("BLOCKCLASS"));
        if (tags != null) {
            geometrySerializer.write(feature.getMultiPolygon(), tags);
        }
    }

    private static Map<String, String> tags(Integer blockclass) {
        switch (blockclass) {
            case FOREST:
                return ImmutableMap.of("landuse", "forest");
            case GRASS:
                return ImmutableMap.of("landuse", "grass");
            case GREEEN_URBAN_AREA:
                return ImmutableMap.of("landuse", "grass");
            case PARK:
                return ImmutableMap.of("leisure", "park");
        }
        return null;
    }
}
