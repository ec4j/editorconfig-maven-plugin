/**
 * Copyright (c) ${project.inceptionYear} EditorConfig Maven Plugin
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
import org.ec4j.maven.core.Delete;
import org.ec4j.maven.core.EditableResource;
import org.ec4j.maven.core.Location;
import org.ec4j.maven.core.Validator;
import org.ec4j.maven.core.ValidatorTestUtils;
import org.ec4j.maven.core.Violation;
import org.junit.Test;

public class XmlValidatorTest {
    private final Validator validator = new XmlValidator();

    @Test
    public void simple() throws MojoExecutionException, IOException {
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
        EditableResource doc = ValidatorTestUtils.createDocument(text, ".xml");

        final ResourceProperties props = ResourceProperties.builder() //
                .property(new Property.Builder(null).type(PropertyType.indent_size).value("2").build()) //
                .property(new Property.Builder(null).type(PropertyType.indent_style).value("space").build()) //
                .property(new Property.Builder(null).type(PropertyType.trim_trailing_whitespace).value("true").build()) //
                .build();

        ValidatorTestUtils.assertParse(validator, doc, expectedText, props, //
                new Violation(doc, new Location(5, 5), new Delete(1)), //
                new Violation(doc, new Location(6, 3), new Delete(2)));
    }

}
