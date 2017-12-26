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
 * A {@link Reader} source.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Resource {
    protected final Charset encoding;

    protected final Path absPath;
    protected final Path relPath;

    public Resource(Path absPath, Path relPath, Charset encoding) {
        super();
        this.absPath = absPath;
        this.relPath = relPath;
        this.encoding = encoding;
    }

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

    public Path getPath() {
        return absPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((absPath == null) ? 0 : absPath.hashCode());
        return result;
    }

    public Reader openReader() throws IOException {
        return Files.newBufferedReader(absPath, encoding);
    }

    @Override
    public String toString() {
        return relPath.toString();
    }

}
