package processing.app.gradle;

import processing.app.Mode;
import processing.app.Sketch;
import processing.app.ui.Editor;

import java.io.PrintStream;

public class GradleService {
    public GradleService(Mode mode, Editor editor) { }

    public void setEnabled(boolean enabled) {}
    public boolean getEnabled() { return false; }
    public void prepare(){}
    public void run() {}
    public void export(){}
    public void stop() {}
    public void startService() {}
    public void setSketch(Sketch sketch) {}
    public void setErr(PrintStream err) {}
    public void setOut(PrintStream out) {}
}
