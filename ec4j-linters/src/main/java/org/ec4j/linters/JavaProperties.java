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
package org.ec4j.linters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

public class JavaProperties {

    public static class JavaProperty {

        public static JavaProperty booleanFalse(String jdtFormatterKey, String description) {
            return new JavaProperty(jdtFormatterKey, Boolean.class, Boolean.FALSE, description, Collections.singletonList(toEditorConfigKey(jdtFormatterKey)));
        }
        public static JavaProperty booleanTrue(String jdtFormatterKey, String description) {
            return new JavaProperty(jdtFormatterKey, Boolean.class, Boolean.TRUE, description, Collections.singletonList(toEditorConfigKey(jdtFormatterKey)));
        }
        public static JavaProperty intMax(String jdtFormatterKey, String description) {
            return new JavaProperty(jdtFormatterKey, Integer.class, Integer.MAX_VALUE, description, Collections.singletonList(toEditorConfigKey(jdtFormatterKey)));
        }

        static String toEditorConfigKey(String jdtFormatterKey) {
            assert jdtFormatterKey.startsWith(JDT_KEY_PREFIX);
            return EDITORCONFIG_JAVA_NAMESPACE_PREFIX + jdtFormatterKey.substring(JDT_KEY_PREFIX.length());
        }

        private static List<String> toList(String[] editorConfigKeys) {
            if (editorConfigKeys.length == 0) {
                return Collections.emptyList();
            }
            List<String> l = new ArrayList<>(editorConfigKeys.length);
            for (int i = 0; i < editorConfigKeys.length; i++) {
                l.add(editorConfigKeys[i]);
            }
            return Collections.unmodifiableList(l);
        }
        private final Object defaultValue;
        private final String description;
        private final List<String> editorConfigKeys;
        private final String jdtFormatterKey;

        private final Class<?> type;

        JavaProperty(String jdtFormatterKey, Class<?> type, Object defaultValue,
                String description, List<String> editorConfigKeys) {
            super();
            this.jdtFormatterKey = jdtFormatterKey;
            this.editorConfigKeys = editorConfigKeys;
            this.type = type;
            this.defaultValue = defaultValue;
            this.description = description;
        }

        public JavaProperty(String jdtFormatterKey, Class<?> type, Object defaultValue,
                String description, String... editorConfigKeys) {
            this(jdtFormatterKey, type, defaultValue, description, toList(editorConfigKeys));
        }

    }
    private static final String EDITORCONFIG_JAVA_NAMESPACE_PREFIX = "java/";

    private static final String JDT_KEY_PREFIX = JavaCore.PLUGIN_ID + ".formatter.";

    private final Map<String, JavaProperty> byJdtFormatterKey;

    JavaProperties() {
        Map<String, JavaProperty> m = new TreeMap<String, JavaProperties.JavaProperty>();

        put(m, JavaProperty.booleanFalse(DefaultCodeFormatterConstants.FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS, "Option to align type members of a type declaration on column"));
        put(m, JavaProperty.booleanFalse(DefaultCodeFormatterConstants.FORMATTER_ALIGN_VARIABLE_DECLARATIONS_ON_COLUMNS, "Option to align variable declarations on column"));
        put(m, JavaProperty.booleanFalse(DefaultCodeFormatterConstants.FORMATTER_ALIGN_ASSIGNMENT_STATEMENTS_ON_COLUMNS, "Option to align assignment statements on column"));
        put(m, JavaProperty.booleanFalse(DefaultCodeFormatterConstants.FORMATTER_ALIGN_WITH_SPACES, "Option to use spaces when aligning members, independent of selected tabulation character"));
        put(m, JavaProperty.intMax(DefaultCodeFormatterConstants.FORMATTER_ALIGN_FIELDS_GROUPING_BLANK_LINES, "Option to affect aligning on columns: groups of items are aligned independently if they are separated by at least the selected number of blank lines. Note: since 3.15 the 'fields' part is a (potentially misleading) residue as this option"));

        // https://github.com/JetBrains/intellij-community/blob/0be3657ad0d22c4e5f5d8a5cd376b7b42d5d84e9/plugins/eclipse/src/org/jetbrains/idea/eclipse/importer/EclipseFormatterOptionsHandler.java
        // https://github.com/JetBrains/intellij-community/blob/4999f5293e4307870020f1d0d672a3d35a52f22d/plugins/eclipse/src/org/jetbrains/idea/eclipse/importer/EclipseImportMap.java
        // https://github.com/JetBrains/intellij-community/blob/4999f5293e4307870020f1d0d672a3d35a52f22d/plugins/eclipse/src/org/jetbrains/idea/eclipse/importer/EclipseImportMap.properties
        // https://github.com/JetBrains/intellij-community/blob/4c45cb8cc4599c6aa207aa75005f862ff1316b17/java/java-tests/testSrc/com/intellij/java/psi/formatter/java/AbstractJavaFormatterTest.java#L194

        this.byJdtFormatterKey = Collections.unmodifiableMap(m);
    }

    private void put(Map<String, JavaProperty> m, JavaProperty prop) {
        m.put(prop.jdtFormatterKey, prop);
    }
}
