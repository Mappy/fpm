package com.mappy.fpm.batches.tomtom.download.json;

import com.github.paweladamski.httpclientmock.HttpClientMock;
import com.github.paweladamski.httpclientmock.action.Action;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.override;
import static java.nio.file.Files.list;
import static java.nio.file.Paths.get;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class MapContentDownloaderIT {
    private static final String OUTPUT_FOLDER = "target/download";
    private static final Module MODULE_IT = override(new MapContentModule(OUTPUT_FOLDER, "validToken", "2016.09")).with(new MapContentModuleIT());

    @Test
    public void should_run_download() throws IOException {
        createInjector(MODULE_IT)//
                .getInstance(MapContentDownloader.class)//
                .run();

        assertThat(list(get(OUTPUT_FOLDER))) //
                .containsExactly(get(OUTPUT_FOLDER, "Andorra"));
    }

    private static class MapContentModuleIT extends AbstractModule {
        @Override
        protected void configure() {
            HttpClientMock client = new HttpClientMock();

            try {
                client.onGet("https://api.tomtom.com/mcapi/families").doReturnJSON(getFile("families.json"));

                client.onGet("https://api.test/families/300/products").doReturnJSON(getFile("mn_products.json"));
                client.onGet("https://api.test/families/400/products").doReturnJSON(getFile("sp_products.json"));
                client.onGet("https://api.test/families/500/products").doReturnJSON(getFile("2dcm_products.json"));

                client.onGet("https://api.test/products/310/releases").doReturnJSON(getFile("mn_eur_releases.json"));
                client.onGet("https://api.test/products/320/releases").doReturnJSON(getFile("mn_lam_releases.json"));
                client.onGet("https://api.test/products/410/releases").doReturnJSON(getFile("sp_eur_releases.json"));
                client.onGet("https://api.test/products/510/releases").doReturnJSON(getFile("2dcm_eur_releases.json"));

                client.onGet("https://api.test/releases/312?label=shpd").doReturnJSON(getFile("mn_eur_2016.09_contents.json"));
                client.onGet("https://api.test/releases/322?label=shpd").doReturnJSON(getFile("mn_lam_2016.09_contents.json"));
                client.onGet("https://api.test/releases/411?label=shpd").doReturnJSON(getFile("sp_eur_2016.09_contents.json"));
                client.onGet("https://api.test/releases/511?label=shpd").doReturnJSON(getFile("2dcm_eur_2016.09_contents.json"));

                Action file = r -> {
                    BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "ok");
                    try {
                        response.setEntity(new FileEntity(new File(getClass().getResource("/tomtom/download/json/and.7z.001").toURI())));
                    } catch (URISyntaxException e) {
                        fail();
                    }
                    return response;
                };

                client.onGet("https://api.test/contents/3121").doAction(file);
                client.onGet("https://api.test/contents/3211").doAction(file);
                client.onGet("https://api.test/contents/4111").doAction(file);
                client.onGet("https://api.test/contents/5111").doAction(file);

            } catch (Exception e) {
                fail();
            }

            bind(HttpClient.class).toInstance(client);
        }

        private String getFile(String name) throws IOException, URISyntaxException {
            return readFileToString(new File(getClass().getResource("/tomtom/download/json/" + name).toURI()), "UTF-8");
        }
    }
}