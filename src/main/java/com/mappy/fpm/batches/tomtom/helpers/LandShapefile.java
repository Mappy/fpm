package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import java.io.File;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class LandShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;

    protected LandShapefile(NameProvider nameProvider, String filename) {
        super(filename);
        this.nameProvider = nameProvider;
        if (new File(filename).exists()) {
            this.nameProvider.loadAlternateNames("lxnm.dbf");
        }
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        if (7110 != feature.getInteger("FEATTYP")) {
            serializer.write(feature.getMultiPolygon(), completeTags(feature));
        }
    }

    private Map<String, String> completeTags(Feature feature) {
        Map<String, String> tags = getSpecificTags(feature);

        tags.put("ref:tomtom", String.valueOf(feature.getLong("ID")));

        ofNullable(feature.getString("NAME")).ifPresent(value -> tags.put("name", value));

        tags.putAll(nameProvider.getAlternateNames(feature.getLong("ID")));

        Integer displayType = ofNullable(feature.getInteger("DISPLTYP")).orElse(-1);

        ofNullable(feature.getInteger("FEATTYP"))//
                .flatMap((Integer tomtomFeattype) -> Land.getLand(tomtomFeattype, displayType))
                .map(Land::getOsmTag).ifPresent(tags::putAll);

        return tags;
    }

    protected abstract Map<String, String> getSpecificTags(Feature feature);

}
