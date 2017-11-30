package com.mappy.fpm.batches.tomtom.helpers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;

@Singleton
@Slf4j
public class TollReader {

    private final Map<Long, Toll> tollsByTomtomId;

    @Inject
    public TollReader(TomtomFolder tomtomFolder) {
        tollsByTomtomId = read(tomtomFolder.getTollsFile());
    }

    public Optional<Toll> tollForTomtomId(Long id) {
        return ofNullable(tollsByTomtomId.get(id));
    }

    private Map<Long, Toll> read(String tollsFile) {
        try {
            Map<Long, Toll> tolls = newHashMap();

            File file = new File(tollsFile);

            if (file.exists()) {
                JSONArray array = new JSONArray(IOUtils.toString(file.toURI(), UTF_8));
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    tolls.put(json.getLong("tomtomId"), new Toll(json.getInt("id"), json.getString("name"), json.getString("tollcode1"), json.optString("tollcode2")));
                }

                log.info("Loaded {} tolls", tolls.size());

            } else {
                log.info("File not found : {}", file.getAbsolutePath());
            }

            return tolls;
        }
        catch (IOException|JSONException e) {
            throw propagate(e);
        }
    }

    @Data
    public static class Toll {
        private final Integer id;
        private final String name;
        private final String tollCode1;
        private final String tollCode2;
    }
}
