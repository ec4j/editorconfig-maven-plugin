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
package org.ec4j.lint.api;

import org.ec4j.lint.api.Resource.LineIndex;
import org.junit.Assert;
import org.junit.Test;

public class LineIndexTest {

    private static void assertFindLineStart(String source, int line, int offset) {
        final LineIndex li = LineIndex.of(source);
        Assert.assertEquals(offset, li.findLineStart(line));
    }

    private static void assertFindLocation(String source, int offset, int line, int column) {
        final LineIndex li = LineIndex.of(source);
        Assert.assertEquals(new Location(line, column), li.findLocation(offset));
    }

    private static void assertOf(String source, int[] expectedIndex) {
        final LineIndex li = LineIndex.of(source);
        Assert.assertArrayEquals(expectedIndex, li.lineStartOffsets);
    }

    @Test
    public void findLineStart() {
        assertFindLineStart("", 1, 0);
        assertFindLineStart(" ", 1, 0);
        assertFindLineStart("\n", 1, 0);
        assertFindLineStart("\n", 2, 1);
        assertFindLineStart(" \n \n", 1, 0);
        assertFindLineStart(" \n \n", 2, 2);
        assertFindLineStart(" \n \n", 3, 4);
    }

    @Test
    public void findLocation() {
        assertFindLocation("", 0, 1, 1);
        assertFindLocation(" ", 1, 1, 2);
        assertFindLocation("\n", 0, 1, 1);
        assertFindLocation("\n", 1, 2, 1);
        assertFindLocation(" \n \n", 0, 1, 1);
        assertFindLocation(" \n \n", 1, 1, 2);
        assertFindLocation(" \n \n", 2, 2, 1);
        assertFindLocation(" \n \n ", 3, 2, 2);
        assertFindLocation(" \n \n ", 4, 3, 1);
        assertFindLocation(" \n \n ", 5, 3, 2);
        assertFindLocation(" \n \n ", 6, 3, 3);
        assertFindLocation(" \n", 1, 1, 2);
        assertFindLocation(" \n", 2, 2, 1);
    }

    @Test
    public void of() {
        assertOf("", new int[0]);
        assertOf(" ", new int[0]);
        assertOf("\n", new int[] { 1 });
        assertOf(" \n", new int[] { 2 });
        assertOf(" \n ", new int[] { 2 });
        assertOf("\r\n", new int[] { 2 });
        assertOf(" \r\n", new int[] { 3 });
        assertOf(" \r\n ", new int[] { 3 });
        assertOf(" \n \n", new int[] { 2, 4 });
    }
}
