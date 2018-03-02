package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Slf4j
public class TimeDomainsProvider extends TomtomDbfReader {

    private final Multimap<Long, TimeDomains> timeDomainsMap = TreeMultimap.create();

    @Inject
    public TimeDomainsProvider(TomtomFolder folder) {
        super(folder);
        readFile("td.dbf", this::getTimeDomains);
    }

    public Collection<TimeDomains> getTimeDomains(long id) {
        return timeDomainsMap.get(id);
    }


    private void getTimeDomains(DbfRow row) {
        TimeDomains restriction = TimeDomains.fromDbf(row);
        timeDomainsMap.put(restriction.getId(), restriction);
    }

}
