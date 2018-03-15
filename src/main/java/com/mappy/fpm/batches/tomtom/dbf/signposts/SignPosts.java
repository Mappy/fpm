package com.mappy.fpm.batches.tomtom.dbf.signposts;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPost.PictogramType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.SignPost.ConnectionType.Branch;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.SignPost.ConnectionType.Towards;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.SignPost.InfoType.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class SignPosts extends TomtomDbfReader {

    private static final Comparator<List<SignPost>> bySize = Comparator.comparing(List::size);
    private static final Predicate<SignPost> onlyDestinationRefTowards = signPost -> (signPost.getInfotyp() == Route_Number_on_Shield || signPost.getInfotyp() == Route_Number)
            && signPost.getContyp() == Towards;
    private static final Predicate<SignPost> onlyDestinationLabel = signPost -> signPost.getInfotyp() == Place_Name || signPost.getInfotyp() == Other_Destination;
    private static final Predicate<SignPost> onlyDestination = onlyDestinationRefTowards.or(onlyDestinationLabel);
    private static final Predicate<SignPost> onlySymbol = signPost -> signPost.getInfotyp() == Pictogram;

    private final ListMultimap<Long, SignPost> si = ArrayListMultimap.create();
    private final Map<Long, Long> sg = newHashMap();
    private final Multimap<Long, SignPostPath> sp = TreeMultimap.create();
    private final Map<Long, SignWay> ways = newHashMap();
    private final ListMultimap<Long, Long> lastWayOfPath;
    private final Set<Long> waysWithSign;

    @Inject
    public SignPosts(TomtomFolder folder) {
        super(folder);
        readFile("si.dbf", this::loadSignPostInformation);
        readFile("sp.dbf", this::loadSignPostPath);
        readFile("sg.dbf", this::loadSignPostGeometry);
        readFile("nw.dbf", this::loadWays);
        lastWayOfPath = extractLastWay(sp);
        waysWithSign = regroupPerWays(sp);
    }

    private void loadWays(DbfRow row) {
        ways.put(row.getLong("ID"), new SignWay(row.getLong("ID"), row.getLong("F_JNCTID"), row.getLong("T_JNCTID")));
    }

    private void loadSignPostGeometry(DbfRow row) {
        sg.put(row.getLong("ID"), row.getLong("JNCTID"));
    }

    private void loadSignPostPath(DbfRow row) {
        SignPostPath path = SignPostPath.fromRow(row);
        sp.put(path.getId(), path);
    }

    private void loadSignPostInformation(DbfRow row) {
        SignPost v = SignPost.fromRow(row);
        si.put(v.getId(), v);
    }

    public Map<String, String> getTags(long tomtomId, boolean oneWay, Long fromJunctionId, Long toJunctionId) {
        Map<String, String> tags = newHashMap();
        if(waysWithSign.contains(tomtomId)) {
            ways.put(tomtomId, new SignWay(tomtomId, fromJunctionId, toJunctionId));
        }

        List<Long> signIds = lastWayOfPath.get(tomtomId);
        if (!signIds.isEmpty()) {
            Long signId = signIds.get(0);
            Long junctionId = sg.get(signId);
            String destinationTag;
            if (oneWay) {
                destinationTag = "destination";
            }
            else if (junctionId == null) {
                log.warn("No junction ID for sign post {} on road {}", signId, tomtomId);
                destinationTag = "destination:undefined";
            }
            else if (junctionId.equals(toJunctionId)) {
                destinationTag = "destination:forward";
            }
            else if (junctionId.equals(fromJunctionId)) {
                destinationTag = "destination:backward";
            }
            else {
                Collection<SignPostPath> signPostPaths = sp.get(signId);
                SignWay previewWay = ways.get(signPostPaths.stream().filter(s -> s.getSeqnr() == signPostPaths.size() - 1).findFirst().get().getTomtomId());

                if(previewWay != null) {
                    if (previewWay.getFromJunctionId().equals(fromJunctionId) || previewWay.getToJunctionId().equals(fromJunctionId)) {
                        destinationTag = "destination:forward";
                    } else if (previewWay.getFromJunctionId().equals(toJunctionId) || previewWay.getToJunctionId().equals(toJunctionId)) {
                        destinationTag = "destination:backward";
                    } else {
                        log.warn("Junction ID {} does not correspond to FROM {} or TO {} for sign post {} on road {}", junctionId, fromJunctionId, toJunctionId, signId, tomtomId);
                        destinationTag = "destination:undefined";
                    }
                } else {
                    log.warn("All ways have not been parse for {}", signId);
                    destinationTag = "destination:undefined";
                }
            }

            List<String> signPostHeaderFor = signPostHeaderFor(tomtomId);
            if (isNotEmpty(signPostHeaderFor)) {
                tags.put(destinationTag + ":ref", on(";").join(signPostHeaderFor));
            }

            List<String> signPostContentFor = signPostContentFor(tomtomId);
            if (isNotEmpty(signPostContentFor)) {
                tags.put(destinationTag, on(";").join(signPostContentFor));
            }

            List<String> symbolRefFor = symbolRefFor(tomtomId);
            if (isNotEmpty(symbolRefFor)) {
                tags.put(destinationTag + ":symbol", on(";").join(symbolRefFor));
            }

            List<String> exitRefFor = exitRefFor(tomtomId);
            if (isNotEmpty(exitRefFor)) {
                tags.put("junction:ref", on(";").join(exitRefFor));
            }
        }
        return tags;
    }

    public List<String> signPostHeaderFor(long tomtomId) {
        return refFor(
                tomtomId,
                signPost -> (signPost.getInfotyp() == Route_Number_on_Shield //
                        || signPost.getInfotyp() == Route_Number) //
                        && signPost.getContyp() == Branch).stream().map(SignPost::getTxtcont).collect(toList());
    }

    public List<String> signPostContentFor(long tomtomId) {
        return refFor(tomtomId, onlyDestination).stream().map(SignPost::getTxtcont).collect(toList());
    }

    public List<String> symbolRefFor(long tomtomId) {
        List<String> symbols = newArrayList();

        Collection<List<SignPost>> signPostsBySeqnr = refFor(tomtomId, onlySymbol.or(onlyDestination)).stream().collect(groupingBy(SignPost::getSeqnr)).values();

        for (List<SignPost> signs : signPostsBySeqnr) {
            long numberOfDestinations = signs.stream().filter(onlyDestination).count();
            for (int i = 0; i < numberOfDestinations; i++) {
                symbols.add(signs.stream().filter(onlySymbol).findFirst().map(sp -> PictogramType.name(sp.getTxtcont())).orElse("none"));
            }
        }

        if(symbols.stream().anyMatch(s -> !"none".equals(s))) {
            return symbols;
        }
        return newArrayList();
    }

    public List<String> exitRefFor(long tomtomId) {
        return refFor(tomtomId, signPost -> signPost.getInfotyp() == Exit_Number || signPost.getInfotyp() == Exit_Name).stream() //
                .map(SignPost::getTxtcont).collect(toList());
    }

    private List<SignPost> refFor(long tomtomId, Predicate<SignPost> filter) {
        return lastWayOfPath.get(tomtomId).stream() //
                .map(si::get).sorted(bySize.reversed()).findFirst() //
                .map(signPostList -> signPostList.stream() //
                        .filter(filter) //
                        .sorted() //
                        .distinct() //
                        .collect(toList())) //
                .orElse(newArrayList());
    }

    private static ListMultimap<Long, Long> extractLastWay(Multimap<Long, SignPostPath> sp) {
        ListMultimap<Long, Long> result = ArrayListMultimap.create();

        sp.keySet().forEach(key -> result.put(sp.get(key).iterator().next().getTomtomId(), key));

        return result;
    }

    private static Set<Long> regroupPerWays(Multimap<Long, SignPostPath> sp) {
        Set<Long> result = newHashSet();

        for (Long key : sp.keySet()) {
            for(SignPostPath signPostPath : sp.get(key)) {
                result.add(signPostPath.getTomtomId());
            }
        }

        return result;
    }

    @Data
    private static class SignWay {
        private final Long tomtomId;
        private final Long fromJunctionId;
        private final Long toJunctionId;
    }
}
