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

            switch (feature.getInteger("ADMINCLASS")) {
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

            switch (feature.getInteger("CITYTYP")) {
                case 0:
                    tags.put("place", "village");
                    break;
                case 1:
                    tags.put("place", feature.getInteger("DISPCLASS") < 8 ? "city" : "town");
                    break;
                case 32:
                    tags.put("place", "hamlet");
                    break;
                case 64:
                    tags.put("place", "neighbourhood");
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
}
