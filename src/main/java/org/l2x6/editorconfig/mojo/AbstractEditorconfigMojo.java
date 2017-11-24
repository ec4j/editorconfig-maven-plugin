package org.l2x6.editorconfig.mojo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.ec4j.core.Cache.Caches;
import org.ec4j.core.ResourcePath.ResourcePaths;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.ResourcePropertiesService;
import org.ec4j.core.Resource.Resources;
import org.ec4j.core.model.PropertyType;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.Validator;
import org.l2x6.editorconfig.core.ValidatorRegistry;
import org.l2x6.editorconfig.core.ViolationHandler;

public abstract class AbstractEditorconfigMojo extends AbstractMojo {

    /**
     * Set the includes and excludes for the individual {@link Validator}s
     */
    @Parameter
    protected List<ValidatorConfig> validators = new ArrayList<>();

    /**
     * If set to {@code true}, the class path will be scanned for implementations of {@link Validator} and all
     * {@link Validator}s found will be added to {@link #validators} with their default includes and excludes.
     */
    @Parameter(property = "editorconfig.addValidatorsFromClassPath", defaultValue = "true")
    protected boolean addValidatorsFromClassPath;

    /** File patterns to include into the set of files to process. The patterns are relative to the current project's {@code baseDir}. */
    @Parameter(property = "editorconfig.includes", defaultValue = "*,src/**/*")
    protected String[] includes;

    /** File patterns to exclude from the set of files to process. The patterns are relative to the current project's {@code baseDir}. */
    @Parameter(property = "editorconfig.excludes")
    protected String[] excludes;

    /**
     * The default encoding of files selected by {@link #includes} and {@link #excludes}. This value can be overriden by a {@code charset}
     * property of an {@code .editorconfig} file.
     */
    @Parameter(property = "editorconfig.encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    /** The result of {@code Charset.forName(encoding)} */
    protected Charset charset;

    /** The result of {@code basedir.toPath()} */
    private Path basedirPath;

    /**
     * The base directory of the current Maven project.
     */
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File basedir;
    /**
     * If {@code true} the execution of the Mojo will be skipped; otherwise the Mojo will be executed.
     */
    @Parameter(property = "editorconfig.skip", defaultValue = "false")
    private boolean skip;

    public static FileSet getDefaultFileSet(File baseDir) {

        FileSet result = new FileSet();
        result.setDirectory(baseDir.getAbsolutePath());

        List<String> includes = new ArrayList<String>(2);
        includes.add("*");
        includes.add("src/**/*");
        result.setIncludes(includes);
        result.setExcludes(Collections.<String>emptyList());

        return result;
    }

    public AbstractEditorconfigMojo() {
        super();
    }

    protected abstract Resource createResource(Path file, Charset encoding);

    protected abstract ViolationHandler createHandler();

    /**
     * Called by Maven for executing the Mojo.
     *
     * @throws MojoExecutionException Running the Mojo failed.
     * @throws MojoFailureException   A configuration error was detected.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().debug("Skipping execution, as demanded by user.");
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
            for (String includedPath : includedFiles) {
                final Path file = Paths.get(includedPath); // relative to basedir
                final Path absFile = basedirPath.resolve(file);
                getLog().debug("Processing file " + file);
                ResourceProperties editorConfigProperties = resourcePropertiesService
                        .queryProperties(Resources.ofPath(absFile, charset));
                Charset useEncoding = Charset
                        .forName(editorConfigProperties.getValue(PropertyType.charset, encoding, true));
                final Resource resource = createResource(absFile, useEncoding);
                List<Validator> filteredValidators = validatorRegistry.filter(file);
                for (Validator validator : filteredValidators) {
                    ViolationHandler.ReturnState state = ViolationHandler.ReturnState.RECHECK;
                    while (state != ViolationHandler.ReturnState.FINISHED) {
                        handler.startFile(resource);
                        validator.process(resource, editorConfigProperties, handler);
                        state = handler.endFile();
                    }
                }
            }
            handler.endFiles();
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
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

    protected ValidatorRegistry buildValidatorRegistry() {

        final ValidatorRegistry.Builder validatorRegistryBuilder = ValidatorRegistry.builder();


        if (addValidatorsFromClassPath) {
            validatorRegistryBuilder.scan(getClass().getClassLoader());
        }

        if (validators != null && !validators.isEmpty()) {
            for (ValidatorConfig validator : validators) {
                validatorRegistryBuilder.entry(validator.getClassName(), this.getClass().getClassLoader(), validator);
            }
        }

        return validatorRegistryBuilder.build();

    }

}
