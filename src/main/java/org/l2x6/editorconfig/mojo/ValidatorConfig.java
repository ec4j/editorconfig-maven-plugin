package org.l2x6.editorconfig.mojo;

public interface ValidatorConfig {
    String getClassName();

    String[] getExcludes();

    String[] getIncludes();

    boolean isUseDefaultIncludesAndExcludes();
}
