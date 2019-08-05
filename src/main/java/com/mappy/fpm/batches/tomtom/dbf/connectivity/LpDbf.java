package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Slf4j
@Getter
public class LpDbf {

    private final ArrayListMultimap<Long, ConnectivityPath> connectivityPaths;

    @Inject
    public LpDbf(TomtomFolder folder) {
        connectivityPaths = lpFile(folder);
    }

    private static ArrayListMultimap<Long, ConnectivityPath> lpFile(TomtomFolder folder) {
        File file = new File(folder.getFile("lp.dbf"));
        ArrayListMultimap<Long, ConnectivityPath> connectivityPaths = ArrayListMultimap.create();
        if (!file.exists()) {
            return connectivityPaths;
        }
        log.info("Reading LP {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                connectivityPaths.put(row.getLong("ID"), ConnectivityPath.fromDbf(row));
            }
        }
        log.info("Loaded {} connectivity paths", connectivityPaths.size());
        return connectivityPaths;
    }
}
