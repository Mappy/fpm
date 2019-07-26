package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;

import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class TimeDomainsProvider extends TomtomDbfReader {

    private final Map<Long, ArrayListMultimap<Integer, TimeDomains>> timeDomainsMap = Maps.newHashMap();

    @Inject
    public TimeDomainsProvider(TomtomFolder folder) {
        super(folder);
        readFile("td.dbf", this::getTimeDomains);
    }

    public ArrayListMultimap<Integer, TimeDomains> getSectionTimeDomains(long id) {
        return timeDomainsMap.get(id);
    }

    private void getTimeDomains(DbfRow row) {
        TimeDomains timeDomain = TimeDomains.fromDbf(row);
        long sectionId = timeDomain.getId();
        if (!timeDomainsMap.containsKey(sectionId)) {
            timeDomainsMap.put(sectionId, ArrayListMultimap.create());
        }
        ArrayListMultimap<Integer, TimeDomains> sectionTimeDomains = timeDomainsMap.get(sectionId);

        int sequenceNumber = timeDomain.getSequenceNumber();
        sectionTimeDomains.put(sequenceNumber, timeDomain);
    }
}
