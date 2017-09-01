package com.mappy.fpm.batches.toll;

import com.google.common.base.Charsets;
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
import java.util.function.Function;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;

@Singleton
@Slf4j
public class TollReader {

    private final Map<Long, Toll> tollsByTomtomId;
    private final TomtomFolder tomtomFolder;

    @Inject
    public TollReader(TomtomFolder tomtomFolder) {
        this.tomtomFolder = tomtomFolder;
        tollsByTomtomId = tollsByTomtomId();
    }

    public Optional<Toll> tollForTomtomId(Long id) {
        return ofNullable(tollsByTomtomId.get(id));
    }

    private Map<Long, Toll> tollsByTomtomId() {
        return read(json -> {
            try {
                return json.getLong("tomtomId");
            }
            catch (JSONException e) {
                throw propagate(e);
            }
        });
    }

    private <T> Map<T, Toll> read(Function<JSONObject, T> fun) {
        try {
            Map<T, Toll> tolls = newHashMap();

            File file = new File(tomtomFolder.getTollsFile());

            if (file.exists()) {
                JSONArray array = new JSONArray(IOUtils.toString(file.toURI(), Charsets.UTF_8));
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    T apply = fun.apply(json);
                    if (apply != null) {
                        tolls.put(apply, new Toll(String.valueOf(json.getInt("id")), json.getString("name"), json.getString("tollcode1"), json.optString("tollcode2")));
                    }
                }
            } else {
                log.info("No tolls file was found at {}", file.getAbsolutePath());
            }

            return tolls;
        }
        catch (IOException|JSONException e) {
            throw propagate(e);
        }
    }

    @Data
    public static class Toll {
        private final String id;
        private final String name;
        private final String tollcode1;
        private final String tollcode2;
    }
}
