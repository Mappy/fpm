package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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

@Slf4j
public class TollProvider {
    private final Map<Long, Toll> tolls = newHashMap();

    public TollProvider(String tollsPath) {
        this(tollsPath, "tolls.json");
    }

    @VisibleForTesting
    public TollProvider(String tollsPath, String file) {
        read(tollsPath, file);
    }

    public Optional<Toll> byId(Long id) {
        return ofNullable(tolls.get(id));
    }

    private void read(String tollsPath, String tollsFileName) {
        File tollsFile = new File(tollsPath, tollsFileName);
        if (!tollsFile.exists() && FilenameUtils.isExtension("", "")) {
            log.info("Tolls file not found={} parsing skipped", tollsFile.getAbsolutePath());
            return;
        }
        try {
            JSONArray array = new JSONArray(IOUtils.toString(tollsFile.toURI(), UTF_8));
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                tolls.put(json.getLong("tomtomId"), new Toll(json.getInt("id"), json.getString("name"), json.getString("tollcode1"), json.optString("tollcode2")));
            }
            log.info("Loaded {} tolls", this.tolls.size());
        } catch (IOException | JSONException e) {
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
