package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.jamel.dbf.processor.DbfProcessor.processDbf;

@Slf4j
public class MpDbf {

    private final TomtomFolder folder;

    @Inject
    public MpDbf(TomtomFolder folder) {
        this.folder = folder;
    }

    public List<ManeuverPath> paths() {
        List<ManeuverPath> paths = newArrayList();
        File file = new File(folder.getFile("mp.dbf"));
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return paths;
        }

        log.info("Reading MP {}", file);
        processDbf(file, row -> {
            ManeuverPath path = ManeuverPath.fromDbf(row);
            if (path.isStreet()) {
                paths.add(path);
            }
        });

        log.info("Loaded {} maneuver path", paths.size());
        return paths;
    }
}