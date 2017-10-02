package org.l2x6.editorconfig.format;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.l2x6.editorconfig.check.ViolationCollector;
import org.l2x6.editorconfig.core.EditorConfigService.OptionMap;
import org.l2x6.editorconfig.core.Location;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.Violation;
import org.l2x6.editorconfig.validator.xml.XmlValidator;

public class XmlParserTest
{
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
        EditableDocument doc = doc( text );

        assertParse( doc, expectedText, //
                     new Violation( doc, new Location( 5, 5 ), new Delete(1) ), //
                     new Violation( doc, new Location( 6, 3 ), new Delete(2) )
                        );
    }

    private static EditableDocument doc(String text) throws IOException {
        Path file = File.createTempFile( XmlParserTest.class.getSimpleName(), ".xml" ).toPath();
        EditableDocument doc = new EditableDocument( file , StandardCharsets.UTF_8, text );
        return doc;
    }

    private static void assertParse(EditableDocument doc, String expectedText, Violation... expected) throws IOException, MojoExecutionException
    {

        final XmlValidator validator = new XmlValidator();
        final OptionMap options = OptionMap.builder().indetSize(2).indetSpace().trimTrailingSpace().build();

        ViolationCollector collector = new ViolationCollector( false );
        collector.startFileSets();
        collector.startFile( doc );
        validator.process(doc, options , collector);
        collector.endFile();
        collector.endFileSets();

        Map<Resource, List<Violation>> violations = collector.getViolations();
        List<Violation> actual = violations.get( doc );

        Assert.assertEquals( expected.length, actual.size() );


        for ( int i = 0; i < expected.length; i++ )
        {
            Violation violation = actual.get( i );
            Assert.assertEquals( expected[i], violation );
        }

        FormattingHandler formatter = new FormattingHandler( false, false );
        formatter.startFileSets();
        formatter.startFile( doc );
        validator.process(doc, options, formatter);
        formatter.endFile();
        formatter.endFileSets();

        Assert.assertEquals( expectedText, doc.asString() );

    }

}
