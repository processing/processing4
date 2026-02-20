// temporary band-aid class to support modes which are still looking here for the now-refactored class.
//  - josh giesbrecht

package processing.app;

@Deprecated
// please migrate to using processing.utils.SketchException instead! all class functionality is the same as before.
public class SketchException extends processing.utils.SketchException {

    // Idea complained without all these super wrappers for constructors. ¯\_(ツ)_/¯ sure, why not?
    public SketchException(String message) {
        super(message);
    }

    public SketchException(String message, boolean showStackTrace) {
        super(message, showStackTrace);
    }

    public SketchException(String message, int file, int line) {
        super(message, file, line);
    }

    public SketchException(String message, int file, int line, int column) {
        super(message, file, line, column);
    }

    public SketchException(String message, int file, int line, int column, boolean showStackTrace) {
        super(message, file, line, column, showStackTrace);
    }
}
