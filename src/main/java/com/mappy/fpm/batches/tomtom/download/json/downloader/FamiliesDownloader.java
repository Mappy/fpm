package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.google.gson.Gson;
import com.mappy.fpm.batches.tomtom.download.json.model.Families;
import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class FamiliesDownloader {

    private static final String FAMILIES_URL = "https://api.tomtom.com/mcapi/families";
    private static final List<String> ALLOWED = newArrayList("2DCM", "MN", "SP");

    private final HttpClient client;
    private final String token;

    @Inject
    public FamiliesDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    public Stream<Family> get() {
        log.info("Get all families");
        HttpGet get = new HttpGet(FAMILIES_URL);
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {

            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Families.class).getContent().stream() //
                    .filter(f -> ALLOWED.contains(f.getAbbreviation()));
        } catch (IOException e) {
            throw propagate(e);
        }
    }
}
