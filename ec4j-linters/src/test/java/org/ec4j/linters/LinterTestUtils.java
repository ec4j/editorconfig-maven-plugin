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
package org.ec4j.linters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ec4j.core.ResourceProperties;
import org.ec4j.lint.api.FormattingHandler;
import org.ec4j.lint.api.Linter;
import org.ec4j.lint.api.Logger;
import org.ec4j.lint.api.Logger.LogLevel;
import org.ec4j.lint.api.Resource;
import org.ec4j.lint.api.Violation;
import org.ec4j.lint.api.ViolationCollector;
import org.junit.Assert;

public class LinterTestUtils {

    public static String assertParse(Linter linter, Resource doc, String expectedText, ResourceProperties props,
            Violation... expected) throws IOException {
        final StringBuilder log = new StringBuilder();
        ViolationCollector collector = new ViolationCollector(false, "mvn editorconfig:format", new Logger.AppendableLogger(LogLevel.TRACE, log));
        collector.startFiles();
        collector.startFile(doc);
        linter.process(doc, props, collector);
        collector.endFile();
        collector.endFiles();

        Map<Resource, List<Violation>> violations = collector.getViolations();
        List<Violation> actual = violations.get(doc);

        if (expected.length == 0) {
            Assert.assertNull("" + expected.length + " violations expected, found " + actual, actual);
        } else {
            Assert.assertNotNull(
                    "found none while expected " + expected.length + " violations: " + Arrays.toString(expected),
                    actual);
            Assert.assertEquals(Arrays.asList(expected), actual);
        }

        FormattingHandler formatter = new FormattingHandler(false, ".bak", Logger.NO_OP);
        formatter.startFiles();
        formatter.startFile(doc);
        linter.process(doc, props, formatter);
        formatter.endFile();
        formatter.endFiles();

        Assert.assertEquals(expectedText, doc.getText());

        return log.toString();

    }

    public static Resource createDocument(String text, String fileExtension) throws IOException {
        Path file = File.createTempFile(LinterTestUtils.class.getSimpleName(), fileExtension).toPath();
        Resource doc = new Resource(file, file, StandardCharsets.UTF_8, text);
        return doc;
    }

    public static Resource createDocument(Path path) throws IOException {
        final Path testDir = Paths.get("target/test-trees/"+ ((int)(Math.random() * 1000000)));
        Files.createDirectories(testDir);
        final Path testFile = testDir.resolve(path.getFileName());
        Files.copy(path, testFile);
        final Resource doc = new Resource(testFile, testFile.getFileName(), StandardCharsets.UTF_8, new String(Files.readAllBytes(path)));
        return doc;
    }

}
