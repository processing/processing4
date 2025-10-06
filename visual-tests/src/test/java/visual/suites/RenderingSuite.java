package visual.suites;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Rendering Tests")
@SelectPackages("processing.test.visual.rendering")
@IncludeTags("rendering")
public class RenderingSuite {
    // Empty class - just holds annotations
}
