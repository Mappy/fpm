package com.mappy.data.batches.tomtom.download;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mappy.data.batches.tomtom.download.MetalinkParser.MetalinkUrl;
import com.mappy.data.batches.tomtom.download.TomtomCountries.TomtomCountry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;

@Slf4j
public class ShapefileDownloader {

    private final File outputFolder;
    private final CloseableHttpClient client = HttpClientBuilder.create().setMaxConnPerRoute(10).setConnectionReuseStrategy(INSTANCE).build();
    private final LoadingCache<String, File> directories = CacheBuilder.newBuilder().build(new CacheLoader<String, File>() {
        @Override
        public File load(String country) {
            File countryDirectory = new File(outputFolder, replaceSpecialLetters(country));
            countryDirectory.mkdirs();
            return countryDirectory;
        }
    });

    public ShapefileDownloader(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void download(TomtomCountry country, MetalinkUrl url) {
        for (int i = 0; i < 3; i++) {
            try {
                HttpGet get = new HttpGet(url.getUrl());
                File countryDirectory = directories.get(country.getLabel());

                File downloaded = new File(countryDirectory, url.getName());
                try (InputStream content = client.execute(get).getEntity().getContent(); FileOutputStream fos = new FileOutputStream(downloaded)) {
                    IOUtils.copyLarge(content, fos);
                }
                ShapefileExtractor.decompress(countryDirectory, downloaded, tablesNeeded(country.isOuterworld(), url));
                downloaded.delete();
                return;
            }
            catch (IOException | ExecutionException ex) {
                log.error("Retrying.. ", ex);
            }
        }
        throw new RuntimeException("Too many retry");
    }

    private List<String> tablesNeeded(boolean outerworld, MetalinkUrl url) {
        if (outerworld) {
            return newArrayList("nw.");
        }
        String type = url.getType();
        if ("mn".equals(type)) {
            return newArrayList(
                    "_nw.", // roads
                    "_rs.", // road restrictions
                    "_mn.", // road maneuver
                    "_mp.", // road maneuver
                    "_lc.", // land covers
                    "_lu.", // land uses
                    "_wa.", // water areas
                    "_wl.", // water lines
                    "_sm.", // cities
                    "_smnm.", // alternate city names
                    "_bl.", // coastlines
                    "_rr.", // railroads
                    "_gc.", // Geocoding Information (alternate roads names)
                    "_sr.", // speeds restrictions (for maxspeed)
                    "_si.", // sign post information
                    "_sp.", // sign post paths
                    "_sg.", // sign post
                    "_ld.", // lane directions
                    "_a0.",
                    "_a1.",
                    "_a2.",
                    "_a3.",
                    "_a4.",
                    "_a5.",
                    "_a6.",
                    "_a7.",
                    "_a8.",
                    "_a9.");
        }
        else if ("sp".equals(type)) {
            return newArrayList("_hsnp.", "_hspr.");
        }
        else if ("2dcmnb".equals(type)) {
            return newArrayList("_2dbd.", "_2dtb.");
        }
        throw new IllegalStateException();
    }

    private static String replaceSpecialLetters(String text) {
        return normalize(text, NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}