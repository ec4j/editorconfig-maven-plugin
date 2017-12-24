package org.ec4j.maven.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.Property.Builder;
import org.ec4j.maven.check.ViolationCollector;
import org.ec4j.maven.core.Resource;
import org.ec4j.maven.core.Validator;
import org.ec4j.maven.core.Violation;
import org.ec4j.maven.format.EditableDocument;
import org.ec4j.maven.format.FormattingHandler;
import org.ec4j.maven.validator.XmlValidator;
import org.ec4j.maven.validator.XmlValidatorTest;
import org.ec4j.core.model.PropertyType;
import org.junit.Assert;

public class ValidatorTestUtils {

    public static EditableDocument createDocument(String text, String fileExtension) throws IOException {
        Path file = File.createTempFile(XmlValidatorTest.class.getSimpleName(), fileExtension).toPath();
        EditableDocument doc = new EditableDocument(file, StandardCharsets.UTF_8, text);
        return doc;
    }

    public static void assertParse(Validator validator, EditableDocument doc, String expectedText, ResourceProperties props, Violation... expected)
            throws IOException, MojoExecutionException {

        ViolationCollector collector = new ViolationCollector(false);
        collector.startFiles();
        collector.startFile(doc);
        validator.process(doc, props, collector);
        collector.endFile();
        collector.endFiles();

        Map<Resource, List<Violation>> violations = collector.getViolations();
        List<Violation> actual = violations.get(doc);

        if (expected.length == 0) {
            Assert.assertNull(""+ expected.length +" violations expected, found "+ actual, actual);
        } else {
            Assert.assertNotNull(""+ expected.length +" violations expected, found none", actual);
        }
        Assert.assertEquals(Arrays.asList(expected), actual);

        FormattingHandler formatter = new FormattingHandler(false, false);
        formatter.startFiles();
        formatter.startFile(doc);
        validator.process(doc, props, formatter);
        formatter.endFile();
        formatter.endFiles();

        Assert.assertEquals(expectedText, doc.asString());

    }

}
