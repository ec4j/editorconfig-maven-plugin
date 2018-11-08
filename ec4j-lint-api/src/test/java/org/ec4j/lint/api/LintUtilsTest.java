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

import org.junit.Assert;
import org.junit.Test;

public class LintUtilsTest {

    @Test
    public void escape() {
        Assert.assertNull(LintUtils.escape(null));
        Assert.assertEquals("", LintUtils.escape(""));
        Assert.assertEquals("   ", LintUtils.escape("   "));
        Assert.assertEquals("\\n", LintUtils.escape("\n"));
        Assert.assertEquals("\\r\\n", LintUtils.escape("\r\n"));
        Assert.assertEquals(" \\t ", LintUtils.escape(" \t "));
        Assert.assertEquals(" \\\\ ", LintUtils.escape(" \\ "));
    }
}
