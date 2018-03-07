package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.override;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class MapContentDownloaderIT {

    private static final Module MODULE_IT = override(new MapContentModule("target/download", "validToken", "version"))
            .with(new MapContentModuleIT());

    @Test
    public void should_run_download() {
        createInjector(MODULE_IT)//
                .getInstance(MapContentDownloader.class)//
                .run();
    }

    private static class MapContentModuleIT extends AbstractModule {
        @Override
        protected void configure() {
            HttpClient client = mock(HttpClient.class);

            try {
                HttpResponse familyResponse = mock(HttpResponse.class);
                when(familyResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/tomtom/download/json/families.json").toURI())));
                HttpGet get = new HttpGet("https://api.tomtom.com/mcapi/families");
                when(client.execute(get)).thenReturn(familyResponse);

            } catch (Exception e) {
                fail();
            }

            bind(HttpClient.class).toInstance(client);
        }
    }
}