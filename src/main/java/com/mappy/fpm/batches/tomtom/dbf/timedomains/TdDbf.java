package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Slf4j
public class TdDbf {

    private final ArrayListMultimap<Long, TimeDomains> timeDomainsMap;

    @Inject
    public TdDbf(TomtomFolder folder) {
        timeDomainsMap = loadTimeDomains(folder.getFile("td.dbf"));
    }

    public List<TimeDomains> getTimeDomains(long id) {
        return timeDomainsMap.get(id);
    }

    private static ArrayListMultimap<Long, TimeDomains> loadTimeDomains(String filename) {
        ArrayListMultimap<Long, TimeDomains> times = ArrayListMultimap.create();
        File file = new File(filename);
        if (!file.exists()) {
            return times;
        }
        log.info("Reading TD {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                TimeDomains restriction = new TimeDomains(row.getLong("ID"), row.getString("TIMEDOM"));
                times.put(restriction.getId(), restriction);
            }
        }
        log.info("Loaded {} times domains", times.size());

        return times;
    }
}
