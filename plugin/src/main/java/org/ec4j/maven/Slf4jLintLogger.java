/*
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
package org.ec4j.maven;

import org.ec4j.lint.api.Logger;

/**
 * A {@link Slf4jLintLogger} that delegates to a SLF4J {@link org.slf4j.Logger}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Slf4jLintLogger extends Logger.AbstractLogger {
    static LogLevelSupplier toEc4jLogLevelSupplier(final org.slf4j.Logger log) {
        return () -> {
            if (log.isTraceEnabled()) {
                return LogLevel.TRACE;
            } else if (log.isDebugEnabled()) {
                return LogLevel.DEBUG;
            } else if (log.isInfoEnabled()) {
                return LogLevel.INFO;
            } else if (log.isWarnEnabled()) {
                return LogLevel.WARN;
            } else if (log.isErrorEnabled()) {
                return LogLevel.ERROR;
            } else {
                return LogLevel.NONE;
            }
        };
    }

    private final org.slf4j.Logger delegate;

    public Slf4jLintLogger(org.slf4j.Logger delegate) {
        super(toEc4jLogLevelSupplier(delegate));
        this.delegate = delegate;
    }

    @Override
    public void log(LogLevel level, String string, Object... args) {
        switch (level) {
            case TRACE:
                delegate.trace(string, args);
                break;
            case DEBUG:
                delegate.debug(string, args);
                break;
            case INFO:
                delegate.info(string, args);
                break;
            case WARN:
                delegate.warn(string, args);
                break;
            case ERROR:
                delegate.error(string, args);
                break;
            default:
                throw new IllegalStateException("Unexpected " + LogLevel.class.getName() + " [" + level + "]");
        }
    }

}
