package com.mappy.fpm.batches.splitter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Envelope;
import crosby.binary.osmosis.OsmosisSerializer;
import org.openstreetmap.osmosis.core.misc.v0_6.NullWriter;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;
import static java.util.stream.Collectors.toList;

public class SplitterSerializers {
    private final String pbf;
    private final SplitAreas areas = new SplitAreas();
    private final List<Sink> serializers = Lists.newArrayList();
    private final Map<String, Integer> index = Maps.newHashMap();
    private final String parent;

    @Inject
    public SplitterSerializers(@Named("com.mappy.fpm.splitter.output") String parent, @Named("com.mappy.fpm.tomtom.zone") String pbf) {
        this.parent = parent;
        this.pbf = pbf + ".osm.pbf";
    }

    public Sink serializer(int j) {
        if (j == -1) {
            return new NullWriter();
        }
        return serializers.get(j);
    }

    public Sink serializer(double x, double y) {
        return serializer(serializerIndex(x, y));
    }

    public List<Integer> serializer(Envelope env) {
        return areas.file(env).stream().map(this::serializerIndex).filter(i -> i >= 0).collect(toList());
    }

    private int serializerIndex(double x, double y) {
        String file = areas.file(x, y);
        if (file == null) {
            return -1;
        }
        return serializerIndex(file);
    }

    private int serializerIndex(String filename) {
        try {
            if (index.containsKey(filename)) {
                return index.get(filename);
            }
            File file = new File(parent, filename + "/" + pbf);
            file.getParentFile().mkdirs();
            BlockOutputStream os = new BlockOutputStream(new FileOutputStream(file));
            os.setCompress("none");
            OsmosisSerializer serializer = new OsmosisSerializer(os);
            serializers.add(serializer);
            int last = serializers.size() - 1;
            index.put(filename, last);
            return last;
        }
        catch (FileNotFoundException e) {
            throw propagate(e);
        }
    }

    public void close() {
        for (Sink serializer : serializers) {
            serializer.complete();
            serializer.release();
        }
    }

}