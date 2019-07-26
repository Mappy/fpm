package com.mappy.fpm.batches.tomtom.dbf.restrictions;

import lombok.Data;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;

import org.jamel.dbf.exception.DbfException;
import org.jamel.dbf.structure.DbfRow;

@Data
public class Restriction {
    private final long id;
    private final int sequenceNumber;
    private final Validity validity;
    private final Type type;
    private final int value;
    private final VehicleType vehicleType;

    public enum Validity {
        notApplicable,
        inBothLineDirections,
        inPositiveLineDirection,
        inNegativeLineDirection,
        atStart,
        atEnd;
    }

    public enum Type {
        notApplicable,
        blockedPassage,
        specialCharge,
        prohibitedManoeuvreType,
        bifurcationType,
        removableBlockage,
        directionOfTrafficFlow,
        constructionStatus,
        lowEmissionRestrictionType,
        vehicleRestriction,
        demolitionDate;

        public static Type fromCode(String typeCode) {
            switch (typeCode) {
                case "":
                    return notApplicable;
                case "BP":
                    return blockedPassage;
                case "1M":
                    return specialCharge;
                case "8I":
                    return prohibitedManoeuvreType;
                case "4B":
                    return bifurcationType;
                case "RB":
                    return removableBlockage;
                case "DF":
                    return directionOfTrafficFlow;
                case "6Z":
                    return constructionStatus;
                case "LY":
                    return lowEmissionRestrictionType;
                case "6Q":
                    return vehicleRestriction;
                case "D9":
                    return demolitionDate;
                default:
                    throw new RuntimeException("Unknown restriction type: " + typeCode);
            }
        }
    }

    public static Restriction fromRow(DbfRow row) {
        return new Restriction(
            row.getLong("ID"),
            row.getInt("SEQNR"),
            Validity.values()[row.getInt("DIR_POS")],
            Type.fromCode(row.getString("RESTRTYP")),
            row.getInt("RESTRVAL"),
            VehicleType.fromId(row.getInt("VT"))
        );
    }
}
