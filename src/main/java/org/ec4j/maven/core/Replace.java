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

import org.ec4j.core.model.PropertyType;

/**
 * A replacement operation.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Replace implements Edit {
    public static Replace endOfLine(PropertyType.EndOfLineValue replaced, PropertyType.EndOfLineValue replacement) {
        return new Replace(replaced.getEndOfLineString().length(), replacement.getEndOfLineString(),
                "Replace '" + replaced.name() + "' with '" + replacement.name() + "'");
    }

    /**
     * @param replaced the string to replace
     * @param replacement the replacement
     * @return a new {@link Replace} operation
     */
    public static Replace ofReplaced(String replaced, String replacement) {
        return new Replace(replaced.length(), replacement, "Replace '" + replaced + "' with '" + replacement + "'");
    }

    private final String message;

    private final int replacedLength;

    private final String replacement;

    /**
     * @param replacedLength the length of the span to replace
     * @param replacement the replacement
     * @param message a human readable description of this {@link Replace} operation
     */
    public Replace(int replacedLength, String replacement, String message) {
        super();
        this.replacement = replacement;
        this.message = message;
        this.replacedLength = replacedLength;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Replace replace = (Replace) o;

        if (replacedLength != replace.replacedLength)
            return false;
        if (replacement != null ? !replacement.equals(replace.replacement) : replace.replacement != null)
            return false;
        return message != null ? message.equals(replace.message) : replace.message == null;
    }

    /** {@inheritDoc} */
    @Override
    public void perform(EditableResource document, int offset) {
        document.replace(offset, offset + replacedLength, replacement);
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = replacement != null ? replacement.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + replacedLength;
        return result;
    }
}
