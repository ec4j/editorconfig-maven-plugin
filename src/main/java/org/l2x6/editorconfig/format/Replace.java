package org.l2x6.editorconfig.format;

import org.ec4j.core.model.PropertyType;

public class Replace implements Edit {
    private final String replacement;
    private final String message;
    private final int replacedLength;

    public static Replace endOfLine(PropertyType.EndOfLineValue replaced, PropertyType.EndOfLineValue replacement) {
        return new Replace(replaced.getEndOfLineString().length(), replacement.getEndOfLineString(), "Replace '" + replaced.name() + "' with '" + replacement.name() + "'.");
    }

    public static Replace ofReplaced(String replaced, String replacement) {
        return new Replace(replaced.length(), replacement, "Replace '" + replaced + "' with '" + replacement + "'.");
    }

    public Replace(int replacedLength, String replacement, String message) {
        super();
        this.replacement = replacement;
        this.message = message;
        this.replacedLength = replacedLength;
    }


    @Override
    public void fix(EditableDocument document, int offset) {
        document.replace(offset, offset + replacedLength, replacement);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Replace replace = (Replace) o;

        if (replacedLength != replace.replacedLength) return false;
        if (replacement != null ? !replacement.equals(replace.replacement) : replace.replacement != null) return false;
        return message != null ? message.equals(replace.message) : replace.message == null;
    }

    @Override
    public int hashCode() {
        int result = replacement != null ? replacement.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + replacedLength;
        return result;
    }
}
