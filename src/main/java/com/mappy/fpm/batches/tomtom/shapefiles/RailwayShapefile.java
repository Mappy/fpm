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
    public String getOutputFileName() {
        return "rr";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        Map<String, String> tags = Maps.newHashMap();
        tags.put("ref:tomtom", String.valueOf(feature.getLong("ID")));
        tags.put("railway", "rail");
        Boolean isReversed = "TF".equals(feature.getString("ONEWAY"));
        tags.putAll(level(feature, isReversed));
        addTagIf("tunnel", "yes", TUNNEL.equals(feature.getInteger("PARTSTRUC")), tags);
        addTagIf("bridge", "yes", BRIDGE.equals(feature.getInteger("PARTSTRUC")), tags);
        serializer.write(feature.getMultiLineString(), tags);
    }
}
