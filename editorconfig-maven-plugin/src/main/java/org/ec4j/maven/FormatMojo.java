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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ec4j.lint.api.FormattingHandler;
import org.ec4j.lint.api.ViolationHandler;
import org.slf4j.LoggerFactory;

/**
 * Formats a set of files so that they comply with rules defined in {@code .editorconfig} files.
 *
 * @since 0.0.1
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Mojo(defaultPhase = LifecyclePhase.NONE, name = "format", threadSafe = true)
public class FormatMojo extends AbstractEditorconfigMojo {

    /**
     * If {@code true}, a backup file will be created for every file that needs to be formatted just before the
     * formatted version is stored. If {@code false}, no backup is done and the files are formatted in place. See also
     * {@link #backupSuffix}.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.backup", defaultValue = "false")
    private boolean backup;

    /**
     * A suffix to append to a file name to create its backup. See also {@link #backup}.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.backupSuffix", defaultValue = ".bak")
    private String backupSuffix;

    /** {@inheritDoc} */
    @Override
    protected ViolationHandler createHandler() {
        return new FormattingHandler(backup, backupSuffix,
                new Slf4jLintLogger(LoggerFactory.getLogger(FormattingHandler.class)));
    }

}
