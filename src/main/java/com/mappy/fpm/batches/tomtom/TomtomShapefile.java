package com.mappy.fpm.batches.tomtom;

import com.google.common.base.Stopwatch;
import com.mappy.fpm.batches.utils.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.mappy.fpm.batches.GenerateFullPbf.OSM_SUFFIX;
import static java.io.File.separator;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Getter
public abstract class TomtomShapefile {

    private final File file;
    private String outputFile;

    protected TomtomShapefile(String filename) {
        file = new File(filename);
    }

    public void serialize(GeometrySerializer serializer) {

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

            } finally {
                try {
                    serializer.close();
                } catch (IOException e) {
                    log.error("Unable to correctly close serializer.");
                }
            }
        }
        else {
            log.info("File not found : {}", file.getAbsolutePath());
        }
    }

    private static void log(int counter, long time) {
        log.info("Added {} object(s){}", counter, counter > 0 ? " in " + time + " ms at rate " + String.format("%.2f", counter * 1.0 / time) + " obj/ms" : "");
    }

    public final OsmosisSerializer getSerializer(String outputDirectory) throws FileNotFoundException {
        outputFile = outputDirectory + separator + getOutputFileName() + OSM_SUFFIX;
        return new OsmosisSerializer(new BoundComputerAndSorterSink(new PbfSink(new FileOutputStream(outputFile), false)), "Tomtom", DateTime.now().toDate());
    }

    public abstract String getOutputFileName();

    public abstract void serialize(GeometrySerializer serializer, Feature feature);

    public void complete(GeometrySerializer serializer) {}
}