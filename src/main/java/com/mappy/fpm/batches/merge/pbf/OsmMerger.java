package com.mappy.fpm.batches.merge.pbf;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import crosby.binary.osmosis.OsmosisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.google.common.io.Files.copy;
import static java.util.stream.Collectors.toList;

@Slf4j
public class OsmMerger {

    public void merge(List<String> inputFiles, String outputFile) throws IOException {
        Preconditions.checkArgument(!inputFiles.isEmpty(), "At least one input file must be specify.");

        if (inputFiles.size() == 1) {
            copy(new File(inputFiles.get(0)), new File(outputFile));

        } else {
            Stopwatch watch = Stopwatch.createStarted();

            PbfIterator[] iterators = inputFiles.stream().map(PbfIterator::new).collect(toList()).toArray(new PbfIterator[] {});
            Iterator<EntityContainer> merge = MergingOsmPbfIterator.merge(iterators);

            log.info("Writing merged data to {}", outputFile);
            try (FileOutputStream output = new FileOutputStream(outputFile)) {
                OsmosisSerializer serializer = new OsmosisSerializer(new BlockOutputStream(output));
                while (merge.hasNext()) {
                    serializer.process(merge.next());
                }
            }
            double size = ((double) new File(outputFile).length()) / 1024 / 1024;
            log.info("Done writing {} ({} Mo) in {}.", outputFile, String.format("%.2f", size), watch);
        }
    }
}
