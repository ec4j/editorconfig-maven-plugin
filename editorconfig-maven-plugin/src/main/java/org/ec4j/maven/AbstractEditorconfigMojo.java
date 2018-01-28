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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.ec4j.core.Cache.Caches;
import org.ec4j.core.Resource.Resources;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.ResourcePropertiesService;
import org.ec4j.core.model.PropertyType;
import org.ec4j.maven.lint.api.Constants;
import org.ec4j.maven.lint.api.FormatException;
import org.ec4j.maven.lint.api.Linter;
import org.ec4j.maven.lint.api.LinterRegistry;
import org.ec4j.maven.lint.api.Resource;
import org.ec4j.maven.lint.api.ViolationHandler;
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
     * If set to {@code true}, the class path will be scanned for implementations of {@link Linter} and all
     * {@link Linter}s found will be added to {@link #linters} with their default includes and excludes.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.addLintersFromClassPath", defaultValue = "true")
    protected boolean addLintersFromClassPath;

    /**
     * The base directory of the current Maven project.
     *
     * @since 0.0.1
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
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    /**
     * If {@code true} the default exclude patterns (that exclude binary files and other non-source code files, see
     * {@link Constants#DEFAULT_EXCLUDES}) will be prepended to the list of {@link #excludes}. Otherwise, no defaults will be
     * prepended to {@link #excludes}.
     *
     * @since 0.0.3
     */
    @Parameter(property = "editorconfig.excludeNonSourceFiles", defaultValue = "true")
    protected boolean excludeNonSourceFiles;

    /**
     * File patterns to exclude from the set of files to process. The patterns are relative to the current project's
     * {@code baseDir}. See also {@link #excludeNonSourceFiles} and {@link #excludeSubmodules}.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.excludes")
    protected String[] excludes;

    /**
     * If {@code true} the Maven submodule directories of the current project will be prepended to the list of
     * {@link #excludes}. Otherwise, the module directories will not be excluded.
     *
     * @since 0.0.3
     */
    @Parameter(property = "editorconfig.excludeSubmodules", defaultValue = "true")
    protected boolean excludeSubmodules;

    /**
     * If {@code true} the plugin execution will fail with an error in case no single {@code .editorconfig} property
     * matches any file of the current Maven project - this usually means that there is no {@code .editorconfig} file in
     * the whole source tree. If {@code false}, only a warning is produced in such a situation.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.failOnNoMatchingProperties", defaultValue = "true")
    protected boolean failOnNoMatchingProperties;

    /**
     * File patterns to include into the set of files to process. The patterns are relative to the current project's
     * {@code baseDir}.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.includes", defaultValue = "**")
    protected String[] includes;

    /**
     * Set the includes and excludes for the individual {@link Linter}s
     *
     * @since 0.0.1
     */
    @Parameter
    protected List<LinterConfig> linters = new ArrayList<>();

    @Component
    public MavenProject project;

    /**
     * If {@code true} the execution of the Mojo will be skipped; otherwise the Mojo will be executed.
     *
     * @since 0.0.1
     */
    @Parameter(property = "editorconfig.skip", defaultValue = "false")
    private boolean skip;

    public AbstractEditorconfigMojo() {
        super();
    }

    protected LinterRegistry buildLinterRegistry() {

        final LinterRegistry.Builder linterRegistryBuilder = LinterRegistry.builder();

        if (addLintersFromClassPath) {
            linterRegistryBuilder.scan(getClass().getClassLoader());
        }

        if (linters != null && !linters.isEmpty()) {
            for (LinterConfig linter : linters) {
                if (linter.isEnabled()) {
                    linterRegistryBuilder.entry(linter.getId(), linter.getClassName(), this.getClass().getClassLoader(),
                            linter.getIncludes(), linter.getExcludes(), linter.isUseDefaultIncludesAndExcludes());
                } else {
                    linterRegistryBuilder.removeEntry(linter.getId());
                }
            }
        }

        return linterRegistryBuilder.build();

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

        if (this.encoding == null || this.encoding.isEmpty()) {
            this.charset = Charset.defaultCharset();
            log.warn(
                    "Using current platform's default encoding {} to read .editorconfig files. You do not want this. Set either 'project.build.sourceEncoding' or 'editorconfig.encoding' property.",
                    charset);
        } else {
            this.charset = Charset.forName(this.encoding);
        }
        this.basedirPath = basedir.toPath();

        LinterRegistry linterRegistry = buildLinterRegistry();
        final String[] includedFiles = scanIncludedFiles();

        try {
            final ViolationHandler handler = createHandler();

            final ResourcePropertiesService resourcePropertiesService = ResourcePropertiesService.builder() //
                    .cache(Caches.permanent()) //
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
                    final List<Linter> filteredLinters = linterRegistry.filter(file);
                    ViolationHandler.ReturnState state = ViolationHandler.ReturnState.RECHECK;
                    while (state != ViolationHandler.ReturnState.FINISHED) {
                        for (Linter linter : filteredLinters) {
                            if (log.isTraceEnabled()) {
                                log.trace("Processing file '{}' using linter {}", file, linter.getClass().getName());
                            }
                            handler.startFile(resource);
                            linter.process(resource, editorConfigProperties, handler);
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

        Set<String> excls = new LinkedHashSet<>();
        if (excludeNonSourceFiles) {
            excls.addAll(Constants.DEFAULT_EXCLUDES);
        }
        if (excludeSubmodules && project != null) {
            {
                final List<String> modules = project.getModules();
                if (modules != null) {
                    for (String module : modules) {
                        excls.add(module + "/**");
                    }
                }
            }

            final List<Profile> profiles = project.getModel().getProfiles();
            if (profiles != null) {
                for (Profile profile : profiles) {
                    final List<String> modules = profile.getModules();
                    if (modules != null) {
                        for (String module : modules) {
                            excls.add(module + "/**");
                        }
                    }
                }
            }

        }
        if (excludes != null && excludes.length > 0) {
            for (String include : excludes) {
                excls.add(include);
            }
        }
        scanner.setExcludes(excls.toArray(new String[0]));

        scanner.scan();
        return scanner.getIncludedFiles();
    }

}
