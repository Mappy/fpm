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
                    Integer adminclass = feature.getInteger("ADMINCLASS");
                    Integer citytyp = feature.getInteger("CITYTYP");
                    Integer dispclass = feature.getInteger("DISPCLASS");
                    Point point = feature.getPoint();
                    Centroid centroid = new Centroid(id, name, adminclass, citytyp, dispclass, point);

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
        private final Integer adminclass;
        private final Integer citytyp;
        private final Integer dispclass;
        private final Point point;
    }

//    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
//        Map<String, String> tags = newHashMap();
//        String name = feature.getString("NAME");
//        if (name != null) {
//            tags.put("name", name);
//
//            switch (feature.getInteger("ADMINCLASS")) {
//                case 0:
//                    tags.put("capital", "yes");
//                    break;
//                case 1:
//                    tags.put("capital", "1");
//                    break;
//                case 7:
//                    tags.put("capital", "6");
//                    break;
//                case 8:
//                    tags.put("capital", "8");
//                    break;
//                case 9:
//                    tags.put("capital", "9");
//                    break;
//            }
//
//            switch (feature.getInteger("CITYTYP")) {
//                case 0:
//                    tags.put("place", "village");
//                    break;
//                case 1:
//                    tags.put("place", feature.getInteger("DISPCLASS") < 8 ? "city" : "town");
//                    break;
//                case 32:
//                    tags.put("place", "hamlet");
//                    break;
//                case 64:
//                    tags.put("place", "neighbourhood");
//                    break;
//            }
//            Map<String, String> alternateNames = nameProvider.getAlternateNames(feature.getLong("ID"));
//            tags.putAll(alternateNames);
//            relationProvider.getPop(feature.getLong("ID")).ifPresent(pop -> tags.put("population", pop));
//            Optional<Node> node = geometrySerializer.writePoint(feature.getPoint(), tags);
//
//            relationProvider.getMembers(feature.getLong("ID")).ifPresent(relationMembers -> {
//                        node.ifPresent(adminCenter ->
//                                relationMembers.getRelationMembers().add(new RelationMember(adminCenter.getId(), Node, "admin_center"))
//                        );
//                        Map<String, String> relationMemberTags = relationMembers.getTags();
//                        relationMemberTags.putAll(alternateNames);
//                        geometrySerializer.writeRelation(relationMembers.getRelationMembers(), relationMemberTags);
//                    }
//            );
//        }
//    }
}
