package com.mappy.fpm.batches.tomtom.download;

import com.mappy.fpm.batches.tomtom.download.MetalinkParser.Metalink;
import com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl;
import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.union;
import static com.mappy.fpm.batches.tomtom.download.TomtomCountries.countries;
import static com.mappy.fpm.batches.tomtom.download.TomtomCountries.outerworld;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;

@Slf4j
public class TomtomDownloader {

    private final MetalinkDownloader downloader;
    private final ShapefileDownloader shpDownloader;
    private final Set<TomtomCountry> countries;

    public TomtomDownloader(MetalinkDownloader downloader, ShapefileDownloader shpDownloader, Set<TomtomCountry> countries) {
        this.downloader = downloader;
        this.shpDownloader = shpDownloader;
        this.countries = countries != null ? countries : union(countries(), outerworld());
    }

    public static void main(String args[]) throws IOException {
        if (args.length != 4) {
            throw new IllegalArgumentException("Should have 4 arguments : tomtomFolder, tomtomVersion, tomtomLogin, tomtomPassword");
        }
        File outputDirectory = new File(args[0]);
        outputDirectory.mkdirs();
        HttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(10).setConnectionReuseStrategy(INSTANCE).build();
        new TomtomDownloader(new MetalinkDownloader(args[2], args[3], args[1], HttpClientBuilder.create().build()), new ShapefileDownloader(outputDirectory, httpClient), null).run();
    }

    public void run() throws IOException {
        Metalink metalink = downloader.download();
        checkState(!metalink.isEmpty(), "No data in metalink");

        countries.stream().parallel().forEach(country -> {
            log.info("Downloading {}", country.getLabel());

            Metalink local = metalink.forCountry(country.getId().toLowerCase());
            checkState(!local.isEmpty(), "No data for " + country.getLabel());

            List<String> zones = local.zones();
            checkState(zones.size() > 1, "Too few zones for " + country.getLabel());

            for (String zone : zones) {
                if (country.isOuterworld() && "ax".equals(zone)) {
                    continue;
                }
                Metalink veryLocal = local.forZone(zone);
                log.info("Processing area {} in {}", zone, country.getLabel());
                checkState("ax".equals(zone) ? veryLocal.size() == 1 : veryLocal.types().contains("mn"));

                boolean isMixed = isMixed(veryLocal);
                for (MetalinkUrl url : veryLocal.getUrls()) {
                    if (!isMixed || url.getPrefix().startsWith("eur")) {
                        shpDownloader.download(country, url);
                    }
                }
            }
        });
    }

    private boolean isMixed(Metalink veryLocal) {
        long nbEur = veryLocal.getUrls().stream().filter(u -> u.getPrefix().startsWith("eur")).count();
        return nbEur != 0 && nbEur != veryLocal.getUrls().size();
    }
}