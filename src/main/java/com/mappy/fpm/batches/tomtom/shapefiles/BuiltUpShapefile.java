package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.LandShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;

public class BuiltUpShapefile extends BoundariesShapefile {
    private final TownTagger townTagger;

    @Inject
    public BuiltUpShapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, TownTagger townTagger) {
        super(folder.getFile("bu.shp"),10 , nameProvider, osmLevelGenerator);
        this.townTagger = townTagger;

        if(new File(folder.getFile("bu.shp")).exists()) {
            nameProvider.loadFromCityFile("smnm.dbf");
        }

    }


    @Override
    public void finishRelation(GeometrySerializer serializer, Map<String, String> adminTags, List<RelationMember> members, Feature feature) {

        TownTagger.Centroid cityCenter = townTagger.getHamlet(feature.getLong("ID"));

        if (cityCenter != null) {
            Map<String, String> tags = newHashMap();

            tags.putAll(of("name", cityCenter.getName(), "landuse", "residential"));
            Optional<String> postcode = Optional.ofNullable(cityCenter.getPostcode());
            postcode.ifPresent(code -> tags.put("addr:postcode", code));
            tags.putAll(nameProvider.getAlternateCityNames(cityCenter.getId()));

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