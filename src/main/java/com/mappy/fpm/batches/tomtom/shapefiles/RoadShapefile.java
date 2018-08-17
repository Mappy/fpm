package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.maneuvers.RestrictionsAccumulator;
import com.mappy.fpm.batches.tomtom.helpers.RoadTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mappy.fpm.batches.tomtom.helpers.RoadTagger.isReversed;

public class RoadShapefile extends TomtomShapefile {

    private final RoadTagger roadTagger;
    private final RestrictionsAccumulator restrictions;

    @Inject
    public RoadShapefile(TomtomFolder folder, RoadTagger roadTagger, RestrictionsAccumulator restrictions) {
        super(folder.getFile("nw.shp"));
        this.roadTagger = roadTagger;
        this.restrictions = restrictions;
    }

    @Override
    public String getOutputFileName() {
        return "nw";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        LineString raw = geom(feature);
        LineString geom = isReversed(feature) ? (LineString) raw.reverse() : raw;
        Map<String, String> tags = roadTagger.tag(feature);
        Way way = serializer.write(geom, tags);
        restrictions.register(feature, way);
    }

    private static LineString geom(Feature feature) {
        MultiLineString multiLine = feature.getMultiLineString();
        checkArgument(multiLine.getNumGeometries() == 1, "Tomtom road multiline should contain only line");
        return (LineString) multiLine.getGeometryN(0);
    }

    @Override
    public void complete(GeometrySerializer serializer) {
        restrictions.complete(serializer);
    }
}