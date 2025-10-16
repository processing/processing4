package processing.webgpu;

import processing.core.NativeLibrary;
import processing.ffi.processing_h;

/**
 * PWebGPU provides the native interface layer for libProcessing's WebGPU support.
 */
public class PWebGPU {

    static {
        NativeLibrary.ensureLoaded();
    }

    /**
     * Ensure the native library is loaded.
     */
    public static void ensureLoaded() {
        NativeLibrary.ensureLoaded();
    }

    /**
     * It's just math, silly!
     */
    public static long add(long left, long right) {
        return processing_h.processing_add(left, right);
    }
}
