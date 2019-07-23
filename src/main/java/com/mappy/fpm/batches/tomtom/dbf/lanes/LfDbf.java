package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Slf4j
public class LfDbf {

    private final ArrayListMultimap<Long, LaneTrafficFlow> trafficFlow;

    @Inject
    public LfDbf(TomtomFolder folder) {
        trafficFlow = lfFile(folder);
    }

    public boolean containsKey(long id) {
        return trafficFlow.containsKey(id);
    }

    public List<LaneTrafficFlow> get(long id) {
        return trafficFlow.get(id);
    }

    private static ArrayListMultimap<Long, LaneTrafficFlow> lfFile(TomtomFolder folder) {
        File file = new File(folder.getFile("lf.dbf"));
        ArrayListMultimap<Long, LaneTrafficFlow> trafficFlow = ArrayListMultimap.create();
        if (!file.exists()) {
            return trafficFlow;
        }
        log.info("Reading LF {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                trafficFlow.put(row.getLong("ID"), LaneTrafficFlow.fromRow(row));
            }
        }
        log.info("Loaded {} lane directions of traffic flow", trafficFlow.size());
        return trafficFlow;
    }
}
