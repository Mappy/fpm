package com.mappy.fpm.batches.tomtom.dbf.signposts;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

public enum InfoType {
    Route_Number_Type("6W"), Route_Number("RN"), Route_Number_on_Shield("RV"), Validity_Direction("7V"), //
    Route_Directional("7G"), Route_Name("RJ"), Street_Name_Type("7A"), Exit_Name("4G"), Exit_Number("4E"), //
    Other_Destination("4I"), Pictogram("4H"), Place_Name("9D"), Street_Name("6T");

    public final String code;

    public static final Map<String, InfoType> byCode = stream(InfoType.values())
            .collect(toMap(type -> type.code, type -> type));

    InfoType(String code) {
        this.code = code;
    }

    public static boolean isaRouteNumber(InfoType infoType) {
        return infoType == Route_Number_on_Shield || infoType == Route_Number;
    }
}
