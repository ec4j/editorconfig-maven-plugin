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
 * An edit operation on a file.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface Edit {

    /**
     * Perform this {@link Edit} operation on the given {@link EditableResource} starting at the given {@code offset}.
     *
     * @param resource
     *            the {@link EditableResource} to edit
     * @param offset
     *            a zero based character index in the given {@code resource} where the edit operation should start
     */
    void perform(EditableResource resource, int offset);

    /**
     * @return a human readable message that describes this {@link Edit} operation
     */
    String getMessage();
}
