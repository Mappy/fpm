package com.mappy.data.batches.tomtom.shapefiles;

import com.google.common.collect.Maps;
import javax.inject.Inject;
import com.mappy.data.batches.tomtom.*;
import com.mappy.data.batches.utils.*;

import java.util.Map;

import static com.mappy.data.batches.tomtom.helpers.RoadTagger.*;

public class RailwayShapefile extends TomtomShapefile {
    @Inject
    public RailwayShapefile(TomtomFolder folder) {
        super(folder.getFile("rr.shp"));
    }

    @Override
    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
        Map<String, String> tags = Maps.newHashMap();
        tags.put("railway", "rail");
        tags.putAll(level(feature));
        addTagIf("tunnel", "yes", TUNNEL.equals(feature.getInteger("PARTSTRUC")), tags);
        addTagIf("bridge", "yes", BRIDGE.equals(feature.getInteger("PARTSTRUC")), tags);
        geometrySerializer.write(feature.getMultiLineString(), tags);
    }
}
