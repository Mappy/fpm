package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Maps.newHashMap;

public class LaneTagger {

    private final LdDbf ldDbf;
    private final LfDbf lfDbf;

    @Inject
    public LaneTagger(LdDbf ldDbf, LfDbf lfDbf) {
        this.ldDbf = ldDbf;
        this.lfDbf = lfDbf;
    }

    public Map<String, String> lanesFor(Feature feature, Boolean isReversed) {
        Map<String, String> tags = newHashMap();

        if (ldDbf.containsKey(feature.getLong("ID"))) {
            tags.put("turn:lanes", on("|").join(reorder(feature, isReversed)));
        }

        Integer lanes = feature.getInteger("LANES");
        if (lanes > 0) {
            tags.put("lanes", String.valueOf(lanes));
        }

        return tags;
    }

    private List<String> reorder(Feature feature, Boolean isReversed) {
        List<String> parts = ldDbf.get(feature.getLong("ID"));
        if (isReversed) {
            return parts;
        }
        return reverse(parts);
    }
}
