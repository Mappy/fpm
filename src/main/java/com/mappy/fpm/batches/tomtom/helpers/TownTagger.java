package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.ShapefileIterator;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.helpers.Centroid.from;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
public class TownTagger {

    private final Map<Long, Centroid> centroidsCity = newHashMap();
    private final Map<Long, Centroid> centroidsHamlet = newHashMap();

    @Inject
    public TownTagger(TomtomFolder folder) {
        File file = new File(folder.getFile("sm.shp"));
        if (file.exists()) {
            log.info("Opening {}", file.getAbsolutePath());
            try (ShapefileIterator iterator = new ShapefileIterator(file, true)) {
                while (iterator.hasNext()) {
                    Feature feature = iterator.next();

                    Centroid centroid = from(feature);

                    Long id = feature.getLong("ID");

                    centroidsCity.put(id, centroid.withId(id));

                    putCentroidHamlet(feature, centroid);
                }
            }
        } else {
            log.info("File not found: {}", file.getAbsolutePath());
        }
    }

    public Centroid get(Long centroidId) {
        return centroidsCity.get(centroidId);
    }

    public Centroid getHamlet(Long centroidId) {
        return centroidsHamlet.get(centroidId);
    }

    private void putCentroidHamlet(Feature feature, Centroid centroid) {
        ofNullable(feature.getString("BUANAME")).ifPresent(buaname -> {

            Optional<String> axname = ofNullable(feature.getString("AXNAME"));
            String name = feature.getString("NAME");

            if((!axname.isPresent() || !axname.get().equals(name)) && buaname.equals(name)) {
                ofNullable(feature.getLong("BUAID")).ifPresent(buaid -> centroidsHamlet.put(buaid, centroid.withId(buaid)));
            }
        });
    }
}
