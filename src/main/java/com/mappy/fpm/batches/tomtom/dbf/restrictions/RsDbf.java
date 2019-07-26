package com.mappy.fpm.batches.tomtom.dbf.restrictions;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.restrictions.Restriction;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Slf4j
public class RsDbf {

    private final ArrayListMultimap<Long, Restriction> restrictionsMap;

    @Inject
    public RsDbf(TomtomFolder folder) {
        restrictionsMap = loadRestrictions(folder.getFile("rs.dbf"));
    }

    private static ArrayListMultimap<Long, Restriction> loadRestrictions(String filename) {
        ArrayListMultimap<Long, Restriction> restrictions = ArrayListMultimap.create();
        File file = new File(filename);
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return restrictions;
        }
        log.info("Reading RS {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                Restriction restriction = Restriction.fromRow(row);
                restrictions.put(restriction.getId(), restriction);
            }
        }
        log.info("Loaded {} restrictions", restrictions.size());

        return restrictions;
    }

    public List<Restriction> getRestrictions(long id) {
        return restrictionsMap.get(id);
    }
}
