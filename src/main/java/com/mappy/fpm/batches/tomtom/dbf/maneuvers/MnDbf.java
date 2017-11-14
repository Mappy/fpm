package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.jamel.dbf.processor.DbfProcessor.processDbf;

@Slf4j
public class MnDbf {

    private final TomtomFolder folder;

    @Inject
    public MnDbf(TomtomFolder folder) {
        this.folder = folder;
    }

    public List<Maneuver> maneuvers() {
        List<Maneuver> maneuvers = newArrayList();
        File file = new File(folder.getFile("mn.dbf"));
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return maneuvers;
        }

        log.info("Reading MN {}", file.getAbsolutePath());
        processDbf(file, row -> {
            Maneuver maneuver = Maneuver.fromFeature(row);
            if (maneuver.isNotPrivateRoad()) {
                maneuvers.add(maneuver);
            }
        });

        log.info("Loaded {} maneuvers", maneuvers.size());

        return maneuvers;
    }
}