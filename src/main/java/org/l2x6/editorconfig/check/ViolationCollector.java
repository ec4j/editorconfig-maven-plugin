package org.l2x6.editorconfig.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.l2x6.editorconfig.core.FormatException;
import org.l2x6.editorconfig.core.Resource;
import org.l2x6.editorconfig.core.Violation;
import org.l2x6.editorconfig.core.ViolationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViolationCollector
    implements ViolationHandler
{
    private static final Logger log = LoggerFactory.getLogger(ViolationCollector.class);

    private Resource currentFile;
    private final boolean failOnFormatViolation;
    private int processedFileCount = 0;
    private final Map<Resource, List<Violation>> violations =
        new LinkedHashMap<Resource, List<Violation>>();

    public ViolationCollector( boolean failOnFormatViolation )
    {
        super();
        this.failOnFormatViolation = failOnFormatViolation;
    }

    @Override
    public ReturnState endFile()
    {
        if ( log.isDebugEnabled() && !hasViolations( currentFile ) )
        {
            log.debug( "No formatting violations found in file " + currentFile );
        }
        this.currentFile = null;
        processedFileCount++;
        return ReturnState.FINISHED;
    }

    /**
     *
     */
    @Override
    public void endFiles()
    {
        log.info( "Processed " + processedFileCount + (processedFileCount == 1 ? " file" : " files"));
        if ( failOnFormatViolation && hasViolations() )
        {
            throw new FormatException( "There are XML formatting violations. Check the above log for details. You may want to run mvn xml:format" );
        }
    }

    public Map<Resource, List<Violation>> getViolations()
    {
        return Collections.unmodifiableMap( violations );
    }

    @Override
    public void handle( Violation violation )
    {
        List<Violation> list = violations.get( violation.getFile() );
        if ( list == null )
        {
            list = new ArrayList<Violation>();
            violations.put( violation.getFile(), list );
        }
        list.add( violation );
        if ( failOnFormatViolation )
        {
            log.error( violation.toString() );
        }
        else
        {
            log.warn( violation.toString() );
        }
    }

    public boolean hasViolations()
    {
        return !violations.isEmpty();
    }

    public boolean hasViolations( Resource file )
    {
        List<Violation> list = violations.get( file );
        return list != null && !list.isEmpty();
    }

    @Override
    public void startFile(Resource file)
    {
        this.currentFile = file;
    }

    @Override
    public void startFiles()
    {
        processedFileCount = 0;
    }

}