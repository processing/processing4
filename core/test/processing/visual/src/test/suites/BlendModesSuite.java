package processing.visual.src.test.suites;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Blend Modes Visual Tests")
@SelectPackages("processing.visual.src.test.blendmodes")
@ExcludePackages("processing.visual.src.test.suites")
@IncludeTags("blend-modes")
public class BlendModesSuite {
    // Empty class - just holds annotations
}