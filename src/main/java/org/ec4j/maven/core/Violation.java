/**
 * Copyright (c) ${project.inceptionYear} EditorConfig Maven Plugin
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
 * A violation of a prescribed XML formatting.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Violation {
    private final Resource file;

    private final Edit fix;

    private final Location location;

    public Violation(Resource file, Location location, Edit fix) {
        super();
        this.file = file;
        this.location = location;
        this.fix = fix;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Violation other = (Violation) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (fix == null) {
            if (other.fix != null)
                return false;
        } else if (!fix.equals(other.fix))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        return true;
    }

    /**
     * @return the file in which the violation was detected.
     */
    public Resource getFile() {
        return file;
    }

    public Edit getFix() {
        return fix;
    }

    /**
     * @return the location where the violation was detected. The first column number is 1
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((fix == null) ? 0 : fix.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return file + ":" + location + ": " + fix.getMessage();
    }
}