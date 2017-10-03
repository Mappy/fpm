package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;

public class BoundariesA9Shapefile extends BoundariesShapefile {

    private final TownTagger townTagger;

    @Inject
    public BoundariesA9Shapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, TownTagger townTagger) {
        super(folder.getFile("___a9.shp"), 9, nameProvider, osmLevelGenerator);
        this.townTagger = townTagger;
        nameProvider.loadFromCityFile("smnm.dbf", "NAME", false);
    }

    @Override
    public void finishRelation(GeometrySerializer serializer, Map<String, String> adminTags, List<RelationMember> members, Feature feature) {

        TownTagger.Centroid cityCenter = townTagger.get(feature.getLong("CITYCENTER"));

        if (cityCenter != null) {
            Map<String, String> tags = newHashMap();

            tags.put("name", cityCenter.getName());
            tags.putAll(nameProvider.getAlternateCityNames(cityCenter.getId()));

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

            switch (cityCenter.getCitytyp()) {
                case 0:
                    tags.put("place", "village");
                    break;
                case 1:
                    tags.put("place", cityCenter.getDispclass() < 8 ? "city" : "town");
                    break;
                case 32:
                    tags.put("place", "hamlet");
                    break;
                case 64:
                    tags.put("place", "neighbourhood");
                    break;
            }

            if(serializer.containPoint(cityCenter.getPoint())) {
                cityCenter.getPoint().getCoordinate().x = cityCenter.getPoint().getCoordinate().x +0.000001;
                cityCenter.getPoint().getCoordinate().y = cityCenter.getPoint().getCoordinate().y +0.000001;
            }
            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), tags);
            node.ifPresent(adminCenter -> members.add(new RelationMember(adminCenter.getId(), EntityType.Node, "admin_center")));
        }

        serializer.writeRelation(members, adminTags);
    }
}
