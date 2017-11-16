package com.mappy.fpm.batches.naturalearth;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.google.inject.name.Names.named;
import static com.mappy.fpm.batches.GenerateFullPbf.OSM_SUFFIX;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Slf4j
public class NaturalEarth2Pbf {
    private final GeometrySerializer serializer;
    private final Injector injector;

    @Inject
    public NaturalEarth2Pbf(GeometrySerializer serializer, Injector injector) {
        this.serializer = serializer;
        this.injector = injector;
    }

    public void run() throws IOException {
        for (ClassInfo clazz : shapefiles()) {
            log.info("Converting {}", clazz.getSimpleName());
            NaturalEarthShapefile instance = (NaturalEarthShapefile) injector.getInstance(clazz.load());
            instance.serialize(serializer);
        }
        serializer.close();
    }

    public List<ClassInfo> shapefiles() throws IOException {
        return ClassPath.from(getClass().getClassLoader())
                .getTopLevelClasses(getClass().getPackage().getName() + ".shapefiles").stream()
                .filter(clazz -> NaturalEarthShapefile.class.isAssignableFrom(clazz.load()))
                .sorted(comparing(ClassInfo::getSimpleName))
                .collect(toList());
    }

    public static class NaturalEarthModule extends AbstractModule {
        private final String input;

        public NaturalEarthModule(String input) {
            this.input = input;
        }

        @Override
        protected void configure() {
            bindConstant().annotatedWith(named("com.mappy.fpm.geonames")).to(input + "/geonames");
            bindConstant().annotatedWith(named("com.mappy.fpm.naturalearth.data")).to(input + "/naturalearth");
            bindConstant().annotatedWith(named("com.mappy.fpm.serializer.output")).to(input + "/naturalearth" + OSM_SUFFIX);
            bindConstant().annotatedWith(named("com.mappy.fpm.serializer.username")).to("NaturalEarth");
        }
    }
}
