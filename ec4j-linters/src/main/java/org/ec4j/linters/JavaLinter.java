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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.IndentStyleValue;
import org.ec4j.lint.api.Delete;
import org.ec4j.lint.api.Edit;
import org.ec4j.lint.api.Insert;
import org.ec4j.lint.api.LintUtils;
import org.ec4j.lint.api.Linter;
import org.ec4j.lint.api.Location;
import org.ec4j.lint.api.Logger;
import org.ec4j.lint.api.Replace;
import org.ec4j.lint.api.Resource;
import org.ec4j.lint.api.Violation;
import org.ec4j.lint.api.ViolationHandler;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.text.edits.CopySourceEdit;
import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.CopyingRangeMarker;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;
import org.eclipse.text.edits.UndoEdit;

/**
 * A {@link Linter} for Java source files, based in Eclipse JDT Formatter. Because Eclipse JDT Formatter cannot be
 * configured to skip certain checks, we have to either accept the defaults set by JDT or define our own defaults for
 * situations when certain {@code .editorconfig} properties are not defined.
 *
 * <p>
 * Supports the following {@code .editorconfig} properties:
 * <ul>
 * <li>{@code indent_style}, default: {@code space}</li>
 * <li>{@code indent_size}, default: {@code 4}</li>
 * </ul>
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 * @since 0.0.9
 *
 */
public class JavaLinter implements Linter {
    private static final Integer DEFAULT_JAVA_INDENT_SIZE = Integer.valueOf(4);

    static class EditVisitor extends TextEditVisitor {
        static class ReplaceEditInfo {
            final int length;
            final int offset;
            final char[] replacement;

            ReplaceEditInfo(int offset, int length, char[] replacement) {
                super();
                this.offset = offset;
                this.length = length;
                this.replacement = replacement;
            }
        }

        /**
         * For some reason the {@link ReplaceEdit}s delivered by the JDT formatter are non-minimal, meaning that
         * replaced and replacement may have common suffix or prefix or both. This methods removes any common prefix or
         * suffix from the given {@code edit} and adjusts the offset and length retuning a new {@link ReplaceEditInfo}.
         *
         * @param edit the {@link ReplaceEdit} to minimize.
         * @param source the source the given {@code edit} applies to
         * @return a new {@link ReplaceEditInfo}
         */
        static ReplaceEditInfo minimize(ReplaceEdit edit, String source) {
            int cutLeading = 0;
            char[] replacement = edit.getText().toCharArray();
            for (; cutLeading < replacement.length; cutLeading++) {
                final int sourceIndex = edit.getOffset() + cutLeading;
                if (source.charAt(sourceIndex) != replacement[cutLeading]) {
                    break;
                }
            }
            int cutTrailing = replacement.length - 1;
            for (; cutTrailing >= cutLeading; cutTrailing--) {
                final int sourceIndex = edit.getOffset() + cutTrailing;
                if (source.charAt(sourceIndex) != replacement[cutTrailing]) {
                    break;
                }
            }
            cutTrailing++;
            if (cutLeading == 0 && cutTrailing == replacement.length) {
                /* no change */
                return new ReplaceEditInfo(edit.getOffset(), edit.getLength(), replacement);
            } else {
                final int newReplacementLength = cutTrailing - cutLeading;
                final char[] newReplacement = new char[newReplacementLength];
                System.arraycopy(replacement, cutLeading, newReplacement, 0, newReplacementLength);
                return new ReplaceEditInfo(edit.getOffset() + cutLeading,
                        edit.getLength() - (replacement.length - newReplacementLength), newReplacement);
            }
        }

        private final Linter linter;
        private final Logger log;

        private final Resource resource;

        private final String source;

        private final ViolationHandler violationHandler;

        EditVisitor(Logger log, ViolationHandler violationHandler, String source, Resource resource, Linter linter) {
            super();
            this.log = log;
            this.violationHandler = violationHandler;
            this.source = source;
            this.resource = resource;
            this.linter = linter;
        }

        private int hasAllCharsSame(char[] text) {
            final char first = text[0];
            for (int i = 1; i < text.length; i++) {
                if (text[i] != first) {
                    return -1;
                }
            }
            return first;
        }

        @Override
        public boolean visit(CopyingRangeMarker edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(CopySourceEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(CopyTargetEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(DeleteEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(InsertEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(MoveSourceEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(MoveTargetEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(MultiTextEdit edit) {
            return true;
        }

        @Override
        public boolean visit(RangeMarker edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visit(ReplaceEdit edit) {
            final ReplaceEditInfo minEdit = minimize(edit, source);

            final char[] replacement = minEdit.replacement;
            final Edit ec4jEdit;
            if (minEdit.length == 0) {
                /* Actually an insertion */
                final int ch = hasAllCharsSame(replacement);
                switch (ch) {
                case ' ':
                case '\t':
                    ec4jEdit = Insert.repeat((char) ch, replacement.length);
                    break;
                default:
                    final StringBuilder msg = new StringBuilder(11 + replacement.length);
                    msg.append("Insert '");
                    LintUtils.escape(msg, replacement);
                    msg.append("'");
                    ec4jEdit = new Insert(new String(replacement), msg.toString());
                    break;
                }
            } else if (replacement.length == 0) {
                /* Deletion */
                ec4jEdit = new Delete(minEdit.length);
            } else {
                final char[] replaced = new char[minEdit.length];
                source.getChars(minEdit.offset, minEdit.offset + minEdit.length, replaced, 0);
                ec4jEdit = Replace.ofReplaced(replaced, replacement);
            }

            Location location = resource.findLocation(minEdit.offset);
            violationHandler.handle(new Violation(resource, location, ec4jEdit, linter, "java/<unknown>"));
            return true;
        }

        @Override
        public boolean visit(UndoEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

        @Override
        public boolean visitNode(TextEdit edit) {
            throw new UnsupportedOperationException(
                    String.format("Cannot handle %s: %s", edit.getClass().getName(), edit));
        }

    }

    private static final List<String> DEFAULT_EXCLUDES = Collections.emptyList();

    private static final List<String> DEFAULT_INCLUDES = Collections.unmodifiableList(Arrays.asList("**/*.java"));

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
        processInternal(resource, properties, violationHandler);
    }

    /**
     * Returns {@link TextEdit} for testing purposes.
     *
     * @param resource
     * @param properties
     * @param violationHandler
     * @return
     * @throws IOException
     */
    TextEdit processInternal(Resource resource, ResourceProperties properties, ViolationHandler violationHandler)
            throws IOException {

        final Logger log = violationHandler.getLogger();
        final String source = resource.getText();
        Map<String, String> options = toJdtFormatterOptions(properties, resource, log);
        DefaultCodeFormatter formatter = new DefaultCodeFormatter(options);
        final int kind = (resource.getPath().getFileName().toString().equals(IModule.MODULE_INFO_JAVA)
                ? CodeFormatter.K_MODULE_INFO
                : CodeFormatter.K_COMPILATION_UNIT) | CodeFormatter.F_INCLUDE_COMMENTS;
        final PropertyType.EndOfLineValue eol = properties.getValue(PropertyType.end_of_line, null, true);
        final TextEdit result = formatter.format(kind, source, 0, source.length(), 0, eol.getEndOfLineString());
        result.accept(new EditVisitor(log, violationHandler, source, resource, this));
        return result;
    }

    private Map<String, String> toJdtFormatterOptions(ResourceProperties properties, Resource resource, Logger log) {
        Map<String, String> result = new TreeMap<>();
        final IndentStyleValue indentStyle = properties.getValue(PropertyType.indent_style, IndentStyleValue.space,
                false);
        switch (indentStyle) {
        case tab:
        case space:
            result.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, indentStyle.name());
            break;
        default:
            throw new IllegalStateException(
                    String.format("Unexpected %s: [%s]", IndentStyleValue.class.getName(), indentStyle));
        }

        final Integer indentSize = properties.getValue(PropertyType.indent_size, DEFAULT_JAVA_INDENT_SIZE, false);
        result.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, indentSize.toString());

        final Integer maxLineLength = properties.getValue(PropertyType.max_line_length, Integer.MAX_VALUE, true);
        result.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, maxLineLength.toString());

        return result;
    }

}
