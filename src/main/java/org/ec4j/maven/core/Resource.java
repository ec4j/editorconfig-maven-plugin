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
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A readable resource. Consists of a file {@link Path} and a {@link Charset}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Resource {
    protected final Path absPath;

    protected final Charset encoding;
    protected final Path relPath;

    /**
     * @param absPath
     *            the absolute path to the underlying file
     * @param relPath
     *            the path to the underlying file relative to the current projects root directory (used for reporting
     *            only)
     * @param encoding
     *            the {@link Charset} to use when reading from the underlying file
     */
    public Resource(Path absPath, Path relPath, Charset encoding) {
        super();
        this.absPath = absPath;
        this.relPath = relPath;
        this.encoding = encoding;
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
        Resource other = (Resource) obj;
        if (encoding == null) {
            if (other.encoding != null)
                return false;
        } else if (!encoding.equals(other.encoding))
            return false;
        if (absPath == null) {
            if (other.absPath != null)
                return false;
        } else if (!absPath.equals(other.absPath))
            return false;
        return true;
    }

    /**
     * @return the absolute {@link Path} to the underlying file
     */
    public Path getPath() {
        return absPath;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((absPath == null) ? 0 : absPath.hashCode());
        return result;
    }

    /**
     * @return a new Reader from the underlying file
     * @throws IOException
     *             on I/O problems
     */
    public Reader openReader() throws IOException {
        return Files.newBufferedReader(absPath, encoding);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return relPath.toString();
    }

}
