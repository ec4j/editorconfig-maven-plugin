package org.ec4j.maven;

import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ec4j.maven.core.Resource;
import org.ec4j.maven.core.ViolationHandler;
import org.ec4j.maven.format.EditableDocument;
import org.ec4j.maven.format.FormattingHandler;

/**
 * Formats a set of files so that they comply with rules defined in {@code .editorconfig} files.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Mojo(defaultPhase = LifecyclePhase.NONE, name = "format", threadSafe = false)
public class FormatMojo extends AbstractEditorconfigMojo {
    /**
     * If {@code true}, a backup file will be created for every file that needs to be formatted just before the
     * formatted version is stored. If {@code false}, no backup is done and the files are formatted in place. See also
     * {@link #backupSuffix}.
     */
    @Parameter(property = "editorconfig.backup", defaultValue = "false")
    private boolean backup;

    /**
     * A suffix to append to a file name to create its backup. See also {@link #backup}.
     */
    @Parameter(property = "editorconfig.backupSuffix", defaultValue = ".bak")
    private boolean backupSuffix;

    @Override
    protected ViolationHandler createHandler() {
        return new FormattingHandler(backup, backupSuffix);
    }

    @Override
    protected Resource createResource(Path file, Charset encoding) {
        return new EditableDocument(file, encoding);
    }

}
