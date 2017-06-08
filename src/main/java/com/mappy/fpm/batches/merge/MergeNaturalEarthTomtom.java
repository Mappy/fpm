package com.mappy.fpm.batches.merge;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.mappy.fpm.batches.naturalearth.NaturalEarth2Pbf;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.mappy.fpm.batches.utils.ShapefileWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import static com.google.inject.Guice.createInjector;
import static com.mappy.fpm.batches.merge.PolygonsUtils.polygons;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MergeNaturalEarthTomtom {

    private final GeometrySerializer serializer;
    private final TomtomWorldFactory tomtomFactory;
    private final NaturalEarthWorldFactory naturalEarthFactory;
    private final Injector injector;
    private final MultiPolygon poly;

    @Inject
    public MergeNaturalEarthTomtom(
            GeometrySerializer serializer,
            TomtomWorldFactory tomtomFactory,
            NaturalEarthWorldFactory naturalEarthFactory,
            Injector injector, MultiPolygon poly) {
        this.serializer = serializer;
        this.tomtomFactory = tomtomFactory;
        this.naturalEarthFactory = naturalEarthFactory;
        this.injector = injector;
        this.poly = poly;
    }

    public void run() throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        TomtomWorld tomtom = tomtomFactory.loadTomtom();
        NaturalEarthWorld naturalEarth = naturalEarthFactory.loadNaturalEarth(tomtom);

        List<Country> concat = ImmutableList.<Country> builder().addAll(naturalEarth.getCountries()).addAll(tomtom.getCountries()).build();

        for (Country country : concat) {
            List<RelationMember> members = Lists.newArrayList();
            for (Polygon polygon : polygons(country.getGeometry())) {
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {
                    Way way = serializer.write((LineString) geom, ImmutableMap.of(//
                            "boundary", "administrative", //
                            "admin_level", "2"));
                    members.add(new RelationMember(way.getId(), Way, "outer"));
                }
            }
            serializer.writeRelation(members, ImmutableMap.of( //
                    "type", "boundary", //
                    "boundary", "administrative", //
                    "admin_level", "2", //
                    "name", country.getName()));
        }

        ShapefileWriter.write(
                new File("/workspace/lands.shp"),
                typeBuilder -> {
                    typeBuilder.add("the_geom", MultiPolygon.class);
                    typeBuilder.add("name", String.class);
                },
                (featureBuilder, country) -> {
                    featureBuilder.add(country.getGeometry());
                    featureBuilder.add(country.getName());
                },
                concat);

        new NaturalEarth2Pbf(new FilteredGeometrySerializer(tomtom, serializer, poly), injector).run();

        new Oceans().generate(new File("/workspace/lands.shp"));

        log.info("Time: {}s", stopwatch.elapsed(SECONDS));
    }

    public static void main(String[] args) throws IOException {
        createInjector(new MergeNaturalEarthTomtomModule()).getInstance(MergeNaturalEarthTomtom.class).run();
    }
}
