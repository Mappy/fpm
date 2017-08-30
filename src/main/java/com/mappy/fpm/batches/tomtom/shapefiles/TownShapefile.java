package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.RelationProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.Order;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;

@Order(2)
public class TownShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;
    private RelationProvider relationProvider;


    @Inject
    public TownShapefile(NameProvider nameProvider, TomtomFolder folder, RelationProvider relationProvider) {
        super(folder.getFile("sm.shp"));
        this.nameProvider = nameProvider;
        this.nameProvider.loadFromFile("smnm.dbf", "NAME", false);
        this.relationProvider = relationProvider;
    }

    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
        Map<String, String> tags = newHashMap();
        String name = feature.getString("NAME");
        if (name != null) {
            tags.put("name", name);
        }
        switch (feature.getInteger("ADMINCLASS")) {
            case 1:
                tags.put("place", "city");
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                tags.put("place", "town");
                break;
            case 8:
            case 9:
                tags.put("place", "village");
                break;
        }
        Map<String, String> alternateNames = nameProvider.getAlternateNames(feature.getLong("ID"));
        tags.putAll(alternateNames);
        relationProvider.getPop(feature.getLong("ID")).ifPresent(pop -> tags.put("population", pop));
        Optional<Node> node = geometrySerializer.writePoint(feature.getPoint(), tags);

        relationProvider.getMembers(feature.getLong("ID")).ifPresent(relationMembers -> {
                    node.ifPresent(adminCenter ->
                            relationMembers.getRelationMembers().add(new RelationMember(adminCenter.getId(), Node, "admin_center"))
                    );
                    Map<String, String> relationMemberTags = relationMembers.getTags();
                    relationMemberTags.putAll(alternateNames);
                    geometrySerializer.writeRelation(relationMembers.getRelationMembers(), relationMemberTags);
                }
        );
    }
}
