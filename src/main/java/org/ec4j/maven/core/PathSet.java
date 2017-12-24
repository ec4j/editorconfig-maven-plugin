package org.ec4j.maven.core;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;

public class PathSet {

    public static class Builder {

        private static final FileSystem fileSystem = FileSystems.getDefault();
        private List<PathMatcher> includes = new ArrayList<>();
        private List<PathMatcher> excludes = new ArrayList<>();

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
            excludes.add(fileSystem.getPathMatcher("glob:"+ glob));
            return this;
        }

        public Builder excludes(String... globs) {
            if (globs != null) {
                for (String glob : globs) {
                    excludes.add(fileSystem.getPathMatcher("glob:"+ glob));
                }
            }
            return this;
        }

        public Builder excludes(List<String> globs) {
            if (globs != null) {
                for (String glob : globs) {
                    excludes.add(fileSystem.getPathMatcher("glob:"+ glob));
                }
            }
            return this;
        }

        public Builder include(String glob) {
            includes.add(fileSystem.getPathMatcher("glob:"+ glob));
            return this;
        }

        public Builder includes(String... globs) {
            if (globs != null) {
                for (String glob : globs) {
                    includes.add(fileSystem.getPathMatcher("glob:"+ glob));
                }
            }
            return this;
        }

        public Builder includes(List<String> globs) {
            for (String glob : globs) {
                includes.add(fileSystem.getPathMatcher("glob:"+ glob));
            }
            return this;
        }

    }

    private static final Path CURRENT_DIR = Paths.get(".");

    private final List<PathMatcher> includes;
    private final List<PathMatcher> excludes;

    public static Builder builder() {
        return new Builder();
    }

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

    public static PathSet ofIncludes(String... includes) {
        return builder().includes(includes).build();
    }
}
