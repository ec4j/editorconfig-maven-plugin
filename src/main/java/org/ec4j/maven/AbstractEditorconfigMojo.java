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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.ec4j.core.Cache.Caches;
import org.ec4j.core.Resource.Resources;
import org.ec4j.core.ResourcePath.ResourcePaths;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.ResourcePropertiesService;
import org.ec4j.core.model.PropertyType;
import org.ec4j.maven.core.FormatException;
import org.ec4j.maven.core.Resource;
import org.ec4j.maven.core.Validator;
import org.ec4j.maven.core.ValidatorRegistry;
import org.ec4j.maven.core.ViolationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base for {@link CheckMojo} and {@link FormatMojo}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public abstract class AbstractEditorconfigMojo extends AbstractMojo {

    private static final Logger log = LoggerFactory.getLogger(AbstractEditorconfigMojo.class);

    /**
     * If set to {@code true}, the class path will be scanned for implementations of {@link Validator} and all
     * {@link Validator}s found will be added to {@link #validators} with their default includes and excludes.
     */
    @Parameter(property = "editorconfig.addValidatorsFromClassPath", defaultValue = "true")
    protected boolean addValidatorsFromClassPath;

    /**
     * The base directory of the current Maven project.
     */
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File basedir;

    /** The result of {@code basedir.toPath()} */
    protected Path basedirPath;

    /** The result of {@code Charset.forName(encoding)} */
    protected Charset charset;

    /**
     * The default encoding of files selected by {@link #includes} and {@link #excludes}. This value can be overriden by
     * a {@code charset} property of an {@code .editorconfig} file.
     */
    @Parameter(property = "editorconfig.encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    /**
     * File patterns to exclude from the set of files to process. The patterns are relative to the current project's
     * {@code baseDir}.
     */
    @Parameter(property = "editorconfig.excludes")
    protected String[] excludes;

    /**
     * If {@code true} the plugin execution will fail with an error in case no single {@code .editorconfig} property
     * matches any file of the current Maven project - this usually means that there is no {@code .editorconfig} file in
     * the whole source tree. If {@code false}, only a warning is produced in such a situation.
     */
    @Parameter(property = "editorconfig.failOnNoMatchingProperties", defaultValue = "true")
    protected boolean failOnNoMatchingProperties;

    /**
     * File patterns to include into the set of files to process. The patterns are relative to the current project's
     * {@code baseDir}.
     */
    @Parameter(property = "editorconfig.includes", defaultValue = "*,src/**/*")
    protected String[] includes;

    /**
     * If {@code true} the execution of the Mojo will be skipped; otherwise the Mojo will be executed.
     */
    @Parameter(property = "editorconfig.skip", defaultValue = "false")
    private boolean skip;
    /**
     * Set the includes and excludes for the individual {@link Validator}s
     */
    @Parameter
    protected List<ValidatorConfig> validators = new ArrayList<>();

    public AbstractEditorconfigMojo() {
        super();
    }

    protected ValidatorRegistry buildValidatorRegistry() {

        final ValidatorRegistry.Builder validatorRegistryBuilder = ValidatorRegistry.builder();

        if (addValidatorsFromClassPath) {
            validatorRegistryBuilder.scan(getClass().getClassLoader());
        }

        if (validators != null && !validators.isEmpty()) {
            for (ValidatorConfig validator : validators) {
                if (validator.isEnabled()) {
                    validatorRegistryBuilder.entry(validator.getId(), validator.getClassName(),
                            this.getClass().getClassLoader(), validator.getIncludes(), validator.getExcludes(),
                            validator.isUseDefaultIncludesAndExcludes());
                } else {
                    validatorRegistryBuilder.removeEntry(validator.getId());
                }
            }
        }

        return validatorRegistryBuilder.build();

    }

    protected abstract ViolationHandler createHandler();

    /**
     * @param absFile
     *            the {@link Path} to create a {@link Resource} for. Must be absolute.
     * @param relFile
     *            the {@link Path} to create a {@link Resource} for. Must be relative to {@link #basedirPath}.
     * @param encoding
     * @return
     */
    protected abstract Resource createResource(Path absFile, Path relFile, Charset encoding);

    /**
     * Called by Maven for executing the Mojo.
     *
     * @throws MojoExecutionException
     *             Running the Mojo failed.
     * @throws MojoFailureException
     *             A configuration error was detected.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            log.debug("Skipping execution, as demanded by user.");
            return;
        }

        this.charset = Charset.forName(this.encoding);
        this.basedirPath = basedir.toPath();

        ValidatorRegistry validatorRegistry = buildValidatorRegistry();
        final String[] includedFiles = scanIncludedFiles();

        try {
            final ViolationHandler handler = createHandler();
            final Path sourceTreeRoot = Paths.get(System.getProperty("maven.multiModuleProjectDirectory"));

            final ResourcePropertiesService resourcePropertiesService = ResourcePropertiesService.builder() //
                    .cache(Caches.permanent()) //
                    .rootDirectory(ResourcePaths.ofPath(sourceTreeRoot, charset)) //
                    .build();
            handler.startFiles();
            boolean propertyMatched = false;
            for (String includedPath : includedFiles) {
                final Path file = Paths.get(includedPath); // relative to basedir
                final Path absFile = basedirPath.resolve(file);
                log.debug("Processing file '{}'", file);
                final ResourceProperties editorConfigProperties = resourcePropertiesService
                        .queryProperties(Resources.ofPath(absFile, charset));
                if (!editorConfigProperties.getProperties().isEmpty()) {
                    propertyMatched = true;
                    final Charset useEncoding = Charset
                            .forName(editorConfigProperties.getValue(PropertyType.charset, encoding, true));
                    final Resource resource = createResource(absFile, file, useEncoding);
                    final List<Validator> filteredValidators = validatorRegistry.filter(file);
                    ViolationHandler.ReturnState state = ViolationHandler.ReturnState.RECHECK;
                    while (state != ViolationHandler.ReturnState.FINISHED) {
                        for (Validator validator : filteredValidators) {
                            if (log.isTraceEnabled()) {
                                log.trace("Processing file '{}' using validator {}", file,
                                        validator.getClass().getName());
                            }
                            handler.startFile(resource);
                            validator.process(resource, editorConfigProperties, handler);
                        }
                        state = handler.endFile();
                    }
                }
            }
            if (!propertyMatched) {
                if (failOnNoMatchingProperties) {
                    log.error("No .editorconfig properties applicable for files under '{}'", basedirPath);
                } else {
                    log.warn("No .editorconfig properties applicable for files under '{}'", basedirPath);
                }
            }
            handler.endFiles();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (FormatException e) {
            throw new MojoFailureException("\n\n" + e.getMessage() + "\n\n", e);
        }

    }

    /**
     * A {@link DirectoryScanner} boiler plate.
     *
     * @return A {@link String} array of included files
     */
    private String[] scanIncludedFiles() {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(basedir);
        scanner.setIncludes(includes);
        if (excludes != null && excludes.length > 0) {
            scanner.setExcludes(excludes);
        }
        scanner.scan();
        return scanner.getIncludedFiles();
    }

}
