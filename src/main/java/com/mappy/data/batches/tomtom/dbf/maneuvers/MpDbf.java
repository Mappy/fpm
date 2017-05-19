package com.mappy.data.batches.tomtom.dbf.maneuvers;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;

import java.io.File;
import java.util.List;

import org.jamel.dbf.processor.DbfProcessor;

import static com.google.common.collect.Lists.*;

@Slf4j
public class MpDbf {
    private final TomtomFolder folder;

    @Inject
    public MpDbf(TomtomFolder folder) {
        this.folder = folder;
    }

    public List<ManeuverPath> paths() {
        File mpFile = new File(folder.getFile("mp.dbf"));
        if (!mpFile.exists()) {
            return newArrayList();
        }
        log.info("Reading MP file {}", mpFile);
        List<ManeuverPath> paths = newArrayList();
        DbfProcessor.processDbf(mpFile, row -> {
            ManeuverPath path = ManeuverPath.fromDbf(row);
            if (path.isStreet()) {
                paths.add(path);
            }
        });
        return paths;
    }
}