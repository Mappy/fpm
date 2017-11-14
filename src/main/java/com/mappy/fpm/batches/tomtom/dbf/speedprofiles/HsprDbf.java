package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.Speed.PrecomputeSpeedProfile;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class HsprDbf {

    private final Map<Integer, PrecomputeSpeedProfile> profiles;

    @Inject
    public HsprDbf(TomtomFolder folder) {
        profiles = loadSpeedProfiles(folder.getFile("hspr.dbf"));
    }

    public PrecomputeSpeedProfile getProfileById(Integer id) {
        return profiles.get(id);
    }

    private static Map<Integer, PrecomputeSpeedProfile> loadSpeedProfiles(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            log.info("File not found : {}", file.getAbsolutePath());
            return newHashMap();
        }

        TreeMultimap<Integer, SpeedProfile> profilesMap = TreeMultimap.create();
        log.info("Reading HSPR {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                SpeedProfile profile = new SpeedProfile(row.getInt("PROFILE_ID"), row.getInt("TIME_SLOT"), row.getDouble("REL_SP"));
                profilesMap.put(profile.getId(), profile);
            }
        }
        log.info("Loaded {} hspr", profilesMap.size());

        return profilesMap.asMap().entrySet().stream().collect(toMap(Entry::getKey, e -> precomputeProfile(e.getValue())));
    }

    private static PrecomputeSpeedProfile precomputeProfile(Collection<SpeedProfile> speeds) {
        return new PrecomputeSpeedProfile(profile(speeds), min(speeds));
    }

    private static String profile(Collection<SpeedProfile> speedProfiles) {
        return speedProfiles.stream().findFirst().get().getId() + ":"
                + speedProfiles.stream().map(speedProfile -> String.valueOf((int) (100 - speedProfile.getRelSpeed()))).collect(joining("_"));
    }

    private static Double min(Collection<SpeedProfile> speedProfiles) {
        return speedProfiles.stream().map(SpeedProfile::getRelSpeed).min(naturalOrder()).orElse(0.0);
    }
}