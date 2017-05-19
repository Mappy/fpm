package com.mappy.data.batches.tomtom.shapefiles;

import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.TomtomShapefile;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class BuildingShapefile extends TomtomShapefile {
    @Inject
    public BuildingShapefile(TomtomFolder folder) {
        super(folder.getFile("2dbd.shp"));
    }

    @Override
    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
        Map<String, String> tags = newHashMap();

        switch (feature.getInteger("BUILDCLASS")) {
            case 1:
                tags.put("building", "yes");
                tags.put("aeroway", "terminal");
                break;
            case 4:
                tags.put("building", "commercial");
                break;
            case 5:
                tags.put("building", "cultural");
                break;
            case 6:
                tags.put("building", "industrial");
                break;
            case 7:
                tags.put("building", "public");
                break;
            case 8:
                tags.put("building", "hospital");
                break;
            case 9:
                tags.put("building", "hotel");
                break;
            case 13:
                tags.put("building", "parking");
                break;
            case 15:
                tags.put("building", "yes");
                tags.put("amenity", "place_of_worship");
                break;
            case 16:
                tags.put("area","yes");
                tags.put("railway", "platform");
                break;
            case 17:
                tags.put("building", "train_station");
                break;
            case 19:
                tags.put("building", "school");
                break;
            case 20:
                tags.put("area","yes");
                tags.put("railway", "platform");
                tags.put("station","subway");
                break;
            default:
                tags.put("building", "yes");
                break;
        }
        geometrySerializer.write(feature.getMultiPolygon(), tags);
    }
}
