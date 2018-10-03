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

/**
 * Our own logger interface. We cannot depend on SLF4J, because SLF4J is not available e.g. in Ant.
 *
 * @since 0.0.6
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface Logger {

    /**
     * A {@link Logger} implementation that does nothing, use the singleton {@link Logger#NO_OP}.
     *
     * @since 0.0.6
     */
    class NoOpLogger implements Logger {

        /** Does nothing */
        @Override
        public void debug(String string, Object... args) {
        }

        /** Does nothing */
        @Override
        public void error(String string, Object... args) {
        }

        /** Does nothing */
        @Override
        public void info(String string, Object... args) {
        }

        /** @return always {@code false} */
        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        /** @return always {@code false} */
        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        /** @return always {@code false} */
        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        /** @return always {@code false} */
        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        /** @return always {@code false} */
        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        /** Does nothing */
        @Override
        public void trace(String string, Object... args) {
        }

        /** Does nothing */
        @Override
        public void warn(String string, Object... args) {
        }

    }

    /**
     * The no operation singleton
     *
     * @since 0.0.6
     */
    Logger NO_OP = new NoOpLogger();

    /**
     * @param string the message, possibly with <code>{}</code> placeholders
     * @param args the args to replace for the <code>{}</code> placeholders
     * @since 0.0.6
     */
    void debug(String string, Object... args);

    /**
     * @param string the message, possibly with <code>{}</code> placeholders
     * @param args the args to replace for the <code>{}</code> placeholders
     * @since 0.0.6
     */
    void error(String string, Object... args);

    /**
     * @param string the message, possibly with <code>{}</code> placeholders
     * @param args the args to replace for the <code>{}</code> placeholders
     * @since 0.0.6
     */
    void info(String string, Object... args);

    /**
     * @return {@code true} if debug or lower log level is enabled; {@code false} otherwise
     * @since 0.0.6
     */
    boolean isDebugEnabled();

    /**
     * @return {@code true} if error or lower log level is enabled; {@code false} otherwise
     * @since 0.0.6
     */
    boolean isErrorEnabled();

    /**
     * @return {@code true} if info or lower log level is enabled; {@code false} otherwise
     * @since 0.0.6
     */
    boolean isInfoEnabled();

    /**
     * @return {@code true} if trace or lower log level is enabled; {@code false} otherwise
     * @since 0.0.6
     */
    boolean isTraceEnabled();

    /**
     * @return {@code true} if warn or lower log level is enabled; {@code false} otherwise
     * @since 0.0.6
     */
    boolean isWarnEnabled();

    /**
     * @param string the message, possibly with <code>{}</code> placeholders
     * @param args the args to replace for the <code>{}</code> placeholders
     * @since 0.0.6
     */
    void trace(String string, Object... args);

    /**
     * @param string the message, possibly with <code>{}</code> placeholders
     * @param args the args to replace for the <code>{}</code> placeholders
     * @since 0.0.6
     */
    void warn(String string, Object... args);

}
