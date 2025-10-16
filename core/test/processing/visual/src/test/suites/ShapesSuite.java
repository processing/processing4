package processing.visual.src.test.suites;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Basic Shapes Visual Tests")
@SelectPackages("processing.visual.src.test.shapes")
@ExcludePackages("processing.visual.src.test.suites")
@IncludeTags("shapes")
public class ShapesSuite {
    // Empty class - just holds annotations
}