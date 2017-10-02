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
import org.eclipse.ec4e.services.EditorConfigException;
import org.l2x6.editorconfig.core.EditorConfigService;
import org.l2x6.editorconfig.core.EditorConfigService.OptionMap;
import org.l2x6.editorconfig.core.EditorConfigService.WellKnownKey;
import org.l2x6.editorconfig.core.FormatException;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.Validator;
import org.l2x6.editorconfig.core.Validators;
import org.l2x6.editorconfig.core.ViolationHandler;

public abstract class AbstractEditorconfigMojo extends AbstractMojo {

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

    /**
     * File patterns to include. The patterns are relative to the current project's {@code baseDir}.
     */
    @Parameter
    protected List<FileSet> fileSets = new ArrayList<FileSet>();

    /**
     * The base directory of the current Maven project.
     */
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File basedir;

    /**
     * If set to {@code true}, the result of {@link FormatFileSet#getDefault(String, int)} will be appended to
     * {@link #fileSets} before the processing.
     */
    @Parameter(property = "editorconfig.useDefaultFileSet", defaultValue = "true")
    protected boolean useDefaultFileSet;

    /**
     * If {@code true} the execution of the Mojo will be skipped; otherwise the Mojo will be executed.
     */
    @Parameter(property = "editorconfig.skip", defaultValue = "false")
    private boolean skip;

    /**
     * The default encoding of files included in {@link #fileSets}. This value can be overriden by a {@code charset}
     * property of an {@code .editorconfig} file.
     */
    @Parameter(property = "editorconfig.encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;
    protected Charset charset;

    public AbstractEditorconfigMojo() {
        super();
    }

    protected abstract Resource createResource(Path file, Charset encoding);

    protected abstract ViolationHandler createHandler();

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
            getLog().debug("Skipping execution, as demanded by user.");
            return;
        }

        if (useDefaultFileSet) {
            fileSets.add(getDefaultFileSet(basedir));
        }
        if (fileSets == null || fileSets.isEmpty()) {
            /* nothing to do */
            return;
        }

        charset = Charset.forName(encoding);

        try {
            final ViolationHandler handler = createHandler();
            final Path sourceTreeRoot = Paths.get(System.getProperty("maven.multiModuleProjectDirectory"));
            final EditorConfigService editorConfigService = new EditorConfigService(
                    Collections.singleton(sourceTreeRoot));
            final Validators parsers = Validators.scan(this.getClass().getClassLoader());

            handler.startFileSets();
            for (FileSet fileSet : fileSets) {
                Path fileSetDirectory = Paths.get(fileSet.getDirectory());
                String[] includedFiles = scan(fileSet);
                for (String includedPath : includedFiles) {
                    final Path file = fileSetDirectory.resolve(includedPath);
                    getLog().debug("Processing file " + file);
                    Validator parser = parsers.findParser(file);
                    OptionMap options = editorConfigService.getOptions(file);
                    Charset useEncoding = options.get(WellKnownKey.charset, charset);
                    final Resource resource = createResource(file, useEncoding);
                    ViolationHandler.ReturnState state = ViolationHandler.ReturnState.RECHECK;
                    while (state != ViolationHandler.ReturnState.FINISHED) {
                        handler.startFile(resource);
                        parser.process(resource, options, handler);
                        state = handler.endFile();
                    }
                }
            }
            handler.endFileSets();
        } catch (FormatException | EditorConfigException | IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

    }

    /**
     * A {@link DirectoryScanner} boiler plate.
     *
     * @param fileSet
     *            {@link FileSet} to scan
     * @return the included paths
     */
    private String[] scan(FileSet fileSet) {
        File basedir = new File(fileSet.getDirectory());
        if (!basedir.exists() || !basedir.isDirectory()) {
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner();

        @SuppressWarnings("unchecked")
        List<String> includes = fileSet.getIncludes();
        @SuppressWarnings("unchecked")
        List<String> excludes = fileSet.getExcludes();

        if (includes != null && includes.size() > 0) {
            scanner.setIncludes(includes.toArray(new String[0]));
        }

        if (excludes != null && excludes.size() > 0) {
            scanner.setExcludes(excludes.toArray(new String[0]));
        }

        scanner.setBasedir(basedir);

        scanner.scan();
        return scanner.getIncludedFiles();
    }

}
