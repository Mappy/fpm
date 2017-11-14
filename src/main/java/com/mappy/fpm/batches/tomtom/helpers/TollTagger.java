package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.toll.TollReader;
import com.mappy.fpm.batches.toll.TollReader.Toll;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.valueOf;

@Slf4j
public class TollTagger {

    private final TollReader tollReader;

    @Inject
    public TollTagger(TollReader tollReader) {
        this.tollReader = tollReader;
    }

    public Map<String, String> tag(long id) {
        return tollReader.tollForTomtomId(id).map(TollTagger::tags).orElse(of());
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
