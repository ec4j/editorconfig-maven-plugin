package org.l2x6.editorconfig.core;

import java.io.IOException;
import java.nio.file.Path;

import org.l2x6.editorconfig.core.EditorConfigService.OptionMap;

public interface Validator {
    boolean canParse(Path path);

    /**
     * Checks the formatting of the given {@code file}. The file is read using the given {@code encoding} and the
     * violations are reported to the given {@code violationHandler}.
     *
     * @param resource
     * @param options
     * @param violationHandler
     * @throws IOException
     */
    void process(Resource resource, OptionMap options, ViolationHandler violationHandler) throws IOException;
}
