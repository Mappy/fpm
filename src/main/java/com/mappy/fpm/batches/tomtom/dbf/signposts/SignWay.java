package com.mappy.fpm.batches.tomtom.dbf.signposts;

import lombok.Data;

@Data
class SignWay {
    private final Long tomtomId;
    private final Long fromJunctionId;
    private final Long toJunctionId;
}
