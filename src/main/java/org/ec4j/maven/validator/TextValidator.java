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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ec4j.core.ResourceProperties;
import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.EndOfLineValue;
import org.ec4j.maven.core.Delete;
import org.ec4j.maven.core.Edit;
import org.ec4j.maven.core.Insert;
import org.ec4j.maven.core.LineReader;
import org.ec4j.maven.core.Location;
import org.ec4j.maven.core.Replace;
import org.ec4j.maven.core.Resource;
import org.ec4j.maven.core.Validator;
import org.ec4j.maven.core.Violation;
import org.ec4j.maven.core.ViolationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 * @since 0.0.1
 */
public class TextValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(TextValidator.class);

    private static final List<String> DEFAULT_EXCLUDES = Collections.emptyList();

    private static final List<String> DEFAULT_INCLUDES = Collections.unmodifiableList(Arrays.asList("**/*"));
    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("[ \t]+$", Pattern.MULTILINE);

    static String findEolString(String line) {
        if (line.isEmpty()) {
            return "";
        } else {
            int start = line.length();
            while (start > 0) {
                char ch = line.charAt(start - 1);
                switch (ch) {
                case '\n':
                case '\r':
                    start--;
                    break;
                default:
                    return line.substring(start);
                }
            }
            return line.substring(start);
        }
    }

    static String escape(String line, EndOfLineValue eol) {
        if (eol == null) {
            return line;
        }
        final String escapedEol;
        switch (eol) {
        case lf:
            escapedEol = "\\n";
            break;
        case crlf:
            escapedEol = "\\r\\n";
            break;
        case cr:
            escapedEol = "\\r";
            break;
        default:
            throw new IllegalStateException("Unexpected " + EndOfLineValue.class.getName() + " '" + eol + "'");
        }
        return line.substring(0, line.length() - eol.getEndOfLineString().length()) + escapedEol;
    }

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
        PropertyType.EndOfLineValue eol = properties.getValue(PropertyType.end_of_line, null, true);
        final Boolean trimTrailingWsBox = properties.getValue(PropertyType.trim_trailing_whitespace, Boolean.FALSE,
                true);
        final boolean trimTrailingWs = trimTrailingWsBox != null && trimTrailingWsBox.booleanValue();
        final boolean insertFinalNewline = properties.getValue(PropertyType.insert_final_newline, Boolean.FALSE, false)
                .booleanValue();
        if (log.isTraceEnabled()) {
            log.trace("Checking end_of_line value '{}' in {}", eol, resource);
            log.trace("Checking trim_trailing_whitespace value '{}' in {}", trimTrailingWsBox, resource);
            log.trace("Checking insert_final_newline value '{}' in {}", insertFinalNewline, resource);
        }
        try (LineReader in = LineReader.of(resource.openReader())) {
            String line = null;
            String lastActualEol = null;
            String lastLine = null;
            int lineNumber = 1;
            while ((line = in.readLine()) != null) {
                if (log.isTraceEnabled()) {
                    final String actualEol = findEolString(line);
                    log.trace("Processing line '{}'",
                            escape(line, actualEol == null ? null : EndOfLineValue.ofEndOfLineString(actualEol)));
                }
                lastLine = line;
                if (trimTrailingWs) {
                    final Matcher m = TRAILING_WHITESPACE_PATTERN.matcher(line);
                    if (m.find()) {
                        int start = m.start();
                        final Violation violation = new Violation(resource, new Location(lineNumber, start + 1),
                                new Delete(m.end() - start), this, PropertyType.trim_trailing_whitespace.getName(),
                                "true");
                        violationHandler.handle(violation);
                    }
                }
                if (eol != null) {
                    lastActualEol = findEolString(line);
                    final String eolString = eol.getEndOfLineString();
                    if (!eolString.equals(lastActualEol)) {
                        final int actualEolLength = lastActualEol.length();
                        final int eolLength = eolString.length();
                        if (actualEolLength == 0) {
                            /*
                             * This can only be the last line which is no violation of end_of_line itself. Note that we
                             * handle insert_final_newline later in this method
                             */
                        } else {
                            final Edit fix;
                            final int column;
                            if (actualEolLength == eolLength) {
                                /* replace */
                                column = line.length();
                                fix = Replace.endOfLine(PropertyType.EndOfLineValue.ofEndOfLineString(lastActualEol),
                                        eol);
                            } else if (actualEolLength < eolLength) {
                                /* insert */
                                switch (lastActualEol.charAt(0)) {
                                case '\r':
                                    column = line.length() + 1;
                                    fix = Insert.endOfLine(PropertyType.EndOfLineValue.lf);
                                    break;
                                case '\n':
                                    column = line.length();
                                    fix = Insert.endOfLine(PropertyType.EndOfLineValue.cr);
                                    break;
                                default:
                                    throw new IllegalStateException();
                                }
                            } else {
                                /* actualEolLength > eolLength */
                                fix = new Delete(1);
                                switch (eol) {
                                case cr:
                                    column = line.length();
                                    break;
                                case lf:
                                    column = line.length() - 1;
                                    break;
                                default:
                                    throw new IllegalStateException();
                                }
                            }
                            final Violation violation = new Violation(resource, new Location(lineNumber, column), fix,
                                    this, PropertyType.end_of_line.getName(), eol.name());
                            violationHandler.handle(violation);
                        }
                    }
                }
                lineNumber++;
            }
            if (insertFinalNewline) {
                final int col;
                if (lastLine == null) {
                    /* an empty document */
                    lastActualEol = "";
                    lineNumber = 1;
                    col = 1;
                } else {
                    if (lastActualEol == null) {
                        /* probably because eol is null */
                        lastActualEol = findEolString(lastLine);
                    }
                    lineNumber--;
                    col = lastLine.length() + 1;
                }
                if (lastActualEol.isEmpty()) {
                    /* we need to insert an EOL */
                    if (eol == null) {
                        // https://github.com/editorconfig/editorconfig/issues/335
                    } else {
                        /* eol != null */
                        final Violation insertFinalNewlineViolation = new Violation(resource,
                                new Location(lineNumber, col), Insert.endOfLine(eol), this,
                                PropertyType.insert_final_newline.getName(), "true");
                        violationHandler.handle(insertFinalNewlineViolation);
                    }
                }
            }
        }
    }

}
