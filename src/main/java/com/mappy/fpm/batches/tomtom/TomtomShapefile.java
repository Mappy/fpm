package com.mappy.fpm.batches.tomtom;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Stopwatch;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.ShapefileIterator;

import java.io.File;

import static java.util.concurrent.TimeUnit.*;

@Slf4j
public abstract class TomtomShapefile {

    private final String filename;

    protected TomtomShapefile(String filename) {
        this.filename = filename;
    }

    public void serialize(GeometrySerializer serializer) {
        File file = new File(filename);
        if (file.exists()) {
            log.info("Opening {}", file.getAbsolutePath());
            try (ShapefileIterator iterator = new ShapefileIterator(file, true)) {
                Stopwatch stopwatch = Stopwatch.createStarted();
                int counter = 0;
                while (iterator.hasNext()) {
                    serialize(serializer, iterator.next());
                    counter++;
                }
                log(counter, stopwatch.elapsed(MILLISECONDS));
                complete(serializer);
            }
        }
        else {
            log.info("File not found : {}", file.getAbsolutePath());
        }
    }

    private static void log(int counter, long time) {
        log.info("Added {} object(s){}", counter, counter > 0 ? " in " + time + " ms at rate " + String.format("%.2f", counter * 1.0 / time) + " obj/ms" : "");
    }

    public abstract void serialize(GeometrySerializer serializer, Feature feature);

    public void complete(GeometrySerializer serializer) {}
}