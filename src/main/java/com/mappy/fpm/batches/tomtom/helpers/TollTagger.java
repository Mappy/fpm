package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.tomtom.helpers.TollProvider.Toll;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.valueOf;

@Slf4j
public class TollTagger {
    private final TollProvider tollProvider;

    public TollTagger(TollProvider tollProvider) {
        this.tollProvider = tollProvider;
    }

    public Map<String, String> tag(long id) {
        return tollProvider.byId(id).map(TollTagger::tags).orElse(of());
    }

    private static ImmutableMap<String, String> tags(Toll t) {
        return of( //
                "toll", "yes", //
                "toll:name", t.getName(), //
                "ref:mappy", valueOf(t.getId()), //
                "ref:asfa:1", t.getTollCode1(), //
                "ref:asfa:2", t.getTollCode2());
    }
}
