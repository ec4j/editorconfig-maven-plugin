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

import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ec4j.maven.core.Resource;
import org.ec4j.maven.core.ViolationCollector;
import org.ec4j.maven.core.ViolationHandler;

/**
 * Checks whether files are formatted according to rules defined in {@code .editorconfig} files.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Mojo(defaultPhase = LifecyclePhase.VERIFY, name = "check")
public class CheckMojo extends AbstractEditorconfigMojo {

    /**
     * Tells the mojo what to do in case formatting violations are found. if {@code true}, all violations will be
     * reported on the console as ERRORs and the build will fail. if {@code false}, all violations will be reported on
     * the console as WARNs and the build will proceed further.
     */
    @Parameter(property = "editorconfig.failOnFormatViolation", defaultValue = "true")
    private boolean failOnFormatViolation;

    @Override
    protected ViolationHandler createHandler() {
        return new ViolationCollector(failOnFormatViolation);
    }

    @Override
    protected Resource createResource(Path absFile, Path relFile, Charset encoding) {
        return new Resource(absFile, relFile, encoding);
    }

}
