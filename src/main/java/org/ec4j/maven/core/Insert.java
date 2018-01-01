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

import java.util.Arrays;

import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.EndOfLineValue;

/**
 * An insertion of a string at some offset in a file.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Insert implements Edit {

    /**
     * An insertion of an end of line
     *
     * @param eol
     *            the {@link EndOfLineValue} to insert
     * @return a new {@link Insert} operation
     */
    public static Insert endOfLine(PropertyType.EndOfLineValue eol) {
        return new Insert(eol.getEndOfLineString(), "Insert " + eol.name());
    }

    /**
     * An insertion of a character {@code ch} {@code count} times.
     *
     * @param ch
     *            the character to insert
     * @param count
     *            the number of times to insert {@code ch}
     * @return a new {@link Insert} operation
     */
    public static Insert repeat(char ch, int count) {
        char[] insertion = new char[count];
        Arrays.fill(insertion, ch);
        final String ofWhat;
        switch (ch) {
        case ' ':
            ofWhat = count == 1 ? "space" : "spaces";
            break;
        case '\t':
            ofWhat = count == 1 ? "tab" : "tabs";
            break;
        default:
            ofWhat = count == 1 ? ("'" + ch + "' character") : ("'" + ch + "' characters");
            break;
        }
        return new Insert(new String(insertion), "Insert " + count + " " + ofWhat);
    }

    private final CharSequence insertion;
    private final String message;

    /**
     * @param insertion
     *            the string to insert
     * @param message
     *            a human readable message that describes this {@link Insert} operation
     */
    public Insert(CharSequence insertion, String message) {
        super();
        this.insertion = insertion;
        this.message = message;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Insert other = (Insert) obj;
        if (insertion == null) {
            if (other.insertion != null)
                return false;
        } else if (!insertion.equals(other.insertion))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void perform(EditableResource document, int offset) {
        document.insert(offset, insertion);
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((insertion == null) ? 0 : insertion.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

}
