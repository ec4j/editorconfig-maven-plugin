package org.l2x6.editorconfig.core;

import java.io.IOException;
import java.util.List;

import org.ec4j.core.ResourceProperties;

public interface Validator {

    List<String> getDefaultIncludes();
    List<String> getDefaultExcludes();

    /**
     * Checks the formatting of the given {@code file}. The file is read using the given {@code encoding} and the
     * violations are reported to the given {@code violationHandler}.
     *
     * @param resource
     * @param properties
     * @param violationHandler
     * @throws IOException
     */
    void process(Resource resource, ResourceProperties properties, ViolationHandler violationHandler) throws IOException;
}
