package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

@Slf4j
public class ArchiveDownloader implements Function<Content, File> {

    private final File outputFolder;
    private final HttpClient client;
    private final String token;

    @Inject
    public ArchiveDownloader(@Named("outputFolder") File outputFolder, HttpClient client, @Named("token") String token) {
        this.outputFolder = outputFolder;
        this.client = client;
        this.token = token;
    }

    public File apply(Content content) {
        File downloaded = new File(outputFolder, content.getName());

        HttpGet get = new HttpGet(content.getLocation());
        get.addHeader("Authorization", token);

        for (int i = 0; i < 3; i++) {
            try {
                log.info("Downloading {} to \"{}\"", content.getLocation(), downloaded.getAbsolutePath());
                copyInputStreamToFile(client.execute(get).getEntity().getContent(), downloaded);
                return downloaded;
            }
            catch (IOException ex) {
                log.error("Retrying.. ", ex);
            }
        }
        throw new RuntimeException("Too many retry");
    }
}
