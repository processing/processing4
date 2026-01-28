package processing.webgpu;

import processing.core.PShape;
import processing.core.PVector;

/**
 * WebGPU implementation of PShape.
 */
public class PShapeWebGPU extends PShape {

    /** Reference to the graphics context */
    protected PGraphicsWebGPU pg;

    /** Native geometry ID (0 = not yet created) */
    protected long geometryId = 0;

    /** Native layout ID for custom vertex attributes */
    protected long layoutId = 0;

    /** Current topology for this shape */
    protected byte topology = PWebGPU.TOPOLOGY_TRIANGLE_LIST;

    /** Track if we're currently building the shape */
    protected boolean building = false;

    /** Pending normal for next vertex */
    protected float normalX, normalY, normalZ;
    protected boolean hasNormal = false;

    /** Pending color for next vertex */
    protected float colorR, colorG, colorB, colorA;
    protected boolean hasColor = false;

    /** Pending UV for next vertex */
    protected float uvU, uvV;
    protected boolean hasUV = false;

    /**
     * Create a new WebGPU shape.
     */
    public PShapeWebGPU(PGraphicsWebGPU pg, int family) {
        this.pg = pg;
        this.family = family;
    }

    /**
     * Map Processing shape kinds to WebGPU topologies.
     */
    protected byte kindToTopology(int kind) {
        return switch (kind) {
            case POINTS -> PWebGPU.TOPOLOGY_POINT_LIST;
            case LINES -> PWebGPU.TOPOLOGY_LINE_LIST;
            case LINE_STRIP -> PWebGPU.TOPOLOGY_LINE_STRIP;
            case TRIANGLES -> PWebGPU.TOPOLOGY_TRIANGLE_LIST;
            case TRIANGLE_STRIP -> PWebGPU.TOPOLOGY_TRIANGLE_STRIP;
            case TRIANGLE_FAN -> PWebGPU.TOPOLOGY_TRIANGLE_LIST; // Will need tessellation
            case QUADS -> PWebGPU.TOPOLOGY_TRIANGLE_LIST; // Will need tessellation
            case QUAD_STRIP -> PWebGPU.TOPOLOGY_TRIANGLE_STRIP;
            default -> PWebGPU.TOPOLOGY_TRIANGLE_LIST;
        };
    }

    @Override
    public void beginShape(int kind) {
        this.kind = kind;
        this.topology = kindToTopology(kind);

        // Create or reset the geometry
        if (geometryId != 0) {
            PWebGPU.geometryDestroy(geometryId);
        }
        geometryId = PWebGPU.geometryCreate(topology);
        building = true;

        // Reset pending attributes
        hasNormal = false;
        hasColor = false;
        hasUV = false;
    }

    @Override
    public void endShape(int mode) {
        building = false;
        // If CLOSE mode and we have a polygon, we might need to add indices
        // For now, the geometry is ready to be rendered
    }

    @Override
    public void normal(float nx, float ny, float nz) {
        if (geometryId != 0) {
            PWebGPU.geometryNormal(geometryId, nx, ny, nz);
        }
        normalX = nx;
        normalY = ny;
        normalZ = nz;
        hasNormal = true;
    }

    @Override
    public void vertex(float x, float y) {
        vertex(x, y, 0);
    }

    @Override
    public void vertex(float x, float y, float z) {
        if (geometryId == 0) {
            return;
        }
        if (hasColor) {
            PWebGPU.geometryColor(geometryId, colorR, colorG, colorB, colorA);
        }
        if (hasUV) {
            PWebGPU.geometryUv(geometryId, uvU, uvV);
        }
        PWebGPU.geometryVertex(geometryId, x, y, z);
    }

    @Override
    public void vertex(float x, float y, float u, float v) {
        texture(u, v);
        vertex(x, y, 0);
    }

    @Override
    public void vertex(float x, float y, float z, float u, float v) {
        texture(u, v);
        vertex(x, y, z);
    }

    /**
     * Set texture coordinates for the next vertex.
     */
    public void texture(float u, float v) {
        uvU = u;
        uvV = v;
        hasUV = true;
    }

    /**
     * Set the fill color for subsequent vertices.
     */
    @Override
    public void fill(int rgb) {
        colorR = ((rgb >> 16) & 0xFF) / 255f;
        colorG = ((rgb >> 8) & 0xFF) / 255f;
        colorB = (rgb & 0xFF) / 255f;
        colorA = ((rgb >> 24) & 0xFF) / 255f;
        hasColor = true;
    }

    @Override
    public void fill(float gray) {
        colorR = colorG = colorB = gray / 255f;
        colorA = 1f;
        hasColor = true;
    }

    @Override
    public void fill(float r, float g, float b) {
        colorR = r / 255f;
        colorG = g / 255f;
        colorB = b / 255f;
        colorA = 1f;
        hasColor = true;
    }

    @Override
    public void fill(float r, float g, float b, float a) {
        colorR = r / 255f;
        colorG = g / 255f;
        colorB = b / 255f;
        colorA = a / 255f;
        hasColor = true;
    }

    /**
     * Add an index for indexed rendering.
     */
    public void index(int i) {
        if (geometryId != 0) {
            PWebGPU.geometryIndex(geometryId, i);
        }
    }

    @Override
    public int getVertexCount() {
        if (geometryId == 0) {
            return 0;
        }
        return PWebGPU.geometryVertexCount(geometryId);
    }

    /**
     * Get the index count for this shape.
     */
    public int getIndexCount() {
        if (geometryId == 0) {
            return 0;
        }
        return PWebGPU.geometryIndexCount(geometryId);
    }

    @Override
    public void setVertex(int index, float x, float y, float z) {
        if (geometryId != 0) {
            PWebGPU.geometrySetVertex(geometryId, index, x, y, z);
        }
    }

    @Override
    public void setNormal(int index, float nx, float ny, float nz) {
        if (geometryId != 0) {
            PWebGPU.geometrySetNormal(geometryId, index, nx, ny, nz);
        }
    }

    /**
     * Set the color of a specific vertex.
     */
    public void setColor(int index, float r, float g, float b, float a) {
        if (geometryId != 0) {
            PWebGPU.geometrySetColor(geometryId, index, r, g, b, a);
        }
    }

    /**
     * Set the UV coordinates of a specific vertex.
     */
    public void setUv(int index, float u, float v) {
        if (geometryId != 0) {
            PWebGPU.geometrySetUv(geometryId, index, u, v);
        }
    }

    /**
     * Get the native geometry ID for direct rendering.
     */
    public long getGeometryId() {
        return geometryId;
    }

    /**
     * Draw this shape using the associated graphics context.
     */
    public void draw() {
        if (geometryId != 0 && pg != null) {
            pg.model(geometryId);
        }
    }

    /**
     * Release native resources.
     */
    public void dispose() {
        if (geometryId != 0) {
            PWebGPU.geometryDestroy(geometryId);
            geometryId = 0;
        }
        if (layoutId != 0) {
            PWebGPU.geometryLayoutDestroy(layoutId);
            layoutId = 0;
        }
    }

    /**
     * Create a box geometry.
     */
    public static PShapeWebGPU createBox(PGraphicsWebGPU pg, float width, float height, float depth) {
        PShapeWebGPU shape = new PShapeWebGPU(pg, GEOMETRY);
        shape.geometryId = PWebGPU.geometryBox(width, height, depth);
        return shape;
    }
}
