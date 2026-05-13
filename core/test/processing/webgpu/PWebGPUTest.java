package processing.webgpu;

import org.junit.Test;

/**
 * Tests for the PWebGPU native interface.
 */
public class PWebGPUTest {
    @Test
    public void itLoads() {
        PWebGPU.ensureLoaded();
    }
}
