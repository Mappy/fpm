package com.mappy.fpm.batches.tomtom.dbf;

import com.google.common.base.Stopwatch;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class TomtomDbfReader {

    private final TomtomFolder folder;

    public TomtomDbfReader(TomtomFolder folder) {
        this.folder = folder;
    }

    protected void readFile(String filename, Consumer<DbfRow> fun) {
        File file = new File(folder.getFile(filename));

        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return;
        }

        log.info("Reading {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            Stopwatch stopwatch = Stopwatch.createStarted();
            int counter = 0;

            while ((row = reader.nextRow()) != null) {
                fun.accept(row);
                counter++;
            }
            long time = stopwatch.elapsed(MILLISECONDS);
            stopwatch.stop();
            log.info("Added {} object(s){}", counter, counter > 0 ? " in " + time + " ms at rate " + String.format("%.2f", counter * 1.0 / time) + " obj/ms" : "");
        }
    }
}
