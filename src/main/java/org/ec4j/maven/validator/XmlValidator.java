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
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.IndentStyleValue;
import org.ec4j.maven.core.Delete;
import org.ec4j.maven.core.Edit;
import org.ec4j.maven.core.Insert;
import org.ec4j.maven.core.Location;
import org.ec4j.maven.core.Resource;
import org.ec4j.maven.core.Validator;
import org.ec4j.maven.core.Violation;
import org.ec4j.maven.core.ViolationHandler;
import org.ec4j.maven.validator.xml.XmlLexer;
import org.ec4j.maven.validator.xml.XmlParser;
import org.ec4j.maven.validator.xml.XmlParser.AttributeContext;
import org.ec4j.maven.validator.xml.XmlParser.ChardataContext;
import org.ec4j.maven.validator.xml.XmlParser.CommentContext;
import org.ec4j.maven.validator.xml.XmlParser.ContentContext;
import org.ec4j.maven.validator.xml.XmlParser.DocumentContext;
import org.ec4j.maven.validator.xml.XmlParser.ElementContext;
import org.ec4j.maven.validator.xml.XmlParser.EndNameContext;
import org.ec4j.maven.validator.xml.XmlParser.MiscContext;
import org.ec4j.maven.validator.xml.XmlParser.ProcessingInstructionContext;
import org.ec4j.maven.validator.xml.XmlParser.PrologContext;
import org.ec4j.maven.validator.xml.XmlParser.ReferenceContext;
import org.ec4j.maven.validator.xml.XmlParser.SeaWsContext;
import org.ec4j.maven.validator.xml.XmlParser.StartEndNameContext;
import org.ec4j.maven.validator.xml.XmlParser.StartNameContext;
import org.ec4j.maven.validator.xml.XmlParser.TextContext;
import org.ec4j.maven.validator.xml.XmlParserListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Validator} specialized for XML files.
 * <p>
 * Supports the following {@code .editorconfig} properties:
 * <ul>
 * <li>{@code indent_style}</li>
 * <li>{@code indent_size}</li>
 * </ul>
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 * @since 0.0.1
 */
public class XmlValidator implements Validator {

    /**
     * An {@link XmlParserListener} implementation that detects formatting violations and reports them to the supplied
     * {@link #violationHandler}.
     *
     */
    static class FormatParserListener implements XmlParserListener {

        /**
         * An entry that can be stored on a stack
         */
        private static class ElementEntry {
            private final String elementName;

            private final FormatParserListener.Indent expectedIndent;

            private final FormatParserListener.Indent foundIndent;

            ElementEntry(String elementName, FormatParserListener.Indent foundIndent) {
                super();
                this.elementName = elementName;
                this.foundIndent = foundIndent;
                this.expectedIndent = foundIndent;
            }

            ElementEntry(String elementName, FormatParserListener.Indent foundIndent,
                    FormatParserListener.Indent expectedIndent) {
                super();
                this.elementName = elementName;
                this.foundIndent = foundIndent;
                this.expectedIndent = expectedIndent;
            }

            @Override
            public String toString() {
                return "<" + elementName + "> " + foundIndent;
            }
        }

        /**
         * An indent occurrence within a file characterized by {@link #lineNumber} and {@link #size}.
         */
        static class Indent {

            /**
             * An {@link Indent} usable at the beginning of a typical XML file.
             */
            public static final FormatParserListener.Indent START = new Indent(1, 0);

            /**
             * The line number where this {@link Indent} occurs. The first line number in a file is {@code 1}.
             */
            private final int lineNumber;

            /** The number of spaces in this {@link Indent}. */
            private final int size;

            Indent(int lineNumber, int size) {
                super();
                this.lineNumber = lineNumber;
                this.size = size;
            }

            @Override
            public String toString() {
                return "Indent [size=" + size + ", lineNumber=" + lineNumber + "]";
            }
        }

        static class LastTerminalFinder extends AbstractParseTreeVisitor<Object> {

            private TerminalNode lastTerminal;

            public TerminalNode getLastTerminal() {
                return lastTerminal;
            }

            @Override
            public Object visitTerminal(TerminalNode node) {

                lastTerminal = node;
                return null;
            }

        }

        private final StringBuilder charBuffer = new StringBuilder();

        private int charLineNumber;

        /** The file being checked */
        private final Resource file;

        private final char indentChar;
        private final int indentSize;

        private final IndentStyleValue indentStyle;

        private FormatParserListener.Indent lastIndent = Indent.START;

        /** The element stack */
        private Deque<FormatParserListener.ElementEntry> stack = new java.util.ArrayDeque<FormatParserListener.ElementEntry>();

        private final Validator validator;

        /** The {@link ViolationHandler} for reporting found violations */
        private final ViolationHandler violationHandler;

        FormatParserListener(Validator validator, Resource file, IndentStyleValue indentStyle, int indetSize,
                ViolationHandler violationHandler) {
            super();
            this.validator = validator;
            this.file = file;
            this.indentStyle = indentStyle;
            this.indentChar = indentStyle.getIndentChar();
            this.indentSize = indetSize;
            this.violationHandler = violationHandler;
        }

        private void consumeText(ParserRuleContext ctx) {
            charBuffer.append(ctx.getText());
            charLineNumber = ctx.getStop().getLine();
        }

        @Override
        public void enterAttribute(AttributeContext ctx) {
        }

        @Override
        public void enterChardata(ChardataContext ctx) {
        }

        @Override
        public void enterComment(CommentContext ctx) {
        }

        @Override
        public void enterContent(ContentContext ctx) {
        }

        @Override
        public void enterDocument(DocumentContext ctx) {

        }

        @Override
        public void enterElement(ElementContext ctx) {
        }

        @Override
        public void enterEndName(EndNameContext ctx) {
            flushWs();
            final String qName = ctx.getText();
            if (stack.isEmpty()) {
                final Token start = ctx.getStart();
                throw new IllegalStateException("Stack must not be empty when closing the element " + qName
                        + " around line " + start.getLine() + " and column " + (start.getCharPositionInLine() + 1));
            }
            ElementEntry startEntry = stack.pop();
            int indentDiff = lastIndent.size - startEntry.expectedIndent.size;
            int expectedIndent = startEntry.expectedIndent.size;
            if (lastIndent.lineNumber != startEntry.foundIndent.lineNumber && indentDiff != 0) {
                /*
                 * diff should be zero unless we are on the same line as start element
                 */
                int opValue = expectedIndent - lastIndent.size;
                final Edit fix;
                final int len = Math.abs(opValue);
                final Token start = ctx.getStart();
                int col = start.getCharPositionInLine() //
                        + 1 // because getCharPositionInLine() is zero based
                        - 2 // because we want the column of '<' while we are on the first char of the name
                ;
                if (opValue > 0) {
                    fix = Insert.repeat(indentChar, len);
                } else {
                    fix = new Delete(len);
                    col -= len;
                }
                final Location loc = new Location(start.getLine(), col);

                Violation violation = new Violation(file, loc, fix, validator, PropertyType.indent_style.getName(),
                        indentStyle.name(), PropertyType.indent_size.getName(), String.valueOf(indentSize));
                violationHandler.handle(violation);
            }
        }

        @Override
        public void enterEveryRule(ParserRuleContext ctx) {
        }

        @Override
        public void enterMisc(MiscContext ctx) {
        }

        @Override
        public void enterProcessingInstruction(ProcessingInstructionContext ctx) {
        }

        @Override
        public void enterProlog(PrologContext ctx) {
            flushWs();
        }

        @Override
        public void enterReference(ReferenceContext ctx) {
        }

        @Override
        public void enterSeaWs(SeaWsContext ctx) {
        }

        @Override
        public void enterStartEndName(StartEndNameContext ctx) {
        }

        @Override
        public void enterStartName(StartNameContext ctx) {
            flushWs();
            final String qName = ctx.getText();
            ElementEntry currentEntry = new ElementEntry(qName, lastIndent);
            if (!stack.isEmpty()) {
                ElementEntry parentEntry = stack.peek();
                /*
                 * note that we use parentEntry.expectedIndent rather than parentEntry.foundIndent this is to make the
                 * messages more useful
                 */
                int indentDiff = currentEntry.foundIndent.size - parentEntry.expectedIndent.size;
                int expectedIndent = parentEntry.expectedIndent.size + indentSize;
                if (indentDiff == 0 && currentEntry.foundIndent.lineNumber == parentEntry.foundIndent.lineNumber) {
                    /*
                     * Zero foundIndent acceptable only if current is on the same line as parent This is OK, therefore
                     * do nothing
                     */
                } else if (indentDiff != indentSize) {
                    /* generally unexpected foundIndent */
                    int opValue = expectedIndent - currentEntry.foundIndent.size;

                    final Edit fix;
                    final int len = Math.abs(opValue);
                    final Token start = ctx.getStart();
                    int col = start.getCharPositionInLine() //
                            + 1 // because getCharPositionInLine() is zero based
                            - 1 // because we want the column of '<' while we are on the first char of the name
                    ;
                    if (opValue > 0) {
                        fix = Insert.repeat(indentChar, len);
                    } else {
                        fix = new Delete(len);
                        col -= len;
                    }
                    final Location loc = new Location(start.getLine(), col);

                    final Violation violation = new Violation(file, loc, fix, validator,
                            PropertyType.indent_style.getName(), indentStyle.name(), PropertyType.indent_size.getName(),
                            String.valueOf(indentSize));
                    violationHandler.handle(violation);

                    /* reset the expected indent in the entry we'll push */
                    currentEntry = new ElementEntry(qName, lastIndent,
                            new Indent(lastIndent.lineNumber, expectedIndent));
                }
            }
            stack.push(currentEntry);
        }

        @Override
        public void enterText(TextContext ctx) {
        }

        @Override
        public void exitAttribute(AttributeContext ctx) {
        }

        @Override
        public void exitChardata(ChardataContext ctx) {
        }

        @Override
        public void exitComment(CommentContext ctx) {
            flushWs();
        }

        @Override
        public void exitContent(ContentContext ctx) {
        }

        @Override
        public void exitDocument(DocumentContext ctx) {
        }

        @Override
        public void exitElement(ElementContext ctx) {
        }

        @Override
        public void exitEndName(EndNameContext ctx) {
        }

        @Override
        public void exitEveryRule(ParserRuleContext ctx) {
        }

        @Override
        public void exitMisc(MiscContext ctx) {
        }

        @Override
        public void exitProcessingInstruction(ProcessingInstructionContext ctx) {
            flushWs();
        }

        @Override
        public void exitProlog(PrologContext ctx) {
            flushWs();
        }

        @Override
        public void exitReference(ReferenceContext ctx) {
        }

        @Override
        public void exitSeaWs(SeaWsContext ctx) {
            consumeText(ctx);
        }

        @Override
        public void exitStartEndName(StartEndNameContext ctx) {
        }

        @Override
        public void exitStartName(StartNameContext ctx) {
        }

        @Override
        public void exitText(TextContext ctx) {
            consumeText(ctx);
        }

        /**
         * Sets {@link lastIndent} based on {@link #charBuffer} and resets {@link #charBuffer}.
         */
        private void flushWs() {
            int indentLength = 0;
            int len = charBuffer.length();
            /*
             * Count characters from end of ignorable whitespace to first end of line we hit
             */
            for (int i = len - 1; i >= 0; i--) {
                char ch = charBuffer.charAt(i);
                switch (ch) {
                case '\n':
                case '\r':
                    lastIndent = new Indent(charLineNumber, indentLength);
                    charBuffer.setLength(0);
                    return;
                case ' ':
                case '\t':
                    indentLength++;
                    break;
                default:
                    /*
                     * No end of line foundIndent in the trailing whitespace. Leave the foundIndent from previous
                     * ignorable whitespace unchanged
                     */
                    charBuffer.setLength(0);
                    return;
                }
            }
        }

        @Override
        public void visitErrorNode(ErrorNode node) {
        }

        @Override
        public void visitTerminal(TerminalNode node) {
        }

    }

    private static final List<String> DEFAULT_EXCLUDES = Collections.emptyList();

    private static final List<String> DEFAULT_INCLUDES = Collections
            .unmodifiableList(Arrays.asList("**/*.xml", "**/*.xsl"));
    private static final Logger log = LoggerFactory.getLogger(XmlValidator.class);

    @Override
    public List<String> getDefaultExcludes() {
        return DEFAULT_EXCLUDES;
    }

    @Override
    public List<String> getDefaultIncludes() {
        return DEFAULT_INCLUDES;
    }

    @Override
    public void process(Resource resource, ResourceProperties properties, ViolationHandler violationHandler)
            throws IOException {

        IndentStyleValue indentStyle = properties.getValue(PropertyType.indent_style, null, false);
        Integer indentSize = properties.getValue(PropertyType.indent_size, null, false);
        if (log.isTraceEnabled()) {
            log.trace("Checking indent_style value '{}' in {}", indentStyle, resource);
            log.trace("Checking indent_size value '{}' in {}", indentSize, resource);
        }
        if (indentStyle == null && indentSize == null) {
            /* nothing to do */
        } else if (indentStyle != null && indentSize != null) {
            try (Reader in = resource.openReader()) {
                XmlParser parser = new XmlParser(
                        new CommonTokenStream(new XmlLexer(CharStreams.fromReader(in, resource.toString()))));

                ParseTree rootContext = parser.document();
                ParseTreeWalker walker = new ParseTreeWalker();
                walker.walk(
                        new FormatParserListener(this, resource, indentStyle, indentSize.intValue(), violationHandler),
                        rootContext);
            }
        } else {
            log.warn(this.getClass().getName() + " expects both indent_style and indent_size to be set for file '{}'",
                    resource);
        }
    }

}
