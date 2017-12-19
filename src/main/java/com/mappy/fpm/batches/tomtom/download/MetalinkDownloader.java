package com.mappy.fpm.batches.tomtom.download;

import com.mappy.fpm.batches.tomtom.download.MetalinkParser.Metalink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;

import static org.joda.time.LocalDateTime.now;
import static org.joda.time.format.DateTimeFormat.forPattern;

@Slf4j
public class MetalinkDownloader {

    private final String login;
    private final String password;
    private final String tomtomVersion;
    private final String outputDirectory;
    private final CloseableHttpClient client;

    public MetalinkDownloader(String login, String password, String tomtomVersion, String outputDirectory, CloseableHttpClient client) {
        this.login = login;
        this.password = password;
        this.tomtomVersion = tomtomVersion;
        this.outputDirectory = outputDirectory;
        this.client = client;
    }

    public Metalink download() throws IOException {
        HttpGet metalinkRequest = new HttpGet(
                "https://edelivery-ad.tomtom.com/automaticdownload/filter/token/" + getToken() + "/product/*/region/*/country/*/release/" + tomtomVersion + "/format/*/output/metalink/file/*");

        File outputFile = new File(outputDirectory + "/Europe." + now().toString(forPattern("ddMMyyyy-HHmm")) + ".metalink");

        log.info("Fetching metalink in path={}", outputFile.getAbsolutePath());

        try (InputStream content = client.execute(metalinkRequest).getEntity().getContent();
            FileOutputStream fos = new FileOutputStream(outputFile)) {
            IOUtils.copyLarge(content, fos);
        }
        log.info("Success!");

        return MetalinkParser.parse(new FileInputStream(outputFile));
    }

    private String getToken() throws IOException {
        HttpPost tokenRequest = new HttpPost("https://edelivery-ad.tomtom.com/automaticdownload/login");
        tokenRequest.setEntity(new StringEntity("user=" + login + "&password=" + password));
        log.info("Getting token");
        try (InputStream content = client.execute(tokenRequest).getEntity().getContent()) {
            return IOUtils.toString(content, "UTF-8");
        }
    }
}