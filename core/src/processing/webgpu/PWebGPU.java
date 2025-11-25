package processing.webgpu;

import processing.core.NativeLibrary;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static processing.ffi.processing_h.*;
import processing.ffi.Color;

/**
 * PWebGPU provides the native interface layer for libprocessing's WebGPU support.
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
     * @param displayHandle The native display handle
     * @param width Window width in physical pixels
     * @param height Window height in phsyical pixels
     * @param scaleFactor os provided scale factor
     * @return Window ID to use for subsequent operations
     */
    public static long createSurface(long windowHandle, long displayHandle, int width, int height, float scaleFactor) {
        long surfaceId = processing_create_surface(windowHandle, displayHandle, width, height, scaleFactor);
        checkError();
        return surfaceId;
    }

    /**
     * Destroys a WebGPU surface.
     *
     * @param surfaceId The window ID returned from createSurface
     */
    public static void destroySurface(long surfaceId) {
        processing_destroy_surface(surfaceId);
        checkError();
    }

    /**
     * Updates a window's size.
     *
     * @param surfaceId The window ID returned from createSurface
     * @param width New physical window width in pixels
     * @param height New physical window height in pixels
     */
    public static void windowResized(long surfaceId, int width, int height) {
        processing_resize_surface(surfaceId, width, height);
        checkError();
    }

    
    public static void beginDraw(long surfaceId) {
        processing_begin_draw(surfaceId);
        checkError();
    }

    public static void flush(long surfaceId) {
        processing_flush(surfaceId);
        checkError();
    }

    public static void endDraw(long surfaceId) {
        processing_end_draw(surfaceId);
        checkError();
    }

    /**
     * Cleans up the WebGPU subsystem. Should be called on application exit.
     */
    public static void exit() {
        processing_exit((byte) 0);
        checkError();
    }

    public static void backgroundColor(long surfaceId, float r, float g, float b, float a) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = Color.allocate(arena);

            Color.r(color, r);
            Color.g(color, g);
            Color.b(color, b);
            Color.a(color, a);

            processing_background_color(surfaceId, color);
            checkError();
        }
    }

    /**
     * Set the fill color.
     */
    public static void setFill(long surfaceId, float r, float g, float b, float a) {
        processing_set_fill(surfaceId, r, g, b, a);
        checkError();
    }

    /**
     * Set the stroke color.
     */
    public static void setStrokeColor(long surfaceId, float r, float g, float b, float a) {
        processing_set_stroke_color(surfaceId, r, g, b, a);
        checkError();
    }

    /**
     * Set the stroke weight.
     */
    public static void setStrokeWeight(long surfaceId, float weight) {
        processing_set_stroke_weight(surfaceId, weight);
        checkError();
    }

    /**
     * Disable fill for subsequent shapes.
     */
    public static void noFill(long surfaceId) {
        processing_no_fill(surfaceId);
        checkError();
    }

    /**
     * Disable stroke for subsequent shapes.
     */
    public static void noStroke(long surfaceId) {
        processing_no_stroke(surfaceId);
        checkError();
    }

    /**
     * Draw a rectangle.
     */
    public static void rect(long surfaceId, float x, float y, float w, float h,
                           float tl, float tr, float br, float bl) {
        processing_rect(surfaceId, x, y, w, h, tl, tr, br, bl);
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
