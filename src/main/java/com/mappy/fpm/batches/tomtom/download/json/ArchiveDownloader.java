package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.json.model.Content;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

@Slf4j
public class ArchiveDownloader {

    private final File outputFolder;
    private final HttpClient client;
    private final String token;

    @Inject
    public ArchiveDownloader(@Named("outputFolder") File outputFolder, HttpClient client, @Named("token") String token) {
        this.outputFolder = outputFolder;
        this.client = client;
        this.token = token;
    }

    public void download(Content content) {
        for (int i = 0; i < 3; i++) {
            try {
                File downloaded = new File(outputFolder, content.getName());
                log.info("Downloading {} to \"{}\"", content.getLocation(), downloaded.getAbsolutePath());

                HttpGet get = new HttpGet(content.getLocation());
                get.addHeader("Authorization", token);
                HttpResponse response = client.execute(get);
                if (response.getStatusLine().getStatusCode() >= 400) {
                    log.error("Error when executing request, see error code={} with content={}", response.getStatusLine().getStatusCode(), response.getEntity());
                    throw new IOException("Error when executing request, see error code=" + response.getStatusLine().getStatusCode() + " with content=" + response.getEntity());
                }
                try (InputStream file = response.getEntity().getContent()) {
                    copyInputStreamToFile(file, downloaded);
                }
                return;
            }
            catch (IOException ex) {
                log.error("Retrying.. ", ex);
            }
        }
        throw new RuntimeException("Too many retry");
    }
}
