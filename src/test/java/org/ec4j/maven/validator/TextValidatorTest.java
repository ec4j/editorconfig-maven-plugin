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
package org.ec4j.maven.validator;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.EndOfLineValue;
import org.ec4j.maven.core.Delete;
import org.ec4j.maven.core.EditableResource;
import org.ec4j.maven.core.Insert;
import org.ec4j.maven.core.Location;
import org.ec4j.maven.core.Replace;
import org.ec4j.maven.core.Validator;
import org.ec4j.maven.core.Violation;
import org.junit.Test;

public class TextValidatorTest {

    private final Validator validator = new TextValidator();

    @Test
    public void end_of_line_cr() throws IOException, MojoExecutionException {
        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("cr").build()) //
                .build();
        String text = "line 1\n" + //
                "line 2 \r" + //
                "line 3\t\r" + //
                "line 4\r\n" + //
                "line 5\r" + //
                "line 6 ";
        String expectedText = "line 1\r" + //
                "line 2 \r" + //
                "line 3\t\r" + //
                "line 4\r" + //
                "line 5\r" + //
                "line 6 ";
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".txt");

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(1, 7),
                        Replace.endOfLine(PropertyType.EndOfLineValue.lf, PropertyType.EndOfLineValue.cr), validator,
                        PropertyType.end_of_line.getName(), "cr"), //
                new Violation(doc, new Location(4, 8), new Delete(1), validator, PropertyType.end_of_line.getName(),
                        "cr") //
        );
    }

    @Test
    public void end_of_line_crlf() throws IOException, MojoExecutionException {
        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("crlf").build()) //
                .build();
        String text = "line 1\n" + //
                "line 2 \r\n" + //
                "line 3\t\r\n" + //
                "line 4\r\n" + //
                "line 5\r" + //
                "line 6 ";
        String expectedText = "line 1\r\n" + //
                "line 2 \r\n" + //
                "line 3\t\r\n" + //
                "line 4\r\n" + //
                "line 5\r\n" + //
                "line 6 ";
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".txt");

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(1, 7), Insert.endOfLine(PropertyType.EndOfLineValue.cr), validator,
                        PropertyType.end_of_line.getName(), "crlf"), //
                new Violation(doc, new Location(5, 8), Insert.endOfLine(PropertyType.EndOfLineValue.lf), validator,
                        PropertyType.end_of_line.getName(), "crlf") //
        );
    }

    @Test
    public void end_of_line_lf() throws IOException, MojoExecutionException {
        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .build();
        String text = "line 1\n" + //
                "line 2 \n" + //
                "line 3\t\n" + //
                "line 4\r\n" + //
                "line 5\r" + //
                "line 6 ";
        String expectedText = "line 1\n" + //
                "line 2 \n" + //
                "line 3\t\n" + //
                "line 4\n" + //
                "line 5\n" + //
                "line 6 ";
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".txt");

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(4, 7), new Delete(1), validator, PropertyType.end_of_line.getName(),
                        "lf"), //
                new Violation(doc, new Location(5, 7),
                        Replace.endOfLine(PropertyType.EndOfLineValue.cr, PropertyType.EndOfLineValue.lf), validator,
                        PropertyType.end_of_line.getName(), "lf"));
    }

    @Test
    public void insert_final_newline() throws IOException, MojoExecutionException {
        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.insert_final_newline).value("true").build()) //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .build();
        String text = "line 1\n" + //
                "line 2"//
        ;
        String expectedText = "line 1\n" + //
                "line 2\n" //
        ;
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".txt");

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(2, 7), Insert.endOfLine(EndOfLineValue.lf), validator,
                        PropertyType.insert_final_newline.getName(), "true"));
    }

    @Test
    public void insert_final_newline_empty() throws IOException, MojoExecutionException {
        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.insert_final_newline).value("true").build()) //
                .property(new Property.Builder(null).type(PropertyType.end_of_line).value("lf").build()) //
                .build();
        String text = "";
        String expectedText = "\n";
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".txt");

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(1, 1), Insert.endOfLine(EndOfLineValue.lf), validator,
                        PropertyType.insert_final_newline.getName(), "true"));
    }

    @Test
    public void trim_trailing_whitespace() throws IOException, MojoExecutionException {
        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();
        String text = "line 1\n" + //
                "line 2 \n" + //
                "line 3\t\n" + //
                "line 4\r\n" + //
                "line 5\r" + //
                "line 6 ";
        String expectedText = "line 1\n" + //
                "line 2\n" + //
                "line 3\n" + //
                "line 4\r\n" + //
                "line 5\r" + //
                "line 6";
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".txt");

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(2, 7), new Delete(1), validator,
                        PropertyType.trim_trailing_whitespace.getName(), "true"), //
                new Violation(doc, new Location(3, 7), new Delete(1), validator,
                        PropertyType.trim_trailing_whitespace.getName(), "true"), //
                new Violation(doc, new Location(6, 7), new Delete(1), validator,
                        PropertyType.trim_trailing_whitespace.getName(), "true"));

    }

}
