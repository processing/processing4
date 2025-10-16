package processing.webgpu;

import processing.core.NativeLibrary;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static processing.ffi.processing_h.processing_check_error;
import static processing.ffi.processing_h.processing_init;

/**
 * PWebGPU provides the native interface layer for libProcessing's WebGPU support.
 */
public class PWebGPU {

    static {
        ensureLoaded();
        init();
    }

    /**
     * Ensure the native library is loaded.
     */
    public static void ensureLoaded() {
        NativeLibrary.ensureLoaded();
    }

    /**
     * Initializes the WebGPU subsystem. Must be called before any other WebGPU methods.
     */
    public static void init() {
        processing_init();
        checkError();
    }

    /**
     * Checks for errors from the native library and throws a PWebGPUException if an error occurred.
     */
    private static void checkError() {
        MemorySegment ret = processing_check_error();
        if (ret.equals(NULL)) {
            return;
        }

        String errorMsg = ret.getString(0);
        if (errorMsg != null && !errorMsg.isEmpty()) {
            throw new PWebGPUException(errorMsg);
        }
    }
}
