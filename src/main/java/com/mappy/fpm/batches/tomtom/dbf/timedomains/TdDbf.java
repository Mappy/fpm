package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;

@Slf4j
public class TdDbf {

    private final Multimap<Long, TimeDomains> timeDomainsMap;

    @Inject
    public TdDbf(TomtomFolder folder) {
        timeDomainsMap = loadTimeDomains(folder.getFile("td.dbf"));
    }

    public Collection<TimeDomains> getTimeDomains(long id) {
        return timeDomainsMap.get(id);
    }

    private static Multimap<Long, TimeDomains> loadTimeDomains(String filename) {
        Multimap<Long, TimeDomains> times = TreeMultimap.create();
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
