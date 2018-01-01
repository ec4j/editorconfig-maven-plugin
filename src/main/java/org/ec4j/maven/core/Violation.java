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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A violation of some {@code .editorconfig} properties found at {@link #location} in a {@link #resource}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Violation {
    private final Edit fix;

    private final Location location;
    private final Resource resource;
    private String toString;
    private final Validator validator;
    private final List<String> violatedProperties;

    Violation(Resource resource, Location location, Edit fix, Validator validator, String... violatedProperties) {
        this.resource = resource;
        this.location = location;
        this.fix = fix;
        this.validator = validator;
        this.violatedProperties = Collections.unmodifiableList(Arrays.asList(violatedProperties));
    }

    public Violation(Resource resource, Location location, Edit fix, Validator validator, String violatedKey0,
            String violatedValue0) {
        this(resource, location, fix, validator, new String[] { violatedKey0, violatedValue0 });
    }

    public Violation(Resource resource, Location location, Edit fix, Validator validator, String violatedKey0,
            String violatedValue0, String violatedKey1, String violatedValue1) {
        this(resource, location, fix, validator,
                new String[] { violatedKey0, violatedValue0, violatedKey1, violatedValue1 });
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
        if (resource == null) {
            if (other.resource != null)
                return false;
        } else if (!resource.equals(other.resource))
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
        if (validator == null) {
            if (other.validator != null)
                return false;
        } else if (!validator.equals(other.validator))
            return false;
        if (violatedProperties == null) {
            if (other.violatedProperties != null)
                return false;
        } else if (!violatedProperties.equals(other.violatedProperties))
            return false;
        return true;
    }

    /**
     * @return an {@link Edit} operation able to fix this {@link Violation}
     */
    public Edit getFix() {
        return fix;
    }

    /**
     * @return the location where the violation was detected. The first column number is 1
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return the resource in which the violation was detected.
     */
    public Resource getResource() {
        return resource;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        result = prime * result + ((fix == null) ? 0 : fix.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((validator == null) ? 0 : validator.hashCode());
        result = prime * result + ((violatedProperties == null) ? 0 : violatedProperties.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (toString == null) {
            StringBuilder result = new StringBuilder() //
                    .append(resource) //
                    .append("@") //
                    .append(location) //
                    .append(": ") //
                    .append(fix.getMessage()) //
                    .append(" - violates ") //
            ;
            Iterator<String> it = violatedProperties.iterator();
            while (it.hasNext()) {
                result.append(it.next());
                if (it.hasNext()) {
                    result.append(" = ").append(it.next());
                }
                result.append(", ");
            }
            toString = result.append("reported by ").append(validator.getClass().getName()).toString();
        }
        return toString;
    }
}