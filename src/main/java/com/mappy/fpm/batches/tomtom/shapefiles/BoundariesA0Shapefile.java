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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;

public class BoundariesA0Shapefile extends BoundariesShapefile {

    private final CapitalProvider capitalProvider;

    @Inject
    public BoundariesA0Shapefile(TomtomFolder folder, NameProvider nameProvider, CapitalProvider capitalProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("___a0.shp"), 0, nameProvider, osmLevelGenerator);
        this.capitalProvider = capitalProvider;
    }

    @Override
    public String getOutputFileName() {
        return "a0";
    }

    @Override
    protected void finishRelation(GeometrySerializer serializer, Map<String, String> tags, List<RelationMember> members, Feature feature) {

        List<Centroid> capitals = capitalProvider.get(0);
        if (!capitals.isEmpty()) {
            Centroid cityCenter = capitals.get(0);
            Map<String, String> adminTags = newHashMap(of("name", cityCenter.getName()));
            cityCenter.getPlace().ifPresent(p -> adminTags.put("place", p));
            String capital = osmLevelGenerator.getOsmLevel(zone, cityCenter.getAdminclass());
            adminTags.put("capital", "2".equals(capital) ? "yes" : capital);
            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), adminTags);
            node.ifPresent(adminCenter -> members.add(new RelationMember(adminCenter.getId(), EntityType.Node, "admin_center")));
        }
        serializer.writeRelation(members, tags);
    }
}
