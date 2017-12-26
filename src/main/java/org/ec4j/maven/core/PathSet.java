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

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathSet {

    public static class Builder {

        private static final FileSystem fileSystem = FileSystems.getDefault();
        private List<PathMatcher> excludes = new ArrayList<>();
        private List<PathMatcher> includes = new ArrayList<>();

        Builder() {
            super();
        }

        public PathSet build() {
            List<PathMatcher> useExcludes = this.excludes;
            this.excludes = null;
            List<PathMatcher> useIncludes = this.includes;
            this.includes = null;
            return new PathSet(Collections.unmodifiableList(useIncludes), Collections.unmodifiableList(useExcludes));
        }

        public Builder exclude(String glob) {
            excludes.add(fileSystem.getPathMatcher("glob:" + glob));
            return this;
        }

        public Builder excludes(List<String> globs) {
            if (globs != null) {
                for (String glob : globs) {
                    excludes.add(fileSystem.getPathMatcher("glob:" + glob));
                }
            }
            return this;
        }

        public Builder excludes(String... globs) {
            if (globs != null) {
                for (String glob : globs) {
                    excludes.add(fileSystem.getPathMatcher("glob:" + glob));
                }
            }
            return this;
        }

        public Builder include(String glob) {
            includes.add(fileSystem.getPathMatcher("glob:" + glob));
            return this;
        }

        public Builder includes(List<String> globs) {
            for (String glob : globs) {
                includes.add(fileSystem.getPathMatcher("glob:" + glob));
            }
            return this;
        }

        public Builder includes(String... globs) {
            if (globs != null) {
                for (String glob : globs) {
                    includes.add(fileSystem.getPathMatcher("glob:" + glob));
                }
            }
            return this;
        }

    }

    private static final Path CURRENT_DIR = Paths.get(".");

    public static Builder builder() {
        return new Builder();
    }
    public static PathSet ofIncludes(String... includes) {
        return builder().includes(includes).build();
    }

    private final List<PathMatcher> excludes;

    private final List<PathMatcher> includes;

    PathSet(List<PathMatcher> includes, List<PathMatcher> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    public boolean contains(Path path) {
        path = CURRENT_DIR.resolve(path);
        for (PathMatcher exclude : excludes) {
            if (exclude.matches(path)) {
                return false;
            }
        }
        for (PathMatcher include : includes) {
            if (include.matches(path)) {
                return true;
            }
        }
        return false;
    }
}
