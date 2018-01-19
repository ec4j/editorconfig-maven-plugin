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
package org.ec4j.maven.linters;

import java.io.IOException;

import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.IndentStyleValue;
import org.ec4j.maven.lint.api.Delete;
import org.ec4j.maven.lint.api.EditableResource;
import org.ec4j.maven.lint.api.Linter;
import org.ec4j.maven.lint.api.Location;
import org.ec4j.maven.lint.api.Replace;
import org.ec4j.maven.lint.api.Violation;
import org.junit.Test;

public class XmlLinterTest {
    private final Linter linter = new XmlLinter();

    @Test
    public void indentedRootElement() throws IOException {
        String text = "        <root>\n" + //
                "            <child1>\n" + //
                "                <child2 />\n" + //
                "            </child1>\n" + //
                "        </root>\n";
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        /* no change, no violations */
        LinterTestUtils.assertParse(linter, doc, text, props);
    }

    @Test
    public void indentedRootElementAfterCommentDiffLevel() throws IOException {
        String text = "<!-- comment -->\n" + //
                "        <root>\n" + //
                "            <child1>\n" + //
                "                <child2 />\n" + //
                "            </child1>\n" + //
                "        </root>\n";
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        /* no change, no violations */
        LinterTestUtils.assertParse(linter, doc, text, props);
    }

    @Test
    public void indentedRootElementAfterCommentSameLevel() throws IOException {
        String text = "        <!-- comment -->\n" + //
                "        <root>\n" + //
                "            <child1>\n" + //
                "                <child2 />\n" + //
                "            </child1>\n" + //
                "        </root>\n";
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        /* no change, no violations */
        LinterTestUtils.assertParse(linter, doc, text, props);
    }

    @Test
    public void indentedRootElementAfterUnindentedComment() throws IOException {
        String text = "<!-- comment -->\n" + //
                "        <root>\n" + //
                "            <child1>\n" + //
                "                <child2 />\n" + //
                "            </child1>\n" + //
                "        </root>\n";
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        /* no change, no violations */
        LinterTestUtils.assertParse(linter, doc, text, props);
    }

    @Test
    public void simple() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<!-- license -->\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "     <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<!-- license -->\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 5), new Delete(1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"), //
                new Violation(doc, new Location(6, 3), new Delete(2), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsEndDeleteFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "\t  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 1), new Delete(1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsEndDeleteManyFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "\t   </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 1), new Delete(2), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsEndDeleteManyLast() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "     \t</parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 3), //
                        new Delete(4), linter, PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsEndReplaceMulti() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                " \t  \t </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 2), Replace.indent(4, IndentStyleValue.space, 2), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsEndReplaceMultiBorder() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "\t    \t</parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 1), Replace.indent(6, IndentStyleValue.space, 4), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsEndReplaceOne() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                " <parent-1>\n" + //
                "  <text-1>text in text-1</text-1>\n" + //
                "\t</parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                " <parent-1>\n" + //
                "  <text-1>text in text-1</text-1>\n" + //
                " </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("1").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(5, 1), //
                        Replace.indent(1, IndentStyleValue.space, 1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "1"));
    }

    @Test
    public void spacesForTabsSingleDeleteFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "\t    <text-1/>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1/>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 1), new Delete(1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsSingleDeleteManyFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "\t     <text-1/>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1/>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 1), new Delete(2), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsSingleDeleteManyLast() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "       \t<text-1/>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1/>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 5), //
                        new Delete(4), linter, PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsSingleReplaceMulti() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                " \t    \t  <text-1/>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1/>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 2), Replace.indent(6, IndentStyleValue.space, 5), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsSingleReplaceMultiBorder() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "\t       \t<text-1/>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1/>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 1), Replace.indent(9, IndentStyleValue.space, 8), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsSingleReplaceOne() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "\t<text-1/>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                " <text-1/>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("1").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 1), //
                        Replace.indent(1, IndentStyleValue.space, 1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "1"));
    }

    @Test
    public void spacesForTabsStartDeleteFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "\t  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 1), new Delete(1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsStartDeleteManyFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "\t   <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 1), new Delete(2), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsStartDeleteManyLast() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "     \t<parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text in text-1</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 3), //
                        new Delete(4), linter, PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsStartEndDeleteFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "\t    <text-1>text</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 1), new Delete(1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsStartEndDeleteManyFirst() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "\t     <text-1>text</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 1), new Delete(2), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsStartEndDeleteManyLast() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "       \t<text-1>text</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "  <parent-1>\n" + //
                "    <text-1>text</text-1>\n" + //
                "  </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 5), //
                        new Delete(4), linter, PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "2"));
    }

    @Test
    public void spacesForTabsStartEndReplaceMulti() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                " \t    \t  <text-1>text</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 2), Replace.indent(6, IndentStyleValue.space, 5), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsStartEndReplaceMultiBorder() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "\t       \t<text-1>text</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(4, 1), Replace.indent(9, IndentStyleValue.space, 8), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsStartEndReplaceOne() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "\t<text-1>text</text-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                " <text-1>text</text-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("1").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 1), //
                        Replace.indent(1, IndentStyleValue.space, 1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "1"));
    }

    @Test
    public void spacesForTabsStartReplaceMulti() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                " \t  \t <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 2), Replace.indent(4, IndentStyleValue.space, 2), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsStartReplaceMultiBorder() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "\t    \t<parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "    <parent-1>\n" + //
                "        <text-1>text in text-1</text-1>\n" + //
                "    </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("4").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 1), Replace.indent(6, IndentStyleValue.space, 4), linter,
                        PropertyType.indent_style.getName(), IndentStyleValue.space.name(),
                        PropertyType.indent_size.getName(), "4"));
    }

    @Test
    public void spacesForTabsStartReplaceOne() throws IOException {
        String text = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                "\t<parent-1>\n" + //
                "  <text-1>text in text-1</text-1>\n" + //
                " </parent-1>\n" + //
                "</root>"; //
        String expectedText = "<?xml version=\"1.0\"?>\n" + //
                "<root>\n" + //
                " <parent-1>\n" + //
                "  <text-1>text in text-1</text-1>\n" + //
                " </parent-1>\n" + //
                "</root>"; //
        EditableResource doc = LinterTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("1").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        LinterTestUtils.assertParse(linter, doc, expectedText, props, //
                new Violation(doc, new Location(3, 1), //
                        Replace.indent(1, IndentStyleValue.space, 1), linter, PropertyType.indent_style.getName(),
                        IndentStyleValue.space.name(), PropertyType.indent_size.getName(), "1"));
    }

}
