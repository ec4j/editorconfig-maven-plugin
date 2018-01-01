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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.ec4j.maven.validator.TextValidator;
import org.ec4j.maven.validator.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for {@link Validator}s.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ValidatorRegistry {

    /**
     * A {@link ValidatorRegistry} builder.
     */
    public static class Builder {
        private final Map<String, ValidatorEntry.Builder> entries = new LinkedHashMap<>();

        Builder() {
            super();
        }

        /**
         * @return a new {@link ValidatorRegistry}
         */
        public ValidatorRegistry build() {
            Map<String, ValidatorEntry> useEntries = new LinkedHashMap<>(entries.size());
            for (Map.Entry<String, ValidatorEntry.Builder> en : entries.entrySet()) {
                useEntries.put(en.getKey(), en.getValue().build());
            }
            return new ValidatorRegistry(Collections.unmodifiableMap(useEntries));
        }

        /**
         * @param id
         * @param validatorClass
         * @param classLoader
         * @param includes
         * @param excludes
         * @param useDefaultIncludesAndExcludes
         * @return
         */
        public Builder entry(String id, String validatorClass, final ClassLoader classLoader, String[] includes,
                String[] excludes, boolean useDefaultIncludesAndExcludes) {
            ValidatorEntry.Builder en = entries.get(id);
            if (en == null) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<Validator> cl = (Class<Validator>) classLoader.loadClass(validatorClass);
                    Validator validator = cl.newInstance();
                    en = new ValidatorEntry.Builder(validator);
                    entries.put(id, en);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Could not load class " + validatorClass, e);
                }
            }
            en.useDefaultIncludesAndExcludes = useDefaultIncludesAndExcludes;
            final PathSet.Builder pathSetBuilder = en.pathSetBuilder;
            pathSetBuilder.includes(includes);
            pathSetBuilder.excludes(excludes);
            return this;
        }

        public Builder entry(Validator validator) {
            final String validatorClass = validator.getClass().getName();
            ValidatorEntry.Builder en = entries.get(validatorClass);
            if (en == null) {
                en = new ValidatorEntry.Builder(validator);

                entries.put(validatorClass, en);
            }

            return this;
        }

        public Builder removeEntry(String id) {
            entries.remove(id);
            return this;
        }

        public Builder scan(ClassLoader classLoader) {
            entry(new TextValidator());
            entry(new XmlValidator());
            final ServiceLoader<Validator> loader = java.util.ServiceLoader.load(Validator.class, classLoader);
            final Iterator<Validator> it = loader.iterator();
            while (it.hasNext()) {
                Validator validator = it.next();
                entry(validator);
            }
            return this;
        }

    }

    /**
     * A pair consisting of a {@link PathSet} and a {@link Validator}.
     */
    static class ValidatorEntry {

        /**
         * A {@link ValidatorEntry} builder.
         */
        public static class Builder {
            private final PathSet.Builder pathSetBuilder = new PathSet.Builder();
            private boolean useDefaultIncludesAndExcludes = true;
            private final Validator validator;

            Builder(Validator validator) {
                super();
                this.validator = validator;
            }

            /**
             * @return a new {@link ValidatorEntry}
             */
            public ValidatorEntry build() {
                if (this.useDefaultIncludesAndExcludes) {
                    pathSetBuilder.includes(validator.getDefaultIncludes());
                    pathSetBuilder.excludes(validator.getDefaultExcludes());
                }
                return new ValidatorEntry(validator, pathSetBuilder.build());
            }
        }

        private final PathSet pathSet;
        private final Validator validator;

        ValidatorEntry(Validator validator, PathSet pathSet) {
            super();
            this.validator = validator;
            this.pathSet = pathSet;
        }

        /**
         * @return a {@link PathSet} whose {@link Path}s should be handled by the {@link Validator} returned by
         *         {@link #getValidator()}
         */
        public PathSet getPathSet() {
            return pathSet;
        }

        /**
         * @return the Validator responsible for handling {@link Path}s contained in the {@link PathSet} returned by
         *         {@link #getPathSet()}
         */
        public Validator getValidator() {
            return validator;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(ValidatorRegistry.class);

    public static Builder builder() {
        return new Builder();
    }

    private final Map<String, ValidatorEntry> entries;

    ValidatorRegistry(Map<String, ValidatorEntry> entries) {
        super();
        this.entries = entries;
    }

    /**
     * Iterates through registered entries and filters those ones whose {@link PathSet} contains the given {@link Path}.
     *
     * @param path
     *            the {@link Path} to find {@link Validator}s for
     * @return an unmodifiable list of {@link Validator}s
     */
    public List<Validator> filter(Path path) {
        log.trace("Filtering validators for file '{}'", path);
        final List<Validator> result = new ArrayList<>(entries.size());
        for (ValidatorEntry validatorEntry : entries.values()) {
            if (validatorEntry.getPathSet().contains(path)) {
                final Validator validator = validatorEntry.getValidator();
                if (log.isTraceEnabled()) {
                    log.trace("Adding validator {}", validator.getClass().getName());
                }
                result.add(validator);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
