package org.l2x6.editorconfig.mojo;

import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.l2x6.editorconfig.check.ViolationCollector;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.ViolationHandler;

/**
 * Checks whether files are formatted according to rules defined in {@code .editorconfig} files.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Mojo( defaultPhase = LifecyclePhase.PROCESS_SOURCES, name = "check" )
public class CheckMojo
    extends AbstractEditorconfigMojo
{

    /**
     * Tells the mojo what to do in case formatting violations are found. if {@code true}, all violations will be
     * reported on the console as ERRORs and the build will fail. if {@code false}, all violations will be reported on
     * the console as WARNs and the build will proceed further.
     */
    @Parameter( property = "editorconfig.failOnFormatViolation", defaultValue = "true" )
    private boolean failOnFormatViolation;

    @Override
    protected ViolationHandler createHandler()
    {
        return new ViolationCollector(failOnFormatViolation);
    }

    @Override
    protected Resource createResource(Path file, Charset encoding)
    {
        return new Resource( file, encoding );
    }

}
