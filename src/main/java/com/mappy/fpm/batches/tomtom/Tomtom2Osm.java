package com.mappy.fpm.batches.tomtom;

import com.google.common.base.Stopwatch;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Injector;
import com.mappy.fpm.batches.merge.pbf.OsmMerger;
import com.mappy.fpm.batches.splitter.Splitter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.GenerateFullPbf.OSM_SUFFIX;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@Slf4j
public class Tomtom2Osm {

    private final Injector injector;
    private final OsmMerger osmMerger;
    private final Splitter splitter;
    private final String outputZone;

    @Inject
    public Tomtom2Osm(Injector injector, OsmMerger osmMerger, Splitter splitter, @Named("com.mappy.fpm.serializer.output") String outputZone) {
        this.injector = injector;
        this.osmMerger = osmMerger;
        this.splitter = splitter;
        this.outputZone = outputZone;
    }

    public Optional<String> run() throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("Start generating {}", outputZone);
        new File(outputZone).mkdirs();

        List<String> profileShapeFiles = newArrayList();

        for (ClassInfo clazz : shapefiles()) {
            log.info("Converting {}", clazz.getSimpleName());
            TomtomShapefile shapefile = (TomtomShapefile) injector.getInstance(clazz.load());
            if (shapefile.getFile().exists()) {
                shapefile.serialize(outputZone);
                profileShapeFiles.add(shapefile.getOutputFile());
            } else {
                log.info("No input file found");
            }
        }

        if (profileShapeFiles.isEmpty()) {
            return empty();
        }

        osmMerger.merge(profileShapeFiles, outputZone + OSM_SUFFIX);
        log.info("Done generating {} in {}", outputZone + OSM_SUFFIX, stopwatch);

        stopwatch.reset();
        stopwatch.start();

        splitter.run();
        log.info("Done splitting {} in {}", outputZone, stopwatch);

        return of(outputZone + OSM_SUFFIX);
    }

    private List<ClassInfo> shapefiles() throws IOException {
        return ClassPath.from(getClass().getClassLoader()).getTopLevelClasses(getClass().getPackage().getName() + ".shapefiles").stream()
                .filter(clazz -> TomtomShapefile.class.isAssignableFrom(clazz.load())).collect(toList());
    }
}
