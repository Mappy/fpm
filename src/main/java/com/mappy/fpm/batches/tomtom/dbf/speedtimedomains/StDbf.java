package com.mappy.fpm.batches.tomtom.dbf.speedtimedomains;

import com.google.common.collect.Maps;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

@Slf4j
public class StDbf {

    private final Map<Long, Map<Integer, TimeDomains>> speedTimeDomainsMap;

    @Inject
    public StDbf(TomtomFolder folder) {
        speedTimeDomainsMap = loadSpeedTimeDomains(folder.getFile("st.dbf"));
    }

    private static Map<Long, Map<Integer, TimeDomains>> loadSpeedTimeDomains(String filename) {
        Map<Long, Map<Integer, TimeDomains>> timeDomains = Maps.newHashMap();
        File file = new File(filename);
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return timeDomains;
        }
        log.info("Reading ST {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                TimeDomains timeDomain = TimeDomains.fromDbf(row);
                long sectionId = timeDomain.getId();
                if (!timeDomains.containsKey(sectionId)) {
                    timeDomains.put(sectionId, Maps.newHashMap());
                }
                timeDomains.get(sectionId).put(timeDomain.getSequenceNumber(), timeDomain);
            }
        }
        log.info("Loaded {} time domains", timeDomains.size());

        return timeDomains;
    }

    public TimeDomains getSpeedTimeDomain(long id, int sequenceNumber) {
        Map<Integer, TimeDomains> sectionSpeedMap = speedTimeDomainsMap.get(id);
        if (sectionSpeedMap == null) {
            return null;
        }
        return sectionSpeedMap.get(sequenceNumber);
    }
}
