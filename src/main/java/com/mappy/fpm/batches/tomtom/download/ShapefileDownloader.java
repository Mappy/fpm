package com.mappy.fpm.batches.tomtom.download;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl;
import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;

@Slf4j
public class ShapefileDownloader {

    private final File outputFolder;
    private final HttpClient client;
    private final LoadingCache<String, File> directories = CacheBuilder.newBuilder().build(new CacheLoader<String, File>() {
        @Override
        public File load(String country) {
            File countryDirectory = new File(outputFolder, replaceSpecialLetters(country));
            countryDirectory.mkdirs();
            return countryDirectory;
        }
    });

    public ShapefileDownloader(File outputFolder, HttpClient client) {
        this.client = client;
        this.outputFolder = outputFolder;
    }

    public void download(TomtomCountry country, MetalinkUrl component) {
        for (int i = 0; i < 3; i++) {
            try {
                HttpGet get = new HttpGet(component.getUrl());
                File countryDirectory = directories.get(country.getLabel());

                File downloaded = new File(countryDirectory, component.getName());
                try (InputStream content = client.execute(get).getEntity().getContent(); FileOutputStream fos = new FileOutputStream(downloaded)) {
                    IOUtils.copyLarge(content, fos);
                }
                ShapefileExtractor.decompress(countryDirectory, downloaded, country.isOuterworld(), component.getType());
                downloaded.delete();
                return;
            }
            catch (IOException | ExecutionException ex) {
                log.error("Retrying.. ", ex);
            }
        }
        throw new RuntimeException("Too many retry");
    }

    private static String replaceSpecialLetters(String text) {
        return normalize(text, NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}