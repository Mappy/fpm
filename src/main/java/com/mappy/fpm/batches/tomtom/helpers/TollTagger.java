package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.toll.TollReader;
import com.mappy.fpm.batches.toll.TollReader.Toll;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Map;

@Slf4j
public class TollTagger {

    private final TollReader tollReader;

    @Inject
    public TollTagger(TollReader tollReader) {
        this.tollReader = tollReader;
    }

    public Map<String, String> tag(long id) {
        return tollReader.tollForTomtomId(id).map(TollTagger::tags).orElse(ImmutableMap.of());
    }

    private static ImmutableMap<String, String> tags(Toll t) {
        return ImmutableMap.of( //
                "toll", "yes", //
                "toll:name", t.getName(), //
                "ref:mappy", t.getId(), //
                "ref:asfa:1", t.getTollcode1(), //
                "ref:asfa:2", t.getTollcode2());
    }
}
