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
package org.ec4j.maven.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ViolationHandler} that performs the {@link Edit} operations on the files for which they were reported.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class FormattingHandler implements ViolationHandler {

    private static final Logger log = LoggerFactory.getLogger(FormattingHandler.class);
    private final boolean backup;

    private final boolean backupSuffix;
    private EditableResource currentFile;
    private int editedFileCount = 0;
    private int processedFileCount = 0;
    private List<Violation> violations = new ArrayList<Violation>();

    public FormattingHandler(boolean backup, boolean backupSuffix) {
        super();
        this.backup = backup;
        this.backupSuffix = backupSuffix;
    }

    /**
     * @throws IOException
     */
    private void backupAndStoreIfNeeded() throws IOException {
        if (currentFile.changed()) {
            if (backup) {
                final Path originalFile = currentFile.getPath();
                final Path backupFile = Paths.get(originalFile.toString() + backupSuffix);
                Files.move(originalFile, backupFile);
            }
            currentFile.store();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ReturnState endFile() {
        try {
            if (violations.isEmpty()) {
                log.debug("No formatting violations found in file '{}' ", currentFile);
                backupAndStoreIfNeeded();
                return ReturnState.FINISHED;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Fixing {} formatting {}  in file '{}'", violations.size(),
                            (violations.size() == 1 ? "violation" : "violations"), currentFile);
                }
                editedFileCount++;

                /*
                 * We want to allow only one edit per line to avoid edit conflicts We should actually check that the
                 * edits do not span over multiple lines, which we do not ATM
                 */
                Set<Integer> linesEdited = new HashSet<>();
                boolean recheckNeeded = false;
                for (Violation violation : violations) {
                    Location loc = violation.getLocation();
                    final Integer line = Integer.valueOf(loc.getLine());
                    if (!linesEdited.contains(line)) {
                        int lineStartOffset = currentFile.findLineStart(loc.getLine());
                        int editOffset = lineStartOffset + loc.getColumn() - 1;
                        final Edit fix = violation.getFix();
                        log.debug("About to perform '{}' at {}, lineStartOffset {}, editOffset {}", fix.getMessage(),
                                loc, lineStartOffset, editOffset);
                        fix.perform(currentFile, editOffset);
                        linesEdited.add(line);
                    } else {
                        recheckNeeded = true;
                    }
                }
                if (recheckNeeded) {
                    return ReturnState.RECHECK;
                } else {
                    backupAndStoreIfNeeded();
                    return ReturnState.FINISHED;
                }
            }
        } catch (IOException e) {
            throw new FormatException("Could not format file " + currentFile, e);
        } finally {
            processedFileCount++;
            this.currentFile = null;
            this.violations.clear();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endFiles() {
        log.info("Formatted {} out of {} {}", editedFileCount, processedFileCount,
                (editedFileCount == 1 ? "file" : "files"));
    }

    /** {@inheritDoc} */
    @Override
    public void handle(Violation violation) {
        log.info(violation.toString());
        violations.add(violation);
    }

    /**
     * @return
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void startFile(Resource file) {
        this.currentFile = (EditableResource) file;
    }

    /** {@inheritDoc} */
    @Override
    public void startFiles() {
        processedFileCount = 0;
    }

}