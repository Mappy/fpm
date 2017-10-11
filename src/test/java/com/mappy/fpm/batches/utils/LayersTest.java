package com.mappy.fpm.batches.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LayersTest {
    @Test
    public void should_be_0_by_default() throws Exception {
        ImmutableMap<String, String> tags = ImmutableMap.of();

        assertThat(Layers.layer(tags, true, false)).isEqualTo(0);
        assertThat(Layers.layer(tags, false, false)).isEqualTo(0);
        assertThat(Layers.layer(tags, false, true)).isEqualTo(0);
    }

    @Test
    public void should_handle_way_at_same_level() throws Exception {
        Map<String, String> tags = ImmutableMap.of("layer", "1");

        assertThat(Layers.layer(tags, true, false)).isEqualTo(1);
        assertThat(Layers.layer(tags, false, false)).isEqualTo(1);
        assertThat(Layers.layer(tags, false, true)).isEqualTo(1);
    }

    @Test
    public void should_handle_way_at_different_level() throws Exception {
        Map<String, String> tags = ImmutableMap.of("layer:from", "0", "layer:to", "-1");

        assertThat(Layers.layer(tags, true, false)).isEqualTo(0);
        assertThat(Layers.layer(tags, false, false)).isEqualTo(0);
        assertThat(Layers.layer(tags, false, true)).isEqualTo(2);
    }

    @Test
    public void should_find_layers() throws Exception {
        assertThat(Layers.layer("-9")).isEqualTo(18);
        assertThat(Layers.layer("-4")).isEqualTo(8);
        assertThat(Layers.layer("-3")).isEqualTo(6);
        assertThat(Layers.layer("-2")).isEqualTo(4);
        assertThat(Layers.layer("-1")).isEqualTo(2);
        assertThat(Layers.layer("0")).isEqualTo(0);
        assertThat(Layers.layer("1")).isEqualTo(1);
        assertThat(Layers.layer("2")).isEqualTo(3);
        assertThat(Layers.layer("3")).isEqualTo(5);
        assertThat(Layers.layer("4")).isEqualTo(7);
        assertThat(Layers.layer("9")).isEqualTo(17);
    }
}
