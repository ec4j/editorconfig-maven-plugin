package org.l2x6.editorconfig.validator.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ec4j.core.ResourceProperties;
import org.l2x6.editorconfig.core.PathSet;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.Validator;
import org.l2x6.editorconfig.core.ViolationHandler;
import org.l2x6.editorconfig.parser.xml.XmlLexer;
import org.l2x6.editorconfig.parser.xml.XmlParser;

public class XmlValidator implements Validator {
    private static final List<String> DEFAULT_INCLUDES = Collections.unmodifiableList(Arrays.asList("**/*.xml", "**/*.xsl"));
    private static final List<String> DEFAULT_EXCLUDES = Collections.emptyList();

    @Override
    public List<String> getDefaultIncludes() {
        return DEFAULT_INCLUDES;
    }

    @Override
    public List<String> getDefaultExcludes() {
        return DEFAULT_EXCLUDES;
    }

    @Override
    public void process(Resource resource, ResourceProperties properties, ViolationHandler violationHandler) throws IOException {
        try (Reader in = resource.openReader()) {
            XmlParser parser = new XmlParser(
                    new CommonTokenStream(new XmlLexer(CharStreams.fromReader(in, resource.toString()))));

            ParseTree rootContext = parser.document();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new FormatParserListener(resource, properties, violationHandler), rootContext);
        }
    }

}
