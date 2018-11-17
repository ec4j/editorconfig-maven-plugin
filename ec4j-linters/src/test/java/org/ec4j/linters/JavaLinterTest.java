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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.PropertyType;
import org.ec4j.lint.api.Delete;
import org.ec4j.lint.api.FormattingHandler;
import org.ec4j.lint.api.Insert;
import org.ec4j.lint.api.Location;
import org.ec4j.lint.api.Logger;
import org.ec4j.lint.api.Logger.LogLevel;
import org.ec4j.lint.api.Replace;
import org.ec4j.lint.api.Resource;
import org.ec4j.lint.api.Violation;
import org.ec4j.lint.api.ViolationCollector;
import org.ec4j.linters.JavaLinter.EditVisitor.ReplaceEditInfo;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;
import org.junit.Test;

public class JavaLinterTest {
    private static void assertMinimize(String source, int offset, int length, String replacement, int expecedOffset,
            int expectedLength, String expectedReplacement) {
        final ReplaceEdit edit = new ReplaceEdit(offset, length, replacement);
        final ReplaceEditInfo actual = JavaLinter.EditVisitor.minimize(edit, source);
        Assert.assertEquals("offset", expecedOffset, actual.offset);
        Assert.assertEquals("length", expectedLength, actual.length);
        Assert.assertEquals("replacement", expectedReplacement, new String(actual.replacement));
    }

    private final JavaLinter linter = new JavaLinter();

    String assertLint(Resource doc, String expectedPath, ResourceProperties props, Violation... expected)
            throws IOException, MalformedTreeException, BadLocationException {
        final StringBuilder log = new StringBuilder();
        ViolationCollector collector = new ViolationCollector(false, "mvn editorconfig:format",
                new Logger.AppendableLogger(LogLevel.TRACE, log));
        collector.startFiles();
        collector.startFile(doc);
        linter.process(doc, props, collector);
        collector.endFile();
        collector.endFiles();

        Map<Resource, List<Violation>> violations = collector.getViolations();
        List<Violation> actual = violations.get(doc);

        System.out.println(log);

        final IDocument d = new Document();
        d.set(doc.getText());

        FormattingHandler formatter = new FormattingHandler(false, ".bak", Logger.NO_OP);
        formatter.startFiles();
        formatter.startFile(doc);
        final TextEdit result = linter.processInternal(doc, props, formatter);
        formatter.endFile();
        formatter.endFiles();

        final String expectedText = new String(Files.readAllBytes(Paths.get(expectedPath)), StandardCharsets.UTF_8);
        if (!expectedText.equals(doc.getText())) {
            Files.write(Paths.get(doc.getPath().toString().replace(".java", ".expected.java")),
                    doc.getText().getBytes(StandardCharsets.UTF_8));
        }

        if (expected.length == 0) {
            Assert.assertNull("" + expected.length + " violations expected, found " + actual, actual);
        } else {
            Assert.assertNotNull(
                    "found none while expected " + expected.length + " violations: " + Arrays.toString(expected),
                    actual);
            Assert.assertEquals(Arrays.asList(expected), actual);
        }

        Assert.assertEquals(expectedText, doc.getText());

        if (result != null) {
            result.apply(d);
        }
        Assert.assertEquals(expectedText, d.get());

        return log.toString();

    }

    @Test
    public void good() throws IOException, MalformedTreeException, BadLocationException {

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .build();

        final Resource doc = LinterTestUtils.createDocument(Paths.get("src/test/resources/java/Good.java"));

        /* no change, no violations */
        final String log = assertLint(doc, "src/test/resources/java/Good.java", props);
    }

    @Test
    public void minimize() {
        assertMinimize("abcdefg", 1, 3, "bCd", 2, 1, "C");
        assertMinimize("abcdefg", 1, 3, "Bcd", 1, 1, "B");
        assertMinimize("abcdefg", 1, 3, "bcD", 3, 1, "D");
    }

    @Test
    public void mixedIndent() throws IOException, MalformedTreeException, BadLocationException {

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .build();

        final Resource doc = LinterTestUtils.createDocument(Paths.get("src/test/resources/java/MixedIndent.java"));

        /* no change, no violations */
        final String log = assertLint(doc, "src/test/resources/java/MixedIndent.expected.java", props, //
                new Violation(doc, new Location(21, 3), Insert.repeat(' ', 2), linter, "java/<unknown>"), //
                new Violation(doc, new Location(24, 9), new Delete(2), linter, "java/<unknown>"), //
                new Violation(doc, new Location(29, 5), Replace.ofReplaced("\t", "    "), linter, "java/<unknown>") //
        );

    }

    @Test
    public void longLinesSplit120() throws IOException, MalformedTreeException, BadLocationException {

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .property(new Property.Builder(null).type(PropertyType.max_line_length).value("120").build()) //
                .build();

        final Resource doc = LinterTestUtils.createDocument(Paths.get("src/test/resources/java/LongLines.java"));

        /* no change, no violations */
        final String log = assertLint(doc, "src/test/resources/java/LongLines.expected.java", props, //
                new Violation(doc, new Location(27, 22), Insert.text("\n            "), linter, "java/<unknown>") //
        );

    }

    @Test
    public void longLinesAllowed() throws IOException, MalformedTreeException, BadLocationException {

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .build();

        final Resource doc = LinterTestUtils.createDocument(Paths.get("src/test/resources/java/LongLines.java"));

        /* no change, no violations */
        final String log = assertLint(doc, "src/test/resources/java/LongLines.java", props);

    }

}
