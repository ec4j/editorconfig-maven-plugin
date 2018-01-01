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
package org.ec4j.maven;

import org.ec4j.maven.core.Validator;
import org.ec4j.maven.validator.TextValidator;

/**
 * A configuration of a {@link Validator}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ValidatorConfig {

    private String className;
    private boolean enabled = true;
    private String[] excludes;
    private String id;
    private String[] includes;
    private boolean useDefaultIncludesAndExcludes = true;

    public String getClassName() {
        return className.indexOf('.') < 0 ? TextValidator.class.getPackage().getName() + "." + className + "Validator"
                : className;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public String getId() {
        return id == null ? getClassName() : id;
    }

    public String[] getIncludes() {
        return includes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isUseDefaultIncludesAndExcludes() {
        return useDefaultIncludesAndExcludes;
    }

    public void setClassName(String class_) {
        this.className = class_;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public void setUseDefaultIncludesAndExcludes(boolean useDefaultIncludesAndExcludes) {
        this.useDefaultIncludesAndExcludes = useDefaultIncludesAndExcludes;
    }
}
