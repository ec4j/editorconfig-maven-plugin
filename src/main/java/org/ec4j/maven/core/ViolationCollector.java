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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ViolationHandler} that just collects the {@link Violation}s reported to it.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ViolationCollector implements ViolationHandler {
    private static final Logger log = LoggerFactory.getLogger(ViolationCollector.class);

    private Resource currentFile;
    private final boolean failOnFormatViolation;
    private int processedFileCount = 0;
    private final Map<Resource, List<Violation>> violations = new LinkedHashMap<Resource, List<Violation>>();

    public ViolationCollector(boolean failOnFormatViolation) {
        super();
        this.failOnFormatViolation = failOnFormatViolation;
    }

    /** {@inheritDoc} */
    @Override
    public ReturnState endFile() {
        if (log.isDebugEnabled() && !hasViolations(currentFile)) {
            log.debug("No formatting violations found in file '{}'", currentFile);
        }
        this.currentFile = null;
        processedFileCount++;
        return ReturnState.FINISHED;
    }

    /**
     *
     */
    /** {@inheritDoc} */
    @Override
    public void endFiles() {
        log.info("Checked {} {}", processedFileCount, (processedFileCount == 1 ? "file" : "files"));
        if (failOnFormatViolation && hasViolations()) {
            throw new FormatException(
                    "There are .editorconfig violations. You may want to run mvn editorconfig:format to fix them automagically.");
        }
    }

    /**
     * @return an unmodifiable {@link Map} from {@link Resource}s to {@link Violation}s reported to this
     *         {@link ViolationCollector} via {@link #handle(Violation)}
     */
    public Map<Resource, List<Violation>> getViolations() {
        return Collections.unmodifiableMap(violations);
    }

    /** {@inheritDoc} */
    @Override
    public void handle(Violation violation) {
        List<Violation> list = violations.get(violation.getResource());
        if (list == null) {
            list = new ArrayList<Violation>();
            violations.put(violation.getResource(), list);
        }
        list.add(violation);
        if (failOnFormatViolation) {
            log.error(violation.toString());
        } else {
            log.warn(violation.toString());
        }
    }

    /**
     * @return true if some violations were reported to this {@link ViolationCollector} via {@link #handle(Violation)}
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    /**
     * @param resource
     *            the resource for which to check whether any violations were reported for it
     * @return {@code true} if violations were reported for the given {@link Path} via {@link #handle(Violation)}
     */
    public boolean hasViolations(Resource resource) {
        List<Violation> list = violations.get(resource);
        return list != null && !list.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void startFile(Resource file) {
        this.currentFile = file;
    }

    /** {@inheritDoc} */
    @Override
    public void startFiles() {
        this.processedFileCount = 0;
    }

}