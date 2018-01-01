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

/**
 * A set of {@link Path}s defined by include and exclude globs.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class PathSet {

    /**
     * A {@link PathSet} builder.
     */
    public static class Builder {

        private static final FileSystem fileSystem = FileSystems.getDefault();
        private List<PathMatcher> excludes = new ArrayList<>();
        private List<PathMatcher> includes = new ArrayList<>();

        Builder() {
            super();
        }

        /**
         * @return a new {@link PathSet}
         */
        public PathSet build() {
            List<PathMatcher> useExcludes = this.excludes;
            this.excludes = null;
            List<PathMatcher> useIncludes = this.includes;
            this.includes = null;
            return new PathSet(Collections.unmodifiableList(useIncludes), Collections.unmodifiableList(useExcludes));
        }

        /**
         * Adds an exclude glob
         *
         * @param glob
         *            the glob to add
         * @return this {@link Builder}
         */
        public Builder exclude(String glob) {
            excludes.add(fileSystem.getPathMatcher("glob:" + glob));
            return this;
        }

        /**
         * Adds multiple exclude globs
         *
         * @param globs
         *            the globs to add
         * @return this {@link Builder}
         */
        public Builder excludes(List<String> globs) {
            if (globs != null) {
                for (String glob : globs) {
                    excludes.add(fileSystem.getPathMatcher("glob:" + glob));
                }
            }
            return this;
        }

        /**
         * Adds multiple exclude globs
         *
         * @param globs
         *            the globs to add
         * @return this {@link Builder}
         */
        public Builder excludes(String... globs) {
            if (globs != null) {
                for (String glob : globs) {
                    excludes.add(fileSystem.getPathMatcher("glob:" + glob));
                }
            }
            return this;
        }

        /**
         * Adds an include glob
         *
         * @param glob
         *            the glob to add
         * @return this {@link Builder}
         */
        public Builder include(String glob) {
            includes.add(fileSystem.getPathMatcher("glob:" + glob));
            return this;
        }

        /**
         * Adds multiple include globs
         *
         * @param globs
         *            the globs to add
         * @return this {@link Builder}
         */
        public Builder includes(List<String> globs) {
            for (String glob : globs) {
                includes.add(fileSystem.getPathMatcher("glob:" + glob));
            }
            return this;
        }

        /**
         * Adds multiple include globs
         *
         * @param globs
         *            the globs to add
         * @return this {@link Builder}
         */
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

    /**
     * @return new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param includes
     *            the globs to define a new {@link PathSet}
     * @return a {@link PathSet} defined by the given {@code includes}
     */
    public static PathSet ofIncludes(String... includes) {
        return builder().includes(includes).build();
    }

    private final List<PathMatcher> excludes;

    private final List<PathMatcher> includes;

    PathSet(List<PathMatcher> includes, List<PathMatcher> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    /**
     * @param path
     *            the {@link Path} to check
     * @return {@code true} if this {@link PathSet} contains the given {@link Path} or {@code false} otherwise
     */
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
