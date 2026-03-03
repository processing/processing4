package processing.visual.src.test.suites;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Typography Visual Tests")
@SelectPackages("processing.visual.src.test.typography")
@ExcludePackages("processing.visual.src.test.suites")
@IncludeTags("typography")
public class TypographySuite {
    // Empty class - just holds annotations
}