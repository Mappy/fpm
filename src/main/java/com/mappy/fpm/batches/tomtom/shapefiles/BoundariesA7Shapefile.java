package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger.Centroid;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;

public class BoundariesA7Shapefile extends BoundariesShapefile {

    private static final int TOMTOM_LEVEL = 7;

    private final TownTagger townTagger;

    @Inject
    public BoundariesA7Shapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, TownTagger townTagger) {
        super(folder.getFile("___a7.shp"), TOMTOM_LEVEL, nameProvider, osmLevelGenerator);
        this.townTagger = townTagger;
    }

    @Override
    public String getOutputFileName() {
        return "a7";
    }

    @Override
    protected void finishRelation(GeometrySerializer serializer, Map<String, String> tags, List<RelationMember> members, Feature feature) {

        List<Centroid> capitals = townTagger.getCapital(TOMTOM_LEVEL).stream().filter(c -> feature.getGeometry().contains(c.getPoint())).collect(toList());

        if(!capitals.isEmpty()) {
            Centroid cityCenter = capitals.get(0);
            cityCenter.getPlace().ifPresent(p -> tags.put("place", p));

            switch (cityCenter.getAdminclass()) {
                case 0:
                    tags.put("capital", "yes");
                    break;
                case 1:
                    tags.put("capital", "1");
                    break;
                case 7:
                    tags.put("capital", "6");
                    break;
                case 8:
                    tags.put("capital", "8");
                    break;
                case 9:
                    tags.put("capital", "9");
                    break;
            }

            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), tags);
            node.ifPresent(adminCenter -> members.add(new RelationMember(adminCenter.getId(), Node, "admin_center")));
        }

        serializer.writeRelation(members, tags);
    }
}
