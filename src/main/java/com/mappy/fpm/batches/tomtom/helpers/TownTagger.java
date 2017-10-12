package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.ShapefileIterator;
import com.vividsolutions.jts.geom.Point;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Slf4j
public class TownTagger {

    private final Map<Long, Centroid> centroids = newHashMap();

    @Inject
    public TownTagger(TomtomFolder folder) {
        generateCentroids(folder.getFile("sm.shp"));
    }

    private void generateCentroids(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            log.info("Opening {}", file.getAbsolutePath());
            try (ShapefileIterator iterator = new ShapefileIterator(file, true)) {
                while (iterator.hasNext()) {
                    Feature feature = iterator.next();
                    Long id = feature.getLong("ID");
                    String name = feature.getString("NAME");
                    String postcode = feature.getString("POSTCODE");
                    Integer adminclass = feature.getInteger("ADMINCLASS");
                    Integer citytyp = feature.getInteger("CITYTYP");
                    Integer dispclass = feature.getInteger("DISPCLASS");
                    Point point = feature.getPoint();
                    Centroid centroid = new Centroid(id, name, postcode, adminclass, citytyp, dispclass, point);

                    centroids.put(id, centroid);
                }
            }
        }
        else {
            log.info("File not found: {}", file.getAbsolutePath());
        }
    }

    public Centroid get(Long centroidId) {
        return centroids.get(centroidId);
    }

    @Data
    public static class Centroid {

        private final Long id;
        private final String name;
        private final String postcode;
        private final Integer adminclass;
        private final Integer citytyp;
        private final Integer dispclass;
        private final Point point;
    }
}
