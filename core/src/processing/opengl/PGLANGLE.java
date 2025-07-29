package processing.opengl;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

import javaangle.JavaANGLE;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PSurface;

public class PGLANGLE extends PGL {

    
    ///////////////////////////////////////////////////////////

    // Constants

    static {
        FALSE = JavaANGLE.GL_FALSE;
        TRUE  = JavaANGLE.GL_TRUE;

        INT            = JavaANGLE.GL_INT;
        BYTE           = JavaANGLE.GL_BYTE;
        SHORT          = JavaANGLE.GL_SHORT;
        FLOAT          = JavaANGLE.GL_FLOAT;
        BOOL           = JavaANGLE.GL_BOOL;
        UNSIGNED_INT   = JavaANGLE.GL_UNSIGNED_INT;
        UNSIGNED_BYTE  = JavaANGLE.GL_UNSIGNED_BYTE;
        UNSIGNED_SHORT = JavaANGLE.GL_UNSIGNED_SHORT;

        RGB             = JavaANGLE.GL_RGB;
        RGBA            = JavaANGLE.GL_RGBA;
        ALPHA           = JavaANGLE.GL_ALPHA;
        LUMINANCE       = JavaANGLE.GL_LUMINANCE;
        LUMINANCE_ALPHA = JavaANGLE.GL_LUMINANCE_ALPHA;

        UNSIGNED_SHORT_5_6_5   = JavaANGLE.GL_UNSIGNED_SHORT_5_6_5;
        UNSIGNED_SHORT_4_4_4_4 = JavaANGLE.GL_UNSIGNED_SHORT_4_4_4_4;
        UNSIGNED_SHORT_5_5_5_1 = JavaANGLE.GL_UNSIGNED_SHORT_5_5_5_1;

        RGBA4   = JavaANGLE.GL_RGBA4;
        RGB5_A1 = JavaANGLE.GL_RGB5_A1;
        RGB565  = JavaANGLE.GL_RGB565;
        RGB8    = JavaANGLE.GL_RGB8;
        RGBA8   = JavaANGLE.GL_RGBA8;
        ALPHA8  = JavaANGLE.GL_ALPHA8;

        READ_ONLY  = JavaANGLE.GL_READ_ONLY;
        WRITE_ONLY = JavaANGLE.GL_WRITE_ONLY;
        READ_WRITE = JavaANGLE.GL_READ_WRITE;

        TESS_WINDING_NONZERO = GLU.GLU_TESS_WINDING_NONZERO;
        TESS_WINDING_ODD     = GLU.GLU_TESS_WINDING_ODD;
        TESS_EDGE_FLAG       = GLU.GLU_TESS_EDGE_FLAG;

        GENERATE_MIPMAP_HINT = 0;   // TODO
        FASTEST              = JavaANGLE.GL_FASTEST;
        NICEST               = JavaANGLE.GL_NICEST;
        DONT_CARE            = JavaANGLE.GL_DONT_CARE;

        VENDOR                   = JavaANGLE.GL_VENDOR;
        RENDERER                 = JavaANGLE.GL_RENDERER;
        VERSION                  = JavaANGLE.GL_VERSION;
        EXTENSIONS               = JavaANGLE.GL_EXTENSIONS;
        SHADING_LANGUAGE_VERSION = JavaANGLE.GL_SHADING_LANGUAGE_VERSION;

        MAX_SAMPLES = JavaANGLE.GL_MAX_SAMPLES;
        SAMPLES     = JavaANGLE.GL_SAMPLES;

        ALIASED_LINE_WIDTH_RANGE = JavaANGLE.GL_ALIASED_LINE_WIDTH_RANGE;
        ALIASED_POINT_SIZE_RANGE = JavaANGLE.GL_ALIASED_POINT_SIZE_RANGE;

        DEPTH_BITS   = JavaANGLE.GL_DEPTH_BITS;
        STENCIL_BITS = JavaANGLE.GL_STENCIL_BITS;

        CCW = JavaANGLE.GL_CCW;
        CW  = JavaANGLE.GL_CW;

        VIEWPORT = JavaANGLE.GL_VIEWPORT;

        ARRAY_BUFFER         = JavaANGLE.GL_ARRAY_BUFFER;
        ELEMENT_ARRAY_BUFFER = JavaANGLE.GL_ELEMENT_ARRAY_BUFFER;
        PIXEL_PACK_BUFFER    = JavaANGLE.GL_PIXEL_PACK_BUFFER;

        MAX_VERTEX_ATTRIBS  = JavaANGLE.GL_MAX_VERTEX_ATTRIBS;

        STATIC_DRAW  = JavaANGLE.GL_STATIC_DRAW;
        DYNAMIC_DRAW = JavaANGLE.GL_DYNAMIC_DRAW;
        STREAM_DRAW  = JavaANGLE.GL_STREAM_DRAW;
        STREAM_READ  = JavaANGLE.GL_STREAM_READ;

        BUFFER_SIZE  = JavaANGLE.GL_BUFFER_SIZE;
        BUFFER_USAGE = JavaANGLE.GL_BUFFER_USAGE;

        POINTS         = JavaANGLE.GL_POINTS;
        LINE_STRIP     = JavaANGLE.GL_LINE_STRIP;
        LINE_LOOP      = JavaANGLE.GL_LINE_LOOP;
        LINES          = JavaANGLE.GL_LINES;
        TRIANGLE_FAN   = JavaANGLE.GL_TRIANGLE_FAN;
        TRIANGLE_STRIP = JavaANGLE.GL_TRIANGLE_STRIP;
        TRIANGLES      = JavaANGLE.GL_TRIANGLES;

        CULL_FACE      = JavaANGLE.GL_CULL_FACE;
        FRONT          = JavaANGLE.GL_FRONT;
        BACK           = JavaANGLE.GL_BACK;
        FRONT_AND_BACK = JavaANGLE.GL_FRONT_AND_BACK;

        POLYGON_OFFSET_FILL = JavaANGLE.GL_POLYGON_OFFSET_FILL;

        UNPACK_ALIGNMENT = JavaANGLE.GL_UNPACK_ALIGNMENT;
        PACK_ALIGNMENT   = JavaANGLE.GL_PACK_ALIGNMENT;

        TEXTURE_2D        = JavaANGLE.GL_TEXTURE_2D;
        TEXTURE_RECTANGLE = 0;  // TODO

        TEXTURE_BINDING_2D        = JavaANGLE.GL_TEXTURE_BINDING_2D;
        TEXTURE_BINDING_RECTANGLE = 0;  // TODO

        MAX_TEXTURE_SIZE           = JavaANGLE.GL_MAX_TEXTURE_SIZE;
        TEXTURE_MAX_ANISOTROPY     = 0;  // TODO
        MAX_TEXTURE_MAX_ANISOTROPY = 0;  // TODO

        MAX_VERTEX_TEXTURE_IMAGE_UNITS   = JavaANGLE.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
        MAX_TEXTURE_IMAGE_UNITS          = JavaANGLE.GL_MAX_TEXTURE_IMAGE_UNITS;
        MAX_COMBINED_TEXTURE_IMAGE_UNITS = JavaANGLE.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

        NUM_COMPRESSED_TEXTURE_FORMATS = JavaANGLE.GL_NUM_COMPRESSED_TEXTURE_FORMATS;
        COMPRESSED_TEXTURE_FORMATS     = JavaANGLE.GL_COMPRESSED_TEXTURE_FORMATS;

        NEAREST               = JavaANGLE.GL_NEAREST;
        LINEAR                = JavaANGLE.GL_LINEAR;
        LINEAR_MIPMAP_NEAREST = JavaANGLE.GL_LINEAR_MIPMAP_NEAREST;
        LINEAR_MIPMAP_LINEAR  = JavaANGLE.GL_LINEAR_MIPMAP_LINEAR;

        CLAMP_TO_EDGE = JavaANGLE.GL_CLAMP_TO_EDGE;
        REPEAT        = JavaANGLE.GL_REPEAT;

        TEXTURE0           = JavaANGLE.GL_TEXTURE0;
        TEXTURE1           = JavaANGLE.GL_TEXTURE1;
        TEXTURE2           = JavaANGLE.GL_TEXTURE2;
        TEXTURE3           = JavaANGLE.GL_TEXTURE3;
        TEXTURE_MIN_FILTER = JavaANGLE.GL_TEXTURE_MIN_FILTER;
        TEXTURE_MAG_FILTER = JavaANGLE.GL_TEXTURE_MAG_FILTER;
        TEXTURE_WRAP_S     = JavaANGLE.GL_TEXTURE_WRAP_S;
        TEXTURE_WRAP_T     = JavaANGLE.GL_TEXTURE_WRAP_T;
        TEXTURE_WRAP_R     = JavaANGLE.GL_TEXTURE_WRAP_R;

        TEXTURE_CUBE_MAP = JavaANGLE.GL_TEXTURE_CUBE_MAP;
        TEXTURE_CUBE_MAP_POSITIVE_X = JavaANGLE.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
        TEXTURE_CUBE_MAP_POSITIVE_Y = JavaANGLE.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
        TEXTURE_CUBE_MAP_POSITIVE_Z = JavaANGLE.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
        TEXTURE_CUBE_MAP_NEGATIVE_X = JavaANGLE.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
        TEXTURE_CUBE_MAP_NEGATIVE_Y = JavaANGLE.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
        TEXTURE_CUBE_MAP_NEGATIVE_Z = JavaANGLE.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;

        VERTEX_SHADER        = JavaANGLE.GL_VERTEX_SHADER;
        FRAGMENT_SHADER      = JavaANGLE.GL_FRAGMENT_SHADER;
        INFO_LOG_LENGTH      = JavaANGLE.GL_INFO_LOG_LENGTH;
        SHADER_SOURCE_LENGTH = JavaANGLE.GL_SHADER_SOURCE_LENGTH;
        COMPILE_STATUS       = JavaANGLE.GL_COMPILE_STATUS;
        LINK_STATUS          = JavaANGLE.GL_LINK_STATUS;
        VALIDATE_STATUS      = JavaANGLE.GL_VALIDATE_STATUS;
        SHADER_TYPE          = JavaANGLE.GL_SHADER_TYPE;
        DELETE_STATUS        = JavaANGLE.GL_DELETE_STATUS;

        FLOAT_VEC2   = JavaANGLE.GL_FLOAT_VEC2;
        FLOAT_VEC3   = JavaANGLE.GL_FLOAT_VEC3;
        FLOAT_VEC4   = JavaANGLE.GL_FLOAT_VEC4;
        FLOAT_MAT2   = JavaANGLE.GL_FLOAT_MAT2;
        FLOAT_MAT3   = JavaANGLE.GL_FLOAT_MAT3;
        FLOAT_MAT4   = JavaANGLE.GL_FLOAT_MAT4;
        INT_VEC2     = JavaANGLE.GL_INT_VEC2;
        INT_VEC3     = JavaANGLE.GL_INT_VEC3;
        INT_VEC4     = JavaANGLE.GL_INT_VEC4;
        BOOL_VEC2    = JavaANGLE.GL_BOOL_VEC2;
        BOOL_VEC3    = JavaANGLE.GL_BOOL_VEC3;
        BOOL_VEC4    = JavaANGLE.GL_BOOL_VEC4;
        SAMPLER_2D   = JavaANGLE.GL_SAMPLER_2D;
        SAMPLER_CUBE = JavaANGLE.GL_SAMPLER_CUBE;

        LOW_FLOAT    = JavaANGLE.GL_LOW_FLOAT;
        MEDIUM_FLOAT = JavaANGLE.GL_MEDIUM_FLOAT;
        HIGH_FLOAT   = JavaANGLE.GL_HIGH_FLOAT;
        LOW_INT      = JavaANGLE.GL_LOW_INT;
        MEDIUM_INT   = JavaANGLE.GL_MEDIUM_INT;
        HIGH_INT     = JavaANGLE.GL_HIGH_INT;

        CURRENT_VERTEX_ATTRIB = JavaANGLE.GL_CURRENT_VERTEX_ATTRIB;

        VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
        VERTEX_ATTRIB_ARRAY_ENABLED        = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_ENABLED;
        VERTEX_ATTRIB_ARRAY_SIZE           = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_SIZE;
        VERTEX_ATTRIB_ARRAY_STRIDE         = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_STRIDE;
        VERTEX_ATTRIB_ARRAY_TYPE           = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_TYPE;
        VERTEX_ATTRIB_ARRAY_NORMALIZED     = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
        VERTEX_ATTRIB_ARRAY_POINTER        = JavaANGLE.GL_VERTEX_ATTRIB_ARRAY_POINTER;

        BLEND               = JavaANGLE.GL_BLEND;
        ONE                 = JavaANGLE.GL_ONE;
        ZERO                = JavaANGLE.GL_ZERO;
        SRC_ALPHA           = JavaANGLE.GL_SRC_ALPHA;
        DST_ALPHA           = JavaANGLE.GL_DST_ALPHA;
        ONE_MINUS_SRC_ALPHA = JavaANGLE.GL_ONE_MINUS_SRC_ALPHA;
        ONE_MINUS_DST_COLOR = JavaANGLE.GL_ONE_MINUS_DST_COLOR;
        ONE_MINUS_SRC_COLOR = JavaANGLE.GL_ONE_MINUS_SRC_COLOR;
        DST_COLOR           = JavaANGLE.GL_DST_COLOR;
        SRC_COLOR           = JavaANGLE.GL_SRC_COLOR;

        SAMPLE_ALPHA_TO_COVERAGE = JavaANGLE.GL_SAMPLE_ALPHA_TO_COVERAGE;
        SAMPLE_COVERAGE          = JavaANGLE.GL_SAMPLE_COVERAGE;

        KEEP      = JavaANGLE.GL_KEEP;
        REPLACE   = JavaANGLE.GL_REPLACE;
        INCR      = JavaANGLE.GL_INCR;
        DECR      = JavaANGLE.GL_DECR;
        INVERT    = JavaANGLE.GL_INVERT;
        INCR_WRAP = JavaANGLE.GL_INCR_WRAP;
        DECR_WRAP = JavaANGLE.GL_DECR_WRAP;
        NEVER     = JavaANGLE.GL_NEVER;
        ALWAYS    = JavaANGLE.GL_ALWAYS;

        EQUAL    = JavaANGLE.GL_EQUAL;
        LESS     = JavaANGLE.GL_LESS;
        LEQUAL   = JavaANGLE.GL_LEQUAL;
        GREATER  = JavaANGLE.GL_GREATER;
        GEQUAL   = JavaANGLE.GL_GEQUAL;
        NOTEQUAL = JavaANGLE.GL_NOTEQUAL;

        FUNC_ADD              = JavaANGLE.GL_FUNC_ADD;
        FUNC_MIN              = JavaANGLE.GL_MIN;
        FUNC_MAX              = JavaANGLE.GL_MAX;
        FUNC_REVERSE_SUBTRACT = JavaANGLE.GL_FUNC_REVERSE_SUBTRACT;
        FUNC_SUBTRACT         = JavaANGLE.GL_FUNC_SUBTRACT;

        DITHER = JavaANGLE.GL_DITHER;

        CONSTANT_COLOR           = JavaANGLE.GL_CONSTANT_COLOR;
        CONSTANT_ALPHA           = JavaANGLE.GL_CONSTANT_ALPHA;
        ONE_MINUS_CONSTANT_COLOR = JavaANGLE.GL_ONE_MINUS_CONSTANT_COLOR;
        ONE_MINUS_CONSTANT_ALPHA = JavaANGLE.GL_ONE_MINUS_CONSTANT_ALPHA;
        SRC_ALPHA_SATURATE       = JavaANGLE.GL_SRC_ALPHA_SATURATE;

        SCISSOR_TEST    = JavaANGLE.GL_SCISSOR_TEST;
        STENCIL_TEST    = JavaANGLE.GL_STENCIL_TEST;
        DEPTH_TEST      = JavaANGLE.GL_DEPTH_TEST;
        DEPTH_WRITEMASK = JavaANGLE.GL_DEPTH_WRITEMASK;

        COLOR_BUFFER_BIT   = JavaANGLE.GL_COLOR_BUFFER_BIT;
        DEPTH_BUFFER_BIT   = JavaANGLE.GL_DEPTH_BUFFER_BIT;
        STENCIL_BUFFER_BIT = JavaANGLE.GL_STENCIL_BUFFER_BIT;

        FRAMEBUFFER        = JavaANGLE.GL_FRAMEBUFFER;
        COLOR_ATTACHMENT0  = JavaANGLE.GL_COLOR_ATTACHMENT0;
        COLOR_ATTACHMENT1  = JavaANGLE.GL_COLOR_ATTACHMENT1;
        COLOR_ATTACHMENT2  = JavaANGLE.GL_COLOR_ATTACHMENT2;
        COLOR_ATTACHMENT3  = JavaANGLE.GL_COLOR_ATTACHMENT3;
        RENDERBUFFER       = JavaANGLE.GL_RENDERBUFFER;
        DEPTH_ATTACHMENT   = JavaANGLE.GL_DEPTH_ATTACHMENT;
        STENCIL_ATTACHMENT = JavaANGLE.GL_STENCIL_ATTACHMENT;
        READ_FRAMEBUFFER   = JavaANGLE.GL_READ_FRAMEBUFFER;
        DRAW_FRAMEBUFFER   = JavaANGLE.GL_DRAW_FRAMEBUFFER;

        DEPTH24_STENCIL8 = JavaANGLE.GL_DEPTH24_STENCIL8;

        DEPTH_COMPONENT   = JavaANGLE.GL_DEPTH_COMPONENT;
        DEPTH_COMPONENT16 = JavaANGLE.GL_DEPTH_COMPONENT16;
        DEPTH_COMPONENT24 = JavaANGLE.GL_DEPTH_COMPONENT24;
        DEPTH_COMPONENT32 = 0;   // TODO
        STENCIL_INDEX  = JavaANGLE.GL_STENCIL_INDEX;
        STENCIL_INDEX1 = 0;  // TODO
        STENCIL_INDEX4 = 0;  // TODO
        STENCIL_INDEX8 = JavaANGLE.GL_STENCIL_INDEX8;

        DEPTH_STENCIL = JavaANGLE.GL_DEPTH_STENCIL;

        FRAMEBUFFER_COMPLETE                      = JavaANGLE.GL_FRAMEBUFFER_COMPLETE;
        FRAMEBUFFER_UNDEFINED                     = JavaANGLE.GL_FRAMEBUFFER_UNDEFINED;
        FRAMEBUFFER_INCOMPLETE_ATTACHMENT         = JavaANGLE.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = JavaANGLE.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_DIMENSIONS         = JavaANGLE.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
        FRAMEBUFFER_INCOMPLETE_FORMATS            = 0;  // TODO
        FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        = 0;  // TODO
        FRAMEBUFFER_INCOMPLETE_READ_BUFFER        = 0;  // TODO
        FRAMEBUFFER_UNSUPPORTED                   = JavaANGLE.GL_FRAMEBUFFER_UNSUPPORTED;
        FRAMEBUFFER_INCOMPLETE_MULTISAMPLE        = JavaANGLE.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
        FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS      = JavaANGLE.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;

        FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE           = JavaANGLE.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE;
        FRAMEBUFFER_ATTACHMENT_OBJECT_NAME           = JavaANGLE.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL         = JavaANGLE.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = JavaANGLE.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE;

        RENDERBUFFER_WIDTH           = JavaANGLE.GL_RENDERBUFFER_WIDTH;
        RENDERBUFFER_HEIGHT          = JavaANGLE.GL_RENDERBUFFER_HEIGHT;
        RENDERBUFFER_RED_SIZE        = JavaANGLE.GL_RENDERBUFFER_RED_SIZE;
        RENDERBUFFER_GREEN_SIZE      = JavaANGLE.GL_RENDERBUFFER_GREEN_SIZE;
        RENDERBUFFER_BLUE_SIZE       = JavaANGLE.GL_RENDERBUFFER_BLUE_SIZE;
        RENDERBUFFER_ALPHA_SIZE      = JavaANGLE.GL_RENDERBUFFER_ALPHA_SIZE;
        RENDERBUFFER_DEPTH_SIZE      = JavaANGLE.GL_RENDERBUFFER_DEPTH_SIZE;
        RENDERBUFFER_STENCIL_SIZE    = JavaANGLE.GL_RENDERBUFFER_STENCIL_SIZE;
        RENDERBUFFER_INTERNAL_FORMAT = JavaANGLE.GL_RENDERBUFFER_INTERNAL_FORMAT;

        MULTISAMPLE    = 0;   // TODO
        LINE_SMOOTH    = JavaANGLE.GL_LINE_SMOOTH;
        POLYGON_SMOOTH = JavaANGLE.GL_POLYGON_SMOOTH;

        SYNC_GPU_COMMANDS_COMPLETE = JavaANGLE.GL_SYNC_GPU_COMMANDS_COMPLETE;
        ALREADY_SIGNALED           = JavaANGLE.GL_ALREADY_SIGNALED;
        CONDITION_SATISFIED        = JavaANGLE.GL_CONDITION_SATISFIED;
    }
    
    protected float[] projMatrix;
    protected FloatBuffer projMatrixBuffer;
    protected float[] mvMatrix;

    private HashMap<Integer, Buffer> buffers = new HashMap<Integer, Buffer>();
    private int boundBuffer = -1;

    private void report(String m) {
        System.out.println(m);
    }

    public PGLANGLE(PGraphicsOpenGL pg) {
        super(pg);
    }

    @Override
    public Object getNative() {
        report("Object getNative");
        return null;
    }

    @Override
    protected void setFrameRate(float fps) {
        report("void setFrameRate");

    }

    @Override
    protected void initSurface(int antialias) {
        report("void initSurface");
        
    }

    @Override
    protected void reinitSurface() {
        report("void reinitSurface");
        
    }

    @Override
    protected void registerListeners() {
        report("void registerListeners");
        
    }

    @Override
    protected int getDepthBits() {
        report("int getDepthBits");
        return JavaANGLE.GL_DEPTH_BITS;
    }

    @Override
    protected int getStencilBits() {
        report("int getStencilBits");
        return JavaANGLE.GL_STENCIL_BITS;
    }

    @Override
    protected float getPixelScale() {
        report("float getPixelScale");
        PSurface surf = sketch.getSurface();
        if (surf == null) {
        return graphics.pixelDensity;
        } else if (surf instanceof PSurfaceJOGL) {
        return ((PSurfaceANGLE)surf).getPixelScale();
        } else {
        throw new RuntimeException("Renderer cannot find a JOGL surface");
        }
        
    }

    @Override
    protected void getGL(PGL pgl) {
        report("void getGL");
        
    }

    @Override
    protected boolean canDraw() {
        report("boolean canDraw");
        return true;
    }

    @Override
    protected void requestFocus() {
        report("void requestFocus");
        
    }

    @Override
    protected void requestDraw() {
        report("void requestDraw");
        
    }

    @Override
    protected void swapBuffers() {
        report("void swapBuffers");
        PSurfaceANGLE surf = (PSurfaceANGLE)sketch.getSurface();
        surf.swapBuffers();
    }

    @Override
    protected void beginGL() {
        report("void beginGL");
        PMatrix3D proj = graphics.projection;
        PMatrix3D mdl = graphics.modelview;
        if (projMatrix == null) {
            projMatrix = new float[16];
            
            ByteBuffer buffer = ByteBuffer.allocateDirect(Float.BYTES*16);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            projMatrixBuffer = buffer.asFloatBuffer();
        }


        JavaANGLE.glMatrixMode(JavaANGLE.GL_PROJECTION);
        projMatrix[ 0] = proj.m00;
        projMatrix[ 1] = proj.m10;
        projMatrix[ 2] = proj.m20;
        projMatrix[ 3] = proj.m30;
        projMatrix[ 4] = proj.m01;
        projMatrix[ 5] = proj.m11;
        projMatrix[ 6] = proj.m21;
        projMatrix[ 7] = proj.m31;
        projMatrix[ 8] = proj.m02;
        projMatrix[ 9] = proj.m12;
        projMatrix[10] = proj.m22;
        projMatrix[11] = proj.m32;
        projMatrix[12] = proj.m03;
        projMatrix[13] = proj.m13;
        projMatrix[14] = proj.m23;
        projMatrix[15] = proj.m33;
        projMatrixBuffer.rewind();
        projMatrixBuffer.put(projMatrix);
        JavaANGLE.glLoadMatrixf(projMatrixBuffer);

        if (mvMatrix == null) {
            mvMatrix = new float[16];
        }
        JavaANGLE.glMatrixMode(JavaANGLE.GL_MODELVIEW);
        mvMatrix[ 0] = mdl.m00;
        mvMatrix[ 1] = mdl.m10;
        mvMatrix[ 2] = mdl.m20;
        mvMatrix[ 3] = mdl.m30;
        mvMatrix[ 4] = mdl.m01;
        mvMatrix[ 5] = mdl.m11;
        mvMatrix[ 6] = mdl.m21;
        mvMatrix[ 7] = mdl.m31;
        mvMatrix[ 8] = mdl.m02;
        mvMatrix[ 9] = mdl.m12;
        mvMatrix[10] = mdl.m22;
        mvMatrix[11] = mdl.m32;
        mvMatrix[12] = mdl.m03;
        mvMatrix[13] = mdl.m13;
        mvMatrix[14] = mdl.m23;
        mvMatrix[15] = mdl.m33;

        projMatrixBuffer.rewind();
        projMatrixBuffer.put(projMatrix);
        JavaANGLE.glLoadMatrixf(projMatrixBuffer);
    }

    @Override
    protected boolean hasFBOs() {
        report("boolean hasFBOs");
        // if (context.hasBasicFBOSupport()) return true;
        // else 
        return super.hasFBOs();
    }


    @Override
    protected boolean hasShaders() {
        report("boolean hasShaders");
        // if (context.hasGLSL()) return true;
        // else 
        return super.hasShaders();
    }
    
    @Override
    protected void initFBOLayer() {
        report("void initFBOLayer");
        IntBuffer buf = allocateDirectIntBuffer(fboWidth * fboHeight);

        if (hasReadBuffer()) readBuffer(BACK);
        readPixelsImpl(0, 0, fboWidth, fboHeight, RGBA, UNSIGNED_BYTE, buf);
        bindTexture(TEXTURE_2D, glColorTex.get(frontTex));
        texSubImage2D(TEXTURE_2D, 0, 0, 0, fboWidth, fboHeight, RGBA, UNSIGNED_BYTE, buf);

        bindTexture(TEXTURE_2D, glColorTex.get(backTex));
        texSubImage2D(TEXTURE_2D, 0, 0, 0, fboWidth, fboHeight, RGBA, UNSIGNED_BYTE, buf);

        bindTexture(TEXTURE_2D, 0);
        bindFramebufferImpl(FRAMEBUFFER, 0);
    }
    
    @Override
    protected void enableTexturing(int target) {
        report("void enableTexturing");
        if (target == TEXTURE_2D) {
        texturingTargets[0] = true;
        } else if (target == TEXTURE_RECTANGLE) {
        texturingTargets[1] = true;
        }
    }


    @Override
    protected void disableTexturing(int target) {
        report("void disableTexturing");
        if (target == TEXTURE_2D) {
        texturingTargets[0] = false;
        } else if (target == TEXTURE_RECTANGLE) {
        texturingTargets[1] = false;
        }
    }
    
    @Override
    protected String[] loadVertexShader(String filename) {
        report("String[] loadVertexShader");
        return loadVertexShader(filename, getGLSLVersion(), getGLSLVersionSuffix());
    }


    @Override
    protected String[] loadFragmentShader(String filename) {
        report("String[] loadFragmentShader");
        return loadFragmentShader(filename, getGLSLVersion(), getGLSLVersionSuffix());
    }


    @Override
    protected String[] loadVertexShader(URL url) {
        report("String[] loadVertexShader");
        return loadVertexShader(url, getGLSLVersion(), getGLSLVersionSuffix());
    }


    @Override
    protected String[] loadFragmentShader(URL url) {
        report("String[] loadFragmentShader");
        return loadFragmentShader(url, getGLSLVersion(), getGLSLVersionSuffix());
    }


    @Override
    protected String[] loadFragmentShader(String filename, int version, String versionSuffix) {
        report("String[] loadFragmentShader");
        String[] fragSrc0 = sketch.loadStrings(filename);
        return preprocessFragmentSource(fragSrc0, version, versionSuffix);
    }


    @Override
    protected String[] loadVertexShader(String filename, int version, String versionSuffix) {
        report("String[] loadVertexShader");
        String[] vertSrc0 = sketch.loadStrings(filename);
        return preprocessVertexSource(vertSrc0, version, versionSuffix);
    }


    @Override
    protected String[] loadFragmentShader(URL url, int version, String versionSuffix) {
        report("String[] loadFragmentShader");
        try {
        String[] fragSrc0 = PApplet.loadStrings(url.openStream());
        return preprocessFragmentSource(fragSrc0, version, versionSuffix);
        } catch (IOException e) {
        PGraphics.showException("Cannot load fragment shader " + url.getFile());
        }
        return null;
    }


    @Override
    protected String[] loadVertexShader(URL url, int version, String versionSuffix) {
        report("String[] loadVertexShader");
        try {
        String[] vertSrc0 = PApplet.loadStrings(url.openStream());
        return preprocessVertexSource(vertSrc0, version, versionSuffix);
        } catch (IOException e) {
        PGraphics.showException("Cannot load vertex shader " + url.getFile());
        }
        return null;
    }



    @Override
    protected int getGLSLVersion() {
        report("int getGLSLVersion");
        // TODO: This is incorrect.
        return JavaANGLE.GL_ES_VERSION_3_1;
    }

    @Override
    protected String getGLSLVersionSuffix() {
        report("String getGLSLVersionSuffix");
        return "";
    }
    
    @SuppressWarnings("deprecation")
    private FontMetrics getFontMetrics(Font font) {  // ignore
        report("FontMetrics getFontMetrics");
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    @Override
    protected int getTextWidth(Object font, char[] buffer, int start, int stop) {
        report("int getTextWidth");
        int length = stop - start;
        FontMetrics metrics = getFontMetrics((Font) font);
        return metrics.charsWidth(buffer, start, length);
    }

    @Override
    protected Object getDerivedFont(Object font, float size) {
        report("Object getDerivedFont");
        return ((Font) font).deriveFont(size);
        
    }
    

    @Override
    protected Tessellator createTessellator(TessellatorCallback callback) {
        report("Tessellator createTessellator");
        
        return new Tessellator(callback);
    }

    protected static class Tessellator implements PGL.Tessellator {
    protected GLUtessellator tess;
    protected TessellatorCallback callback;
    protected GLUCallback gluCallback;

    public Tessellator(TessellatorCallback callback) {
        this.callback = callback;
        tess = GLU.gluNewTess();
        gluCallback = new GLUCallback();

        GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, gluCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_END, gluCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, gluCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, gluCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR, gluCallback);
        }

        @Override
        public void setCallback(int flag) {
        GLU.gluTessCallback(tess, flag, gluCallback);
        }

        @Override
        public void setWindingRule(int rule) {
        setProperty(GLU.GLU_TESS_WINDING_RULE, rule);
        }

        @Override
        public void setProperty(int property, int value) {
        GLU.gluTessProperty(tess, property, value);
        }

        @Override
        public void beginPolygon() {
        beginPolygon(null);
        }

        @Override
        public void beginPolygon(Object data) {
        GLU.gluTessBeginPolygon(tess, data);
        }

        @Override
        public void endPolygon() {
        GLU.gluTessEndPolygon(tess);
        }

        @Override
        public void beginContour() {
        GLU.gluTessBeginContour(tess);
        }

        @Override
        public void endContour() {
        GLU.gluTessEndContour(tess);
        }

        @Override
        public void addVertex(double[] v) {
        addVertex(v, 0, v);
        }

        @Override
        public void addVertex(double[] v, int n, Object data) {
        GLU.gluTessVertex(tess, v, n, data);
        }

        protected class GLUCallback extends GLUtessellatorCallbackAdapter {
        @Override
        public void begin(int type) {
            callback.begin(type);
        }

        @Override
        public void end() {
            callback.end();
        }

        @Override
        public void vertex(Object data) {
            callback.vertex(data);
        }

        @Override
        public void combine(double[] coords, Object[] data,
                            float[] weight, Object[] outData) {
            callback.combine(coords, data, weight, outData);
        }

        @Override
        public void error(int errnum) {
            callback.error(errnum);
        }
        }
    }
    
    private FontRenderContext getFontRenderContext(Font font) {  // ignore
        report("FontRenderContext getFontRenderContext");
        return getFontMetrics(font).getFontRenderContext();
    }

    protected class FontOutline implements PGL.FontOutline {
        PathIterator iter;

        public FontOutline(char ch, Font font) {
        report("FontOutline");
        char[] textArray = new char[] { ch };
        FontRenderContext frc = getFontRenderContext(font);
        GlyphVector gv = font.createGlyphVector(frc, textArray);
        Shape shp = gv.getOutline();
        iter = shp.getPathIterator(null);
        }

        public boolean isDone() {
        report("boolean isDone");
        return iter.isDone();
        }

        public int currentSegment(float[] coords) {
        report("int currentSegment");
        return iter.currentSegment(coords);
        }

        public void next() {
        report("void next");
        iter.next();
        }
    }
    

    @Override
    protected FontOutline createFontOutline(char ch, Object font) {
        report("FontOutline createFontOutline");
        return new FontOutline(ch, (Font) font);
    }

    @Override
    public void flush() {
        report("void flush");
        JavaANGLE.glFlush();
    }

    @Override
    public void finish() {
        report("void finish");
        JavaANGLE.glFinish();
    }

    @Override
    public void hint(int target, int hint) {
        report("void hint");
        JavaANGLE.glHint(target, hint);
    }

    @Override
    public void enable(int value) {
        report("void enable");
        if (-1 < value) {
            JavaANGLE.glEnable(value);
        }
    }

    @Override
    public void disable(int value) {
        report("void disable");
        if (-1 < value) {
            JavaANGLE.glDisable(value);
        }
    }

    @Override
    public void getBooleanv(int value, IntBuffer data) {
        report("void getBooleanv");
        if (data == null) return;
        if (-1 < value) {
            if (byteBuffer.capacity() < data.capacity()) {
                byteBuffer = allocateDirectByteBuffer(data.capacity());
            }
            JavaANGLE.glGetBooleanv(value, byteBuffer.asIntBuffer());
            for (int i = 0; i < data.capacity(); i++) {
                data.put(i, byteBuffer.get(i));
            }
        } 
        else {
        fillIntBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public void getIntegerv(int value, IntBuffer data) {
        report("void getIntegerv");
        if (data == null) return;
        if (-1 < value) {
            JavaANGLE.glGetIntegerv(value, data);
        } else {
            fillIntBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public void getFloatv(int value, FloatBuffer data) {
        report("void getFloatv");
        if (data == null) return;
        if (-1 < value) {
            JavaANGLE.glGetFloatv(value, data);
        } else {
            fillFloatBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public boolean isEnabled(int value) {
        report("boolean isEnabled");
        return JavaANGLE.glIsEnabled(value);
    }

    @Override
    public String getString(int name) {
        report("String getString");
        return JavaANGLE.glGetString(name);
    }

    @Override
    public int getError() {
        report("int getError");
        return JavaANGLE.glGetError();
    }

    @Override
    public String errorString(int err) {
        report("String errorString");
        return "";
    }

    @Override
    public void genBuffers(int n, IntBuffer buffers) {
        report("void genBuffers");
        if (buffers == null) return;
        JavaANGLE.glGenBuffers(n, buffers);
    }

    @Override
    public void deleteBuffers(int n, IntBuffer buffers) {
        report("void deleteBuffers");
        if (buffers == null) return;
        JavaANGLE.glDeleteBuffers(n, buffers);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        report("void bindBuffer");
        boundBuffer = buffer;
        JavaANGLE.glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, int size, Buffer data, int usage) {
        report("void bufferData");
        if (data == null) return;
        buffers.put(boundBuffer, data);
        JavaANGLE.glBufferData(target, size, data, usage);
    }

    @Override
    public void bufferSubData(int target, int offset, int size, Buffer data) {
        report("void bufferSubData");
        if (data == null) return;
        // buffers.put(boundBuffer, data);

        // TODO: Oh god we got to write our own buffersubdata for the
        // buffers hashmap oh god.

        JavaANGLE.glBufferSubData(target, offset, size, data);
    }

    @Override
    public void isBuffer(int buffer) {
        report("void isBuffer");
        JavaANGLE.glIsBuffer(buffer);
    }

    @Override
    public void getBufferParameteriv(int target, int value, IntBuffer data) {
        report("void getBufferParameteriv");
        if (data == null) return;
        JavaANGLE.glGetBufferParameteriv(target, value, data);
    }

    @Override
    public ByteBuffer mapBuffer(int target, int access) {
        report("ByteBuffer mapBuffer");
        return null;
    }

    @Override
    public ByteBuffer mapBufferRange(int target, int offset, int length, int access) {
        report("ByteBuffer mapBufferRange");
        // TODO: wow.
        // return JavaANGLE.glMapBufferRange(target, offset, length, access);
        return null;
    }

    @Override
    public void unmapBuffer(int target) {
        report("void unmapBuffer");
        JavaANGLE.glUnmapBuffer(target);
        
    }

    @Override
    public long fenceSync(int condition, int flags) {
        report("long fenceSync");
        return JavaANGLE.glFenceSync(condition, flags);
    }

    @Override
    public void deleteSync(long sync) {
        report("void deleteSync");
        JavaANGLE.glDeleteSync(sync);
    }

    @Override
    public int clientWaitSync(long sync, int flags, long timeout) {
        report("int clientWaitSync");
        return JavaANGLE.glClientWaitSync(sync, flags, timeout);
    }

    @Override
    public void depthRangef(float n, float f) {
        report("void depthRangef");
        // TODO: Oops.
        // JavaANGLE.glDepthRange(n, f);
    }

    @Override
    public void viewport(int x, int y, int w, int h) {
        report("void viewport");
        float scale = getPixelScale();
        viewportImpl((int)scale * x, (int)(scale * y), (int)(scale * w), (int)(scale * h));
    }

    @Override
    protected void viewportImpl(int x, int y, int w, int h) {
        report("void viewportImpl");
        JavaANGLE.glViewport(x, y, w, h);
    }

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, Buffer buffer) {
        report("void readPixelsImpl");
        if (buffer == null) return;
        JavaANGLE.glReadPixels(x, y, width, height, format, type, buffer);
    }

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, long offset) {
        report("void readPixelsImpl");
        // TODO
        // JavaANGLE.glReadPixels(x, y, width, height, format, type, 0);
    }

    @Override
    public void vertexAttrib1f(int index, float value) {
        report("void vertexAttrib1f");
        JavaANGLE.glVertexAttrib1f(index, value);
    }

    @Override
    public void vertexAttrib2f(int index, float value0, float value1) {
        report("void vertexAttrib2f");
        JavaANGLE.glVertexAttrib2f(index, value0, value1);
    }

    @Override
    public void vertexAttrib3f(int index, float value0, float value1, float value2) {
        report("void vertexAttrib3f");
        JavaANGLE.glVertexAttrib3f(index, value0, value1, value2);
    }

    @Override
    public void vertexAttrib4f(int index, float value0, float value1, float value2, float value3) {
        report("void vertexAttrib4f");
        JavaANGLE.glVertexAttrib4f(index, value0, value1, value2, value3);
    }

    @Override
    public void vertexAttrib1fv(int index, FloatBuffer values) {
        report("void vertexAttrib1fv");
        if (values == null) return;
        JavaANGLE.glVertexAttrib1fv(index, values);
    }

    @Override
    public void vertexAttrib2fv(int index, FloatBuffer values) {
        report("void vertexAttrib2fv");
        if (values == null) return;
        JavaANGLE.glVertexAttrib2fv(index, values);
    }

    @Override
    public void vertexAttrib3fv(int index, FloatBuffer values) {
        report("void vertexAttrib3fv");
        if (values == null) return;
        JavaANGLE.glVertexAttrib3fv(index, values);
    }

    @Override
    public void vertexAttrib4fv(int index, FloatBuffer values) {
        report("void vertexAttrib4fv");
        if (values == null) return;
        JavaANGLE.glVertexAttrib4fv(index, values);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        report("void vertexAttribPointer");
        JavaANGLE.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void enableVertexAttribArray(int index) {
        report("void enableVertexAttribArray");
        JavaANGLE.glEnableVertexAttribArray(index);
    }

    @Override
    public void disableVertexAttribArray(int index) {
        report("void disableVertexAttribArray");
        JavaANGLE.glDisableVertexAttribArray(index);
    }

    @Override
    public void drawArraysImpl(int mode, int first, int count) {
        report("void drawArraysImpl");
        JavaANGLE.glDrawArrays(mode, first, count);
    }

    @Override
    public void drawElementsImpl(int mode, int count, int type, int offset) {
        report("void drawElementsImpl");
        Buffer buff = buffers.get(boundBuffer);
        if (buff == null) {
            System.err.println("drawElementsImpl: unbound buffer");
            return;
        }
        JavaANGLE.glDrawElements(mode, count, type, buff);
    }

    @Override
    public void lineWidth(float width) {
        report("void lineWidth");
        JavaANGLE.glLineWidth(width);
    }

    @Override
    public void frontFace(int dir) {
        report("void frontFace");
        JavaANGLE.glFrontFace(dir);
    }

    @Override
    public void cullFace(int mode) {
        report("void cullFace");
        JavaANGLE.glCullFace(mode);
    }

    @Override
    public void polygonOffset(float factor, float units) {
        report("void polygonOffset");
        JavaANGLE.glPolygonOffset(factor, units);
    }

    @Override
    public void pixelStorei(int pname, int param) {
        report("void pixelStorei");
        
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer data) {
        report("void texImage2D");
        if (data == null) return;
        
    }

    @Override
    public void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
        report("void copyTexImage2D");
        
    }

    @Override
    public void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, Buffer data) {
        report("void texSubImage2D");
        if (data == null) return;
        
    }

    @Override
    public void copyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
        report("void copyTexSubImage2D");
        
    }

    @Override
    public void compressedTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int imageSize, Buffer data) {
        report("void compressedTexImage2D");
        if (data == null) return;
        
    }

    @Override
    public void compressedTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int imageSize, Buffer data) {
        report("void compressedTexSubImage2D");
        if (data == null) return;
        
    }

    @Override
    public void texParameteri(int target, int pname, int param) {
        report("void texParameteri");
        
    }

    @Override
    public void texParameterf(int target, int pname, float param) {
        report("void texParameterf");
        
    }

    @Override
    public void texParameteriv(int target, int pname, IntBuffer params) {
        report("void texParameteriv");
        if (params == null) return;
        
    }

    @Override
    public void texParameterfv(int target, int pname, FloatBuffer params) {
        report("void texParameterfv");
        if (params == null) return;
        
    }

    @Override
    public void generateMipmap(int target) {
        report("void generateMipmap");
        
    }

    @Override
    public void genTextures(int n, IntBuffer textures) {
        report("void genTextures");
        if (textures == null) return;
        
    }

    @Override
    public void deleteTextures(int n, IntBuffer textures) {
        report("void deleteTextures");
        if (textures == null) return;
        
    }

    @Override
    public void getTexParameteriv(int target, int pname, IntBuffer params) {
        report("void getTexParameteriv");
        if (params == null) return;
        
    }

    @Override
    public void getTexParameterfv(int target, int pname, FloatBuffer params) {
        report("void getTexParameterfv");
        if (params == null) return;
        
    }

    @Override
    public boolean isTexture(int texture) {
        report("boolean isTexture");
        return JavaANGLE.glIsTexture(texture);
    }

    @Override
    protected void activeTextureImpl(int texture) {
        report("void activeTextureImpl");
        
    }

    @Override
    protected void bindTextureImpl(int target, int texture) {
        report("void bindTextureImpl");
        
    }

    @Override
    public int createShader(int type) {
        report("int createShader");
        return JavaANGLE.glCreateShader(type);
    }

    @Override
    public void shaderSource(int shader, String source) {
        report("void shaderSource");
        
    }

    @Override
    public void compileShader(int shader) {
        report("void compileShader");
        
    }

    @Override
    public void releaseShaderCompiler() {
        report("void releaseShaderCompiler");
        
    }

    @Override
    public void deleteShader(int shader) {
        report("void deleteShader");
        
    }

    @Override
    public void shaderBinary(int count, IntBuffer shaders, int binaryFormat, Buffer binary, int length) {
        report("void shaderBinary");
        if (shaders == null || binary == null) return;
        
    }

    @Override
    public int createProgram() {
        report("int createProgram");
        return JavaANGLE.glCreateProgram();
    }

    @Override
    public void attachShader(int program, int shader) {
        report("void attachShader");
        
    }

    @Override
    public void detachShader(int program, int shader) {
        report("void detachShader");
        
    }

    @Override
    public void linkProgram(int program) {
        report("void linkProgram");
        
    }

    @Override
    public void useProgram(int program) {
        report("void useProgram");
        
    }

    @Override
    public void deleteProgram(int program) {
        report("void deleteProgram");
        
    }

    @Override
    public String getActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        report("String getActiveAttrib");
        if (size == null || type == null) return null;
        return JavaANGLE.glGetActiveAttrib(program, index, size, type);
    }

    @Override
    public int getAttribLocation(int program, String name) {
        report("int getAttribLocation");
        return JavaANGLE.glGetAttribLocation(program, name);
    }

    @Override
    public void bindAttribLocation(int program, int index, String name) {
        report("void bindAttribLocation");
        
    }

    @Override
    public int getUniformLocation(int program, String name) {
        report("int getUniformLocation");
        return JavaANGLE.glGetUniformLocation(program, name);
    }

    @Override
    public String getActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        report("String getActiveUniform");
        if (size == null || type == null) return null;
        return JavaANGLE.glGetActiveUniform(program, index, size, type);
    }

    @Override
    public void uniform1i(int location, int value) {
        report("void uniform1i");
        
    }

    @Override
    public void uniform2i(int location, int value0, int value1) {
        report("void uniform2i");
        
    }

    @Override
    public void uniform3i(int location, int value0, int value1, int value2) {
        report("void uniform3i");
        
    }

    @Override
    public void uniform4i(int location, int value0, int value1, int value2, int value3) {
        report("void uniform4i");
        
    }

    @Override
    public void uniform1f(int location, float value) {
        report("void uniform1f");
        
    }

    @Override
    public void uniform2f(int location, float value0, float value1) {
        report("void uniform2f");
        
    }

    @Override
    public void uniform3f(int location, float value0, float value1, float value2) {
        report("void uniform3f");
        
    }

    @Override
    public void uniform4f(int location, float value0, float value1, float value2, float value3) {
        report("void uniform4f");
        
    }

    @Override
    public void uniform1iv(int location, int count, IntBuffer v) {
        report("void uniform1iv");
        if (v == null) return;
        
    }

    @Override
    public void uniform2iv(int location, int count, IntBuffer v) {
        report("void uniform2iv");
        if (v == null) return;
        
    }

    @Override
    public void uniform3iv(int location, int count, IntBuffer v) {
        report("void uniform3iv");
        if (v == null) return;
        
    }

    @Override
    public void uniform4iv(int location, int count, IntBuffer v) {
        report("void uniform4iv");
        if (v == null) return;
        
    }

    @Override
    public void uniform1fv(int location, int count, FloatBuffer v) {
        report("void uniform1fv");
        if (v == null) return;
        
    }

    @Override
    public void uniform2fv(int location, int count, FloatBuffer v) {
        report("void uniform2fv");
        if (v == null) return;
        
    }

    @Override
    public void uniform3fv(int location, int count, FloatBuffer v) {
        report("void uniform3fv");
        if (v == null) return;
        
    }

    @Override
    public void uniform4fv(int location, int count, FloatBuffer v) {
        report("void uniform4fv");
        if (v == null) return;
        
    }

    @Override
    public void uniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix2fv");
        if (mat == null) return;
        
    }

    @Override
    public void uniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix3fv");
        if (mat == null) return;
        
    }

    @Override
    public void uniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix4fv");
        if (mat == null) return;
        
    }

    @Override
    public void validateProgram(int program) {
        report("void validateProgram");
        
    }

    @Override
    public boolean isShader(int shader) {
        report("boolean isShader");
        return JavaANGLE.glIsShader(shader);
    }

    @Override
    public void getShaderiv(int shader, int pname, IntBuffer params) {
        report("void getShaderiv");
        if (params == null) return;
        
    }

    @Override
    public void getAttachedShaders(int program, int maxCount, IntBuffer count, IntBuffer shaders) {
        report("void getAttachedShaders");
        if (count == null || shaders == null) return;
        
    }

    @Override
    public String getShaderInfoLog(int shader) {
        report("String getShaderInfoLog");
        return JavaANGLE.glGetShaderInfoLog(shader);
    }

    @Override
    public String getShaderSource(int shader) {
        report("String getShaderSource");
        return JavaANGLE.glGetShaderSource(shader);
    }

    @Override
    public void getShaderPrecisionFormat(int shaderType, int precisionType, IntBuffer range, IntBuffer precision) {
        report("void getShaderPrecisionFormat");
        if (range == null || precision == null) return;
        
    }

    @Override
    public void getVertexAttribfv(int index, int pname, FloatBuffer params) {
        report("void getVertexAttribfv");
        if (params == null) return;
        
    }

    @Override
    public void getVertexAttribiv(int index, int pname, IntBuffer params) {
        report("void getVertexAttribiv");
        if (params == null) return;
        
    }

    @Override
    public void getVertexAttribPointerv(int index, int pname, ByteBuffer data) {
        report("void getVertexAttribPointerv");
        if (data == null) return;
        
    }

    @Override
    public void getUniformfv(int program, int location, FloatBuffer params) {
        report("void getUniformfv");
        if (params == null) return;
        
    }

    @Override
    public void getUniformiv(int program, int location, IntBuffer params) {
        report("void getUniformiv");
        if (params == null) return;
        
    }

    @Override
    public boolean isProgram(int program) {
        report("boolean isProgram");
        return JavaANGLE.glIsProgram(program);
    }

    @Override
    public void getProgramiv(int program, int pname, IntBuffer params) {
        report("void getProgramiv");
        if (params == null) return;
        
    }

    @Override
    public String getProgramInfoLog(int program) {
        report("String getProgramInfoLog");
        return JavaANGLE.glGetProgramInfoLog(program);
    }

    @Override
    public void scissor(int x, int y, int w, int h) {
        report("void scissor");
        
    }

    @Override
    public void sampleCoverage(float value, boolean invert) {
        report("void sampleCoverage");
        
    }

    @Override
    public void stencilFunc(int func, int ref, int mask) {
        report("void stencilFunc");
        
    }

    @Override
    public void stencilFuncSeparate(int face, int func, int ref, int mask) {
        report("void stencilFuncSeparate");
        
    }

    @Override
    public void stencilOp(int sfail, int dpfail, int dppass) {
        report("void stencilOp");
        
    }

    @Override
    public void stencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        report("void stencilOpSeparate");
        
    }

    @Override
    public void depthFunc(int func) {
        report("void depthFunc");
        
    }

    @Override
    public void blendEquation(int mode) {
        report("void blendEquation");
        
    }

    @Override
    public void blendEquationSeparate(int modeRGB, int modeAlpha) {
        report("void blendEquationSeparate");
        
    }

    @Override
    public void blendFunc(int src, int dst) {
        report("void blendFunc");
        
    }

    @Override
    public void blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        report("void blendFuncSeparate");
        
    }

    @Override
    public void blendColor(float red, float green, float blue, float alpha) {
        report("void blendColor");
        
    }

    @Override
    public void colorMask(boolean r, boolean g, boolean b, boolean a) {
        report("void colorMask");
        
    }

    @Override
    public void depthMask(boolean mask) {
        report("void depthMask");
        
    }

    @Override
    public void stencilMask(int mask) {
        report("void stencilMask");
        
    }

    @Override
    public void stencilMaskSeparate(int face, int mask) {
        report("void stencilMaskSeparate");
        
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        report("void clearColor");
        
    }

    @Override
    public void clearDepth(float d) {
        report("void clearDepth");
        
    }

    @Override
    public void clearStencil(int s) {
        report("void clearStencil");
        
    }

    @Override
    public void clear(int buf) {
        report("void clear");
        
    }

    @Override
    protected void bindFramebufferImpl(int target, int framebuffer) {
        report("void bindFramebufferImpl");
        
    }

    @Override
    public void deleteFramebuffers(int n, IntBuffer framebuffers) {
        report("void deleteFramebuffers");
        if (framebuffers == null) return;
        
    }

    @Override
    public void genFramebuffers(int n, IntBuffer framebuffers) {
        report("void genFramebuffers");
        if (framebuffers == null) return;
        
    }

    @Override
    public void bindRenderbuffer(int target, int renderbuffer) {
        report("void bindRenderbuffer");
        
    }

    @Override
    public void deleteRenderbuffers(int n, IntBuffer renderbuffers) {
        report("void deleteRenderbuffers");
        if (renderbuffers == null) return;
        
    }

    @Override
    public void genRenderbuffers(int n, IntBuffer renderbuffers) {
        report("void genRenderbuffers");
        if (renderbuffers == null) return;
        
    }

    @Override
    public void renderbufferStorage(int target, int internalFormat, int width, int height) {
        report("void renderbufferStorage");
        
    }

    @Override
    public void framebufferRenderbuffer(int target, int attachment, int rendbuferfTarget, int renderbuffer) {
        report("void framebufferRenderbuffer");
        
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int texTarget, int texture, int level) {
        report("void framebufferTexture2D");
        
    }

    @Override
    public int checkFramebufferStatus(int target) {
        report("int checkFramebufferStatus");
        return JavaANGLE.glCheckFramebufferStatus(target);
    }

    @Override
    public boolean isFramebuffer(int framebuffer) {
        report("boolean isFramebuffer");
        return JavaANGLE.glIsFramebuffer(framebuffer);
    }

    @Override
    public void getFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        report("void getFramebufferAttachmentParameteriv");
        if (params == null) return;
        
    }

    @Override
    public boolean isRenderbuffer(int renderbuffer) {
        report("boolean isRenderbuffer");
        return JavaANGLE.glIsRenderbuffer(renderbuffer);
    }

    @Override
    public void getRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        report("void getRenderbufferParameteriv");
        if (params == null) return;
        
    }

    @Override
    public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        report("void blitFramebuffer");
        
    }

    @Override
    public void renderbufferStorageMultisample(int target, int samples, int format, int width, int height) {
        report("void renderbufferStorageMultisample");
        
    }

    @Override
    public void readBuffer(int buf) {
        report("void readBuffer");
        
    }

    @Override
    public void drawBuffer(int buf) {
        report("void drawBuffer");
        
    }
    
}
