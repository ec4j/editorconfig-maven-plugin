package org.l2x6.editorconfig.format;

public interface Edit
{
    void fix( EditableDocument document, int offset );
    String getMessage();
}
