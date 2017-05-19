package com.mappy.data.batches.tomtom.dbf.signposts;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.mappy.data.batches.tomtom.TomtomFolder;
import com.mappy.data.batches.tomtom.dbf.signposts.SignPost.PictogramType;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.data.batches.tomtom.dbf.signposts.SignPost.ConnectionType.Branch;
import static com.mappy.data.batches.tomtom.dbf.signposts.SignPost.ConnectionType.Towards;
import static com.mappy.data.batches.tomtom.dbf.signposts.SignPost.InfoType.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class SignPosts {
    private static final Comparator<List<SignPost>> bySize = Comparator.comparing(List::size);
    private static final Predicate<SignPost> onlyDestinationRefTowards = signPost -> (signPost.getInfotyp() == Route_Number_on_Shield || signPost.getInfotyp() == Route_Number)
            && signPost.getContyp() == Towards;
    private static final Predicate<SignPost> onlyDestinationLabel = signPost -> signPost.getInfotyp() == Place_Name || signPost.getInfotyp() == Other_Destination;
    private static final Predicate<SignPost> onlyDestination = onlyDestinationRefTowards.or(onlyDestinationLabel);
    private static final Predicate<SignPost> onlySymbol = signPost -> signPost.getInfotyp() == Pictogram;

    private final ListMultimap<Long, SignPost> signs;
    private final ListMultimap<Long, Long> sp;
    private final Map<Long, Long> sg;

    @Inject
    public SignPosts(TomtomFolder folder) {
        signs = siFile(folder);
        sp = spFile(folder);
        sg = sgFile(folder);
    }

    public Map<String, String> getTags(long tomtomId, boolean oneWay, Long fromJunctionId, Long toJunctionId) {
        Map<String, String> tags = Maps.newHashMap();
        List<Long> signIds = sp.get(tomtomId);
        if (!signIds.isEmpty()) {
            Long signId = signIds.get(0);
            Long junctionId = sg.get(signId);
            String destinationTag = null;
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
                log.warn("Junction ID {} does not correspond to FROM {} or TO {} for sign post {} on road {}", junctionId, fromJunctionId, toJunctionId, signId, tomtomId);
                destinationTag = "destination:undefined";
            }

            if (isNotEmpty(signPostHeaderFor(tomtomId))) {
                tags.put(destinationTag + ":ref", Joiner.on(";").join(signPostHeaderFor(tomtomId)));
            }
            if (isNotEmpty(signPostContentFor(tomtomId))) {
                tags.put(destinationTag, Joiner.on(";").join(signPostContentFor(tomtomId)));
            }
            if (isNotEmpty(symbolRefFor(tomtomId))) {
                tags.put(destinationTag + ":symbol", Joiner.on(";").join(symbolRefFor(tomtomId)));
            }
            if (isNotEmpty(exitRefFor(tomtomId))) {
                tags.put("junction:ref", Joiner.on(";").join(exitRefFor(tomtomId)));
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
        return symbols;
    }

    public List<String> exitRefFor(long tomtomId) {
        return refFor(tomtomId, signPost -> signPost.getInfotyp() == Exit_Number || signPost.getInfotyp() == Exit_Name).stream() //
                .map(SignPost::getTxtcont).collect(toList());
    }

    private List<SignPost> refFor(long tomtomId, Predicate<SignPost> filter) {
        return sp.get(tomtomId).stream() //
                .map(signs::get).sorted(bySize.reversed()).findFirst() //
                .map(signPostList -> signPostList.stream() //
                        .filter(filter) //
                        .sorted() //
                        .distinct() //
                        .collect(toList())) //
                .orElse(newArrayList());
    }

    private static ListMultimap<Long, SignPost> siFile(TomtomFolder folder) {
        ListMultimap<Long, SignPost> signs = ArrayListMultimap.create();
        File file = new File(folder.getFile("si.dbf"));
        if (!file.exists()) {
            return signs;
        }
        log.info("Reading SI file {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                if (row.getInt("AMBIG") != 1) {
                    SignPost sign = SignPost.fromRow(row);
                    signs.put(sign.getId(), sign);
                }
            }
        }
        log.info("Loaded {} signs", signs.size());
        return signs;
    }

    private static ListMultimap<Long, Long> spFile(TomtomFolder folder) {
        File file = new File(folder.getFile("sp.dbf"));
        ListMultimap<Long, Long> result = ArrayListMultimap.create();
        if (!file.exists()) {
            return result;
        }
        Multimap<Long, SignPostPath> sorted = TreeMultimap.create();
        log.info("Reading SP file {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                SignPostPath path = SignPostPath.fromRow(row);
                sorted.put(path.getId(), path);
            }
        }
        for (Long key : sorted.keySet()) {
            result.put(sorted.get(key).iterator().next().getTomtomId(), key);
        }
        log.info("Loaded {} sign paths", sorted.size());
        return result;
    }

    private static Map<Long, Long> sgFile(TomtomFolder folder) {
        File file = new File(folder.getFile("sg.dbf"));
        Map<Long, Long> speeds = Maps.newHashMap();
        if (!file.exists()) {
            return speeds;
        }

        log.info("Reading SG File {}", file);
        try (DbfReader reader = new DbfReader(file)) {
            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                speeds.put(row.getLong("ID"), row.getLong("JNCTID"));
            }
        }
        log.info("Loaded {} Sign posts junctions", speeds.size());
        return speeds;
    }
}
