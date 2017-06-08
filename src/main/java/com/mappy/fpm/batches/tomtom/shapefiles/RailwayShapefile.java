package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.Maps;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.util.Map;

import static com.mappy.fpm.batches.tomtom.helpers.RoadTagger.*;

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
