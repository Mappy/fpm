package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Slf4j
public class LnDbf {

    private final ArrayListMultimap<Long, ConnectivityInformation> connectivities;

    @Inject
    public LnDbf(TomtomFolder folder) {
        connectivities = lnFile(folder);
    }

    public boolean containsKey(long id) {
        return connectivities.containsKey(id);
    }

    public List<ConnectivityInformation> get(long id) {
        return connectivities.get(id);
    }

    private static ArrayListMultimap<Long, ConnectivityInformation> lnFile(TomtomFolder folder) {
        File file = new File(folder.getFile("ln.dbf"));
        ArrayListMultimap<Long, ConnectivityInformation> connectivities = ArrayListMultimap.create();
        if (!file.exists()) {
            return connectivities;
        }
        log.info("Reading LN {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                connectivities.put(row.getLong("ID"), ConnectivityInformation.fromDbf(row));
            }
        }
        log.info("Loaded {} connectivity informations", connectivities.size());
        return connectivities;
    }
}
