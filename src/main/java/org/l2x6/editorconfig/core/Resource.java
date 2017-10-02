package org.l2x6.editorconfig.core;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Reader} source.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Resource
{
    protected final Charset encoding;

    protected final Path file;

    public Resource( Path file, Charset encoding )
    {
        super();
        this.file = file;
        this.encoding = encoding;
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
        Resource other = (Resource) obj;
        if ( encoding == null )
        {
            if ( other.encoding != null )
                return false;
        }
        else if ( !encoding.equals( other.encoding ) )
            return false;
        if ( file == null )
        {
            if ( other.file != null )
                return false;
        }
        else if ( !file.equals( other.file ) )
            return false;
        return true;
    }

    public Path getFile()
    {
        return file;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( encoding == null ) ? 0 : encoding.hashCode() );
        result = prime * result + ( ( file == null ) ? 0 : file.hashCode() );
        return result;
    }

    public Reader openReader()
        throws IOException
    {
        return Files.newBufferedReader(file, encoding);
    }

    @Override
    public String toString()
    {
        return file.toString();
    }

}
