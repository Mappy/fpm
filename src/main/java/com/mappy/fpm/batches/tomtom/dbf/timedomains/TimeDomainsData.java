package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;

import static org.jamel.dbf.processor.DbfProcessor.processDbf;

@Slf4j
public class TimeDomainsData {

    private final Multimap<Long, TimeDomains> timeDomainsMap;

    @Inject
    public TimeDomainsData(TomtomFolder folder) {
        timeDomainsMap = loadTimeDomains(folder.getFile("td.dbf"));
    }

    public Collection<TimeDomains> getTimeDomains(long id) {
        return timeDomainsMap.get(id);
    }

    private Multimap<Long, TimeDomains> loadTimeDomains(String filename) {
        Multimap<Long, TimeDomains> times = TreeMultimap.create();
        File file = new File(filename);
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return times;
        }

        log.info("Reading TD {}", file);
        processDbf(file, row -> {
            TimeDomains restriction = new TimeDomains(((Double)row[0]).longValue(), new String((byte[]) row[3]).trim());
            times.put(restriction.getId(), restriction);
        });

        log.info("Loaded {} times domains", times.size());

        return times;
    }
}
