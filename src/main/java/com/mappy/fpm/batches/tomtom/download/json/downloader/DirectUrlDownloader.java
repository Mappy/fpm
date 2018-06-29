package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static com.google.common.base.Throwables.propagate;

@Slf4j
public class DirectUrlDownloader implements Function<Content, Content> {
    private final HttpClient client;
    private final String token;

    @Inject
    public DirectUrlDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    public Content apply(Content content) {
        HttpGet get = new HttpGet(content.getLocation() + "/download-url");
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {
            String directUrl = new JSONObject(IOUtils.toString(response, "UTF-8")).getString("url");
            return new Content(content.getName(), directUrl);
        } catch (IOException e) {
            throw propagate(e);
        } catch(JSONException e) {
            log.error("No direct url for: " + content.getLocation() + "/download-url");
            throw propagate(e);
        }
    }
}
