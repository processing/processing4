package processing.webgpu;

import processing.core.NativeLibrary;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static processing.ffi.processing_h.*;
import processing.ffi.Color;

public class PWebGPU {

    static {
        ensureLoaded();
    }

    public static void ensureLoaded() {
        NativeLibrary.ensureLoaded();
    }

    // ── Init / lifecycle ────────────────────────────────────────────────

    public static void init() {
        processing_init();
        checkError();
    }

    public static void exit() {
        processing_exit((byte) 0);
        checkError();
    }

    // ── Surface ─────────────────────────────────────────────────────────

    public static long createSurface(long windowHandle, long displayHandle, int width, int height, float scaleFactor) {
        long surfaceId = processing_surface_create(windowHandle, displayHandle, width, height, scaleFactor);
        checkError();
        return surfaceId;
    }

    public static void destroySurface(long surfaceId) {
        processing_surface_destroy(surfaceId);
        checkError();
    }

    public static void windowResized(long surfaceId, int width, int height) {
        processing_surface_resize(surfaceId, width, height);
        checkError();
    }

    // ── Graphics context ────────────────────────────────────────────────

    public static long graphicsCreate(long surfaceId, int width, int height) {
        long graphicsId = processing_graphics_create(surfaceId, width, height);
        checkError();
        return graphicsId;
    }

    public static void graphicsDestroy(long graphicsId) {
        processing_graphics_destroy(graphicsId);
        checkError();
    }

    public static void beginDraw(long graphicsId) {
        processing_begin_draw(graphicsId);
        checkError();
    }

    public static void flush(long graphicsId) {
        processing_flush(graphicsId);
        checkError();
    }

    public static void endDraw(long graphicsId) {
        processing_end_draw(graphicsId);
        checkError();
    }

    // ── Background ──────────────────────────────────────────────────────

    public static void backgroundColor(long graphicsId, float r, float g, float b, float a) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = allocateColor(arena, r, g, b, a);
            processing_background_color(graphicsId, color);
            checkError();
        }
    }

    public static void backgroundImage(long graphicsId, long imageId) {
        processing_background_image(graphicsId, imageId);
        checkError();
    }

    // ── Color mode ──────────────────────────────────────────────────────

    public static final byte COLOR_SPACE_SRGB = 0;
    public static final byte COLOR_SPACE_HSB = 1;
    public static final byte COLOR_SPACE_LINEAR = 2;

    public static void colorMode(long graphicsId, byte space, float max1, float max2, float max3, float maxAlpha) {
        processing_color_mode(graphicsId, space, max1, max2, max3, maxAlpha);
        checkError();
    }

    // ── Fill / stroke ───────────────────────────────────────────────────

    public static void setFill(long graphicsId, float r, float g, float b, float a) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = allocateColor(arena, r, g, b, a);
            processing_set_fill(graphicsId, color);
            checkError();
        }
    }

    public static void setStrokeColor(long graphicsId, float r, float g, float b, float a) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = allocateColor(arena, r, g, b, a);
            processing_set_stroke_color(graphicsId, color);
            checkError();
        }
    }

    public static void setStrokeWeight(long graphicsId, float weight) {
        processing_set_stroke_weight(graphicsId, weight);
        checkError();
    }

    public static void noFill(long graphicsId) {
        processing_no_fill(graphicsId);
        checkError();
    }

    public static void noStroke(long graphicsId) {
        processing_no_stroke(graphicsId);
        checkError();
    }

    // ── Stroke style ────────────────────────────────────────────────────

    public static final byte STROKE_CAP_ROUND = 0;
    public static final byte STROKE_CAP_SQUARE = 1;
    public static final byte STROKE_CAP_PROJECT = 2;

    public static final byte STROKE_JOIN_ROUND = 0;
    public static final byte STROKE_JOIN_MITER = 1;
    public static final byte STROKE_JOIN_BEVEL = 2;

    public static void setStrokeCap(long graphicsId, byte cap) {
        processing_set_stroke_cap(graphicsId, cap);
        checkError();
    }

    public static void setStrokeJoin(long graphicsId, byte join) {
        processing_set_stroke_join(graphicsId, join);
        checkError();
    }

    // ── Shape modes ─────────────────────────────────────────────────────

    public static void rectMode(long graphicsId, byte mode) {
        processing_rect_mode(graphicsId, mode);
        checkError();
    }

    public static void ellipseMode(long graphicsId, byte mode) {
        processing_ellipse_mode(graphicsId, mode);
        checkError();
    }

    // ── Blend modes ─────────────────────────────────────────────────────

    public static final byte BLEND_MODE_BLEND = 0;
    public static final byte BLEND_MODE_ADD = 1;
    public static final byte BLEND_MODE_SUBTRACT = 2;
    public static final byte BLEND_MODE_DARKEST = 3;
    public static final byte BLEND_MODE_LIGHTEST = 4;
    public static final byte BLEND_MODE_DIFFERENCE = 5;
    public static final byte BLEND_MODE_EXCLUSION = 6;
    public static final byte BLEND_MODE_MULTIPLY = 7;
    public static final byte BLEND_MODE_SCREEN = 8;
    public static final byte BLEND_MODE_REPLACE = 9;

    public static void setBlendMode(long graphicsId, byte mode) {
        processing_set_blend_mode(graphicsId, mode);
        checkError();
    }

    // ── 2D drawing matrix ───────────────────────────────────────────────

    public static void pushMatrix(long graphicsId) {
        processing_push_matrix(graphicsId);
        checkError();
    }

    public static void popMatrix(long graphicsId) {
        processing_pop_matrix(graphicsId);
        checkError();
    }

    public static void resetMatrix(long graphicsId) {
        processing_reset_matrix(graphicsId);
        checkError();
    }

    public static void translate(long graphicsId, float x, float y) {
        processing_translate(graphicsId, x, y);
        checkError();
    }

    public static void rotate(long graphicsId, float angle) {
        processing_rotate(graphicsId, angle);
        checkError();
    }

    public static void scale(long graphicsId, float x, float y) {
        processing_scale(graphicsId, x, y);
        checkError();
    }

    public static void shearX(long graphicsId, float angle) {
        processing_shear_x(graphicsId, angle);
        checkError();
    }

    public static void shearY(long graphicsId, float angle) {
        processing_shear_y(graphicsId, angle);
        checkError();
    }

    // ── 2D primitives ───────────────────────────────────────────────────

    public static void rect(long graphicsId, float x, float y, float w, float h,
                           float tl, float tr, float br, float bl) {
        processing_rect(graphicsId, x, y, w, h, tl, tr, br, bl);
        checkError();
    }

    public static void ellipse(long graphicsId, float cx, float cy, float w, float h) {
        processing_ellipse(graphicsId, cx, cy, w, h);
        checkError();
    }

    public static void circle(long graphicsId, float cx, float cy, float d) {
        processing_circle(graphicsId, cx, cy, d);
        checkError();
    }

    public static void line(long graphicsId, float x1, float y1, float x2, float y2) {
        processing_line(graphicsId, x1, y1, x2, y2);
        checkError();
    }

    public static void triangle(long graphicsId, float x1, float y1, float x2, float y2,
                                float x3, float y3) {
        processing_triangle(graphicsId, x1, y1, x2, y2, x3, y3);
        checkError();
    }

    public static void quad(long graphicsId, float x1, float y1, float x2, float y2,
                            float x3, float y3, float x4, float y4) {
        processing_quad(graphicsId, x1, y1, x2, y2, x3, y3, x4, y4);
        checkError();
    }

    public static void point(long graphicsId, float x, float y) {
        processing_point(graphicsId, x, y);
        checkError();
    }

    public static void square(long graphicsId, float x, float y, float s) {
        processing_square(graphicsId, x, y, s);
        checkError();
    }

    public static void arc(long graphicsId, float cx, float cy, float w, float h,
                           float start, float stop, byte mode) {
        processing_arc(graphicsId, cx, cy, w, h, start, stop, mode);
        checkError();
    }

    public static void bezier(long graphicsId, float x1, float y1, float x2, float y2,
                              float x3, float y3, float x4, float y4) {
        processing_bezier(graphicsId, x1, y1, x2, y2, x3, y3, x4, y4);
        checkError();
    }

    public static void curve(long graphicsId, float x1, float y1, float x2, float y2,
                             float x3, float y3, float x4, float y4) {
        processing_curve(graphicsId, x1, y1, x2, y2, x3, y3, x4, y4);
        checkError();
    }

    // ── 3D primitives ───────────────────────────────────────────────────

    public static void cylinder(long graphicsId, float radius, float height, int detail) {
        processing_cylinder(graphicsId, radius, height, detail);
        checkError();
    }

    public static void cone(long graphicsId, float radius, float height, int detail) {
        processing_cone(graphicsId, radius, height, detail);
        checkError();
    }

    public static void torus(long graphicsId, float radius, float tubeRadius, int majorSegments, int minorSegments) {
        processing_torus(graphicsId, radius, tubeRadius, majorSegments, minorSegments);
        checkError();
    }

    public static void plane(long graphicsId, float width, float height) {
        processing_plane(graphicsId, width, height);
        checkError();
    }

    public static void capsule(long graphicsId, float radius, float length, int detail) {
        processing_capsule(graphicsId, radius, length, detail);
        checkError();
    }

    public static void conicalFrustum(long graphicsId, float radiusTop, float radiusBottom, float height, int detail) {
        processing_conical_frustum(graphicsId, radiusTop, radiusBottom, height, detail);
        checkError();
    }

    public static void tetrahedron(long graphicsId, float radius) {
        processing_tetrahedron(graphicsId, radius);
        checkError();
    }

    // ── Vertex shapes ───────────────────────────────────────────────────

    public static void beginShape(long graphicsId, byte kind) {
        processing_begin_shape(graphicsId, kind);
        checkError();
    }

    public static void endShape(long graphicsId, boolean close) {
        processing_end_shape(graphicsId, close);
        checkError();
    }

    public static void shapeVertex(long graphicsId, float x, float y) {
        processing_vertex(graphicsId, x, y);
        checkError();
    }

    public static void bezierVertex(long graphicsId, float cx1, float cy1, float cx2, float cy2, float x, float y) {
        processing_bezier_vertex(graphicsId, cx1, cy1, cx2, cy2, x, y);
        checkError();
    }

    public static void quadraticVertex(long graphicsId, float cx, float cy, float x, float y) {
        processing_quadratic_vertex(graphicsId, cx, cy, x, y);
        checkError();
    }

    public static void curveVertex(long graphicsId, float x, float y) {
        processing_curve_vertex(graphicsId, x, y);
        checkError();
    }

    public static void beginContour(long graphicsId) {
        processing_begin_contour(graphicsId);
        checkError();
    }

    public static void endContour(long graphicsId) {
        processing_end_contour(graphicsId);
        checkError();
    }

    // ── 3D mode / projection ────────────────────────────────────────────

    public static void mode3d(long graphicsId) {
        processing_mode_3d(graphicsId);
        checkError();
    }

    public static void mode2d(long graphicsId) {
        processing_mode_2d(graphicsId);
        checkError();
    }

    public static void perspective(long graphicsId, float fov, float aspect, float near, float far) {
        processing_perspective(graphicsId, fov, aspect, near, far);
        checkError();
    }

    public static void ortho(long graphicsId, float left, float right, float bottom, float top, float near, float far) {
        processing_ortho(graphicsId, left, right, bottom, top, near, far);
        checkError();
    }

    // ── Entity transforms (3D objects: lights, geometry, etc.) ──────────

    public static void transformSetPosition(long entityId, float x, float y, float z) {
        processing_transform_set_position(entityId, x, y, z);
        checkError();
    }

    public static void transformTranslate(long entityId, float x, float y, float z) {
        processing_transform_translate(entityId, x, y, z);
        checkError();
    }

    public static void transformSetRotation(long entityId, float x, float y, float z) {
        processing_transform_set_rotation(entityId, x, y, z);
        checkError();
    }

    public static void transformRotateX(long entityId, float angle) {
        processing_transform_rotate_x(entityId, angle);
        checkError();
    }

    public static void transformRotateY(long entityId, float angle) {
        processing_transform_rotate_y(entityId, angle);
        checkError();
    }

    public static void transformRotateZ(long entityId, float angle) {
        processing_transform_rotate_z(entityId, angle);
        checkError();
    }

    public static void transformRotateAxis(long entityId, float angle, float axisX, float axisY, float axisZ) {
        processing_transform_rotate_axis(entityId, angle, axisX, axisY, axisZ);
        checkError();
    }

    public static void transformSetScale(long entityId, float x, float y, float z) {
        processing_transform_set_scale(entityId, x, y, z);
        checkError();
    }

    public static void transformScale(long entityId, float x, float y, float z) {
        processing_transform_scale(entityId, x, y, z);
        checkError();
    }

    public static void transformLookAt(long entityId, float targetX, float targetY, float targetZ) {
        processing_transform_look_at(entityId, targetX, targetY, targetZ);
        checkError();
    }

    public static void transformReset(long entityId) {
        processing_transform_reset(entityId);
        checkError();
    }

    // ── Lights ──────────────────────────────────────────────────────────

    public static long lightCreateDirectional(long graphicsId, float r, float g, float b, float a, float illuminance) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = allocateColor(arena, r, g, b, a);
            long id = processing_light_create_directional(graphicsId, color, illuminance);
            checkError();
            return id;
        }
    }

    public static long lightCreatePoint(long graphicsId, float r, float g, float b, float a,
                                        float intensity, float range, float radius) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = allocateColor(arena, r, g, b, a);
            long id = processing_light_create_point(graphicsId, color, intensity, range, radius);
            checkError();
            return id;
        }
    }

    public static long lightCreateSpot(long graphicsId, float r, float g, float b, float a,
                                       float intensity, float range, float radius,
                                       float innerAngle, float outerAngle) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment color = allocateColor(arena, r, g, b, a);
            long id = processing_light_create_spot(graphicsId, color, intensity, range, radius, innerAngle, outerAngle);
            checkError();
            return id;
        }
    }

    // ── Materials ───────────────────────────────────────────────────────

    public static long materialCreatePbr() {
        long id = processing_material_create_pbr();
        checkError();
        return id;
    }

    public static void materialSetFloat(long matId, String name, float value) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nameSegment = arena.allocateFrom(name);
            processing_material_set_float(matId, nameSegment, value);
            checkError();
        }
    }

    public static void materialSetFloat4(long matId, String name, float r, float g, float b, float a) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nameSegment = arena.allocateFrom(name);
            processing_material_set_float4(matId, nameSegment, r, g, b, a);
            checkError();
        }
    }

    public static void materialDestroy(long matId) {
        processing_material_destroy(matId);
        checkError();
    }

    public static void material(long graphicsId, long matId) {
        processing_material(graphicsId, matId);
        checkError();
    }

    // ── Images ──────────────────────────────────────────────────────────

    public static long imageCreate(int width, int height, byte[] data) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment dataSegment = arena.allocateFrom(java.lang.foreign.ValueLayout.JAVA_BYTE, data);
            long imageId = processing_image_create(width, height, dataSegment, data.length);
            checkError();
            return imageId;
        }
    }

    public static long imageLoad(String path) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pathSegment = arena.allocateFrom(path);
            long imageId = processing_image_load(pathSegment);
            checkError();
            return imageId;
        }
    }

    public static void imageResize(long imageId, int newWidth, int newHeight) {
        processing_image_resize(imageId, newWidth, newHeight);
        checkError();
    }

    public static void imageReadback(long imageId, float[] buffer) {
        try (Arena arena = Arena.ofConfined()) {
            int numPixels = buffer.length / 4;
            MemorySegment colorBuffer = Color.allocateArray(numPixels, arena);
            processing_image_readback(imageId, colorBuffer, numPixels);
            checkError();

            for (int i = 0; i < numPixels; i++) {
                MemorySegment color = Color.asSlice(colorBuffer, i);
                buffer[i * 4] = Color.c1(color);
                buffer[i * 4 + 1] = Color.c2(color);
                buffer[i * 4 + 2] = Color.c3(color);
                buffer[i * 4 + 3] = Color.a(color);
            }
        }
    }

    // ── Input: event ingestion (called by PSurfaceGLFW) ─────────────────

    public static void inputMouseMove(long surfaceId, float x, float y) {
        processing_input_mouse_move(surfaceId, x, y);
        checkError();
    }

    public static void inputMouseButton(long surfaceId, byte button, boolean pressed) {
        processing_input_mouse_button(surfaceId, button, pressed);
        checkError();
    }

    public static void inputScroll(long surfaceId, float x, float y) {
        processing_input_scroll(surfaceId, x, y);
        checkError();
    }

    public static void inputKey(long surfaceId, int keyCode, boolean pressed) {
        processing_input_key(surfaceId, keyCode, pressed);
        checkError();
    }

    public static void inputChar(long surfaceId, int keyCode, int codepoint) {
        processing_input_char(surfaceId, keyCode, codepoint);
        checkError();
    }

    public static void inputCursorEnter(long surfaceId) {
        processing_input_cursor_enter(surfaceId);
        checkError();
    }

    public static void inputCursorLeave(long surfaceId) {
        processing_input_cursor_leave(surfaceId);
        checkError();
    }

    public static void inputFocus(long surfaceId, boolean focused) {
        processing_input_focus(surfaceId, focused);
        checkError();
    }

    public static void inputFlush() {
        processing_input_flush();
        checkError();
    }

    // ── Input: state queries ────────────────────────────────────────────

    public static float mouseX(long surfaceId) {
        return processing_mouse_x(surfaceId);
    }

    public static float mouseY(long surfaceId) {
        return processing_mouse_y(surfaceId);
    }

    public static float pmouseX(long surfaceId) {
        return processing_pmouse_x(surfaceId);
    }

    public static float pmouseY(long surfaceId) {
        return processing_pmouse_y(surfaceId);
    }

    public static boolean mouseIsPressed() {
        return processing_mouse_is_pressed();
    }

    public static byte mouseButton() {
        return processing_mouse_button();
    }

    public static boolean keyIsPressed() {
        return processing_key_is_pressed();
    }

    public static boolean keyIsDown(int keyCode) {
        return processing_key_is_down(keyCode);
    }

    public static boolean keyJustPressed(int keyCode) {
        return processing_key_just_pressed(keyCode);
    }

    public static int key() {
        return processing_key();
    }

    public static int keyCode() {
        return processing_key_code();
    }

    public static float movedX() {
        return processing_moved_x();
    }

    public static float movedY() {
        return processing_moved_y();
    }

    public static float mouseWheel() {
        return processing_mouse_wheel();
    }

    // ── Geometry ────────────────────────────────────────────────────────

    public static final byte TOPOLOGY_POINT_LIST = 0;
    public static final byte TOPOLOGY_LINE_LIST = 1;
    public static final byte TOPOLOGY_LINE_STRIP = 2;
    public static final byte TOPOLOGY_TRIANGLE_LIST = 3;
    public static final byte TOPOLOGY_TRIANGLE_STRIP = 4;

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

    public static long geometrySphere(float radius, int sectors, int stacks) {
        long geoId = processing_geometry_sphere(radius, sectors, stacks);
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

    public static void model(long graphicsId, long geoId) {
        processing_model(graphicsId, geoId);
        checkError();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static MemorySegment allocateColor(Arena arena, float r, float g, float b, float a) {
        MemorySegment color = Color.allocate(arena);
        Color.c1(color, r);
        Color.c2(color, g);
        Color.c3(color, b);
        Color.a(color, a);
        Color.space(color, COLOR_SPACE_SRGB);
        return color;
    }

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
