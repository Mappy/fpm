package com.mappy.fpm.batches.naturalearth;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.mappy.fpm.batches.naturalearth.NaturalEarth2Pbf.NaturalEarthModule;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.utils.MemoryGeometrySerializer;

import org.junit.Test;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.override;
import static org.assertj.core.api.Assertions.assertThat;

public class NaturalEarth2PbfIT {
    private final MemoryGeometrySerializer serializer = new MemoryGeometrySerializer();
    private final Injector injector = createInjector(
            override(new NaturalEarthModule(getClass().getResource("/").getPath()))
                    .with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(GeometrySerializer.class).toInstance(serializer);
                        }
                    }));
    private final NaturalEarth2Pbf converter = injector.getInstance(NaturalEarth2Pbf.class);

    @Test
    public void should_process_shapefiles() throws Exception {
        assertThat(converter.shapefiles()).hasSize(9);
    }

    @Test
    public void should_process_data() throws Exception {
        converter.run();

        assertThat(serializer.getPoints())
                .filteredOn(m -> "city".equals(m.get("place")) || "town".equals(m.get("place"))).hasSize(8);

        assertThat(serializer.getPoints())
                .filteredOn(m -> m.containsKey("aeroway"))
                .extracting(m -> m.get("name"))
                .containsOnly("Charles de Gaulle Int'l", "Paris Orly");

        assertThat(serializer.getPolygons()).filteredOn(t -> t.get("landuse").equals("residential")).hasSize(7);
        assertThat(serializer.getMultilinestrings()).filteredOn(t -> t.containsKey("highway")).hasSize(6);
        assertThat(serializer.getMultilinestrings()).filteredOn(t -> t.containsKey("railway")).hasSize(4);
        assertThat(serializer.getMultilinestrings()).filteredOn(t -> t.containsKey("stream")).hasSize(10);
        assertThat(serializer.getMultilinestrings()).filteredOn(t -> "water".equals(t.get("natural"))).hasSize(10);
        assertThat(serializer.getMultipolygons()).filteredOn(t -> "water".equals(t.get("natural"))).hasSize(7);
        assertThat(serializer.getPoints()).filteredOn(t -> "locality".equals(t.get("place"))).hasSize(25);

    }
}
