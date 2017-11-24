package org.l2x6.editorconfig.format;

import org.ec4j.core.model.PropertyType;

public class Delete
    implements Edit
{

    private final int length;

    public Delete( int length )
    {
        super();
        this.length = length;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Delete other = (Delete) obj;
        if ( length != other.length )
            return false;
        return true;
    }

    @Override
    public void fix( EditableDocument document, int offset )
    {
        document.delete( offset, offset + length );
    }

    @Override
    public String getMessage()
    {
        return "Delete "+ length + " "+ (length == 1 ? "character." : "characters.");
    }

}
