package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.Centroid;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class BoundariesA7Shapefile extends BoundariesShapefile {

    private static final int TOMTOM_LEVEL = 7;

    private final CapitalProvider capitalProvider;

    @Inject
    public BoundariesA7Shapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, CapitalProvider capitalProvider) {
        super(folder.getFile("___a7.shp"), TOMTOM_LEVEL, nameProvider, osmLevelGenerator);
        this.capitalProvider = capitalProvider;
    }

    @Override
    public String getOutputFileName() {
        return "a7";
    }

    @Override
    protected void finishRelation(GeometrySerializer serializer, Map<String, String> tags, List<RelationMember> members, Feature feature) {

        List<Centroid> capitals = capitalProvider.get(TOMTOM_LEVEL).stream().filter(c -> feature.getGeometry().contains(c.getPoint())).collect(toList());
        if (!capitals.isEmpty()) {

            Centroid cityCenter = capitals.get(0);
            cityCenter.getPlace().ifPresent(p -> tags.put("place", p));
            String capital = osmLevelGenerator.getOsmLevel(zone, cityCenter.getAdminclass().toString());
            tags.put("capital", "2".equals(capital) ? "yes" : capital);
            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), tags);
            node.ifPresent(adminCenter -> members.add(new RelationMember(adminCenter.getId(), EntityType.Node, "admin_center")));
        }
        serializer.writeRelation(members, tags);
    }
}
