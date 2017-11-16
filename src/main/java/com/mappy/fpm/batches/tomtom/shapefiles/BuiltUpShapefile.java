package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.jetbrains.annotations.NotNull;
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
import static java.util.Optional.ofNullable;

public class BuiltUpShapefile extends BoundariesShapefile {

    private final TownTagger townTagger;
    private String cityType;
    private TownTagger.Centroid cityCenter;
    private Map<String, String> tags;

    @Inject
    public BuiltUpShapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, TownTagger townTagger) {
        super(folder.getFile("bu.shp"), 10, nameProvider, osmLevelGenerator);
        this.townTagger = townTagger;
        this.cityType = "";
        this.cityCenter = null;
        this.tags = newHashMap();

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
        cityCenter = townTagger.getHamlet(feature.getLong("ID"));

        if (cityCenter != null) {
            tags.putAll(of("name", cityCenter.getName(), "landuse", "residential"));
            ofNullable(cityCenter.getPostcode()).ifPresent(code -> tags.put("addr:postcode", code));
            tags.putAll(nameProvider.getAlternateCityNames(cityCenter.getId()));

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
            }

            tags.put("place", cityType);

            if (serializer.containPoint(cityCenter.getPoint())) {
                cityCenter.getPoint().getCoordinate().x = cityCenter.getPoint().getCoordinate().x + 0.000001;
                cityCenter.getPoint().getCoordinate().y = cityCenter.getPoint().getCoordinate().y + 0.000001;
            }
        }

        super.serialize(serializer, feature);
    }

    @Override
    protected void finishRelation(GeometrySerializer serializer, Map<String, String> adminTags, List<RelationMember> members, Feature feature) {
        if (cityCenter != null) {
            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), tags);
            node.ifPresent(adminCenter -> members.add(new RelationMember(adminCenter.getId(), EntityType.Node, "admin_center")));
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