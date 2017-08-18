package com.mappy.fpm.batches.tomtom;

import com.google.common.base.Stopwatch;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Injector;
import com.mappy.fpm.batches.splitter.Splitter;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.Order;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Slf4j
public class Tomtom2Osm {
    private final GeometrySerializer serializer;
    private final Injector injector;
    private final Splitter splitter;
    private final String outputFile;

    @Inject
    public Tomtom2Osm(GeometrySerializer serializer, Injector injector, Splitter splitter, @Named("com.mappy.fpm.serializer.output") String outputFile) {
        this.serializer = serializer;
        this.injector = injector;
        this.splitter = splitter;
        this.outputFile = outputFile;
    }

    public String run() throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("Running Tomtom2Osm to generate {}", outputFile);
        for (ClassInfo clazz : shapefiles()) {
            log.info("Converting {}", clazz.getSimpleName());
            TomtomShapefile instance = (TomtomShapefile) injector.getInstance(clazz.load());
            instance.serialize(serializer);
        }
        serializer.close();
        log.info("Done generating {} in {}", outputFile, stopwatch);

        stopwatch.reset();
        stopwatch.start();
        splitter.run();
        log.info("Done splitting {} in {}", outputFile, stopwatch);
        return outputFile;
    }

    public List<ClassInfo> shapefiles() throws IOException {
        return ClassPath.from(getClass().getClassLoader()).getTopLevelClasses(getClass().getPackage().getName() + ".shapefiles").stream()
                .filter(clazz -> TomtomShapefile.class.isAssignableFrom(clazz.load())).sorted(conparator()).collect(toList());
    }

    private static Comparator<ClassInfo> conparator() {
        Comparator<ClassInfo> writeFirst = comparing(clazz -> !clazz.load().isAnnotationPresent(Order.class) ? Integer.MAX_VALUE : clazz.load().getAnnotation(Order.class).value() );
        return writeFirst.thenComparing(comparing(ClassInfo::getSimpleName));
    }

}
