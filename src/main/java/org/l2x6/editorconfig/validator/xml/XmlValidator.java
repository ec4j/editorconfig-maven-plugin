package org.l2x6.editorconfig.validator.xml;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.l2x6.editorconfig.core.EditorConfigService.OptionMap;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.Validator;
import org.l2x6.editorconfig.core.Validators;
import org.l2x6.editorconfig.core.ViolationHandler;
import org.l2x6.editorconfig.parser.xml.XmlLexer;
import org.l2x6.editorconfig.parser.xml.XmlParser;

public class XmlValidator implements Validator {
    private static final List<String> SUPPORTED_EXTENSIONS = Collections
            .unmodifiableList(Arrays.asList(".xml", ".xsl"));

    @Override
    public boolean canParse(Path path) {
        return Validators.hasAnyOfExtensions(path, SUPPORTED_EXTENSIONS);
    }

    @Override
    public void process(Resource resource, OptionMap options, ViolationHandler violationHandler) throws IOException {
        try (Reader in = resource.openReader()) {
            XmlParser parser = new XmlParser(
                    new CommonTokenStream(new XmlLexer(CharStreams.fromReader(in, resource.toString()))));

            ParseTree rootContext = parser.document();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new FormatParserListener(resource, options, violationHandler), rootContext);
        }
    }

}
