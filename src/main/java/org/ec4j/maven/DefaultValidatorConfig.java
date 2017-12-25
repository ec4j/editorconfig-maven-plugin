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
package org.ec4j.maven;

public class DefaultValidatorConfig implements ValidatorConfig {
    private String className;
    private String[] excludes;
    private String[] includes;
    private boolean useDefaultIncludesAndExcludes = true;

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }

    @Override
    public String[] getIncludes() {
        return includes;
    }

    @Override
    public boolean isUseDefaultIncludesAndExcludes() {
        return useDefaultIncludesAndExcludes;
    }

    public void setClassName(String class_) {
        this.className = class_;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public void setUseDefaultIncludesAndExcludes(boolean useDefaultIncludesAndExcludes) {
        this.useDefaultIncludesAndExcludes = useDefaultIncludesAndExcludes;
    }
}
