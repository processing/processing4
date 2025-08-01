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

import javaangle.GL;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PSurface;

public class PGLANGLE extends PGL {

    
    ///////////////////////////////////////////////////////////

    // Constants

    static {
        FALSE = GL.GL_FALSE;
        TRUE  = GL.GL_TRUE;

        INT            = GL.GL_INT;
        BYTE           = GL.GL_BYTE;
        SHORT          = GL.GL_SHORT;
        FLOAT          = GL.GL_FLOAT;
        BOOL           = GL.GL_BOOL;
        UNSIGNED_INT   = GL.GL_UNSIGNED_INT;
        UNSIGNED_BYTE  = GL.GL_UNSIGNED_BYTE;
        UNSIGNED_SHORT = GL.GL_UNSIGNED_SHORT;

        RGB             = GL.GL_RGB;
        RGBA            = GL.GL_RGBA;
        ALPHA           = GL.GL_ALPHA;
        LUMINANCE       = GL.GL_LUMINANCE;
        LUMINANCE_ALPHA = GL.GL_LUMINANCE_ALPHA;

        UNSIGNED_SHORT_5_6_5   = GL.GL_UNSIGNED_SHORT_5_6_5;
        UNSIGNED_SHORT_4_4_4_4 = GL.GL_UNSIGNED_SHORT_4_4_4_4;
        UNSIGNED_SHORT_5_5_5_1 = GL.GL_UNSIGNED_SHORT_5_5_5_1;

        RGBA4   = GL.GL_RGBA4;
        RGB5_A1 = GL.GL_RGB5_A1;
        RGB565  = GL.GL_RGB565;
        RGB8    = GL.GL_RGB8;
        RGBA8   = GL.GL_RGBA8;
        ALPHA8  = GL.GL_ALPHA8;

        READ_ONLY  = GL.GL_READ_ONLY;
        WRITE_ONLY = GL.GL_WRITE_ONLY;
        READ_WRITE = GL.GL_READ_WRITE;

        TESS_WINDING_NONZERO = GLU.GLU_TESS_WINDING_NONZERO;
        TESS_WINDING_ODD     = GLU.GLU_TESS_WINDING_ODD;
        TESS_EDGE_FLAG       = GLU.GLU_TESS_EDGE_FLAG;

        GENERATE_MIPMAP_HINT = 0;   // TODO
        FASTEST              = GL.GL_FASTEST;
        NICEST               = GL.GL_NICEST;
        DONT_CARE            = GL.GL_DONT_CARE;

        VENDOR                   = GL.GL_VENDOR;
        RENDERER                 = GL.GL_RENDERER;
        VERSION                  = GL.GL_VERSION;
        EXTENSIONS               = GL.GL_EXTENSIONS;
        SHADING_LANGUAGE_VERSION = GL.GL_SHADING_LANGUAGE_VERSION;

        MAX_SAMPLES = GL.GL_MAX_SAMPLES;
        SAMPLES     = GL.GL_SAMPLES;

        ALIASED_LINE_WIDTH_RANGE = GL.GL_ALIASED_LINE_WIDTH_RANGE;
        ALIASED_POINT_SIZE_RANGE = GL.GL_ALIASED_POINT_SIZE_RANGE;

        DEPTH_BITS   = GL.GL_DEPTH_BITS;
        STENCIL_BITS = GL.GL_STENCIL_BITS;

        CCW = GL.GL_CCW;
        CW  = GL.GL_CW;

        VIEWPORT = GL.GL_VIEWPORT;

        ARRAY_BUFFER         = GL.GL_ARRAY_BUFFER;
        ELEMENT_ARRAY_BUFFER = GL.GL_ELEMENT_ARRAY_BUFFER;
        PIXEL_PACK_BUFFER    = GL.GL_PIXEL_PACK_BUFFER;

        MAX_VERTEX_ATTRIBS  = GL.GL_MAX_VERTEX_ATTRIBS;

        STATIC_DRAW  = GL.GL_STATIC_DRAW;
        DYNAMIC_DRAW = GL.GL_DYNAMIC_DRAW;
        STREAM_DRAW  = GL.GL_STREAM_DRAW;
        STREAM_READ  = GL.GL_STREAM_READ;

        BUFFER_SIZE  = GL.GL_BUFFER_SIZE;
        BUFFER_USAGE = GL.GL_BUFFER_USAGE;

        POINTS         = GL.GL_POINTS;
        LINE_STRIP     = GL.GL_LINE_STRIP;
        LINE_LOOP      = GL.GL_LINE_LOOP;
        LINES          = GL.GL_LINES;
        TRIANGLE_FAN   = GL.GL_TRIANGLE_FAN;
        TRIANGLE_STRIP = GL.GL_TRIANGLE_STRIP;
        TRIANGLES      = GL.GL_TRIANGLES;

        CULL_FACE      = GL.GL_CULL_FACE;
        FRONT          = GL.GL_FRONT;
        BACK           = GL.GL_BACK;
        FRONT_AND_BACK = GL.GL_FRONT_AND_BACK;

        POLYGON_OFFSET_FILL = GL.GL_POLYGON_OFFSET_FILL;

        UNPACK_ALIGNMENT = GL.GL_UNPACK_ALIGNMENT;
        PACK_ALIGNMENT   = GL.GL_PACK_ALIGNMENT;

        TEXTURE_2D        = GL.GL_TEXTURE_2D;
        TEXTURE_RECTANGLE = 0;  // TODO

        TEXTURE_BINDING_2D        = GL.GL_TEXTURE_BINDING_2D;
        TEXTURE_BINDING_RECTANGLE = 0;  // TODO

        MAX_TEXTURE_SIZE           = GL.GL_MAX_TEXTURE_SIZE;
        TEXTURE_MAX_ANISOTROPY     = 0;  // TODO
        MAX_TEXTURE_MAX_ANISOTROPY = 0;  // TODO

        MAX_VERTEX_TEXTURE_IMAGE_UNITS   = GL.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
        MAX_TEXTURE_IMAGE_UNITS          = GL.GL_MAX_TEXTURE_IMAGE_UNITS;
        MAX_COMBINED_TEXTURE_IMAGE_UNITS = GL.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

        NUM_COMPRESSED_TEXTURE_FORMATS = GL.GL_NUM_COMPRESSED_TEXTURE_FORMATS;
        COMPRESSED_TEXTURE_FORMATS     = GL.GL_COMPRESSED_TEXTURE_FORMATS;

        NEAREST               = GL.GL_NEAREST;
        LINEAR                = GL.GL_LINEAR;
        LINEAR_MIPMAP_NEAREST = GL.GL_LINEAR_MIPMAP_NEAREST;
        LINEAR_MIPMAP_LINEAR  = GL.GL_LINEAR_MIPMAP_LINEAR;

        CLAMP_TO_EDGE = GL.GL_CLAMP_TO_EDGE;
        REPEAT        = GL.GL_REPEAT;

        TEXTURE0           = GL.GL_TEXTURE0;
        TEXTURE1           = GL.GL_TEXTURE1;
        TEXTURE2           = GL.GL_TEXTURE2;
        TEXTURE3           = GL.GL_TEXTURE3;
        TEXTURE_MIN_FILTER = GL.GL_TEXTURE_MIN_FILTER;
        TEXTURE_MAG_FILTER = GL.GL_TEXTURE_MAG_FILTER;
        TEXTURE_WRAP_S     = GL.GL_TEXTURE_WRAP_S;
        TEXTURE_WRAP_T     = GL.GL_TEXTURE_WRAP_T;
        TEXTURE_WRAP_R     = GL.GL_TEXTURE_WRAP_R;

        TEXTURE_CUBE_MAP = GL.GL_TEXTURE_CUBE_MAP;
        TEXTURE_CUBE_MAP_POSITIVE_X = GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
        TEXTURE_CUBE_MAP_POSITIVE_Y = GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
        TEXTURE_CUBE_MAP_POSITIVE_Z = GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
        TEXTURE_CUBE_MAP_NEGATIVE_X = GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
        TEXTURE_CUBE_MAP_NEGATIVE_Y = GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
        TEXTURE_CUBE_MAP_NEGATIVE_Z = GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;

        VERTEX_SHADER        = GL.GL_VERTEX_SHADER;
        FRAGMENT_SHADER      = GL.GL_FRAGMENT_SHADER;
        INFO_LOG_LENGTH      = GL.GL_INFO_LOG_LENGTH;
        SHADER_SOURCE_LENGTH = GL.GL_SHADER_SOURCE_LENGTH;
        COMPILE_STATUS       = GL.GL_COMPILE_STATUS;
        LINK_STATUS          = GL.GL_LINK_STATUS;
        VALIDATE_STATUS      = GL.GL_VALIDATE_STATUS;
        SHADER_TYPE          = GL.GL_SHADER_TYPE;
        DELETE_STATUS        = GL.GL_DELETE_STATUS;

        FLOAT_VEC2   = GL.GL_FLOAT_VEC2;
        FLOAT_VEC3   = GL.GL_FLOAT_VEC3;
        FLOAT_VEC4   = GL.GL_FLOAT_VEC4;
        FLOAT_MAT2   = GL.GL_FLOAT_MAT2;
        FLOAT_MAT3   = GL.GL_FLOAT_MAT3;
        FLOAT_MAT4   = GL.GL_FLOAT_MAT4;
        INT_VEC2     = GL.GL_INT_VEC2;
        INT_VEC3     = GL.GL_INT_VEC3;
        INT_VEC4     = GL.GL_INT_VEC4;
        BOOL_VEC2    = GL.GL_BOOL_VEC2;
        BOOL_VEC3    = GL.GL_BOOL_VEC3;
        BOOL_VEC4    = GL.GL_BOOL_VEC4;
        SAMPLER_2D   = GL.GL_SAMPLER_2D;
        SAMPLER_CUBE = GL.GL_SAMPLER_CUBE;

        LOW_FLOAT    = GL.GL_LOW_FLOAT;
        MEDIUM_FLOAT = GL.GL_MEDIUM_FLOAT;
        HIGH_FLOAT   = GL.GL_HIGH_FLOAT;
        LOW_INT      = GL.GL_LOW_INT;
        MEDIUM_INT   = GL.GL_MEDIUM_INT;
        HIGH_INT     = GL.GL_HIGH_INT;

        CURRENT_VERTEX_ATTRIB = GL.GL_CURRENT_VERTEX_ATTRIB;

        VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = GL.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
        VERTEX_ATTRIB_ARRAY_ENABLED        = GL.GL_VERTEX_ATTRIB_ARRAY_ENABLED;
        VERTEX_ATTRIB_ARRAY_SIZE           = GL.GL_VERTEX_ATTRIB_ARRAY_SIZE;
        VERTEX_ATTRIB_ARRAY_STRIDE         = GL.GL_VERTEX_ATTRIB_ARRAY_STRIDE;
        VERTEX_ATTRIB_ARRAY_TYPE           = GL.GL_VERTEX_ATTRIB_ARRAY_TYPE;
        VERTEX_ATTRIB_ARRAY_NORMALIZED     = GL.GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
        VERTEX_ATTRIB_ARRAY_POINTER        = GL.GL_VERTEX_ATTRIB_ARRAY_POINTER;

        BLEND               = GL.GL_BLEND;
        ONE                 = GL.GL_ONE;
        ZERO                = GL.GL_ZERO;
        SRC_ALPHA           = GL.GL_SRC_ALPHA;
        DST_ALPHA           = GL.GL_DST_ALPHA;
        ONE_MINUS_SRC_ALPHA = GL.GL_ONE_MINUS_SRC_ALPHA;
        ONE_MINUS_DST_COLOR = GL.GL_ONE_MINUS_DST_COLOR;
        ONE_MINUS_SRC_COLOR = GL.GL_ONE_MINUS_SRC_COLOR;
        DST_COLOR           = GL.GL_DST_COLOR;
        SRC_COLOR           = GL.GL_SRC_COLOR;

        SAMPLE_ALPHA_TO_COVERAGE = GL.GL_SAMPLE_ALPHA_TO_COVERAGE;
        SAMPLE_COVERAGE          = GL.GL_SAMPLE_COVERAGE;

        KEEP      = GL.GL_KEEP;
        REPLACE   = GL.GL_REPLACE;
        INCR      = GL.GL_INCR;
        DECR      = GL.GL_DECR;
        INVERT    = GL.GL_INVERT;
        INCR_WRAP = GL.GL_INCR_WRAP;
        DECR_WRAP = GL.GL_DECR_WRAP;
        NEVER     = GL.GL_NEVER;
        ALWAYS    = GL.GL_ALWAYS;

        EQUAL    = GL.GL_EQUAL;
        LESS     = GL.GL_LESS;
        LEQUAL   = GL.GL_LEQUAL;
        GREATER  = GL.GL_GREATER;
        GEQUAL   = GL.GL_GEQUAL;
        NOTEQUAL = GL.GL_NOTEQUAL;

        FUNC_ADD              = GL.GL_FUNC_ADD;
        FUNC_MIN              = GL.GL_MIN;
        FUNC_MAX              = GL.GL_MAX;
        FUNC_REVERSE_SUBTRACT = GL.GL_FUNC_REVERSE_SUBTRACT;
        FUNC_SUBTRACT         = GL.GL_FUNC_SUBTRACT;

        DITHER = GL.GL_DITHER;

        CONSTANT_COLOR           = GL.GL_CONSTANT_COLOR;
        CONSTANT_ALPHA           = GL.GL_CONSTANT_ALPHA;
        ONE_MINUS_CONSTANT_COLOR = GL.GL_ONE_MINUS_CONSTANT_COLOR;
        ONE_MINUS_CONSTANT_ALPHA = GL.GL_ONE_MINUS_CONSTANT_ALPHA;
        SRC_ALPHA_SATURATE       = GL.GL_SRC_ALPHA_SATURATE;

        SCISSOR_TEST    = GL.GL_SCISSOR_TEST;
        STENCIL_TEST    = GL.GL_STENCIL_TEST;
        DEPTH_TEST      = GL.GL_DEPTH_TEST;
        DEPTH_WRITEMASK = GL.GL_DEPTH_WRITEMASK;

        COLOR_BUFFER_BIT   = GL.GL_COLOR_BUFFER_BIT;
        DEPTH_BUFFER_BIT   = GL.GL_DEPTH_BUFFER_BIT;
        STENCIL_BUFFER_BIT = GL.GL_STENCIL_BUFFER_BIT;

        FRAMEBUFFER        = GL.GL_FRAMEBUFFER;
        COLOR_ATTACHMENT0  = GL.GL_COLOR_ATTACHMENT0;
        COLOR_ATTACHMENT1  = GL.GL_COLOR_ATTACHMENT1;
        COLOR_ATTACHMENT2  = GL.GL_COLOR_ATTACHMENT2;
        COLOR_ATTACHMENT3  = GL.GL_COLOR_ATTACHMENT3;
        RENDERBUFFER       = GL.GL_RENDERBUFFER;
        DEPTH_ATTACHMENT   = GL.GL_DEPTH_ATTACHMENT;
        STENCIL_ATTACHMENT = GL.GL_STENCIL_ATTACHMENT;
        READ_FRAMEBUFFER   = GL.GL_READ_FRAMEBUFFER;
        DRAW_FRAMEBUFFER   = GL.GL_DRAW_FRAMEBUFFER;

        DEPTH24_STENCIL8 = GL.GL_DEPTH24_STENCIL8;

        DEPTH_COMPONENT   = GL.GL_DEPTH_COMPONENT;
        DEPTH_COMPONENT16 = GL.GL_DEPTH_COMPONENT16;
        DEPTH_COMPONENT24 = GL.GL_DEPTH_COMPONENT24;
        DEPTH_COMPONENT32 = 0;   // TODO
        STENCIL_INDEX  = GL.GL_STENCIL_INDEX;
        STENCIL_INDEX1 = 0;  // TODO
        STENCIL_INDEX4 = 0;  // TODO
        STENCIL_INDEX8 = GL.GL_STENCIL_INDEX8;

        DEPTH_STENCIL = GL.GL_DEPTH_STENCIL;

        FRAMEBUFFER_COMPLETE                      = GL.GL_FRAMEBUFFER_COMPLETE;
        FRAMEBUFFER_UNDEFINED                     = GL.GL_FRAMEBUFFER_UNDEFINED;
        FRAMEBUFFER_INCOMPLETE_ATTACHMENT         = GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_DIMENSIONS         = GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
        FRAMEBUFFER_INCOMPLETE_FORMATS            = 0;  // TODO
        FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        = 0;  // TODO
        FRAMEBUFFER_INCOMPLETE_READ_BUFFER        = 0;  // TODO
        FRAMEBUFFER_UNSUPPORTED                   = GL.GL_FRAMEBUFFER_UNSUPPORTED;
        FRAMEBUFFER_INCOMPLETE_MULTISAMPLE        = GL.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
        FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS      = GL.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;

        FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE           = GL.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE;
        FRAMEBUFFER_ATTACHMENT_OBJECT_NAME           = GL.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL         = GL.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = GL.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE;

        RENDERBUFFER_WIDTH           = GL.GL_RENDERBUFFER_WIDTH;
        RENDERBUFFER_HEIGHT          = GL.GL_RENDERBUFFER_HEIGHT;
        RENDERBUFFER_RED_SIZE        = GL.GL_RENDERBUFFER_RED_SIZE;
        RENDERBUFFER_GREEN_SIZE      = GL.GL_RENDERBUFFER_GREEN_SIZE;
        RENDERBUFFER_BLUE_SIZE       = GL.GL_RENDERBUFFER_BLUE_SIZE;
        RENDERBUFFER_ALPHA_SIZE      = GL.GL_RENDERBUFFER_ALPHA_SIZE;
        RENDERBUFFER_DEPTH_SIZE      = GL.GL_RENDERBUFFER_DEPTH_SIZE;
        RENDERBUFFER_STENCIL_SIZE    = GL.GL_RENDERBUFFER_STENCIL_SIZE;
        RENDERBUFFER_INTERNAL_FORMAT = GL.GL_RENDERBUFFER_INTERNAL_FORMAT;

        MULTISAMPLE    = 0;   // TODO
        LINE_SMOOTH    = GL.GL_LINE_SMOOTH;
        POLYGON_SMOOTH = GL.GL_POLYGON_SMOOTH;

        SYNC_GPU_COMMANDS_COMPLETE = GL.GL_SYNC_GPU_COMMANDS_COMPLETE;
        ALREADY_SIGNALED           = GL.GL_ALREADY_SIGNALED;
        CONDITION_SATISFIED        = GL.GL_CONDITION_SATISFIED;
    }
    
    protected float[] projMatrix;
    protected FloatBuffer projMatrixBuffer;
    protected float[] mvMatrix;

    private HashMap<Integer, Buffer> buffers = new HashMap<Integer, Buffer>();
    private int boundBuffer = -1;

    private void report(String m) {
        // System.out.println(m);
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
        return GL.GL_DEPTH_BITS;
    }

    @Override
    protected int getStencilBits() {
        report("int getStencilBits");
        return GL.GL_STENCIL_BITS;
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


        GL.glMatrixMode(GL.GL_PROJECTION);
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
        GL.glLoadMatrixf(projMatrixBuffer);

        if (mvMatrix == null) {
            mvMatrix = new float[16];
        }
        GL.glMatrixMode(GL.GL_MODELVIEW);
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
        GL.glLoadMatrixf(projMatrixBuffer);
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
        return GL.GL_ES_VERSION_3_1;
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
        GL.glFlush();
    }

    @Override
    public void finish() {
        report("void finish");
        GL.glFinish();
    }

    @Override
    public void hint(int target, int hint) {
        report("void hint");
        GL.glHint(target, hint);
    }

    @Override
    public void enable(int value) {
        report("void enable");
        if (-1 < value) {
            GL.glEnable(value);
        }
    }

    @Override
    public void disable(int value) {
        report("void disable");
        if (-1 < value) {
            GL.glDisable(value);
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
            GL.glGetBooleanv(value, byteBuffer.asIntBuffer());
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
            GL.glGetIntegerv(value, data);
        } else {
            fillIntBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public void getFloatv(int value, FloatBuffer data) {
        report("void getFloatv");
        if (data == null) return;
        if (-1 < value) {
            GL.glGetFloatv(value, data);
        } else {
            fillFloatBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public boolean isEnabled(int value) {
        report("boolean isEnabled");
        return GL.glIsEnabled(value);
    }

    @Override
    public String getString(int name) {
        report("String getString");
        return GL.glGetString(name);
    }

    @Override
    public int getError() {
        report("int getError");
        return GL.glGetError();
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
        GL.glGenBuffers(n, buffers);
    }

    @Override
    public void deleteBuffers(int n, IntBuffer buffers) {
        report("void deleteBuffers");
        if (buffers == null) return;
        GL.glDeleteBuffers(n, buffers);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        report("void bindBuffer");
        boundBuffer = buffer;
        GL.glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, int size, Buffer data, int usage) {
        report("void bufferData");
        if (data == null) return;
        buffers.put(boundBuffer, data);
        GL.glBufferData(target, size, data, usage);
    }

    @Override
    public void bufferSubData(int target, int offset, int size, Buffer data) {
        report("void bufferSubData");
        if (data == null) return;
        // buffers.put(boundBuffer, data);

        // TODO: Oh god we got to write our own buffersubdata for the
        // buffers hashmap oh god.

        GL.glBufferSubData(target, offset, size, data);
    }

    @Override
    public void isBuffer(int buffer) {
        report("void isBuffer");
        GL.glIsBuffer(buffer);
    }

    @Override
    public void getBufferParameteriv(int target, int value, IntBuffer data) {
        report("void getBufferParameteriv");
        if (data == null) return;
        GL.glGetBufferParameteriv(target, value, data);
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
        // return GL.glMapBufferRange(target, offset, length, access);
        return null;
    }

    @Override
    public void unmapBuffer(int target) {
        report("void unmapBuffer");
        GL.glUnmapBuffer(target);
        
    }

    @Override
    public long fenceSync(int condition, int flags) {
        report("long fenceSync");
        return GL.glFenceSync(condition, flags);
    }

    @Override
    public void deleteSync(long sync) {
        report("void deleteSync");
        GL.glDeleteSync(sync);
    }

    @Override
    public int clientWaitSync(long sync, int flags, long timeout) {
        report("int clientWaitSync");
        return GL.glClientWaitSync(sync, flags, timeout);
    }

    @Override
    public void depthRangef(float n, float f) {
        report("void depthRangef");
        // TODO: Oops.
        // GL.glDepthRange(n, f);
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
        GL.glViewport(x, y, w, h);
    }

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, Buffer buffer) {
        report("void readPixelsImpl");
        if (buffer == null) return;
        GL.glReadPixels(x, y, width, height, format, type, buffer);
    }

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, long offset) {
        report("void readPixelsImpl");
        // TODO
        // GL.glReadPixels(x, y, width, height, format, type, 0);
    }

    @Override
    public void vertexAttrib1f(int index, float value) {
        report("void vertexAttrib1f");
        GL.glVertexAttrib1f(index, value);
    }

    @Override
    public void vertexAttrib2f(int index, float value0, float value1) {
        report("void vertexAttrib2f");
        GL.glVertexAttrib2f(index, value0, value1);
    }

    @Override
    public void vertexAttrib3f(int index, float value0, float value1, float value2) {
        report("void vertexAttrib3f");
        GL.glVertexAttrib3f(index, value0, value1, value2);
    }

    @Override
    public void vertexAttrib4f(int index, float value0, float value1, float value2, float value3) {
        report("void vertexAttrib4f");
        GL.glVertexAttrib4f(index, value0, value1, value2, value3);
    }

    @Override
    public void vertexAttrib1fv(int index, FloatBuffer values) {
        report("void vertexAttrib1fv");
        if (values == null) return;
        GL.glVertexAttrib1fv(index, values);
    }

    @Override
    public void vertexAttrib2fv(int index, FloatBuffer values) {
        report("void vertexAttrib2fv");
        if (values == null) return;
        GL.glVertexAttrib2fv(index, values);
    }

    @Override
    public void vertexAttrib3fv(int index, FloatBuffer values) {
        report("void vertexAttrib3fv");
        if (values == null) return;
        GL.glVertexAttrib3fv(index, values);
    }

    @Override
    public void vertexAttrib4fv(int index, FloatBuffer values) {
        report("void vertexAttrib4fv");
        if (values == null) return;
        GL.glVertexAttrib4fv(index, values);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        report("void vertexAttribPointer");
        GL.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void enableVertexAttribArray(int index) {
        report("void enableVertexAttribArray");
        GL.glEnableVertexAttribArray(index);
    }

    @Override
    public void disableVertexAttribArray(int index) {
        report("void disableVertexAttribArray");
        GL.glDisableVertexAttribArray(index);
    }

    @Override
    public void drawArraysImpl(int mode, int first, int count) {
        report("void drawArraysImpl");
        GL.glDrawArrays(mode, first, count);
    }

    @Override
    public void drawElementsImpl(int mode, int count, int type, int offset) {
        report("void drawElementsImpl");
        Buffer buff = buffers.get(boundBuffer);
        if (buff == null) {
            System.err.println("drawElementsImpl: unbound buffer");
            return;
        }
        GL.glDrawElements(mode, count, type, buff, offset);
    }

    @Override
    public void lineWidth(float width) {
        report("void lineWidth");
        GL.glLineWidth(width);
    }

    @Override
    public void frontFace(int dir) {
        report("void frontFace");
        GL.glFrontFace(dir);
    }

    @Override
    public void cullFace(int mode) {
        report("void cullFace");
        GL.glCullFace(mode);
    }

    @Override
    public void polygonOffset(float factor, float units) {
        report("void polygonOffset");
        GL.glPolygonOffset(factor, units);
    }

    @Override
    public void pixelStorei(int pname, int param) {
        report("void pixelStorei");
        GL.glPixelStorei(pname, param);
        
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer data) {
        report("void texImage2D");
        if (data == null) return;
        GL.glTexImage2D(target, level, internalFormat, width, height, border, format, type, data);
    }

    @Override
    public void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
        report("void copyTexImage2D");
        GL.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
        
    }

    @Override
    public void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, Buffer data) {
        report("void texSubImage2D");
        if (data == null) return;
        //GL.glTextSubImage2D(target, level, xOffset, yOffset, width, height, format, type, data);
    }

    @Override
    public void copyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
        report("void copyTexSubImage2D");
        GL.glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height);
    }

    @Override
    public void compressedTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int imageSize, Buffer data) {
        report("void compressedTexImage2D");
        if (data == null) return;
        GL.glCompressedTexImage2D(target, level, internalFormat, width, height, border, imageSize, data);
    }

    @Override
    public void compressedTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int imageSize, Buffer data) {
        report("void compressedTexSubImage2D");
        if (data == null) return;
        
    }

    @Override
    public void texParameteri(int target, int pname, int param) {
        report("void texParameteri");
        GL.glTexParameteri(target, pname, param);
    }

    @Override
    public void texParameterf(int target, int pname, float param) {
        report("void texParameterf");
        GL.glTexParameterf(target, pname, param);
    }

    @Override
    public void texParameteriv(int target, int pname, IntBuffer params) {
        report("void texParameteriv");
        if (params == null) return;
        GL.glTexParameteriv(target, pname, params);
    }

    @Override
    public void texParameterfv(int target, int pname, FloatBuffer params) {
        report("void texParameterfv");
        if (params == null) return;
        GL.glTexParameterfv(target, pname, params);
    }

    @Override
    public void generateMipmap(int target) {
        report("void generateMipmap");
        GL.glGenerateMipmap(target);
    }

    @Override
    public void genTextures(int n, IntBuffer textures) {
        report("void genTextures");
        if (textures == null) return;
        GL.glGenTextures(n, textures);
    }

    @Override
    public void deleteTextures(int n, IntBuffer textures) {
        report("void deleteTextures");
        if (textures == null) return;
        GL.glDeleteTextures(n, textures);
    }

    @Override
    public void getTexParameteriv(int target, int pname, IntBuffer params) {
        report("void getTexParameteriv");
        if (params == null) return;
        GL.glGetTexParameteriv(target, pname, params);
    }

    @Override
    public void getTexParameterfv(int target, int pname, FloatBuffer params) {
        report("void getTexParameterfv");
        if (params == null) return;
        GL.glGetTexParameterfv(target, pname, params);
    }

    @Override
    public boolean isTexture(int texture) {
        report("boolean isTexture");
        return GL.glIsTexture(texture);
    }

    @Override
    protected void activeTextureImpl(int texture) {
        report("void activeTextureImpl");
        GL.glActiveTexture(texture);
    }

    @Override
    protected void bindTextureImpl(int target, int texture) {
        report("void bindTextureImpl");
        GL.glBindTexture(target, texture);
    }

    @Override
    public int createShader(int type) {
        report("int createShader");
        return GL.glCreateShader(type);
    }

    @Override
    public void shaderSource(int shader, String source) {
        // System.out.println(source);
        report("void shaderSource");
        GL.glShaderSource(shader, 1, new String[] { source });
    }

    @Override
    public void compileShader(int shader) {
        report("void compileShader");
        GL.glCompileShader(shader);
    }

    @Override
    public void releaseShaderCompiler() {
        report("void releaseShaderCompiler");
        GL.glReleaseShaderCompiler();
    }

    @Override
    public void deleteShader(int shader) {
        report("void deleteShader");
        GL.glDeleteShader(shader);
    }

    @Override
    public void shaderBinary(int count, IntBuffer shaders, int binaryFormat, Buffer binary, int length) {
        report("void shaderBinary");
        if (shaders == null || binary == null) return;
        GL.glShaderBinary(count, shaders, binaryFormat, binary, length);
    }

    @Override
    public int createProgram() {
        report("int createProgram");
        return GL.glCreateProgram();
    }

    @Override
    public void attachShader(int program, int shader) {
        report("void attachShader");
        GL.glAttachShader(program, shader);
    }

    @Override
    public void detachShader(int program, int shader) {
        report("void detachShader");
        GL.glDetachShader(program, shader);
    }

    @Override
    public void linkProgram(int program) {
        report("void linkProgram");
        GL.glLinkProgram(program);
    }

    @Override
    public void useProgram(int program) {
        report("void useProgram");
        GL.glUseProgram(program);
    }

    @Override
    public void deleteProgram(int program) {
        report("void deleteProgram");
        GL.glDeleteProgram(program);
    }

    @Override
    public String getActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        report("String getActiveAttrib");
        if (size == null || type == null) return null;
        return GL.glGetActiveAttrib(program, index, size, type);
    }

    @Override
    public int getAttribLocation(int program, String name) {
        report("int getAttribLocation");
        return GL.glGetAttribLocation(program, name);
    }

    @Override
    public void bindAttribLocation(int program, int index, String name) {
        report("void bindAttribLocation");
        GL.glBindAttribLocation(program, index, name);
    }

    @Override
    public int getUniformLocation(int program, String name) {
        report("int getUniformLocation");
        return GL.glGetUniformLocation(program, name);
    }

    @Override
    public String getActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        report("String getActiveUniform");
        if (size == null || type == null) return null;
        return GL.glGetActiveUniform(program, index, size, type);
    }

    @Override
    public void uniform1i(int location, int value) {
        report("void uniform1i");
        GL.glUniform1i(location, value);
    }

    @Override
    public void uniform2i(int location, int value0, int value1) {
        report("void uniform2i");
        GL.glUniform2i(location, value0, value1);
    }

    @Override
    public void uniform3i(int location, int value0, int value1, int value2) {
        report("void uniform3i");
        GL.glUniform3i(location, value0, value1, value2);
    }

    @Override
    public void uniform4i(int location, int value0, int value1, int value2, int value3) {
        report("void uniform4i");
        GL.glUniform4i(location, value0, value1, value2, value3);
    }

    @Override
    public void uniform1f(int location, float value) {
        report("void uniform1f");
        GL.glUniform1f(location, value);
    }

    @Override
    public void uniform2f(int location, float value0, float value1) {
        report("void uniform2f");
        GL.glUniform2f(location, value0, value1);
    }

    @Override
    public void uniform3f(int location, float value0, float value1, float value2) {
        report("void uniform3f");
        GL.glUniform3f(location, value0, value1, value2);
    }

    @Override
    public void uniform4f(int location, float value0, float value1, float value2, float value3) {
        report("void uniform4f");
        GL.glUniform4f(location, value0, value1, value2, value3);
    }

    @Override
    public void uniform1iv(int location, int count, IntBuffer v) {
        report("void uniform1iv");
        if (v == null) return;
        GL.glUniform1iv(location, count, v);
    }

    @Override
    public void uniform2iv(int location, int count, IntBuffer v) {
        report("void uniform2iv");
        if (v == null) return;
        GL.glUniform2iv(location, count, v);
    }

    @Override
    public void uniform3iv(int location, int count, IntBuffer v) {
        report("void uniform3iv");
        if (v == null) return;
        GL.glUniform3iv(location, count, v);
    }

    @Override
    public void uniform4iv(int location, int count, IntBuffer v) {
        report("void uniform4iv");
        if (v == null) return;
        GL.glUniform4iv(location, count, v);
    }

    @Override
    public void uniform1fv(int location, int count, FloatBuffer v) {
        report("void uniform1fv");
        if (v == null) return;
        GL.glUniform1fv(location, count, v);
    }

    @Override
    public void uniform2fv(int location, int count, FloatBuffer v) {
        report("void uniform2fv");
        if (v == null) return;
        GL.glUniform2fv(location, count, v);
    }

    @Override
    public void uniform3fv(int location, int count, FloatBuffer v) {
        report("void uniform3fv");
        if (v == null) return;
        GL.glUniform3fv(location, count, v);
    }

    @Override
    public void uniform4fv(int location, int count, FloatBuffer v) {
        report("void uniform4fv");
        if (v == null) return;
        GL.glUniform4fv(location, count, v);
    }

    @Override
    public void uniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix2fv");
        if (mat == null) return;
        GL.glUniformMatrix2fv(location, count, transpose, mat);
    }

    @Override
    public void uniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix3fv");
        if (mat == null) return;
        GL.glUniformMatrix3fv(location, count, transpose, mat);
    }

    @Override
    public void uniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix4fv");
        if (mat == null) return;
        GL.glUniformMatrix4fv(location, count, transpose, mat);
    }

    @Override
    public void validateProgram(int program) {
        report("void validateProgram");
        GL.glValidateProgram(program);
    }

    @Override
    public boolean isShader(int shader) {
        report("boolean isShader");
        return GL.glIsShader(shader);
    }

    @Override
    public void getShaderiv(int shader, int pname, IntBuffer params) {
        report("void getShaderiv");
        if (params == null) return;
        GL.glGetShaderiv(shader, pname, params);
    }

    @Override
    public void getAttachedShaders(int program, int maxCount, IntBuffer count, IntBuffer shaders) {
        report("void getAttachedShaders");
        if (count == null || shaders == null) return;
        GL.glGetAttachedShaders(program, maxCount, count, shaders);
    }

    @Override
    public String getShaderInfoLog(int shader) {
        report("String getShaderInfoLog");
        String log = GL.glGetShaderInfoLog(shader);
        System.out.println(log);
        return log;
    }

    @Override
    public String getShaderSource(int shader) {
        report("String getShaderSource");
        return GL.glGetShaderSource(shader);
    }

    @Override
    public void getShaderPrecisionFormat(int shaderType, int precisionType, IntBuffer range, IntBuffer precision) {
        report("void getShaderPrecisionFormat");
        if (range == null || precision == null) return;
        GL.glGetShaderPrecisionFormat(shaderType, precisionType, range, precision);
    }

    @Override
    public void getVertexAttribfv(int index, int pname, FloatBuffer params) {
        report("void getVertexAttribfv");
        if (params == null) return;
        GL.glGetVertexAttribfv(index, pname, params);
    }

    @Override
    public void getVertexAttribiv(int index, int pname, IntBuffer params) {
        report("void getVertexAttribiv");
        if (params == null) return;
        GL.glGetVertexAttribiv(index, pname, params);
    }

    @Override
    public void getVertexAttribPointerv(int index, int pname, ByteBuffer data) {
        report("void getVertexAttribPointerv");
        if (data == null) return;
        // GL.glGetVertexAttribPointerv(index, pname, data);
    }

    @Override
    public void getUniformfv(int program, int location, FloatBuffer params) {
        report("void getUniformfv");
        if (params == null) return;
        GL.glGetUniformfv(program, location, params);
    }

    @Override
    public void getUniformiv(int program, int location, IntBuffer params) {
        report("void getUniformiv");
        if (params == null) return;
        GL.glGetUniformiv(program, location, params);
    }

    @Override
    public boolean isProgram(int program) {
        report("boolean isProgram");
        return GL.glIsProgram(program);
    }

    @Override
    public void getProgramiv(int program, int pname, IntBuffer params) {
        report("void getProgramiv");
        if (params == null) return;
        GL.glGetProgramiv(program, pname, params);
    }

    @Override
    public String getProgramInfoLog(int program) {
        report("String getProgramInfoLog");
        return GL.glGetProgramInfoLog(program);
    }

    @Override
    public void scissor(int x, int y, int w, int h) {
        report("void scissor");
        // return GL.scissor();
    }

    @Override
    public void sampleCoverage(float value, boolean invert) {
        report("void sampleCoverage");
        // GL.glSampleCoverage(value, invert);
    }

    @Override
    public void stencilFunc(int func, int ref, int mask) {
        report("void stencilFunc");
        // GL.glStencilFunc(func, ref, mask);
    }

    @Override
    public void stencilFuncSeparate(int face, int func, int ref, int mask) {
        report("void stencilFuncSeparate");
        // GL.glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
    public void stencilOp(int sfail, int dpfail, int dppass) {
        report("void stencilOp");
        // GL.glStencilOp(sfail, dpfail, dppass);
    }

    @Override
    public void stencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        report("void stencilOpSeparate");
        // GL.glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    @Override
    public void depthFunc(int func) {
        report("void depthFunc");
        // GL.glDepthFunc(func);
    }

    @Override
    public void blendEquation(int mode) {
        report("void blendEquation");
        // GL.glBlendEquation(mode);
    }

    @Override
    public void blendEquationSeparate(int modeRGB, int modeAlpha) {
        report("void blendEquationSeparate");
        // GL.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void blendFunc(int src, int dst) {
        report("void blendFunc");
        // GL.glBlendFunc(src, dst);
    }

    @Override
    public void blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        report("void blendFuncSeparate");
        // GL.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void blendColor(float red, float green, float blue, float alpha) {
        report("void blendColor");
        // GL.glBlendColor(red, green, blue, alpha);
    }

    @Override
    public void colorMask(boolean r, boolean g, boolean b, boolean a) {
        report("void colorMask");
        // GL.glColorMask(r, g, b, a);
    }

    @Override
    public void depthMask(boolean mask) {
        report("void depthMask");
        // GL.glDepthMask(mask);
    }

    @Override
    public void stencilMask(int mask) {
        report("void stencilMask");
        // GL.glStencilMask(mask);
    }

    @Override
    public void stencilMaskSeparate(int face, int mask) {
        report("void stencilMaskSeparate");
        // GL.glStencilMaskSeparate(face, mask);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        report("void clearColor");
        GL.glClearColor(r,g,b,a);
    }

    @Override
    public void clearDepth(float d) {
        report("void clearDepth");
        // GL.glClearDepth((int)d);
    }

    @Override
    public void clearStencil(int s) {
        report("void clearStencil");
        // GL.glClearStencil(s);
    }

    @Override
    public void clear(int buf) {
        report("void clear");
        GL.glClear(buf);
    }

    @Override
    protected void bindFramebufferImpl(int target, int framebuffer) {
        report("void bindFramebufferImpl");
        // GL.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void deleteFramebuffers(int n, IntBuffer framebuffers) {
        report("void deleteFramebuffers");
        if (framebuffers == null) return;
        // GL.glDeleteFramebuffers(n, framebuffers);
    }

    @Override
    public void genFramebuffers(int n, IntBuffer framebuffers) {
        report("void genFramebuffers");
        if (framebuffers == null) return;
        // GL.glGenFramebuffers(n, framebuffers);
    }

    @Override
    public void bindRenderbuffer(int target, int renderbuffer) {
        report("void bindRenderbuffer");
        // GL.glBindFramebuffer(target, renderbuffer);
    }

    @Override
    public void deleteRenderbuffers(int n, IntBuffer renderbuffers) {
        report("void deleteRenderbuffers");
        if (renderbuffers == null) return;
        // GL.glDeleteRenderbuffers(n, renderbuffers);
    }

    @Override
    public void genRenderbuffers(int n, IntBuffer renderbuffers) {
        report("void genRenderbuffers");
        if (renderbuffers == null) return;
        // GL.glGenRenderbuffers(n, renderbuffers);
    }

    @Override
    public void renderbufferStorage(int target, int internalFormat, int width, int height) {
        report("void renderbufferStorage");
        // GL.glRenderbufferStorage(target, internalFormat, width, height);
    }

    @Override
    public void framebufferRenderbuffer(int target, int attachment, int rendbufferTarget, int renderbuffer) {
        report("void framebufferRenderbuffer");
        // GL.glFramebufferRenderbuffer(target, attachment, rendbufferTarget, renderbuffer);
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int texTarget, int texture, int level) {
        report("void framebufferTexture2D");
        GL.glFramebufferTexture2D(target, attachment, texTarget, texture, level);
    }

    @Override
    public int checkFramebufferStatus(int target) {
        report("int checkFramebufferStatus");
        // return GL.glCheckFramebufferStatus(target);
        return 0;
    }

    @Override
    public boolean isFramebuffer(int framebuffer) {
        report("boolean isFramebuffer");
        // return GL.glIsFramebuffer(framebuffer);
        return true;
    }

    @Override
    public void getFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        report("void getFramebufferAttachmentParameteriv");
        if (params == null) return;
        // GL.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    @Override
    public boolean isRenderbuffer(int renderbuffer) {
        report("boolean isRenderbuffer");
        // return GL.glIsRenderbuffer(renderbuffer);
        return true;
    }

    @Override
    public void getRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        report("void getRenderbufferParameteriv");
        if (params == null) return;
        // GL.glGetRenderbufferParameteriv(INT, INT, params);
    }

    @Override
    public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        report("void blitFramebuffer");
        // GL.glBlitFramebuffer(srcX0, srcY0,  srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public void renderbufferStorageMultisample(int target, int samples, int format, int width, int height) {
        report("void renderbufferStorageMultisample");
        // GL.glRenderbufferStorageMultisample(target, samples, format, width, height);
    }

    @Override
    public void readBuffer(int buf) {
        report("void readBuffer");
        // GL.glReadBuffer(buf);
    }

    @Override
    public void drawBuffer(int buf) {
        report("void drawBuffer");
        // GL.glDrawBuffer(buf);
    }
    
}
