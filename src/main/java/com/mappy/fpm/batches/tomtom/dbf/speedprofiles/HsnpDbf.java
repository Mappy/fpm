package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Getter
public class HsnpDbf {

    private final ArrayListMultimap<Long, Speed> speeds;

    @Inject
    public HsnpDbf(TomtomFolder folder) {
        speeds = loadHsnp(folder.getFile("hsnp.dbf"));
    }

    public List<Speed> getById(Long id) {
        return speeds.get(id);
    }

    private static ArrayListMultimap<Long, Speed> loadHsnp(String filename) {
        File file = new File(filename);
        ArrayListMultimap<Long, Speed> speeds = ArrayListMultimap.create();
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return speeds;
        }

        log.info("Reading HSNP {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                Speed speed = parse(row);
                speeds.put(speed.getId(), speed);
            }
        }
        log.info("Loaded {} speed profile", speeds.size());
        return speeds;
    }

    private static Speed parse(DbfRow row) {
        return new Speed( //
                row.getLong("NETWORK_ID"), //
                row.getInt("VAL_DIR"), //
                emptyIfZero(row.getInt("SPFREEFLOW")), //
                emptyIfZero(row.getInt("SPWEEKDAY")), //
                emptyIfZero(row.getInt("SPWEEKEND")), //
                emptyIfZero(row.getInt("SPWEEK")), //
                newArrayList(row.getInt("PROFILE_1"), //
                        row.getInt("PROFILE_2"), //
                        row.getInt("PROFILE_3"), //
                        row.getInt("PROFILE_4"), //
                        row.getInt("PROFILE_5"), //
                        row.getInt("PROFILE_6"), //
                        row.getInt("PROFILE_7")));
    }

    private static Integer emptyIfZero(int i) {
        return i == 0 ? null : i;
    }
}