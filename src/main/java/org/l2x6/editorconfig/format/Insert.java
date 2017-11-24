package org.l2x6.editorconfig.format;

import org.ec4j.core.model.PropertyType;

import java.util.Arrays;

public class Insert implements Edit {
    public static Insert endOfLine(PropertyType.EndOfLineValue eol) {
        return new Insert(eol.getEndOfLineString(), "Insert " + eol.name() + ".");
    }

    public static Insert repeat(char ch, int count) {
        char[] insertion = new char[count];
        Arrays.fill(insertion, ch);
        final String ofWhat;
        switch (ch) {
            case ' ':
                ofWhat = count == 1 ? "space" : "spaces";
                break;
            case '\t':
                ofWhat = count == 1 ? "tab" : "tabs";
                break;
            default:
                ofWhat = count == 1 ? ("'" + ch + "' character") : ("'" + ch + "' characters");
                break;
        }
        return new Insert(new String(insertion), "Insert " + count + " " + ofWhat + ".");
    }

    private final CharSequence insertion;
    private final String message;

    public Insert(CharSequence insertion, String message) {
        super();
        this.insertion = insertion;
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Insert other = (Insert) obj;
        if (insertion == null) {
            if (other.insertion != null)
                return false;
        } else if (!insertion.equals(other.insertion))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

    @Override
    public void fix(EditableDocument document, int offset) {
        document.insert(offset, insertion);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((insertion == null) ? 0 : insertion.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

}
