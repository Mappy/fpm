package com.mappy.fpm.batches.tomtom.dbf.signposts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.*;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPost.PictogramType;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.Colours.GREEN;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.Colours.WHITE;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.Colours.getColourOrGreen;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.InfoType.*;
import static com.mappy.fpm.batches.tomtom.dbf.signposts.SignPost.ConnectionType.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.empty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class SignPosts extends TomtomDbfReader {

    private static final Predicate<SignPost> onlyDestinationRefTowards = signPost -> isaRouteNumber(signPost.getInfotyp()) && Towards.equals(signPost.getContyp());
    private static final Predicate<SignPost> onlyDestinationRefBranch = signPost -> isaRouteNumber(signPost.getInfotyp()) && Branch.equals(signPost.getContyp());
    private static final Predicate<SignPost> onlyDestinationLabel = signPost -> asList(Place_Name, Other_Destination, Exit_Name).contains(signPost.getInfotyp());
    private static final Predicate<SignPost> onlyDestination = onlyDestinationRefTowards.or(onlyDestinationLabel);
    private static final Predicate<SignPost> onlyShield = signPost -> isaShieldNumber(signPost.getInfotyp());
    private static final Predicate<SignPost> onlyColours = signPost -> Route_Number_Type.equals(signPost.getInfotyp());
    private static final Predicate<SignPost> onlySymbol = signPost -> Pictogram.equals(signPost.getInfotyp());
    private static final Predicate<SignPost> onlyExit = signPost -> Exit.equals(signPost.getContyp()) && Exit_Number.equals(signPost.getInfotyp());
    private static final Pattern EXIT_NUMBER_PATTERN = compile("((\\d|\\.)+\\w{0,1})(.*)");
    private static final Pattern EXIT_LABEL_PATTERN = compile("(\\d|\\.)+\\w{0,1} (.*)");

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
        lastWayOfPath = extractLastWay();
        waysWithSign = regroupWaysWithSign();
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
        if (waysWithSign.contains(tomtomId)) {
            ways.put(tomtomId, new SignWay(tomtomId, fromJunctionId, toJunctionId));
        }

        List<Long> signIds = lastWayOfPath.get(tomtomId);
        if (signIds.isEmpty()) {
            return tags;
        }
        Long signId = signIds.get(0);
        Long junctionId = sg.get(signId);
        String destinationTag;
        if (oneWay) {
            destinationTag = "destination";
        } else if (junctionId == null) {
            log.warn("No junction ID for sign post {} on road {}", signId, tomtomId);
            destinationTag = "destination:undefined";
        } else if (junctionId.equals(toJunctionId)) {
            destinationTag = "destination:forward";
        } else if (junctionId.equals(fromJunctionId)) {
            destinationTag = "destination:backward";
        } else {
            destinationTag = getSignOnIntermediateJunction(fromJunctionId, toJunctionId, signId, junctionId);
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

        List<String> signPostColourFor = signPostColourFor(tomtomId);
        if (isNotEmpty(signPostColourFor)) {
            tags.put(destinationTag + ":colour", on(";").join(signPostColourFor));
        }

       // exitRefFor(tomtomId).ifPresent(exitRef -> tags.put("junction:ref", exitRef));
        return tags;
    }

    private String getSignOnIntermediateJunction(Long fromJunctionId, Long toJunctionId, Long signId, Long junctionId) {
        Optional<SignPostPath> previousTomtomId = getPreviousTomtomId(signId);
        if (previousTomtomId.isPresent()) {
            SignWay previousWay = ways.get(previousTomtomId.get().getTomtomId());

            if (previousWay.getFromJunctionId().equals(fromJunctionId) || previousWay.getToJunctionId().equals(fromJunctionId)) {
                return "destination:forward";
            } else if (previousWay.getFromJunctionId().equals(toJunctionId) || previousWay.getToJunctionId().equals(toJunctionId)) {
                return "destination:backward";
            }
            log.warn("Junction ID {} does not correspond to FROM {} or TO {} for sign post {}", junctionId, fromJunctionId, toJunctionId, signId);
        } else {
            log.warn("All ways have not been parse for {}", signId);
        }
        return "destination:undefined";
    }

    private Optional<SignPostPath> getPreviousTomtomId(Long signId) {
        Collection<SignPostPath> signPostPaths = sp.get(signId);
        return signPostPaths.stream()
                .filter(s -> s.getSeqnr() == signPostPaths.size() - 1)
                .findFirst();
    }

    @VisibleForTesting
    List<String> signPostHeaderFor(Long tomtomId) {
        return refFor(tomtomId, onlyDestinationRefBranch)
                .stream()
                .map(SignPost::getTxtcont)
                .collect(toList());
    }

    @VisibleForTesting
    List<String> signPostContentFor(Long tomtomId) {
        Map<Integer, List<List<SignPost>>> signPosts = getSignPostsByDestinationSequentialNumber(tomtomId, onlyDestination);

        List<String> content = new ArrayList<String>();
        for (List<List<SignPost>> signsBySeq : signPosts.values()) {
            String lineStr = new String();
            for (List<SignPost> signsByDestSeq : signsBySeq) {
                for (SignPost item : signsByDestSeq) {
                    lineStr = lineStr.concat(item.getTxtcont()).concat(" ");
                }
            }
            content.add(lineStr.trim());
        }
        Optional<String> exitLabel = exitLabelFor(tomtomId);
        if (exitLabel.isPresent())
            content.add(exitLabel.get());
        return content;
    }

    @VisibleForTesting
    List<String> signPostColourFor(Long tomtomId) {

        Collection<List<SignPost>> signPostsBySeqnr = getSignPostsBySequentialNumber(tomtomId, onlyColours);

        List<String> colors = getColourOnlyForShield(signPostsBySeqnr);

        if (colors.stream().anyMatch(colour -> !WHITE.value.equals(colour))) {
            return colors;
        }
        return newArrayList();

    }

    @VisibleForTesting
    List<String> symbolRefFor(Long tomtomId) {

        Collection<List<SignPost>> signPostsBySeqnr = getSignPostsBySequentialNumber(tomtomId, onlySymbol);

        List<String> symbols = getSymbolOnlyForDestination(signPostsBySeqnr);

        if (symbols.stream().anyMatch(symbol -> !"none".equals(symbol))) {
            return symbols;
        }
        return newArrayList();
    }

    private Map<Integer, List<List<SignPost>>> getSignPostsByDestinationSequentialNumber(Long tomtomId, Predicate<SignPost> onlyType) {
        return refFor(tomtomId, onlyType.or(onlyDestination))
                .stream()
                .collect(groupingBy(SignPost::getSeqnr,
                        collectingAndThen(groupingBy(SignPost::getDestseq),
                                m -> new ArrayList<>(m.values()))));
    }

    private Collection<List<SignPost>> getSignPostsBySequentialNumber(Long tomtomId, Predicate<SignPost> onlyType) {
        return refFor(tomtomId, onlyType.or(onlyDestination))
                .stream()
                .collect(groupingBy(SignPost::getSeqnr))
                .values();
    }

    private List<String> getSymbolOnlyForDestination(Collection<List<SignPost>> signPostsBySeqnr) {
        return signPostsBySeqnr.stream()
                .flatMap(signPosts -> signPosts.stream()
                        .filter(onlyDestination)
                        .map(signPost -> getSymbolOrNone(signPosts)))
                .collect(toList());
    }

    private List<String> getColourOnlyForShield(Collection<List<SignPost>> signPostsBySeqnr) {
        return signPostsBySeqnr.stream()
                .flatMap(signPosts -> signPosts.stream()
                        .filter(onlyShield)
                        .map(signPost -> getColourOrGreen(signPosts)))
                .collect(toList());
    }

    private String getSymbolOrNone(List<SignPost> signs) {
        return signs.stream()
                .filter(onlySymbol)
                .findFirst()
                .map(sp -> PictogramType.name(sp.getTxtcont()))
                .orElse("none");
    }

    private String getColourOrGreen(List<SignPost> signs) {
        return signs.stream()
                .filter(onlyColours)
                .findFirst()
                .map(sp -> Colours.getColourOrGreen(sp.getTxtcont()))
                .orElse(GREEN.value);
    }

    @VisibleForTesting
    Optional<String> exitRefFor(long tomtomId) {
        return getExitText(tomtomId)
                .map(EXIT_NUMBER_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .findFirst();
    }

    private Optional<String> exitLabelFor(long tomtomId) {
        return getExitText(tomtomId)
                .map(EXIT_LABEL_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(2))
                .findFirst();
    }

    private Stream<String> getExitText(long tomtomId) {
        return lastWayOfPath.get(tomtomId).stream().flatMap(a -> si.get(a).stream())
                .filter(onlyExit)
                .map(SignPost::getTxtcont);
    }

    private List<SignPost> refFor(long tomtomId, Predicate<SignPost> filter) {
        return lastWayOfPath.get(tomtomId).stream()
                .map(si::get)
                .max(comparing(List::size))
                .map(signPostList -> signPostList.stream()
                        .filter(filter)
                        .sorted()
                        .distinct()
                        .collect(toList()))
                .orElse(newArrayList());
    }

    private ListMultimap<Long, Long> extractLastWay() {
        ListMultimap<Long, Long> result = ArrayListMultimap.create();

        sp.keySet().forEach(key -> sp.get(key).stream()
                .findFirst()
                .ifPresent(signPostPath -> result.put(signPostPath.getTomtomId(), key)));

        return result;
    }

    private Set<Long> regroupWaysWithSign() {
        return sp.keySet().stream()
                .flatMap(aLong -> sp.get(aLong).stream().map(SignPostPath::getTomtomId))
                .collect(toSet());
    }
}
