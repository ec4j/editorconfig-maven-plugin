package org.l2x6.editorconfig.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.l2x6.editorconfig.mojo.ValidatorConfig;

public class ValidatorRegistry {

    public static class Builder {
        private final Map<String, ValidatorEntry.Builder> entries = new LinkedHashMap<>();

        Builder() {
            super();
        }

        public ValidatorRegistry build() {
            Map<String, ValidatorEntry> useEntries = new LinkedHashMap<>(entries.size());
            for (Map.Entry<String, ValidatorEntry.Builder> en : entries.entrySet()) {
                useEntries.put(en.getKey(), en.getValue().build());
            }
            return new ValidatorRegistry(Collections.unmodifiableMap(useEntries));
        }

        public Builder entry(final String validatorClass, final ClassLoader classLoader, ValidatorConfig validatorConfig) {
            ValidatorEntry.Builder en = entries.get(validatorClass);
            if (en == null) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<Validator> cl = (Class<Validator>) classLoader.loadClass(validatorClass);
                    Validator validator = cl.newInstance();
                    en = new ValidatorEntry.Builder(validator);
                    entries.put(validatorClass, en);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Could not load class " + validatorClass, e);
                }
            }
            en.useDefaultIncludesAndExcludes = validatorConfig.isUseDefaultIncludesAndExcludes();
            final PathSet.Builder pathSetBuilder = en.pathSetBuilder;
            pathSetBuilder.includes(validatorConfig.getIncludes());
            pathSetBuilder.excludes(validatorConfig.getExcludes());
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

        public Builder scan(ClassLoader classLoader) {
            final ServiceLoader<Validator> loader = java.util.ServiceLoader.load(Validator.class, classLoader);
            final Iterator<Validator> it = loader.iterator();
            while (it.hasNext()) {
                Validator validator = it.next();
                entry(validator);
            }
            return this;
        }

    }

    public static class ValidatorEntry {

        static class Builder {
            private final PathSet.Builder pathSetBuilder = new PathSet.Builder();
            private final Validator validator;
            private boolean useDefaultIncludesAndExcludes = true;

            public Builder(Validator validator) {
                super();
                this.validator = validator;
            }

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

        public PathSet getPathSet() {
            return pathSet;
        }

        public Validator getValidator() {
            return validator;
        }

    }
    private final Map<String, ValidatorEntry> entries;

    public static Builder builder() {
        return new Builder();
    }

    ValidatorRegistry(Map<String, ValidatorEntry> entries) {
        super();
        this.entries = entries;
    }

    public List<Validator> filter(Path file) {
        final List<Validator> result = new ArrayList<>(entries.size());
        for (ValidatorEntry validatorEntry : entries.values()) {
            if (validatorEntry.getPathSet().contains(file)) {
                result.add(validatorEntry.getValidator());
            }
        }
        return Collections.unmodifiableList(result);
    }
}
