package org.ec4j.maven;

public interface ValidatorConfig {
    String getClassName();

    String[] getExcludes();

    String[] getIncludes();

    boolean isUseDefaultIncludesAndExcludes();
}
