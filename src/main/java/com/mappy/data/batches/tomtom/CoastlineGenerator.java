package com.mappy.data.batches.tomtom;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.OsmosisSerializer;
import com.mappy.data.batches.utils.ShapefileIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.propagate;
import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.filefilter.FileFilterUtils.directoryFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;

@Slf4j
public class CoastlineGenerator {

    private static final ImmutableMap<String, String> COASTLINE_TAG = ImmutableMap.of("natural", "coastline");
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final Polygon bbox = world();

    private final String tomtomFolder;
    private final OsmosisSerializer serializer;
    private final String workspace;

    public static void main(String[] args) {
        checkState(args.length == 2, "Usage : java CoastlineGenerator <tomtomFilesRoot> <workspace containing naturalearth folder>");
        createInjector(getModule(args[0], args[1])).getInstance(CoastlineGenerator.class).run();
    }

    @Inject
    public CoastlineGenerator(@Named("com.mappy.data.coastline.tomtomFolder") String tomtomFolder, //
                              @Named("com.mappy.data.coastline.workspace") String workspace, //
                              OsmosisSerializer serializer) {
        this.tomtomFolder = tomtomFolder;
        this.workspace = workspace;
        this.serializer = serializer;
    }

    public void run() {
        File rootFolder = new File(tomtomFolder);
        List<String> countries = asList(rootFolder.list(directoryFileFilter()));
        log.info("Listing tomtom files for {} countries : {} ", countries.size(), countries);
        List<Geometry> allPolygons = countries.stream() //
                .map(country -> new File(rootFolder.getAbsolutePath() + "/" + country)) //
                .filter(CoastlineGenerator::hasA0Files) //
                .map(file -> new File(file, getA0Files(file))) //
                .flatMap(file -> readFeatures(file).stream()) //
                .map(Feature::getGeometry) //
                .map(CoastlineGenerator::cropIfNeeded) //
                .collect(toList());

        Geometry naturalEarthPolygon = getNaturalEarthGeometry();
        log.info("Merging {} tomtom polygons with naturalEarth polygon", allPolygons.size());
        allPolygons.add(naturalEarthPolygon);
        MultiPolygon coastlineMultipolygon = (MultiPolygon) new CascadedPolygonUnion(allPolygons).union().reverse();

        log.info("Serializing coastlines.");
        for (int i = 0; i < coastlineMultipolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) coastlineMultipolygon.getGeometryN(i);
            serializer.write(GEOMETRY_FACTORY.createPolygon(polygon.getExteriorRing().getCoordinateSequence()), COASTLINE_TAG);
            if (polygon.getNumInteriorRing() != 0) {
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    serializer.write(GEOMETRY_FACTORY.createPolygon(polygon.getInteriorRingN(j).getCoordinateSequence()), COASTLINE_TAG);
                }
            }
        }
        serializer.close();
    }

    private Geometry getNaturalEarthGeometry() {
        List<Geometry> naturalEarthGeometries = readFeatures(new File(workspace + "/naturalearth/ne_10m_land.shp")).stream().map(Feature::getGeometry).collect(toList());
        MultiPolygon multiPolygon = (MultiPolygon) naturalEarthGeometries.get(0);
        return cropIfNeeded(multiPolygon);
    }

    private static List<Feature> readFeatures(File file) {
        log.info("Loading features from {}", file.getAbsolutePath());
        ShapefileIterator shapefileIterator = new ShapefileIterator(file);
        List<Feature> naturalEarthGeometries = shapefileIterator.stream().collect(toList());
        shapefileIterator.close();
        return naturalEarthGeometries;
    }

    private static Geometry cropIfNeeded(Geometry polygon) {
        if (bbox.contains(polygon)) {
            return polygon;
        }
        return bbox.intersection(polygon);
    }

    private static Polygon world() {
        try {
            return (Polygon) new WKTReader().read("POLYGON((-179.9999 89.9999,179.9999 89.9999,179.9999 -89.9999,-179.9999 -89.9999,-179.9999 89.9999))");
        } catch (ParseException e) {
            throw propagate(e);
        }
    }

    private static boolean hasA0Files(File file) {
        String[] a0Files = file.list(suffixFileFilter("______________a0.shp"));

        if (a0Files != null && a0Files.length > 0) {
            return true;
        }
        log.info("No A0 files found for={}", file);
        return false;
    }

    private static String getA0Files(File file) {
        String[] a0Files = file.list(suffixFileFilter("______________a0.shp"));
        return a0Files[0];
    }

    private static Module getModule(String tomtomFolder, String workspace) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named("com.mappy.data.coastline.workspace")).to(workspace);
                bindConstant().annotatedWith(Names.named("com.mappy.data.coastline.tomtomFolder")).to(tomtomFolder);
                bindConstant().annotatedWith(Names.named("com.mappy.data.serializer.output")).to(workspace + "/coastline.osm.pbf");
                bindConstant().annotatedWith(Names.named("com.mappy.data.serializer.username")).to("Mappy");
            }
        };
    }
}
