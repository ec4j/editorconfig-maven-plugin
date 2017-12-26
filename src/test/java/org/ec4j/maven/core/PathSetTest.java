/**
 * Copyright (c) 2017 EditorConfig Maven Plugin
 * project contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ec4j.maven.core;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.Assert;
import org.junit.Test;

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
        scanner.setIncludes(new String[] { "**/*" });
        scanner.setExcludes(new String[] { "**/*.bad" });
        scanner.scan();
        Set<String> includedFiles = new HashSet<>(Arrays.asList(scanner.getIncludedFiles()));

        Assert.assertTrue(includedFiles.contains("file.good"));
        Assert.assertTrue(includedFiles.contains("dir1/file.good"));
        Assert.assertFalse(includedFiles.contains("file.bad"));
        Assert.assertFalse(includedFiles.contains("dir1/file.bad"));
    }

}