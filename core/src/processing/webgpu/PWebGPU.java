package processing.webgpu;

import processing.core.NativeLibrary;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static processing.ffi.processing_h.*;

/**
 * PWebGPU provides the native interface layer for libProcessing's WebGPU support.
 */
public class PWebGPU {

    static {
        ensureLoaded();
    }

    /**
     * Ensure the native library is loaded.
     */
    public static void ensureLoaded() {
        NativeLibrary.ensureLoaded();
    }

    /**
     * Initializes the WebGPU subsystem. Must be called before any other WebGPU methods.
     * This should be called from the same thread that will call update().
     */
    public static void init() {
        processing_init();
        checkError();
    }

    /**
     * Creates a WebGPU surface from a native window handle.
     *
     * @param windowHandle The native window handle
     * @param width Window width in physical pixels
     * @param height Window height in phsyical pixels
     * @param scaleFactor os provided scale factor
     * @return Window ID to use for subsequent operations
     */
    public static long createSurface(long windowHandle, int width, int height, float scaleFactor) {
        long windowId = processing_create_surface(windowHandle, width, height, scaleFactor);
        checkError();
        return windowId;
    }

    /**
     * Updates a window's size.
     *
     * @param windowId The window ID returned from createSurface
     * @param width New physical window width in pixels
     * @param height New physical window height in pixels
     */
    public static void windowResized(long windowId, int width, int height) {
        processing_window_resized(windowId, width, height);
        checkError();
    }

    /**
     * Updates the WebGPU subsystem. Should be called once per frame after all drawing is complete.
     */
    public static void update() {
        processing_update();
        checkError();
    }

    /**
     * Cleans up the WebGPU subsystem. Should be called on application exit.
     */
    public static void exit() {
        processing_exit((byte) 0);
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
