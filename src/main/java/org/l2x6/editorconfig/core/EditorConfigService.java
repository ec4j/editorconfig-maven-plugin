package org.l2x6.editorconfig.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ec4e.services.EditorConfigConstants;
import org.eclipse.ec4e.services.EditorConfigException;
import org.eclipse.ec4e.services.model.EditorConfig;
import org.eclipse.ec4e.services.model.Option;
import org.eclipse.ec4e.services.model.Section;

public class EditorConfigService {

    public enum EndOfLine {
        cr("\r"), crlf("\r\n"), lf("\n");

        private final String eol;

        private EndOfLine(String eol) {
            this.eol = eol;
        }

        public String getEol() {
            return eol;
        }
    }

    public enum IndentStyle {
        space(' '), tab('\t');

        private final char indentChar;

        private IndentStyle(char indentChar) {
            this.indentChar = indentChar;
        }

        public char getindentChar() {
            return indentChar;
        }

        public static IndentStyle getDefault() {
            return space;
        }
    }

    public static class OptionMap {
        public static class Builder {
            private Map<String, TypedValue> options = new LinkedHashMap<>();
            public OptionMap build() {
                Map<String, TypedValue> opts = Collections.unmodifiableMap(options);
                options = null;
                return new OptionMap(opts);
            }

            public Builder option(Option option) {
                options.put(option.getName(), new TypedValue(option.getValue()));
                return this;
            }

            public Builder option(String key, String value) {
                options.put(key, new TypedValue(value));
                return this;
            }

            public Builder indetSize(int indentSize) {
                options.put(WellKnownKey.indent_size.name(), new TypedValue(String.valueOf(indentSize)));
                return this;
            }

            public Builder trimTrailingSpace() {
                options.put(WellKnownKey.trim_trailing_whitespace.name(), new TypedValue(Boolean.TRUE.toString()));
                return this;
            }

            public Builder indetSpace() {
                options.put(WellKnownKey.indent_style.name(), new TypedValue(IndentStyle.space.name()));
                return this;
            }

            public Builder indetTab() {
                options.put(WellKnownKey.indent_style.name(), new TypedValue(IndentStyle.tab.name()));
                return this;
            }

        }

        private final Map<String, TypedValue> options;

        private OptionMap(Map<String, TypedValue> options) {
            super();
            this.options = options;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String get(String key) {
            TypedValue opt = options.get(key);
            return opt == null ? null : opt.getRawValue();
        }

        @SuppressWarnings("unchecked")
        public <V> V get(TypedKey key, V defaultValue) {
            TypedValue opt = options.get(key.name());
            return opt == null ? defaultValue : (V) opt.getTypedValue(key);
        }

        public <V> V get(TypedKey key) {
            return get(key, null);
        }
    }

    public interface TypedKey {

        <V> V convert(String value);

        Class<?> getType();

        String name();
    }

    public static class TypedValue {
        private final String rawValue;
        private Object typedValue;

        public TypedValue(String rawValue) {
            super();
            this.rawValue = rawValue;
        }

        public String getRawValue() {
            return rawValue;
        }

        @SuppressWarnings("unchecked")
        public <V> V getTypedValue(TypedKey key) {
            if (typedValue == null) {
                typedValue = key.convert(rawValue);
            }
            return (V) typedValue;
        }
    }

    public enum WellKnownKey implements TypedKey {
        charset(Charset.class){
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) Charset.forName(value);
            }
        }, //
        end_of_line(EndOfLine.class) {
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) EndOfLine.valueOf(value);
            }
        },
        indent_size(Integer.class) {
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) Integer.valueOf(value);
            }

        },
        indent_style(IndentStyle.class) {
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) IndentStyle.valueOf(value);
            }
        },
        insert_final_newline(Boolean.class) {
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) Boolean.valueOf(value);
            }
        },
        tab_width(Integer.class) {
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) Integer.valueOf(value);
            }

        },
        trim_trailing_whitespace(Boolean.class) {
            @Override
            public <V> V convert(String value) {
                return value == null ? null : (V) Boolean.valueOf(value);
            }
        };

        private final Class<?> type;

        private WellKnownKey(Class<?> type) {
            this.type = type;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V convert(String value) {
            return (V) value;
        }

        @Override
        public Class<?> getType() {
            return type;
        }
    }

    private final String configFilename = EditorConfigConstants.EDITORCONFIG;
    private final Map<Path, EditorConfig> editorConfigCache = new HashMap<>();
    private final Set<Path> rootDirectories;

    public EditorConfigService(Set<Path> rootDirectories) {
        this.rootDirectories = rootDirectories;
    }

    public OptionMap getOptions(Path file) throws EditorConfigException {
        OptionMap.Builder options = OptionMap.builder();

        try {
            boolean root = false;
            Path dir = file.getParent();
            while (dir != null && !root) {
                Path configFile = dir.resolve(configFilename);
                if (Files.exists(configFile)) {
                    EditorConfig config = editorConfigCache.get(configFile);
                    if (config == null) {
                        config = EditorConfig.load(configFile.toFile());
                    }
                    root = config.isRoot();
                    List<Section> sections = config.getSections();
                    for (Section section : sections) {
                        if (section.match(file.toFile())) {
                            // Section matches the editor file, collect options of the section
                            List<Option> o = section.getOptions();
                            for (Option option : o) {
                                // TODO check the spec what to do when an option is defined on multiple levels
                                options.option(option);
                            }
                        }
                    }
                }
                root |= rootDirectories.contains(dir);
                dir = dir.getParent();
            }
        } catch (IOException e) {
            throw new EditorConfigException(null, e);
        }
        return options.build();
    }
}
