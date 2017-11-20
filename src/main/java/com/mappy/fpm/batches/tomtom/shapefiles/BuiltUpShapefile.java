package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger.Centroid;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.Geohash;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.vividsolutions.jts.geom.Point;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;

public class BuiltUpShapefile extends BoundariesShapefile {

    private final TownTagger townTagger;
    private String cityType;

    @Inject
    public BuiltUpShapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, TownTagger townTagger) {
        super(folder.getFile("bu.shp"), 10, nameProvider, osmLevelGenerator);
        this.townTagger = townTagger;
        this.cityType = "";

        if (new File(folder.getFile("bu.shp")).exists()) {
            nameProvider.loadFromCityFile("smnm.dbf");
        }
    }

    @Override
    public String getOutputFileName() {
        return "bu";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        Centroid cityCenter = townTagger.getHamlet(feature.getLong("ID"));

        if (cityCenter != null) {
            switch (cityCenter.getCitytyp()) {
                case 0:
                    cityType = "village";
                    break;
                case 1:
                    cityType = cityCenter.getDispclass() < 8 ? "city" : "town";
                    break;
                case 32:
                    cityType = "hamlet";
                    break;
                case 64:
                    cityType = "neighbourhood";
                    break;
                default:
                    cityType = "";
                    break;
            }
        }

        super.serialize(serializer, feature);
    }

    @Override
    protected void finishRelation(GeometrySerializer serializer, Map<String, String> adminTags, List<RelationMember> members, Feature feature) {
        Centroid cityCenter = townTagger.getHamlet(feature.getLong("ID"));

        if (cityCenter != null) {

            Map<String, String> tags = newHashMap();

            tags.put("place", cityType);
            tags.putAll(nameProvider.getAlternateCityNames(cityCenter.getId()));
            ofNullable(cityCenter.getPostcode()).ifPresent(code -> tags.put("addr:postcode", code));

            Long adminCenter;
            Point point = cityCenter.getPoint();
            if (!serializer.containPoint(point)) {
                Optional<Node> node = serializer.writePoint(point, tags);
                adminCenter = node.map(Entity::getId).orElse(0L);
            } else {
                adminCenter = Geohash.encodeGeohash(0, point.getX(), point.getY());
            }
            members.add(new RelationMember(adminCenter, Node, "admin_center"));
            serializer.writeRelation(members, adminTags);
        }
    }

    @NotNull
    protected Map<String, String> putWayTags(String name) {
        return of("name", name, "place", cityType);
    }

    protected void putRelationTags(Map<String, String> tags, Map<String, String> wayTags) {
        tags.put("type", "multipolygon");
        tags.put("name", wayTags.get("name"));
        tags.put("landuse", "residential");
        tags.put("layer", "10");
    }
}