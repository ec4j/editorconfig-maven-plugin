package org.ec4j.maven.core;

/**
 * A location in a document.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Location
{
    private static final Location INITIAL = new Location( 1, 1 );

    public static Location initial()
    {
        return INITIAL;
    }

    private final int column;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + line;
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
        Location other = (Location) obj;
        if ( column != other.column )
            return false;
        if ( line != other.line )
            return false;
        return true;
    }

    private final int line;

    public Location( int line, int column )
    {
        super();
        this.line = line;
        this.column = column;
    }

    /**
     * @return a column number, the first column number is 1
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * @return a line number, the first line number is 1
     */
    public int getLine()
    {
        return line;
    }

    @Override
    public String toString()
    {
        return "" + line + ","+ column;
    }

    /**
     * Shift left (if {@code count}) is negative or right.
     *
     * @param count the number of characters to move
     *
     * @return {@code this} if {@code count} is {@code 0} or a new {@link Location}
     */
    public Location shift( int count )
    {
        if (count == 0) {
            return this;
        } else {
            return new Location( line, column + count);
        }
    }
}
