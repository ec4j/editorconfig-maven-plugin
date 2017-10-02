package org.l2x6.editorconfig.core;

/**
 * An interface for reporting {@link Violation}s.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface ViolationHandler
{
    public enum ReturnState {
        FINISHED, RECHECK;
    }

    ReturnState endFile();
    void endFileSets();
    /**
     * Called when an {@link Violation} is found.
     *
     * @param violation the reported violation
     */
    void handle( Violation violation );
    void startFile(Resource resource);
    void startFileSets();
}