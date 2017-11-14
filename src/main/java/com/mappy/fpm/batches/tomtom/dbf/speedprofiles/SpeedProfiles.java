package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.Speed.PrecomputeSpeedProfile;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.min;
import static java.util.Optional.ofNullable;

public class SpeedProfiles {

    private static final String TAG_PREFIX = "mappy_sp_";

    private final HsnpDbf hsnpDbf;
    private final HsprDbf hsprDbf;

    @Inject
    public SpeedProfiles(HsnpDbf hsnpDbf, HsprDbf hsprDbf) {
        this.hsnpDbf = hsnpDbf;
        this.hsprDbf = hsprDbf;
    }

    public Map<String, String> getTags(Feature feature) {
        Map<String, String> tags = newHashMap();

        for (Speed speed : hsnpDbf.getById(feature.getLong("ID"))) {

            ofNullable(speed.getFreeFlow()).ifPresent(value -> tags.put(TAG_PREFIX + speed.direction() + "_freeflow", value.toString()));
            ofNullable(speed.getWeekDay()).ifPresent(value -> tags.put(TAG_PREFIX + speed.direction() + "_weekday", value.toString()));
            ofNullable(speed.getWeekend()).ifPresent(value -> tags.put(TAG_PREFIX + speed.direction() + "_weekend", value.toString()));
            ofNullable(speed.getWeek()).ifPresent(value -> tags.put(TAG_PREFIX + speed.direction() + "_week", value.toString()));

            List<Double> mins = newArrayList();
            for (int i = 0; i < speed.getProfiles().size(); i++) {
                PrecomputeSpeedProfile profile = hsprDbf.getProfileById(speed.getProfiles().get(i));
                if (profile != null) {
                    tags.put(TAG_PREFIX + speed.direction() + "_profile" + (i + 1), profile.getProfile());
                    mins.add(profile.getMin());
                }
            }
            if (!mins.isEmpty()) {
                tags.put(TAG_PREFIX + speed.direction() + "_min_speed_pct_freeflow", String.valueOf(min(mins)));
            }
        }

        return tags;
    }
}
