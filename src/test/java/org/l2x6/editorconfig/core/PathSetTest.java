package org.l2x6.editorconfig.core;

import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class PathSetTest {

    private static void assertContains(PathSet ps, String path, boolean expected) {
        Assert.assertEquals(expected, ps.contains(Paths.get(path)));
    }

    @Test
    public void contains() {
        PathSet ps = PathSet.builder() //
                .include("**/*") //
                .exclude("**/*.bad") //
                .build();

        assertContains(ps, "file.good", true);
        assertContains(ps, "dir1/file.good", true);
        assertContains(ps, "file.bad", false);
        assertContains(ps, "dir1/file.bad", false);
    }

    @Test
    public void containsScanner() {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir("target/test-classes/pathset");
        scanner.setIncludes(new String[]{"**/*"});
        scanner.setExcludes(new String[]{"**/*.bad"});
        scanner.scan();
        Set<String> includedFiles = new HashSet<>(Arrays.asList(scanner.getIncludedFiles()));

        Assert.assertTrue(includedFiles.contains("file.good"));
        Assert.assertTrue(includedFiles.contains("dir1/file.good"));
        Assert.assertFalse(includedFiles.contains("file.bad"));
        Assert.assertFalse(includedFiles.contains("dir1/file.bad"));
    }

}