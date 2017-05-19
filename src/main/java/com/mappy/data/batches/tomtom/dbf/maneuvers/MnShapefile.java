package com.mappy.data.batches.tomtom.dbf.maneuvers;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.utils.ShapefileIterator;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.*;

@Slf4j
public class MnShapefile {
    private final TomtomFolder folder;

    @Inject
    public MnShapefile(TomtomFolder folder) {
        this.folder = folder;
    }

    public List<Maneuver> maneuvers() {
        List<Maneuver> maneuvers = newArrayList();
        File mnFile = new File(folder.getFile("mn.dbf"));
        if (!mnFile.exists()) {
            return maneuvers;
        }
        log.info("Reading MP file {}", mnFile.getAbsolutePath());
        try (ShapefileIterator iterator = new ShapefileIterator(mnFile)) {
            while (iterator.hasNext()) {
                Maneuver maneuver = Maneuver.fromFeature(iterator.next());
                if (maneuver.isNotPrivateRoad()) {
                    maneuvers.add(maneuver);
                }
            }
        }
        return maneuvers;
    }
}