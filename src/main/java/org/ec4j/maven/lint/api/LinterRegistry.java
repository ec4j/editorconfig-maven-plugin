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
package org.ec4j.maven.lint.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.ec4j.maven.linters.TextLinter;
import org.ec4j.maven.linters.XmlLinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for {@link Linter}s.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class LinterRegistry {

    /**
     * A {@link LinterRegistry} builder.
     */
    public static class Builder {
        private final Map<String, LinterEntry.Builder> entries = new LinkedHashMap<>();

        Builder() {
            super();
        }

        /**
         * @return a new {@link LinterRegistry}
         */
        public LinterRegistry build() {
            Map<String, LinterEntry> useEntries = new LinkedHashMap<>(entries.size());
            for (Map.Entry<String, LinterEntry.Builder> en : entries.entrySet()) {
                useEntries.put(en.getKey(), en.getValue().build());
            }
            return new LinterRegistry(Collections.unmodifiableMap(useEntries));
        }

        /**
         * @param id
         * @param linterClass
         * @param classLoader
         * @param includes
         * @param excludes
         * @param useDefaultIncludesAndExcludes
         * @return
         */
        public Builder entry(String id, String linterClass, final ClassLoader classLoader, String[] includes,
                String[] excludes, boolean useDefaultIncludesAndExcludes) {
            LinterEntry.Builder en = entries.get(id);
            if (en == null) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<Linter> cl = (Class<Linter>) classLoader.loadClass(linterClass);
                    Linter linter = cl.newInstance();
                    en = new LinterEntry.Builder(linter);
                    entries.put(id, en);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Could not load class " + linterClass, e);
                }
            }
            en.useDefaultIncludesAndExcludes = useDefaultIncludesAndExcludes;
            final PathSet.Builder pathSetBuilder = en.pathSetBuilder;
            pathSetBuilder.includes(includes);
            pathSetBuilder.excludes(excludes);
            return this;
        }

        public Builder entry(Linter linter) {
            final String linterClass = linter.getClass().getName();
            LinterEntry.Builder en = entries.get(linterClass);
            if (en == null) {
                en = new LinterEntry.Builder(linter);

                entries.put(linterClass, en);
            }

            return this;
        }

        public Builder removeEntry(String id) {
            entries.remove(id);
            return this;
        }

        public Builder scan(ClassLoader classLoader) {
            entry(new TextLinter());
            entry(new XmlLinter());
            final ServiceLoader<Linter> loader = java.util.ServiceLoader.load(Linter.class, classLoader);
            final Iterator<Linter> it = loader.iterator();
            while (it.hasNext()) {
                Linter linter = it.next();
                entry(linter);
            }
            return this;
        }

    }

    /**
     * A pair consisting of a {@link PathSet} and a {@link Linter}.
     */
    static class LinterEntry {

        /**
         * A {@link LinterEntry} builder.
         */
        public static class Builder {
            private final PathSet.Builder pathSetBuilder = new PathSet.Builder();
            private boolean useDefaultIncludesAndExcludes = true;
            private final Linter linter;

            Builder(Linter linter) {
                super();
                this.linter = linter;
            }

            /**
             * @return a new {@link LinterEntry}
             */
            public LinterEntry build() {
                if (this.useDefaultIncludesAndExcludes) {
                    pathSetBuilder.includes(linter.getDefaultIncludes());
                    pathSetBuilder.excludes(linter.getDefaultExcludes());
                }
                return new LinterEntry(linter, pathSetBuilder.build());
            }
        }

        private final PathSet pathSet;
        private final Linter linter;

        LinterEntry(Linter linter, PathSet pathSet) {
            super();
            this.linter = linter;
            this.pathSet = pathSet;
        }

        /**
         * @return a {@link PathSet} whose {@link Path}s should be handled by the {@link Linter} returned by
         *         {@link #getLinter()}
         */
        public PathSet getPathSet() {
            return pathSet;
        }

        /**
         * @return the Linter responsible for handling {@link Path}s contained in the {@link PathSet} returned by
         *         {@link #getPathSet()}
         */
        public Linter getLinter() {
            return linter;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(LinterRegistry.class);

    public static Builder builder() {
        return new Builder();
    }

    private final Map<String, LinterEntry> entries;

    LinterRegistry(Map<String, LinterEntry> entries) {
        super();
        this.entries = entries;
    }

    /**
     * Iterates through registered entries and filters those ones whose {@link PathSet} contains the given {@link Path}.
     *
     * @param path
     *            the {@link Path} to find {@link Linter}s for
     * @return an unmodifiable list of {@link Linter}s
     */
    public List<Linter> filter(Path path) {
        log.trace("Filtering linters for file '{}'", path);
        final List<Linter> result = new ArrayList<>(entries.size());
        for (LinterEntry linterEntry : entries.values()) {
            if (linterEntry.getPathSet().contains(path)) {
                final Linter linter = linterEntry.getLinter();
                if (log.isTraceEnabled()) {
                    log.trace("Adding linter {}", linter.getClass().getName());
                }
                result.add(linter);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
