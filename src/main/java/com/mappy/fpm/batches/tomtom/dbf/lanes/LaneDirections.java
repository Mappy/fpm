package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.google.common.collect.ArrayListMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.joining;

@Slf4j
public class LaneDirections {
    private final Map<Long, List<String>> tags;

    @Inject
    public LaneDirections(TomtomFolder folder) {
        tags = ldFile(folder);
    }

    public boolean containsKey(long id) {
        return tags.containsKey(id);
    }

    public List<String> get(long id) {
        return tags.get(id);
    }

    private static Map<Long, List<String>> ldFile(TomtomFolder folder) {
        File file = new File(folder.getFile("ld.dbf"));
        if (!file.exists()) {
            return newHashMap();
        }
        ArrayListMultimap<Long, LaneDirection> directions = ArrayListMultimap.create();
        log.info("Reading LD file {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                directions.put(row.getLong("ID"), LaneDirection.parse(row.getInt("DIRECTION"), row.getString("VALIDITY")));
            }
        }
        Map<Long, List<String>> tags = newHashMap();
        for (Long id : directions.keySet()) {
            tags.put(id, asText(directions.get(id)));
        }
        log.info("Loaded {} lane directions", directions.size());
        return tags;
    }

    private static List<String> asText(List<LaneDirection> directions) {
        Map<Integer, LaneDirection> byPosition = newHashMap();
        int max = -1;
        for (LaneDirection direction : directions) {
            for (Integer pos : direction.getValidity()) {
                byPosition.put(pos, direction);
                max = Math.max(max, pos);
            }
        }
        checkState(max >= 0);
        List<String> result = newArrayList();
        for (int i = 0; i <= max; i++) {
            result.add(byPosition.containsKey(i) ? byPosition.get(i).getDirections().stream().map(direction -> direction.text).collect(joining(";")) : "none");
        }
        return result;
    }
}