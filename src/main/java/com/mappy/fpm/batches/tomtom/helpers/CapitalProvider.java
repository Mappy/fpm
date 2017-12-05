package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.utils.ShapefileIterator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@Singleton
public class CapitalProvider {

    private final List<Centroid> allCapitals = newArrayList();

    @Inject
    public CapitalProvider(TomtomFolder tomtomFolder) {
        for (String filePath : tomtomFolder.getSMFiles()) {
            File file = new File(filePath);
            try (ShapefileIterator iterator = new ShapefileIterator(file, true)) {
                while (iterator.hasNext()) {
                    Centroid centroid = Centroid.from(iterator.next());
                    if (centroid.getAdminclass() <= 7) {
                        allCapitals.add(centroid);
                    }
                }
            }
        }
    }

    public List<Centroid> get(int tomtomLevel) {
        return allCapitals.stream().filter(c -> c.getAdminclass() <= tomtomLevel).collect(toList());
    }
}
