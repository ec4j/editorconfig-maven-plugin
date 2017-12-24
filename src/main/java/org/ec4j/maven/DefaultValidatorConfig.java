package org.ec4j.maven;

public class DefaultValidatorConfig implements ValidatorConfig {
    private String className;
    private String[] excludes;
    private String[] includes;
    private boolean useDefaultIncludesAndExcludes = true;

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }

    @Override
    public String[] getIncludes() {
        return includes;
    }

    @Override
    public boolean isUseDefaultIncludesAndExcludes() {
        return useDefaultIncludesAndExcludes;
    }

    public void setClassName(String class_) {
        this.className = class_;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public void setUseDefaultIncludesAndExcludes(boolean useDefaultIncludesAndExcludes) {
        this.useDefaultIncludesAndExcludes = useDefaultIncludesAndExcludes;
    }
}
