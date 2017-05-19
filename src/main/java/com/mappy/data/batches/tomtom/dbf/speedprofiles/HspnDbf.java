package com.mappy.data.batches.tomtom.dbf.speedprofiles;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.data.batches.tomtom.TomtomFolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Slf4j
public class HspnDbf {
    @Getter
    private final ArrayListMultimap<Long, Speed> speeds;

    @Inject
    public HspnDbf(TomtomFolder folder) {
        speeds = loadHsnp(folder.getFile("hsnp.dbf"));
    }

    public List<Speed> getById(Long id) {
        return speeds.get(id);
    }

    private static ArrayListMultimap<Long, Speed> loadHsnp(String filename) {
        File file = new File(filename);
        ArrayListMultimap<Long, Speed> speeds = ArrayListMultimap.create();
        if (!file.exists()) {
            return speeds;
        }

        log.info("Reading HSPN {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                Speed speed = parse(row);
                speeds.put(speed.getId(), speed);
            }
        }
        log.info("Loaded {} hsnp", speeds.size());
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
                new int[] { row.getInt("PROFILE_1"), //
                        row.getInt("PROFILE_2"), //
                        row.getInt("PROFILE_3"), //
                        row.getInt("PROFILE_4"), //
                        row.getInt("PROFILE_5"), //
                        row.getInt("PROFILE_6"), //
                        row.getInt("PROFILE_7") });
    }

    private static Optional<Integer> emptyIfZero(int i) {
        return i == 0 ? Optional.empty() : Optional.of(i);
    }
}