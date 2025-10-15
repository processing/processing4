package processing.webgpu;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the PWebGPU native interface.
 */
public class PWebGPUTest {
    @Test
    public void testAddFunction() {
        long result = PWebGPU.add(2, 2);
        assertEquals("2 + 2 should equal 4", 4L, result);
    }
}
