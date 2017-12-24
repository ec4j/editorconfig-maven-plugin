package org.ec4j.maven.format;

public interface Edit
{
    void fix( EditableDocument document, int offset );
    String getMessage();
}
