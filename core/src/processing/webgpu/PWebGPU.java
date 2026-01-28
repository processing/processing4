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
        long surfaceId = processing_surface_create(windowHandle, displayHandle, width, height, scaleFactor);
        checkError();
        return surfaceId;
    }

    /**
     * Destroys a WebGPU surface.
     *
     * @param surfaceId The window ID returned from createSurface
     */
    public static void destroySurface(long surfaceId) {
        processing_surface_destroy(surfaceId);
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
        processing_surface_resize(surfaceId, width, height);
        checkError();
    }

    /**
     * Creates a graphics context for the given surface.
     * This must be called after createSurface before any drawing operations.
     *
     * @param surfaceId The surface ID returned from createSurface
     * @param width Graphics width in pixels
     * @param height Graphics height in pixels
     * @return Graphics ID to use for drawing operations
     */
    public static long graphicsCreate(long surfaceId, int width, int height) {
        long graphicsId = processing_graphics_create(surfaceId, width, height);
        checkError();
        return graphicsId;
    }

    public static void beginDraw(long graphicsId) {
        processing_begin_draw(graphicsId);
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
     * Push the current transformation matrix onto the stack.
     */
    public static void pushMatrix(long surfaceId) {
        processing_push_matrix(surfaceId);
        checkError();
    }

    /**
     * Pop the transformation matrix from the stack.
     */
    public static void popMatrix(long surfaceId) {
        processing_pop_matrix(surfaceId);
        checkError();
    }

    /**
     * Reset the transformation matrix to identity.
     */
    public static void resetMatrix(long surfaceId) {
        processing_reset_matrix(surfaceId);
        checkError();
    }

    /**
     * Translate the coordinate system.
     */
    public static void translate(long surfaceId, float x, float y) {
        processing_translate(surfaceId, x, y);
        checkError();
    }

    /**
     * Rotate around the X axis.
     */
    public static void rotateX(long surfaceId, float angle) {
        processing_rotate_x(surfaceId, angle);
        checkError();
    }

    /**
     * Rotate around the Y axis.
     */
    public static void rotateY(long surfaceId, float angle) {
        processing_rotate_y(surfaceId, angle);
        checkError();
    }

    /**
     * Rotate around the Z axis.
     */
    public static void rotateZ(long surfaceId, float angle) {
        processing_rotate_z(surfaceId, angle);
        checkError();
    }

    /**
     * Rotate the coordinate system.
     */
    public static void rotate(long surfaceId, float angle) {
        rotateZ(surfaceId, angle);
    }

    /**
     * Scale the coordinate system.
     */
    public static void scale(long surfaceId, float x, float y) {
        processing_scale(surfaceId, x, y);
        checkError();
    }

    /**
     * Shear along the X axis.
     */
    public static void shearX(long surfaceId, float angle) {
        processing_shear_x(surfaceId, angle);
        checkError();
    }

    /**
     * Shear along the Y axis.
     */
    public static void shearY(long surfaceId, float angle) {
        processing_shear_y(surfaceId, angle);
        checkError();
    }

    /**
     * Create an image from raw pixel data.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param data RGBA pixel data (4 bytes per pixel)
     * @return Image ID for subsequent operations, or 0 on failure
     */
    public static long imageCreate(int width, int height, byte[] data) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment dataSegment = arena.allocateFrom(java.lang.foreign.ValueLayout.JAVA_BYTE, data);
            long imageId = processing_image_create(width, height, dataSegment, data.length);
            checkError();
            return imageId;
        }
    }

    /**
     * Load an image from a file path.
     *
     * @param path Path to the image file
     * @return Image ID for subsequent operations, or 0 on failure
     */
    public static long imageLoad(String path) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pathSegment = arena.allocateFrom(path);
            long imageId = processing_image_load(pathSegment);
            checkError();
            return imageId;
        }
    }

    /**
     * Resize an image.
     *
     * @param imageId The image ID returned from imageCreate or imageLoad
     * @param newWidth New width in pixels
     * @param newHeight New height in pixels
     */
    public static void imageResize(long imageId, int newWidth, int newHeight) {
        processing_image_resize(imageId, newWidth, newHeight);
        checkError();
    }

    /**
     * Read back pixel data from an image into a buffer.
     *
     * @param imageId The image ID returned from imageCreate or imageLoad
     * @param buffer Buffer to receive Color data (must be width * height elements)
     */
    public static void imageReadback(long imageId, float[] buffer) {
        try (Arena arena = Arena.ofConfined()) {
            int numPixels = buffer.length / 4;
            MemorySegment colorBuffer = Color.allocateArray(numPixels, arena);
            processing_image_readback(imageId, colorBuffer, numPixels);
            checkError();

            for (int i = 0; i < numPixels; i++) {
                MemorySegment color = Color.asSlice(colorBuffer, i);
                buffer[i * 4] = Color.r(color);
                buffer[i * 4 + 1] = Color.g(color);
                buffer[i * 4 + 2] = Color.b(color);
                buffer[i * 4 + 3] = Color.a(color);
            }
        }
    }

    /**
     * Set the background to an image.
     *
     * @param surfaceId The surface ID
     * @param imageId The image ID to use as background
     */
    public static void backgroundImage(long surfaceId, long imageId) {
        processing_background_image(surfaceId, imageId);
        checkError();
    }

    /**
     * Switch to 3D rendering mode.
     */
    public static void mode3d(long surfaceId) {
        processing_mode_3d(surfaceId);
        checkError();
    }

    /**
     * Switch to 2D rendering mode.
     */
    public static void mode2d(long surfaceId) {
        processing_mode_2d(surfaceId);
        checkError();
    }

    /**
     * Set the camera position.
     */
    public static void cameraPosition(long surfaceId, float x, float y, float z) {
        processing_camera_position(surfaceId, x, y, z);
        checkError();
    }

    /**
     * Set where the camera is looking at.
     */
    public static void cameraLookAt(long surfaceId, float targetX, float targetY, float targetZ) {
        processing_camera_look_at(surfaceId, targetX, targetY, targetZ);
        checkError();
    }

    /**
     * Set perspective projection.
     */
    public static void perspective(long surfaceId, float fov, float aspect, float near, float far) {
        processing_perspective(surfaceId, fov, aspect, near, far);
        checkError();
    }

    /**
     * Set orthographic projection.
     */
    public static void ortho(long surfaceId, float left, float right, float bottom, float top, float near, float far) {
        processing_ortho(surfaceId, left, right, bottom, top, near, far);
        checkError();
    }

    /** Topology constants */
    public static final byte TOPOLOGY_POINT_LIST = 0;
    public static final byte TOPOLOGY_LINE_LIST = 1;
    public static final byte TOPOLOGY_LINE_STRIP = 2;
    public static final byte TOPOLOGY_TRIANGLE_LIST = 3;
    public static final byte TOPOLOGY_TRIANGLE_STRIP = 4;

    /** Attribute format constants */
    public static final byte ATTR_FORMAT_FLOAT = 1;
    public static final byte ATTR_FORMAT_FLOAT2 = 2;
    public static final byte ATTR_FORMAT_FLOAT3 = 3;
    public static final byte ATTR_FORMAT_FLOAT4 = 4;

    public static long geometryLayoutCreate() {
        long layoutId = processing_geometry_layout_create();
        checkError();
        return layoutId;
    }

    public static void geometryLayoutAddPosition(long layoutId) {
        processing_geometry_layout_add_position(layoutId);
        checkError();
    }

    public static void geometryLayoutAddNormal(long layoutId) {
        processing_geometry_layout_add_normal(layoutId);
        checkError();
    }

    public static void geometryLayoutAddColor(long layoutId) {
        processing_geometry_layout_add_color(layoutId);
        checkError();
    }

    public static void geometryLayoutAddUv(long layoutId) {
        processing_geometry_layout_add_uv(layoutId);
        checkError();
    }

    public static void geometryLayoutAddAttribute(long layoutId, long attrId) {
        processing_geometry_layout_add_attribute(layoutId, attrId);
        checkError();
    }

    public static void geometryLayoutDestroy(long layoutId) {
        processing_geometry_layout_destroy(layoutId);
        checkError();
    }

    public static long geometryCreate(byte topology) {
        long geoId = processing_geometry_create(topology);
        checkError();
        return geoId;
    }

    public static long geometryCreateWithLayout(long layoutId, byte topology) {
        long geoId = processing_geometry_create_with_layout(layoutId, topology);
        checkError();
        return geoId;
    }

    public static long geometryBox(float width, float height, float depth) {
        long geoId = processing_geometry_box(width, height, depth);
        checkError();
        return geoId;
    }

    public static void geometryDestroy(long geoId) {
        processing_geometry_destroy(geoId);
        checkError();
    }

    public static void geometryNormal(long geoId, float nx, float ny, float nz) {
        processing_geometry_normal(geoId, nx, ny, nz);
        checkError();
    }

    public static void geometryColor(long geoId, float r, float g, float b, float a) {
        processing_geometry_color(geoId, r, g, b, a);
        checkError();
    }

    public static void geometryUv(long geoId, float u, float v) {
        processing_geometry_uv(geoId, u, v);
        checkError();
    }

    public static void geometryVertex(long geoId, float x, float y, float z) {
        processing_geometry_vertex(geoId, x, y, z);
        checkError();
    }

    public static void geometryIndex(long geoId, int i) {
        processing_geometry_index(geoId, i);
        checkError();
    }

    public static long geometryAttributeCreate(String name, byte format) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nameSegment = arena.allocateFrom(name);
            long attrId = processing_geometry_attribute_create(nameSegment, format);
            checkError();
            return attrId;
        }
    }

    public static void geometryAttributeDestroy(long attrId) {
        processing_geometry_attribute_destroy(attrId);
        checkError();
    }

    public static long geometryAttributePosition() {
        return processing_geometry_attribute_position();
    }

    public static long geometryAttributeNormal() {
        return processing_geometry_attribute_normal();
    }

    public static long geometryAttributeColor() {
        return processing_geometry_attribute_color();
    }

    public static long geometryAttributeUv() {
        return processing_geometry_attribute_uv();
    }

    public static void geometryAttributeFloat(long geoId, long attrId, float v) {
        processing_geometry_attribute_float(geoId, attrId, v);
        checkError();
    }

    public static void geometryAttributeFloat2(long geoId, long attrId, float x, float y) {
        processing_geometry_attribute_float2(geoId, attrId, x, y);
        checkError();
    }

    public static void geometryAttributeFloat3(long geoId, long attrId, float x, float y, float z) {
        processing_geometry_attribute_float3(geoId, attrId, x, y, z);
        checkError();
    }

    public static void geometryAttributeFloat4(long geoId, long attrId, float x, float y, float z, float w) {
        processing_geometry_attribute_float4(geoId, attrId, x, y, z, w);
        checkError();
    }

    public static int geometryVertexCount(long geoId) {
        int count = processing_geometry_vertex_count(geoId);
        checkError();
        return count;
    }

    public static int geometryIndexCount(long geoId) {
        int count = processing_geometry_index_count(geoId);
        checkError();
        return count;
    }

    public static void geometrySetVertex(long geoId, int index, float x, float y, float z) {
        processing_geometry_set_vertex(geoId, index, x, y, z);
        checkError();
    }

    public static void geometrySetNormal(long geoId, int index, float nx, float ny, float nz) {
        processing_geometry_set_normal(geoId, index, nx, ny, nz);
        checkError();
    }

    public static void geometrySetColor(long geoId, int index, float r, float g, float b, float a) {
        processing_geometry_set_color(geoId, index, r, g, b, a);
        checkError();
    }

    public static void geometrySetUv(long geoId, int index, float u, float v) {
        processing_geometry_set_uv(geoId, index, u, v);
        checkError();
    }

    public static void model(long surfaceId, long geoId) {
        processing_model(surfaceId, geoId);
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
