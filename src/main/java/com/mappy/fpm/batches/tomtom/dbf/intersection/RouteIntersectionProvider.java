package com.mappy.fpm.batches.tomtom.dbf.intersection;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Singleton
public class RouteIntersectionProvider extends TomtomDbfReader {

    @Inject
    public RouteIntersectionProvider(TomtomFolder folder) {
        super(folder);
    }

    public Map<Long,String> getIntercetionsById() {
        Map<Long, Long> igMap = new HashMap<>() ;
        Map<Long, String> isMap = new HashMap<>() ;
        readFile("ig.dbf", row -> igMap.put(row.getLong("ELEMID"), row.getLong("ID")));
        readFile("is.dbf", row -> isMap.put(row.getLong("ID"), row.getString("NAME")));
        return igMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> isMap.get(entry.getValue())));
    }
}
