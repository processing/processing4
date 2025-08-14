// temporary band-aid class to support modes which are still looking here for the now-refactored class.
// (would be good to delete this file once all modes are updated to use processing.utils.SketchException)
//  - josh giesbrecht

package processing.app;

// extends the refactored class, to defer functionality there and also to hopefully play nicely when
// exception-handling goes to see if this is a processing.utils.SketchException or not!
public class SketchException extends processing.utils.SketchException {

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