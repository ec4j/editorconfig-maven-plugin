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
package org.ec4j.lint.api;

/**
 * Lint utilities.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 * @since 0.0.9
 */
public class LintUtils {
    private LintUtils() {
    }

    /**
     * Appends the escaped {@code source} to {@code output}. {@code '\r'}, {@code '\n'}, {@code '\t'} and {@code '\\'}
     * are escaped using their respective escape sequences {@code "\\r"}, {@code "\\n"}, {@code "\\t"} and
     * {@code "\\\\"}.
     *
     * @param output where to append
     * @param source what to escape and append
     * @return the {@code output}
     */
    public static StringBuilder escape(StringBuilder output, char[] source) {
        return escape(output, source, 0, source.length);
    }

    /**
     * Appends a segment of the escaped {@code source} to {@code output}. {@code '\r'}, {@code '\n'}, {@code '\t'} and
     * {@code '\\'} are escaped using their respective escape sequences {@code "\\r"}, {@code "\\n"}, {@code "\\t"} and
     * {@code "\\\\"}.
     *
     * @param output where to append
     * @param source what to escape and append
     * @param offset zero based index in {@code source} where to start escaping
     * @param length the number of characters after {@code offset} to escape
     * @return the {@code output}
     */
    public static StringBuilder escape(StringBuilder output, char[] source, int offset, int length) {
        final int end = offset + length;
        while (offset < end) {
            final char ch = source[offset++];
            switch (ch) {
            case '\r':
                output.append("\\r");
                break;
            case '\n':
                output.append("\\n");
                break;
            case '\t':
                output.append("\\t");
                break;
            case '\\':
                output.append("\\\\");
                break;
            default:
                output.append(ch);
                break;
            }
        }
        return output;
    }

    /**
     * Returns an escaped {@code source}. {@code '\r'}, {@code '\n'}, {@code '\t'} and {@code '\\'} are escaped using
     * their respective escape sequences {@code "\\r"}, {@code "\\n"}, {@code "\\t"} and {@code "\\\\"}.
     *
     * @param source to escape
     * @return an escaped {@code source}
     */
    public static String escape(String source) {
        if (source == null) {
            return null;
        } else if (source.isEmpty()) {
            return "";
        }
        char[] sourceChars = source.toCharArray();
        for (int i = 0; i < sourceChars.length; i++) {
            switch (sourceChars[i]) {
            case '\r':
            case '\n':
            case '\t':
            case '\\':
                StringBuilder sb = new StringBuilder(sourceChars.length + 2);
                sb.append(sourceChars, 0, i);
                escape(sb, sourceChars, i, sourceChars.length - i);
                return sb.toString();
            default:
                break;
            }
        }
        return source;
    }

}
