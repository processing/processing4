package processing.visual.src.test.suites;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Rendering Tests")
@SelectPackages("processing.visual.src.test.rendering")
@ExcludePackages("processing.visual.src.test.suites")
@IncludeTags("rendering")
public class RenderingSuite {
    // Empty class - just holds annotations
}
