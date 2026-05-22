package processing.webgpu;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PSurface;

import java.util.ArrayList;
import java.util.List;

public class PGraphicsWebGPU extends PGraphics {
    protected long surfaceId = 0;
    private long graphicsId = 0;

    private long currentGeometry = 0;
    private int shapeKind = 0;
    private float normalX = 0, normalY = 0, normalZ = 1;

    private final List<Long> pendingDestroy = new ArrayList<>();


    @Override
    public PSurface createSurface() {
        return surface = new PSurfaceGLFW(this);
    }

    protected void initWebGPUSurface(long windowHandle, long displayHandle, int width, int height, float scaleFactor) {
        surfaceId = PWebGPU.createSurface(windowHandle, displayHandle, width, height, scaleFactor);
        if (surfaceId == 0) {
            System.err.println("Failed to create WebGPU surface");
            return;
        }
        graphicsId = PWebGPU.graphicsCreate(surfaceId, width, height);
        if (graphicsId == 0) {
            System.err.println("Failed to create WebGPU graphics context");
        }
    }

    public long getSurfaceId() {
        return surfaceId;
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (surfaceId != 0) {
            PWebGPU.windowResized(surfaceId, pixelWidth, pixelHeight);
        }
    }

    @Override
    public void beginDraw() {
        super.beginDraw();
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.beginDraw(graphicsId);
        checkSettings();
    }

    @Override
    public void flush() {
        super.flush();
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.flush(graphicsId);

        for (long geometryId : pendingDestroy) {
            PWebGPU.geometryDestroy(geometryId);
        }
        pendingDestroy.clear();
    }

    @Override
    public void endDraw() {
        super.endDraw();
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.endDraw(graphicsId);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (surfaceId != 0) {
            PWebGPU.destroySurface(surfaceId);
            surfaceId = 0;
        }
        PWebGPU.exit();
    }

    // ── Background ──────────────────────────────────────────────────────

    @Override
    protected void backgroundImpl() {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.backgroundColor(graphicsId, backgroundR, backgroundG, backgroundB, backgroundA);
    }

    @Override
    protected void backgroundImpl(PImage image) {
        if (graphicsId == 0) {
            return;
        }
        if (!(image instanceof PImageWebGPU)) {
            throw new RuntimeException("WebGPU renderer requires PImageWebGPU. Use createImage().");
        }
        PImageWebGPU img = (PImageWebGPU) image;
        if (img.getId() == 0) {
            img.loadPixels();
            byte[] rgba = pixelsToRGBA(img.pixels);
            long imageId = PWebGPU.imageCreate(img.pixelWidth, img.pixelHeight, rgba);
            img.setId(imageId);
        }
        PWebGPU.backgroundImage(graphicsId, img.getId());
    }

    // ── Fill / stroke ───────────────────────────────────────────────────

    @Override
    protected void fillFromCalc() {
        super.fillFromCalc();
        if (graphicsId == 0) {
            return;
        }
        if (fill) {
            PWebGPU.setFill(graphicsId, fillR, fillG, fillB, fillA);
        } else {
            PWebGPU.noFill(graphicsId);
        }
    }

    @Override
    protected void strokeFromCalc() {
        super.strokeFromCalc();
        if (graphicsId == 0) {
            return;
        }
        if (stroke) {
            PWebGPU.setStrokeColor(graphicsId, strokeR, strokeG, strokeB, strokeA);
        } else {
            PWebGPU.noStroke(graphicsId);
        }
    }

    @Override
    public void strokeWeight(float weight) {
        super.strokeWeight(weight);
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.setStrokeWeight(graphicsId, weight);
    }

    @Override
    public void noFill() {
        super.noFill();
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.noFill(graphicsId);
    }

    @Override
    public void noStroke() {
        super.noStroke();
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.noStroke(graphicsId);
    }

    @Override
    public void strokeCap(int cap) {
        super.strokeCap(cap);
        if (graphicsId == 0) {
            return;
        }
        byte nativeCap = switch (cap) {
            case ROUND -> PWebGPU.STROKE_CAP_ROUND;
            case SQUARE -> PWebGPU.STROKE_CAP_SQUARE;
            case PROJECT -> PWebGPU.STROKE_CAP_PROJECT;
            default -> PWebGPU.STROKE_CAP_ROUND;
        };
        PWebGPU.setStrokeCap(graphicsId, nativeCap);
    }

    @Override
    public void strokeJoin(int join) {
        super.strokeJoin(join);
        if (graphicsId == 0) {
            return;
        }
        byte nativeJoin = switch (join) {
            case ROUND -> PWebGPU.STROKE_JOIN_ROUND;
            case MITER -> PWebGPU.STROKE_JOIN_MITER;
            case BEVEL -> PWebGPU.STROKE_JOIN_BEVEL;
            default -> PWebGPU.STROKE_JOIN_ROUND;
        };
        PWebGPU.setStrokeJoin(graphicsId, nativeJoin);
    }

    // ── Blend mode ──────────────────────────────────────────────────────

    @Override
    public void blendMode(int mode) {
        super.blendMode(mode);
        if (graphicsId == 0) {
            return;
        }
        byte nativeMode = switch (mode) {
            case BLEND -> PWebGPU.BLEND_MODE_BLEND;
            case ADD -> PWebGPU.BLEND_MODE_ADD;
            case SUBTRACT -> PWebGPU.BLEND_MODE_SUBTRACT;
            case DARKEST -> PWebGPU.BLEND_MODE_DARKEST;
            case LIGHTEST -> PWebGPU.BLEND_MODE_LIGHTEST;
            case DIFFERENCE -> PWebGPU.BLEND_MODE_DIFFERENCE;
            case EXCLUSION -> PWebGPU.BLEND_MODE_EXCLUSION;
            case MULTIPLY -> PWebGPU.BLEND_MODE_MULTIPLY;
            case SCREEN -> PWebGPU.BLEND_MODE_SCREEN;
            case REPLACE -> PWebGPU.BLEND_MODE_REPLACE;
            default -> PWebGPU.BLEND_MODE_BLEND;
        };
        PWebGPU.setBlendMode(graphicsId, nativeMode);
    }

    // ── 2D primitives ───────────────────────────────────────────────────

    @Override
    protected void rectImpl(float x1, float y1, float x2, float y2) {
        rectImpl(x1, y1, x2, y2, 0, 0, 0, 0);
    }

    @Override
    protected void rectImpl(float x1, float y1, float x2, float y2,
                            float tl, float tr, float br, float bl) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.rect(graphicsId, x1, y1, x2 - x1, y2 - y1, tl, tr, br, bl);
    }

    @Override
    protected void ellipseImpl(float a, float b, float c, float d) {
        if (graphicsId == 0) {
            return;
        }
        // ellipseImpl receives corner-form; native expects center.
        PWebGPU.ellipse(graphicsId, a + c / 2f, b + d / 2f, c, d);
    }

    @Override
    protected void arcImpl(float a, float b, float c, float d,
                           float start, float stop, int mode) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.arc(graphicsId, a, b, c, d, start, stop, (byte) mode);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.line(graphicsId, x1, y1, x2, y2);
    }

    @Override
    public void point(float x, float y) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.point(graphicsId, x, y);
    }

    @Override
    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.triangle(graphicsId, x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void quad(float x1, float y1, float x2, float y2,
                     float x3, float y3, float x4, float y4) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.quad(graphicsId, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    // ── Curves ──────────────────────────────────────────────────────────

    @Override
    public void bezier(float x1, float y1, float x2, float y2,
                       float x3, float y3, float x4, float y4) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.bezier(graphicsId, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    @Override
    public void curve(float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.curve(graphicsId, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    // ── 3D shapes ───────────────────────────────────────────────────────

    @Override
    public void box(float w, float h, float d) {
        if (graphicsId == 0) {
            return;
        }
        long boxGeometry = PWebGPU.geometryBox(w, h, d);
        PWebGPU.model(graphicsId, boxGeometry);
        pendingDestroy.add(boxGeometry);
    }

    @Override
    public void sphere(float r) {
        if (graphicsId == 0) {
            return;
        }
        long sphereGeometry = PWebGPU.geometrySphere(r, sphereDetailU, sphereDetailV);
        PWebGPU.model(graphicsId, sphereGeometry);
        pendingDestroy.add(sphereGeometry);
    }

    // ── Vertex shapes ───────────────────────────────────────────────────

    @Override
    public void beginShape(int kind) {
        super.beginShape(kind);
        if (graphicsId == 0) {
            return;
        }
        shapeKind = kind;
        byte topology = shapeKindToTopology(kind);
        currentGeometry = PWebGPU.geometryCreate(topology);
    }

    private byte shapeKindToTopology(int kind) {
        return switch (kind) {
            case POINTS -> PWebGPU.TOPOLOGY_POINT_LIST;
            case LINES -> PWebGPU.TOPOLOGY_LINE_LIST;
            case LINE_STRIP -> PWebGPU.TOPOLOGY_LINE_STRIP;
            case TRIANGLES -> PWebGPU.TOPOLOGY_TRIANGLE_LIST;
            case TRIANGLE_STRIP -> PWebGPU.TOPOLOGY_TRIANGLE_STRIP;
            case TRIANGLE_FAN, QUADS, QUAD_STRIP, POLYGON -> PWebGPU.TOPOLOGY_TRIANGLE_LIST;
            default -> PWebGPU.TOPOLOGY_TRIANGLE_LIST;
        };
    }

    @Override
    public void normal(float nx, float ny, float nz) {
        normalX = nx;
        normalY = ny;
        normalZ = nz;
    }

    @Override
    public void vertex(float x, float y) {
        vertex(x, y, 0);
    }

    @Override
    public void vertex(float x, float y, float z) {
        if (currentGeometry == 0) {
            return;
        }
        PWebGPU.geometryColor(currentGeometry, fillR, fillG, fillB, fillA);
        PWebGPU.geometryNormal(currentGeometry, normalX, normalY, normalZ);
        PWebGPU.geometryVertex(currentGeometry, x, y, z);
    }

    @Override
    public void endShape(int mode) {
        if (graphicsId == 0 || currentGeometry == 0) {
            return;
        }

        if (shapeKind == QUADS) {
            int vertexCount = PWebGPU.geometryVertexCount(currentGeometry);
            for (int i = 0; i < vertexCount; i += 4) {
                PWebGPU.geometryIndex(currentGeometry, i);
                PWebGPU.geometryIndex(currentGeometry, i + 1);
                PWebGPU.geometryIndex(currentGeometry, i + 2);
                PWebGPU.geometryIndex(currentGeometry, i);
                PWebGPU.geometryIndex(currentGeometry, i + 2);
                PWebGPU.geometryIndex(currentGeometry, i + 3);
            }
        }

        PWebGPU.model(graphicsId, currentGeometry);
        pendingDestroy.add(currentGeometry);
        currentGeometry = 0;
    }

    // ── Transform matrix ────────────────────────────────────────────────

    @Override
    public void pushMatrix() {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.pushMatrix(graphicsId);
    }

    @Override
    public void popMatrix() {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.popMatrix(graphicsId);
    }

    @Override
    public void resetMatrix() {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.resetMatrix(graphicsId);
    }

    @Override
    public void translate(float x, float y) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.translate(graphicsId, x, y);
    }

    @Override
    public void rotate(float angle) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.rotate(graphicsId, angle);
    }

    @Override
    public void scale(float x, float y) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.scale(graphicsId, x, y);
    }

    @Override
    public void shearX(float angle) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.shearX(graphicsId, angle);
    }

    @Override
    public void shearY(float angle) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.shearY(graphicsId, angle);
    }

    // ── 3D camera / projection ──────────────────────────────────────────

    @Override
    public void camera(float eyeX, float eyeY, float eyeZ,
                       float centerX, float centerY, float centerZ,
                       float upX, float upY, float upZ) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.mode3d(graphicsId);
        PWebGPU.transformSetPosition(graphicsId, eyeX, eyeY, eyeZ);
        PWebGPU.transformLookAt(graphicsId, centerX, centerY, centerZ);
    }

    public void cameraPosition(float x, float y, float z) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.transformSetPosition(graphicsId, x, y, z);
    }

    public void cameraLookAt(float x, float y, float z) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.transformLookAt(graphicsId, x, y, z);
    }

    public void mode3d() {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.mode3d(graphicsId);
    }

    @Override
    public void perspective(float fov, float aspect, float near, float far) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.mode3d(graphicsId);
        PWebGPU.perspective(graphicsId, fov, aspect, near, far);
    }

    @Override
    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.ortho(graphicsId, left, right, bottom, top, near, far);
    }

    // ── Lights ───────────────────────────────────────────────────────────

    @Override
    public void directionalLight(float r, float g, float b,
                                 float nx, float ny, float nz) {
        if (graphicsId == 0) return;
        long light = PWebGPU.lightCreateDirectional(graphicsId, r, g, b, 1.0f, 600.0f);
        PWebGPU.transformSetRotation(light, nx, ny, nz);
    }

    @Override
    public void pointLight(float r, float g, float b,
                           float x, float y, float z) {
        if (graphicsId == 0) return;
        long light = PWebGPU.lightCreatePoint(graphicsId, r, g, b, 1.0f, 100000.0f, 800.0f, 0.0f);
        PWebGPU.transformSetPosition(light, x, y, z);
    }

    public long directionalLight(float r, float g, float b, float illuminance) {
        if (graphicsId == 0) return 0;
        return PWebGPU.lightCreateDirectional(graphicsId, r, g, b, 1.0f, illuminance);
    }

    public long pointLight(float r, float g, float b,
                           float intensity, float range, float radius,
                           float x, float y, float z) {
        if (graphicsId == 0) return 0;
        long light = PWebGPU.lightCreatePoint(graphicsId, r, g, b, 1.0f, intensity, range, radius);
        PWebGPU.transformSetPosition(light, x, y, z);
        return light;
    }

    public long spotLight(float r, float g, float b,
                          float intensity, float range, float radius,
                          float innerAngle, float outerAngle) {
        if (graphicsId == 0) return 0;
        return PWebGPU.lightCreateSpot(graphicsId, r, g, b, 1.0f,
                intensity, range, radius, innerAngle, outerAngle);
    }

    // ── Images / shapes ─────────────────────────────────────────────────

    public PImageWebGPU createImage(int width, int height, int format) {
        return new PImageWebGPU(width, height, format);
    }

    @Override
    public PShape createShape() {
        return new PShapeWebGPU(this, PShape.GEOMETRY);
    }

    @Override
    public PShape createShape(int type) {
        return new PShapeWebGPU(this, type);
    }

    public void model(long geometryId) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.model(graphicsId, geometryId);
    }

    // ── Materials ───────────────────────────────────────────────────────

    public void useMaterial(Material mat) {
        if (graphicsId == 0) {
            return;
        }
        PWebGPU.material(graphicsId, mat.id());
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private byte[] pixelsToRGBA(int[] pixels) {
        byte[] rgba = new byte[pixels.length * 4];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            rgba[i * 4]     = (byte) ((pixel >> 16) & 0xFF);
            rgba[i * 4 + 1] = (byte) ((pixel >> 8) & 0xFF);
            rgba[i * 4 + 2] = (byte) (pixel & 0xFF);
            rgba[i * 4 + 3] = (byte) ((pixel >> 24) & 0xFF);
        }
        return rgba;
    }
}
