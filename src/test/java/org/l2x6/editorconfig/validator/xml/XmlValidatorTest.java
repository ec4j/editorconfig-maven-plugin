package org.l2x6.editorconfig.validator.xml;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.PropertyType;
import org.junit.Test;
import org.l2x6.editorconfig.core.Location;
import org.l2x6.editorconfig.core.Validator;
import org.l2x6.editorconfig.core.ValidatorTestUtils;
import org.l2x6.editorconfig.core.Violation;
import org.l2x6.editorconfig.format.Delete;
import org.l2x6.editorconfig.format.EditableDocument;

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
        EditableDocument doc = ValidatorTestUtils.createDocument(text, ".xml");

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
