package com.mappy.fpm.batches.utils;

import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class Layers {
    public static int layer(Map<String, String> tags, boolean start, boolean end) {
        if (tags.containsKey("layer:from")) {
            if (start) {
                return layer(tags.get("layer:from"));
            }
            else if (end) {
                return layer(tags.get("layer:to"));
            }
        }
        else {
            return layer(tags.get("layer"));
        }
        return 0;
    }

    public static int layer(String input) {
        if (input != null) {
            Integer level = Integer.valueOf(input);
            if (level == 0) {
                return 0;
            }
            checkState(level >= -10 && level <= 10, "Too many layers");
            return 2 * Math.abs(level) - (level < 0 ? 0 : 1);
        }
        return 0;
    }

}
