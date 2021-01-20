package com.mappy.fpm.batches.tomtom.dbf.restrictions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.dbf.restrictions.RsDbf;
import com.mappy.fpm.batches.tomtom.dbf.restrictions.Restriction.Validity;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsProvider;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;
import com.mappy.fpm.batches.utils.Feature;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestrictionTagger {

    private final RsDbf rsDbf;
    private final TimeDomainsProvider timeDomainsProvider;
    private final TimeDomainsParser timeDomainsParser;

    @Inject
    public RestrictionTagger(RsDbf rsDbf, TimeDomainsProvider timeDomainsProvider, TimeDomainsParser timeDomainsParser) {
        this.rsDbf = rsDbf;
        this.timeDomainsProvider = timeDomainsProvider;
        this.timeDomainsParser = timeDomainsParser;
    }

    /**
     * Add direction of traffic flow information
     *
     * motor_vehicle: if set to no, traffic is forbiddn in both direciton
     * oneway: if set to yes, traffic is only allowed in one direction
     * reversed: if set to yes, with oneway set, direction is reversed (node_to to node_from)
     * opening_hours: list of periods within which the section is closed
     * 
     * if the section is only closed during some periods, it will be considered as open
     * (motor_vehicle not set) with the opening_hours field set.
     * 
     * Current limitations:
     * * this function only consider restrictions of type "DF" (direction of traffic flow)
     * * section access time periods are merged, even if they are not the same for both sides
     *
     * @param feature road to consider
     * @return dict of OSM tags to add to the section
     */
    public Map<String, String> tag(Feature feature) {
        Map<String, String> tags = Maps.newHashMap();
        Long sectionId = feature.getLong("ID");
        Collection<Restriction> restrictions = rsDbf.getRestrictions(sectionId);
        ArrayListMultimap<Integer, TimeDomains> sectionTimeDomains = timeDomainsProvider.getSectionTimeDomains(sectionId);
        Boolean canBeCrossedForward = true;
        Boolean canBeCrossedBackward = true;
        HashSet<TimeDomains> forwardTimeDomains = Sets.newHashSet();;
        HashSet<TimeDomains> backwardTimeDomains = Sets.newHashSet();

        Boolean inConstructionForward = false;
        Boolean inConstructionBackward = false;
        String constructionDateDomains = "";

        String oneway = feature.getString("ONEWAY");
        if (restrictions.isEmpty()) {
            // the "oneway" field considers the road to be crossable in a direction
            // if there are no restrictions for this direction.
            // even if the road is only closed for one day of the year,
            // it will be considered closed by this field
            // Parsing the restricitons table should be enough,
            // and we expect the "oneway" field to be null if there are no restrictions.
            // however, we fallback to parsing this field if there are no restrictions
            // information, just in case.
            if ("TF".equals(oneway) || "N".equals(oneway)) {
                canBeCrossedForward = false;
            }
            if ("FT".equals(oneway) || "N".equals(oneway)) {
                canBeCrossedForward = false;
            }
        }
        for (Restriction restriction: restrictions) {
            if (restriction.getType() == Restriction.Type.directionOfTrafficFlow) {
                if (restriction.getVehicleType() != VehicleType.passengerCars && restriction.getVehicleType() != VehicleType.all) {
                    continue;
                }
                List<TimeDomains> restrictionTimeDomains;
                Boolean noTimeDomains;
                if (sectionTimeDomains == null) {
                    restrictionTimeDomains = null;
                    noTimeDomains = true;
                } else {
                    restrictionTimeDomains = sectionTimeDomains.get(restriction.getSequenceNumber());
                    noTimeDomains = restrictionTimeDomains.isEmpty();
                }
                switch (restriction.getValidity()) {
                case inBothLineDirections:
                    if (noTimeDomains) {
                        canBeCrossedForward = false;
                        canBeCrossedBackward = false;
                    } else {
                        forwardTimeDomains.addAll(restrictionTimeDomains);
                        backwardTimeDomains.addAll(restrictionTimeDomains);
                    }
                    break;
                case inPositiveLineDirection:
                    if (noTimeDomains) {
                        canBeCrossedForward = false;
                    } else {
                        forwardTimeDomains.addAll(restrictionTimeDomains);
                    }
                    break;
                case inNegativeLineDirection:
                    if (noTimeDomains) {
                        canBeCrossedBackward = false;
                    } else {
                        backwardTimeDomains.addAll(restrictionTimeDomains);
                    }
                    break;
                default:
                    throw new RuntimeException(
                            "Cannot interpret restriction validity in given context: " + restriction.getValidity());
                }
            }
            else if (restriction.getType() == Restriction.Type.constructionStatus) {
                List<TimeDomains> restrictionTimeDomains;
                restrictionTimeDomains = sectionTimeDomains.get(restriction.getSequenceNumber());
                switch (restriction.getValidity()) {
                    case inBothLineDirections:
                        tags.put("construction", "both");
                        break;
                    case inPositiveLineDirection:
                        tags.put("construction", "forward");
                        break;
                    case inNegativeLineDirection:
                        tags.put("construction", "backward");
                        break;
                    default:
                        throw new RuntimeException(
                                "Cannot interpret restriction validity in given context: " + restriction.getValidity());
                }
                constructionDateDomains = restrictionTimeDomains.get(0).getDomain();
                try {
                    String[] dates = timeDomainsParser.parseDateInterval(constructionDateDomains);
                    tags.put("construction_start_expected", dates[0]);
                    tags.put("construction_end_expected", dates[1]);
                } catch (IllegalArgumentException iae) {
                    throw new RuntimeException("Could not parse construction date domain: " + iae.getMessage());
                }
            }
        }
        HashSet<TimeDomains> timeDomains;
        if (canBeCrossedForward) {
            if (canBeCrossedBackward) {
                timeDomains = forwardTimeDomains;
                timeDomains.addAll(backwardTimeDomains);
            } else {
                tags.put("oneway", "yes");
                timeDomains = forwardTimeDomains;
            }
        } else {
            if (canBeCrossedBackward) {
                tags.put("oneway", "yes");
                tags.put("reversed:tomtom", "yes");
                timeDomains = backwardTimeDomains;
            } else {
                tags.put("motor_vehicle", "no");
                timeDomains = Sets.newHashSet();
            }
        }
        if (!timeDomains.isEmpty()) {
            try {
                String openingHours = timeDomainsParser.parse(timeDomains);
                tags.put("opening_hours", openingHours);
            } catch (IllegalArgumentException iae) {
                throw new RuntimeException("Could not parse time domain: " + iae.getMessage());
            }
        }
        return tags;
    }
}
