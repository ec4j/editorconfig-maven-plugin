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
import java.util.List;

import org.ec4j.core.ResourceProperties;

public interface Validator {

    List<String> getDefaultExcludes();

    List<String> getDefaultIncludes();

    /**
     * Checks the formatting (as defined by {@code properties}) of the given {@code resource} and reports the violations
     * to the given {@code violationHandler}.
     *
     * @param resource the {@link Resource} to process
     * @param properties a set of {@code .editorconfig} properties
     * @param violationHandler the {@link ViolationHandler} to report to
     * @throws IOException on I/O problems
     */
    void process(Resource resource, ResourceProperties properties, ViolationHandler violationHandler)
            throws IOException;
}
