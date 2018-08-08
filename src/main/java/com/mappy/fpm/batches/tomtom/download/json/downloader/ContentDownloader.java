package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.google.gson.Gson;
import com.mappy.fpm.batches.tomtom.download.json.model.Contents;
import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class ContentDownloader implements Function<Release, Stream<Content>> {
    public static final Pattern PATTERN = Pattern.compile("^(.*?)-shp(.?)-(.*?)-(.*?)-(.*?)\\.7z\\.(.*?)");
    //private static final Set<String> NEEDED = newHashSet(newArrayList("2dcmnb", "2dcm", "mn", "sp", "mnpoi", "mnap"));
    private static final Set<String> NEEDED = newHashSet(newArrayList("2dcmnb", "2dcm"));

    private final HttpClient client;
    private final String token;

    @Inject
    public ContentDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Content> apply(Release release) {
        HttpGet get = new HttpGet(release.getLocation() + "?label=shpd");
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {
            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Contents.class).getContents().stream()
                    .filter(c -> {
                        Matcher matcher = PATTERN.matcher(c.getName());
                        return matcher.matches() && NEEDED.contains(matcher.group(3));
                    });
        } catch (IOException e) {
            throw propagate(e);
        }
    }
}
