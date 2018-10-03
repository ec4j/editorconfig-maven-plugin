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
package org.ec4j.maven;

import org.ec4j.maven.lint.api.Logger;

/**
 * A {@link Slf4jLintLogger} that delegates to a SLF4J {@link org.slf4j.Logger}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Slf4jLintLogger implements Logger {
    private final org.slf4j.Logger delegate;

    public Slf4jLintLogger(org.slf4j.Logger delegate) {
        super();
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String arg0, Object... arg1) {
        delegate.debug(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void error(String arg0, Object... arg1) {
        delegate.error(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void info(String arg0, Object... arg1) {
        delegate.info(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void trace(String arg0, Object... arg1) {
        delegate.trace(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(String arg0, Object... arg1) {
        delegate.warn(arg0, arg1);
    }
}
