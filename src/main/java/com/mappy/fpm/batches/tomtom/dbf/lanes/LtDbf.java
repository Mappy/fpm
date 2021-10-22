package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Slf4j
public class LtDbf {

    public enum RestrictionType {
        directionOfTrafficFlow, laneType, speedRestriction;

        public static RestrictionType fromCode(String code) {
            switch (code) {
                case "LF":
                    return RestrictionType.directionOfTrafficFlow;
                case "LI":
                    return RestrictionType.laneType;
                case "SL":
                    return RestrictionType.speedRestriction;
                default:
                    throw new RuntimeException("Unknown restriction type: " + code);
            }
        }
    }

    private final Map<RestrictionType, Map<Long, Map<Integer, TimeDomains>>> timeDomains;

    @Inject
    public LtDbf(TomtomFolder folder) {
        timeDomains = readFile(folder);
    }

    public TimeDomains getTimeDomains(RestrictionType restrictionType, long id, int sequenceNumber) {
        Map<Integer, TimeDomains> sectionTimeDomains = timeDomains.get(restrictionType).get(id);
        if (sectionTimeDomains == null) {
            return null;
        }
        return sectionTimeDomains.get(sequenceNumber);
    }

    private Map<RestrictionType, Map<Long, Map<Integer, TimeDomains>>> readFile(TomtomFolder folder) {
        File file = new File(folder.getFile("lt.dbf"));
        Map<RestrictionType, Map<Long, Map<Integer, TimeDomains>>> timeDomains = newHashMap();
        Map<RestrictionType, Integer> typeEntryCount = newHashMap();
        for (RestrictionType restrictionType: RestrictionType.values()) {
            timeDomains.put(restrictionType, newHashMap());
            typeEntryCount.put(restrictionType, 0);
        }
        if (!file.exists()) {
            return timeDomains;
        }
        log.info("Reading LT {}", file);

        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                TimeDomains timeDomain = TimeDomains.fromDbf(row);
                long sectionId = row.getLong("ID");
                RestrictionType restrictionType = RestrictionType.fromCode(row.getString("RELRES"));

                Map<Long, Map<Integer, TimeDomains>> typeTimeDomains = timeDomains.get(restrictionType);
                if(!typeTimeDomains.containsKey(sectionId)) {
                    typeTimeDomains.put(sectionId, newHashMap());
                }
                Map<Integer, TimeDomains> sectionTimeDomains = typeTimeDomains.get(sectionId);
                int sequenceNumber = timeDomain.getSequenceNumber();
                TimeDomains existingTimeDomain = sectionTimeDomains.putIfAbsent(sequenceNumber, timeDomain);
                if (existingTimeDomain != null) {
                    String message = "Several time domains for the same restriction (section id: %d, sequence number %d). Only take the first one into consideration";
                    log.warn(message);
                    continue;
                }
                typeEntryCount.put(restrictionType, typeEntryCount.get(restrictionType) + 1);
            }
        }
        log.info(
            "Loaded {} restrictions, {} speed restrictions and {} lane type time domains",
            typeEntryCount.get(RestrictionType.directionOfTrafficFlow),
            typeEntryCount.get(RestrictionType.laneType),
            typeEntryCount.get(RestrictionType.speedRestriction)
        );
        return timeDomains;
    }
}
