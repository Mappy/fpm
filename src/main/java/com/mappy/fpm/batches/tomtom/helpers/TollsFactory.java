package com.mappy.fpm.batches.tomtom.helpers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TollsFactory {

    private TollsFactory() {
    }

    public static TollTagger create(String tollsDirectory) {
        log.info("Creating tolls tagger with following path file={}", tollsDirectory);
        return new TollTagger(new TollProvider(tollsDirectory));
    }
}
