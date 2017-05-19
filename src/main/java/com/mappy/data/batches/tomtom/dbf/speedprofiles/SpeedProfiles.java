package com.mappy.data.batches.tomtom.dbf.speedprofiles;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javax.inject.Inject;
import com.mappy.data.batches.tomtom.dbf.speedprofiles.Speed.PrecomputeSpeedProfile;
import com.mappy.data.batches.utils.Feature;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SpeedProfiles {
    private final HspnDbf hspnDbf;
    private final HsprDbf hsprDbf;

    @Inject
    public SpeedProfiles(HspnDbf hspnDbf, HsprDbf hsprDbf) {
        this.hspnDbf = hspnDbf;
        this.hsprDbf = hsprDbf;
    }

    public Map<String, String> extracted(Feature feature) {
        Map<String, String> result = Maps.newHashMap();
        for (Speed speed : hspnDbf.getById(feature.getLong("ID"))) {
            speed.getFreeFlow().ifPresent(value -> result.put("mappy_sp_" + speed.direction() + "_" + "freeflow", value.toString()));
            speed.getWeekDay().ifPresent(value -> result.put("mappy_sp_" + speed.direction() + "_" + "weekday", value.toString()));
            speed.getWeekend().ifPresent(value -> result.put("mappy_sp_" + speed.direction() + "_" + "weekend", value.toString()));
            speed.getWeek().ifPresent(value -> result.put("mappy_sp_" + speed.direction() + "_" + "week", value.toString()));
            List<Double> mins = Lists.newArrayList();
            for (int i = 0; i < speed.getProfiles().length; i++) {
                PrecomputeSpeedProfile profile = hsprDbf.getProfileById(speed.getProfiles()[i]);
                if (profile != null) {
                    result.put("mappy_sp_" + speed.direction() + "_" + "profile" + (i + 1), profile.getProfile());
                    mins.add(profile.getMin());
                }
            }
            if (!mins.isEmpty()) {
                result.put("mappy_sp_" + speed.direction() + "_" + "min_speed_pct_freeflow", String.valueOf(Collections.min(mins)));
            }
        }
        return result;
    }
}
