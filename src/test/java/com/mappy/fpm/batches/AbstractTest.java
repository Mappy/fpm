package com.mappy.fpm.batches;

import org.junit.BeforeClass;

import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Paths.get;

public abstract class AbstractTest {

    @BeforeClass
    public static void createDir() throws Exception {
        Path dir = get("target", "tests");
        if (!dir.toFile().exists()) {
            createDirectory(dir);
        }
    }
}
