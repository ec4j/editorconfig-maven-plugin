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

/**
 * An interface for reporting {@link Violation}s.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface ViolationHandler {
    /**
     * A return value of {@link ViolationHandler#endFile()}
     */
    enum ReturnState {
        /** There is nothing more to do for the given {@link Resource} */
        FINISHED,
        /** The given {@link Resource} needs to be re-checked */
        RECHECK;
    }

    /**
     * Called after all {@link Violation}s of a given {@link Resource} were submitted to {@link #handle(Violation)}
     *
     * @return whether the {@link Resource} started recently needs to be re-checked
     */
    ReturnState endFile();

    /**
     * Called when all files in the current project were processed.
     */
    void endFiles();

    /**
     * Called when an {@link Violation} is found.
     *
     * @param violation
     *            the reported violation
     */
    void handle(Violation violation);

    /**
     * @param resource
     */
    void startFile(Resource resource);

    /**
     * Called before the files in the current project are going to get processed.
     */
    void startFiles();
}