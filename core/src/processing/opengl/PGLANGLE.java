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
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

// import org.lwjgl.opengles.GLES.*;
import static org.lwjgl.opengles.GLES20.*;
import static org.lwjgl.opengles.GLES30.*;
import static org.lwjgl.opengles.GLES31.*;
import static org.lwjgl.opengles.GLES32.*;


import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PSurface;

public class PGLANGLE extends PGL {

    
    ///////////////////////////////////////////////////////////

    // Constants

    static {
        FALSE = GL_FALSE;
        TRUE  = GL_TRUE;

        INT            = GL_INT;
        BYTE           = GL_BYTE;
        SHORT          = GL_SHORT;
        FLOAT          = GL_FLOAT;
        BOOL           = GL_BOOL;
        UNSIGNED_INT   = GL_UNSIGNED_INT;
        UNSIGNED_BYTE  = GL_UNSIGNED_BYTE;
        UNSIGNED_SHORT = GL_UNSIGNED_SHORT;

        RGB             = GL_RGB;
        RGBA            = GL_RGBA;
        ALPHA           = GL_ALPHA;
        LUMINANCE       = GL_LUMINANCE;
        LUMINANCE_ALPHA = GL_LUMINANCE_ALPHA;

        UNSIGNED_SHORT_5_6_5   = GL_UNSIGNED_SHORT_5_6_5;
        UNSIGNED_SHORT_4_4_4_4 = GL_UNSIGNED_SHORT_4_4_4_4;
        UNSIGNED_SHORT_5_5_5_1 = GL_UNSIGNED_SHORT_5_5_5_1;

        RGBA4   = GL_RGBA4;
        RGB5_A1 = GL_RGB5_A1;
        RGB565  = GL_RGB565;
        RGB8    = GL_RGB8;
        RGBA8   = GL_RGBA8;
        // ALPHA8  = 0;                // TODO

        READ_ONLY  = GL_READ_ONLY;
        WRITE_ONLY = GL_WRITE_ONLY;
        READ_WRITE = GL_READ_WRITE;

        TESS_WINDING_NONZERO = GLU.GLU_TESS_WINDING_NONZERO;
        TESS_WINDING_ODD     = GLU.GLU_TESS_WINDING_ODD;
        TESS_EDGE_FLAG       = GLU.GLU_TESS_EDGE_FLAG;

        // GENERATE_MIPMAP_HINT = GL_GENERATE_MIPMAP_HINT;   // TODO
        FASTEST              = GL_FASTEST;
        NICEST               = GL_NICEST;
        DONT_CARE            = GL_DONT_CARE;

        VENDOR                   = GL_VENDOR;
        RENDERER                 = GL_RENDERER;
        VERSION                  = GL_VERSION;
        EXTENSIONS               = GL_EXTENSIONS;
        SHADING_LANGUAGE_VERSION = GL_SHADING_LANGUAGE_VERSION;

        MAX_SAMPLES = GL_MAX_SAMPLES;
        SAMPLES     = GL_SAMPLES;

        ALIASED_LINE_WIDTH_RANGE = GL_ALIASED_LINE_WIDTH_RANGE;
        ALIASED_POINT_SIZE_RANGE = GL_ALIASED_POINT_SIZE_RANGE;

        DEPTH_BITS   = GL_DEPTH_BITS;
        STENCIL_BITS = GL_STENCIL_BITS;

        CCW = GL_CCW;
        CW  = GL_CW;

        VIEWPORT = GL_VIEWPORT;

        ARRAY_BUFFER         = GL_ARRAY_BUFFER;
        ELEMENT_ARRAY_BUFFER = GL_ELEMENT_ARRAY_BUFFER;
        PIXEL_PACK_BUFFER    = GL_PIXEL_PACK_BUFFER;

        MAX_VERTEX_ATTRIBS  = GL_MAX_VERTEX_ATTRIBS;

        STATIC_DRAW  = GL_STATIC_DRAW;
        DYNAMIC_DRAW = GL_DYNAMIC_DRAW;
        STREAM_DRAW  = GL_STREAM_DRAW;
        STREAM_READ  = GL_STREAM_READ;

        BUFFER_SIZE  = GL_BUFFER_SIZE;
        BUFFER_USAGE = GL_BUFFER_USAGE;

        POINTS         = GL_POINTS;
        LINE_STRIP     = GL_LINE_STRIP;
        LINE_LOOP      = GL_LINE_LOOP;
        LINES          = GL_LINES;
        TRIANGLE_FAN   = GL_TRIANGLE_FAN;
        TRIANGLE_STRIP = GL_TRIANGLE_STRIP;
        TRIANGLES      = GL_TRIANGLES;

        CULL_FACE      = GL_CULL_FACE;
        FRONT          = GL_FRONT;
        BACK           = GL_BACK;
        FRONT_AND_BACK = GL_FRONT_AND_BACK;

        POLYGON_OFFSET_FILL = GL_POLYGON_OFFSET_FILL;

        UNPACK_ALIGNMENT = GL_UNPACK_ALIGNMENT;
        PACK_ALIGNMENT   = GL_PACK_ALIGNMENT;

        TEXTURE_2D        = GL_TEXTURE_2D;
        // TEXTURE_RECTANGLE = 0;  // TODO

        TEXTURE_BINDING_2D        = GL_TEXTURE_BINDING_2D;
        // TEXTURE_BINDING_RECTANGLE = 0;  // TODO

        MAX_TEXTURE_SIZE           = GL_MAX_TEXTURE_SIZE;
        // TEXTURE_MAX_ANISOTROPY     = 0;  // TODO
        // MAX_TEXTURE_MAX_ANISOTROPY = 0;  // TODO

        MAX_VERTEX_TEXTURE_IMAGE_UNITS   = GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
        MAX_TEXTURE_IMAGE_UNITS          = GL_MAX_TEXTURE_IMAGE_UNITS;
        MAX_COMBINED_TEXTURE_IMAGE_UNITS = GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

        NUM_COMPRESSED_TEXTURE_FORMATS = GL_NUM_COMPRESSED_TEXTURE_FORMATS;
        COMPRESSED_TEXTURE_FORMATS     = GL_COMPRESSED_TEXTURE_FORMATS;

        NEAREST               = GL_NEAREST;
        LINEAR                = GL_LINEAR;
        LINEAR_MIPMAP_NEAREST = GL_LINEAR_MIPMAP_NEAREST;
        LINEAR_MIPMAP_LINEAR  = GL_LINEAR_MIPMAP_LINEAR;

        CLAMP_TO_EDGE = GL_CLAMP_TO_EDGE;
        REPEAT        = GL_REPEAT;

        TEXTURE0           = GL_TEXTURE0;
        TEXTURE1           = GL_TEXTURE1;
        TEXTURE2           = GL_TEXTURE2;
        TEXTURE3           = GL_TEXTURE3;
        TEXTURE_MIN_FILTER = GL_TEXTURE_MIN_FILTER;
        TEXTURE_MAG_FILTER = GL_TEXTURE_MAG_FILTER;
        TEXTURE_WRAP_S     = GL_TEXTURE_WRAP_S;
        TEXTURE_WRAP_T     = GL_TEXTURE_WRAP_T;
        TEXTURE_WRAP_R     = GL_TEXTURE_WRAP_R;

        TEXTURE_CUBE_MAP = GL_TEXTURE_CUBE_MAP;
        TEXTURE_CUBE_MAP_POSITIVE_X = GL_TEXTURE_CUBE_MAP_POSITIVE_X;
        TEXTURE_CUBE_MAP_POSITIVE_Y = GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
        TEXTURE_CUBE_MAP_POSITIVE_Z = GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
        TEXTURE_CUBE_MAP_NEGATIVE_X = GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
        TEXTURE_CUBE_MAP_NEGATIVE_Y = GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
        TEXTURE_CUBE_MAP_NEGATIVE_Z = GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;

        VERTEX_SHADER        = GL_VERTEX_SHADER;
        FRAGMENT_SHADER      = GL_FRAGMENT_SHADER;
        INFO_LOG_LENGTH      = GL_INFO_LOG_LENGTH;
        SHADER_SOURCE_LENGTH = GL_SHADER_SOURCE_LENGTH;
        COMPILE_STATUS       = GL_COMPILE_STATUS;
        LINK_STATUS          = GL_LINK_STATUS;
        VALIDATE_STATUS      = GL_VALIDATE_STATUS;
        SHADER_TYPE          = GL_SHADER_TYPE;
        DELETE_STATUS        = GL_DELETE_STATUS;

        FLOAT_VEC2   = GL_FLOAT_VEC2;
        FLOAT_VEC3   = GL_FLOAT_VEC3;
        FLOAT_VEC4   = GL_FLOAT_VEC4;
        FLOAT_MAT2   = GL_FLOAT_MAT2;
        FLOAT_MAT3   = GL_FLOAT_MAT3;
        FLOAT_MAT4   = GL_FLOAT_MAT4;
        INT_VEC2     = GL_INT_VEC2;
        INT_VEC3     = GL_INT_VEC3;
        INT_VEC4     = GL_INT_VEC4;
        BOOL_VEC2    = GL_BOOL_VEC2;
        BOOL_VEC3    = GL_BOOL_VEC3;
        BOOL_VEC4    = GL_BOOL_VEC4;
        SAMPLER_2D   = GL_SAMPLER_2D;
        SAMPLER_CUBE = GL_SAMPLER_CUBE;

        LOW_FLOAT    = GL_LOW_FLOAT;
        MEDIUM_FLOAT = GL_MEDIUM_FLOAT;
        HIGH_FLOAT   = GL_HIGH_FLOAT;
        LOW_INT      = GL_LOW_INT;
        MEDIUM_INT   = GL_MEDIUM_INT;
        HIGH_INT     = GL_HIGH_INT;

        CURRENT_VERTEX_ATTRIB = GL_CURRENT_VERTEX_ATTRIB;

        VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
        VERTEX_ATTRIB_ARRAY_ENABLED        = GL_VERTEX_ATTRIB_ARRAY_ENABLED;
        VERTEX_ATTRIB_ARRAY_SIZE           = GL_VERTEX_ATTRIB_ARRAY_SIZE;
        VERTEX_ATTRIB_ARRAY_STRIDE         = GL_VERTEX_ATTRIB_ARRAY_STRIDE;
        VERTEX_ATTRIB_ARRAY_TYPE           = GL_VERTEX_ATTRIB_ARRAY_TYPE;
        VERTEX_ATTRIB_ARRAY_NORMALIZED     = GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
        VERTEX_ATTRIB_ARRAY_POINTER        = GL_VERTEX_ATTRIB_ARRAY_POINTER;

        BLEND               = GL_BLEND;
        ONE                 = GL_ONE;
        ZERO                = GL_ZERO;
        SRC_ALPHA           = GL_SRC_ALPHA;
        DST_ALPHA           = GL_DST_ALPHA;
        ONE_MINUS_SRC_ALPHA = GL_ONE_MINUS_SRC_ALPHA;
        ONE_MINUS_DST_COLOR = GL_ONE_MINUS_DST_COLOR;
        ONE_MINUS_SRC_COLOR = GL_ONE_MINUS_SRC_COLOR;
        DST_COLOR           = GL_DST_COLOR;
        SRC_COLOR           = GL_SRC_COLOR;

        SAMPLE_ALPHA_TO_COVERAGE = GL_SAMPLE_ALPHA_TO_COVERAGE;
        SAMPLE_COVERAGE          = GL_SAMPLE_COVERAGE;

        KEEP      = GL_KEEP;
        REPLACE   = GL_REPLACE;
        INCR      = GL_INCR;
        DECR      = GL_DECR;
        INVERT    = GL_INVERT;
        INCR_WRAP = GL_INCR_WRAP;
        DECR_WRAP = GL_DECR_WRAP;
        NEVER     = GL_NEVER;
        ALWAYS    = GL_ALWAYS;

        EQUAL    = GL_EQUAL;
        LESS     = GL_LESS;
        LEQUAL   = GL_LEQUAL;
        GREATER  = GL_GREATER;
        GEQUAL   = GL_GEQUAL;
        NOTEQUAL = GL_NOTEQUAL;

        FUNC_ADD              = GL_FUNC_ADD;
        FUNC_MIN              = GL_MIN;
        FUNC_MAX              = GL_MAX;
        FUNC_REVERSE_SUBTRACT = GL_FUNC_REVERSE_SUBTRACT;
        FUNC_SUBTRACT         = GL_FUNC_SUBTRACT;

        DITHER = GL_DITHER;

        CONSTANT_COLOR           = GL_CONSTANT_COLOR;
        CONSTANT_ALPHA           = GL_CONSTANT_ALPHA;
        ONE_MINUS_CONSTANT_COLOR = GL_ONE_MINUS_CONSTANT_COLOR;
        ONE_MINUS_CONSTANT_ALPHA = GL_ONE_MINUS_CONSTANT_ALPHA;
        SRC_ALPHA_SATURATE       = GL_SRC_ALPHA_SATURATE;

        SCISSOR_TEST    = GL_SCISSOR_TEST;
        STENCIL_TEST    = GL_STENCIL_TEST;
        DEPTH_TEST      = GL_DEPTH_TEST;
        DEPTH_WRITEMASK = GL_DEPTH_WRITEMASK;

        COLOR_BUFFER_BIT   = GL_COLOR_BUFFER_BIT;
        DEPTH_BUFFER_BIT   = GL_DEPTH_BUFFER_BIT;
        STENCIL_BUFFER_BIT = GL_STENCIL_BUFFER_BIT;

        FRAMEBUFFER        = GL_FRAMEBUFFER;
        COLOR_ATTACHMENT0  = GL_COLOR_ATTACHMENT0;
        COLOR_ATTACHMENT1  = GL_COLOR_ATTACHMENT1;
        COLOR_ATTACHMENT2  = GL_COLOR_ATTACHMENT2;
        COLOR_ATTACHMENT3  = GL_COLOR_ATTACHMENT3;
        RENDERBUFFER       = GL_RENDERBUFFER;
        DEPTH_ATTACHMENT   = GL_DEPTH_ATTACHMENT;
        STENCIL_ATTACHMENT = GL_STENCIL_ATTACHMENT;
        READ_FRAMEBUFFER   = GL_READ_FRAMEBUFFER;
        DRAW_FRAMEBUFFER   = GL_DRAW_FRAMEBUFFER;

        DEPTH24_STENCIL8 = GL_DEPTH24_STENCIL8;

        DEPTH_COMPONENT   = GL_DEPTH_COMPONENT;
        DEPTH_COMPONENT16 = GL_DEPTH_COMPONENT16;
        DEPTH_COMPONENT24 = GL_DEPTH_COMPONENT24;
        // DEPTH_COMPONENT32 = 0;   // TODO
        STENCIL_INDEX  = GL_STENCIL_INDEX;
        // STENCIL_INDEX1 = 0;  // TODO
        // STENCIL_INDEX4 = 0;  // TODO
        STENCIL_INDEX8 = GL_STENCIL_INDEX8;

        DEPTH_STENCIL = GL_DEPTH_STENCIL;

        FRAMEBUFFER_COMPLETE                      = GL_FRAMEBUFFER_COMPLETE;
        FRAMEBUFFER_UNDEFINED                     = GL_FRAMEBUFFER_UNDEFINED;
        FRAMEBUFFER_INCOMPLETE_ATTACHMENT         = GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_DIMENSIONS         = GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
        // FRAMEBUFFER_INCOMPLETE_FORMATS            = 0;  // TODO
        // FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        = 0;  // TODO
        // FRAMEBUFFER_INCOMPLETE_READ_BUFFER        = 0;  // TODO
        FRAMEBUFFER_UNSUPPORTED                   = GL_FRAMEBUFFER_UNSUPPORTED;
        FRAMEBUFFER_INCOMPLETE_MULTISAMPLE        = GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
        FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS      = GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;

        FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE           = GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE;
        FRAMEBUFFER_ATTACHMENT_OBJECT_NAME           = GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL         = GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE;

        RENDERBUFFER_WIDTH           = GL_RENDERBUFFER_WIDTH;
        RENDERBUFFER_HEIGHT          = GL_RENDERBUFFER_HEIGHT;
        RENDERBUFFER_RED_SIZE        = GL_RENDERBUFFER_RED_SIZE;
        RENDERBUFFER_GREEN_SIZE      = GL_RENDERBUFFER_GREEN_SIZE;
        RENDERBUFFER_BLUE_SIZE       = GL_RENDERBUFFER_BLUE_SIZE;
        RENDERBUFFER_ALPHA_SIZE      = GL_RENDERBUFFER_ALPHA_SIZE;
        RENDERBUFFER_DEPTH_SIZE      = GL_RENDERBUFFER_DEPTH_SIZE;
        RENDERBUFFER_STENCIL_SIZE    = GL_RENDERBUFFER_STENCIL_SIZE;
        RENDERBUFFER_INTERNAL_FORMAT = GL_RENDERBUFFER_INTERNAL_FORMAT;

        // MULTISAMPLE    = 0;   // TODO
        // LINE_SMOOTH    = 0;
        // POLYGON_SMOOTH = 0;

        SYNC_GPU_COMMANDS_COMPLETE = GL_SYNC_GPU_COMMANDS_COMPLETE;
        ALREADY_SIGNALED           = GL_ALREADY_SIGNALED;
        CONDITION_SATISFIED        = GL_CONDITION_SATISFIED;
    }
    
    protected float[] projMatrix;
    protected FloatBuffer projMatrixBuffer;
    protected float[] mvMatrix;

    private HashMap<Integer, ByteBuffer> buffers = new HashMap<Integer, ByteBuffer>();
    private int boundBuffer = -1;

    private ByteBuffer getBoundBuffer(int offset) {
        ByteBuffer buff = buffers.get(boundBuffer);
        
        if (buff == null) {
            System.err.println("unbound buffer");
            return null;
        }

        if (offset > 0) {
            return buff.slice(offset, buff.capacity()-offset);
        }

        return buff;
    }

    private void report(String m) {
        // System.out.println(m);
    }

    public static ByteBuffer convertToByteBuffer(Buffer outputBuffer) {
        if (outputBuffer == null) return null;

        ByteBuffer byteBuffer = null;
        if (outputBuffer instanceof ByteBuffer) {
        // System.out.println("ByteBuffer");
            byteBuffer = (ByteBuffer) outputBuffer;
        } else if (outputBuffer instanceof CharBuffer) {
        // System.out.println("CharBuffer");
            if (outputBuffer.isDirect()) byteBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() * Character.BYTES);
            else byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * Character.BYTES);

            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asCharBuffer().put((CharBuffer) outputBuffer);
        } else if (outputBuffer instanceof ShortBuffer) {
        // System.out.println("ShortBuffer");
            if (outputBuffer.isDirect()) byteBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() * Short.BYTES);
            else byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * Short.BYTES);

            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asShortBuffer().put((ShortBuffer) outputBuffer);
        } else if (outputBuffer instanceof IntBuffer) {
        // System.out.println("IntBuffer");
            if (outputBuffer.isDirect()) byteBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() * Integer.BYTES);
            else byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * Integer.BYTES);

            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asIntBuffer().put((IntBuffer) outputBuffer);
        } else if (outputBuffer instanceof LongBuffer) {
        // System.out.println("LongBuffer");
            if (outputBuffer.isDirect()) byteBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() * Long.BYTES);
            else byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * Long.BYTES);

            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asLongBuffer().put((LongBuffer) outputBuffer);
        } else if (outputBuffer instanceof FloatBuffer) {
            if (outputBuffer.isDirect()) byteBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() * Float.BYTES);
            else byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * Float.BYTES);

            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asFloatBuffer().put((FloatBuffer) outputBuffer);
        } else if (outputBuffer instanceof DoubleBuffer) {
        // System.out.println("DoubleBuffer");
            if (outputBuffer.isDirect()) byteBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() * Double.BYTES);
            else byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * Double.BYTES);
            
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asDoubleBuffer().put((DoubleBuffer) outputBuffer);
        }
        else {
        System.err.println("Unknown buffer "+outputBuffer.getClass().toString());
        }
        return byteBuffer;
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
    protected void initFBOLayer() {
        if (0 < sketch.frameCount) {
        // if (isES()) initFBOLayerES();
        initFBOLayerES();
        // else
        // initFBOLayerGL();
        }
    }
    
    @Override
    protected boolean isES() {
        return true;
    }


    
    private void initFBOLayerGL() {
        // Copy the contents of the front and back screen buffers to the textures
        // of the FBO, so they are properly initialized. Note that the front buffer
        // of the default framebuffer (the screen) contains the previous frame:
        // https://www.opengl.org/wiki/Default_Framebuffer
        // so it is copied to the front texture of the FBO layer:
        if (pclearColor || 0 < pgeomCount || !sketch.isLooping()) {
        if (hasReadBuffer()) readBuffer(FRONT);
        } else {
        // ...except when the previous frame has not been cleared and nothing was
        // rendered while looping. In this case the back buffer, which holds the
        // initial state of the previous frame, still contains the most up-to-date
        // screen state.
        readBuffer(BACK);
        }
        bindFramebufferImpl(DRAW_FRAMEBUFFER, glColorFbo.get(0));
        framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0,
                            TEXTURE_2D, glColorTex.get(frontTex), 0);
        if (hasDrawBuffer()) drawBuffer(COLOR_ATTACHMENT0);
        blitFramebuffer(0, 0, fboWidth, fboHeight,
                        0, 0, fboWidth, fboHeight,
                        COLOR_BUFFER_BIT, NEAREST);

        readBuffer(BACK);
        bindFramebufferImpl(DRAW_FRAMEBUFFER, glColorFbo.get(0));
        framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0,
                            TEXTURE_2D, glColorTex.get(backTex), 0);
        drawBuffer(COLOR_ATTACHMENT0);
        blitFramebuffer(0, 0, fboWidth, fboHeight,
                        0, 0, fboWidth, fboHeight,
                        COLOR_BUFFER_BIT, NEAREST);

        bindFramebufferImpl(FRAMEBUFFER, 0);
    }
    

    private void initFBOLayerES() {
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
    public boolean threadIsCurrent()  {
        return true;  // TODO: actual check instead of always true.
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
        return GL_DEPTH_BITS;
    }

    @Override
    protected int getStencilBits() {
        report("int getStencilBits");
        return GL_STENCIL_BITS;
    }

    @Override
    protected float getPixelScale() {
        report("float getPixelScale");
        PSurface surf = sketch.getSurface();
        if (surf == null) {
        return graphics.pixelDensity;
        } else if (surf instanceof PSurfaceJOGL) {
        return ((PSurfaceJOGL)surf).getPixelScale();
        } else if (surf instanceof PSurfaceANGLE) {
        return ((PSurfaceANGLE)surf).getPixelScale();
        }else {
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
        System.out.println("SWAP!!");
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


        // glMatrixMode(GL_PROJECTION);
        // projMatrix[ 0] = proj.m00;
        // projMatrix[ 1] = proj.m10;
        // projMatrix[ 2] = proj.m20;
        // projMatrix[ 3] = proj.m30;
        // projMatrix[ 4] = proj.m01;
        // projMatrix[ 5] = proj.m11;
        // projMatrix[ 6] = proj.m21;
        // projMatrix[ 7] = proj.m31;
        // projMatrix[ 8] = proj.m02;
        // projMatrix[ 9] = proj.m12;
        // projMatrix[10] = proj.m22;
        // projMatrix[11] = proj.m32;
        // projMatrix[12] = proj.m03;
        // projMatrix[13] = proj.m13;
        // projMatrix[14] = proj.m23;
        // projMatrix[15] = proj.m33;
        // projMatrixBuffer.rewind();
        // projMatrixBuffer.put(projMatrix);
        // glLoadMatrixf(projMatrixBuffer);

        // if (mvMatrix == null) {
        //     mvMatrix = new float[16];
        // }
        // glMatrixMode(GL_MODELVIEW);
        // mvMatrix[ 0] = mdl.m00;
        // mvMatrix[ 1] = mdl.m10;
        // mvMatrix[ 2] = mdl.m20;
        // mvMatrix[ 3] = mdl.m30;
        // mvMatrix[ 4] = mdl.m01;
        // mvMatrix[ 5] = mdl.m11;
        // mvMatrix[ 6] = mdl.m21;
        // mvMatrix[ 7] = mdl.m31;
        // mvMatrix[ 8] = mdl.m02;
        // mvMatrix[ 9] = mdl.m12;
        // mvMatrix[10] = mdl.m22;
        // mvMatrix[11] = mdl.m32;
        // mvMatrix[12] = mdl.m03;
        // mvMatrix[13] = mdl.m13;
        // mvMatrix[14] = mdl.m23;
        // mvMatrix[15] = mdl.m33;

        // projMatrixBuffer.rewind();
        // projMatrixBuffer.put(projMatrix);
        // glLoadMatrixf(projMatrixBuffer);
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
    protected void enableTexturing(int target) {
        report("void enableTexturing");
        if (target == TEXTURE_2D) {
        texturingTargets[0] = true;
        } else if (target == TEXTURE_RECTANGLE()) {
        texturingTargets[1] = true;
        }
    }


    @Override
    protected void disableTexturing(int target) {
        report("void disableTexturing");
        if (target == TEXTURE_2D) {
        texturingTargets[0] = false;
        } else if (target == TEXTURE_RECTANGLE()) {
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
        return 30020;
        // return 0;
    }

    @Override
    protected String getGLSLVersionSuffix() {
        report("String getGLSLVersionSuffix");
        return " es";
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
        glFlush();
    }

    @Override
    public void finish() {
        report("void finish");
        glFinish();
    }

    @Override
    public void hint(int target, int hint) {
        report("void hint");
        glHint(target, hint);
    }

    @Override
    public void enable(int value) {
        report("void enable");
        if (-1 < value) {
            glEnable(value);
        }
    }

    @Override
    public void disable(int value) {
        report("void disable");
        if (-1 < value) {
            glDisable(value);
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
            glGetBooleanv(value, byteBuffer);
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
            glGetIntegerv(value, data);
        } else {
            fillIntBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public void getFloatv(int value, FloatBuffer data) {
        report("void getFloatv");
        if (data == null) return;
        if (-1 < value) {
            glGetFloatv(value, data);
        } else {
            fillFloatBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public boolean isEnabled(int value) {
        report("boolean isEnabled");
        return glIsEnabled(value);
    }

    @Override
    public String getString(int name) {
        report("String getString");
        return glGetString(name);
    }

    @Override
    public int getError() {
        report("int getError");
        return glGetError();
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
        glGenBuffers(buffers);
    }

    @Override
    public void deleteBuffers(int n, IntBuffer buffers) {
        report("void deleteBuffers");
        if (buffers == null) return;
        glDeleteBuffers(buffers);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        report("void bindBuffer");
        boundBuffer = buffer;
        glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, int size, Buffer data, int usage) {
        report("void bufferData");

        if (data == null) {
            long sizel = (long)size;
            glBufferData(target, sizel, usage);
            return;
        }
        buffers.put(boundBuffer, convertToByteBuffer(data));

        // Thread.dumpStack();
        
        // System.out.println(data.getClass().getName());

        glBufferData(target, convertToByteBuffer(data), usage);
    }

    @Override
    public void bufferSubData(int target, int offset, int size, Buffer data) {
        report("void bufferSubData");
        // buffers.put(boundBuffer, data);

        // TODO: Oh god we got to write our own buffersubdata for the
        // buffers hashmap oh god.

        glBufferSubData(target, offset, convertToByteBuffer(data));
    }

    @Override
    public void isBuffer(int buffer) {
        report("void isBuffer");
        glIsBuffer(buffer);
    }

    @Override
    public void getBufferParameteriv(int target, int value, IntBuffer data) {
        report("void getBufferParameteriv");
        if (data == null) return;
        glGetBufferParameteriv(target, value, data);
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
        // return glMapBufferRange(target, offset, length, access);
        return null;
    }

    @Override
    public void unmapBuffer(int target) {
        report("void unmapBuffer");
        glUnmapBuffer(target);
        
    }

    @Override
    public long fenceSync(int condition, int flags) {
        report("long fenceSync");
        return glFenceSync(condition, flags);
    }

    @Override
    public void deleteSync(long sync) {
        report("void deleteSync");
        glDeleteSync(sync);
    }

    @Override
    public int clientWaitSync(long sync, int flags, long timeout) {
        report("int clientWaitSync");
        return glClientWaitSync(sync, flags, timeout);
    }

    @Override
    public void depthRangef(float n, float f) {
        report("void depthRangef");
        // TODO: Oops.
        // glDepthRange(n, f);
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
        glViewport(x, y, w, h);
    }

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, Buffer buffer) {
        report("void readPixelsImpl");
        if (buffer == null) return;
        glReadPixels(x, y, width, height, format, type, convertToByteBuffer(buffer));
    }

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, long offset) {
        report("void readPixelsImpl");
        // TODO
        // glReadPixels(x, y, width, height, format, type, 0);
    }

    @Override
    public void vertexAttrib1f(int index, float value) {
        report("void vertexAttrib1f");
        glVertexAttrib1f(index, value);
    }

    @Override
    public void vertexAttrib2f(int index, float value0, float value1) {
        report("void vertexAttrib2f");
        glVertexAttrib2f(index, value0, value1);
    }

    @Override
    public void vertexAttrib3f(int index, float value0, float value1, float value2) {
        report("void vertexAttrib3f");
        glVertexAttrib3f(index, value0, value1, value2);
    }

    @Override
    public void vertexAttrib4f(int index, float value0, float value1, float value2, float value3) {
        report("void vertexAttrib4f");
        glVertexAttrib4f(index, value0, value1, value2, value3);
    }

    @Override
    public void vertexAttrib1fv(int index, FloatBuffer values) {
        report("void vertexAttrib1fv");
        if (values == null) return;
        glVertexAttrib1fv(index, values);
    }

    @Override
    public void vertexAttrib2fv(int index, FloatBuffer values) {
        report("void vertexAttrib2fv");
        if (values == null) return;
        glVertexAttrib2fv(index, values);
    }

    @Override
    public void vertexAttrib3fv(int index, FloatBuffer values) {
        report("void vertexAttrib3fv");
        if (values == null) return;
        glVertexAttrib3fv(index, values);
    }

    @Override
    public void vertexAttrib4fv(int index, FloatBuffer values) {
        report("void vertexAttrib4fv");
        if (values == null) return;
        glVertexAttrib4fv(index, values);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        report("void vertexAttribPointer");

        ByteBuffer buff = getBoundBuffer(offset);
        // if (buff == null) return;
        glVertexAttribPointer(index, size, type, normalized, stride, buff);
        // glVertexAttribPointer()
        // glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer pointer) : void
        // glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) : void?

    }

    @Override
    public void enableVertexAttribArray(int index) {
        report("void enableVertexAttribArray");
        glEnableVertexAttribArray(index);
    }

    @Override
    public void disableVertexAttribArray(int index) {
        report("void disableVertexAttribArray");
        glDisableVertexAttribArray(index);
    }

    @Override
    public void drawArraysImpl(int mode, int first, int count) {
        report("void drawArraysImpl");
        glDrawArrays(mode, first, count);
    }

    @Override
    public void drawElementsImpl(int mode, int count, int type, int offset) {
        report("void drawElementsImpl");
        ByteBuffer buff = getBoundBuffer(offset);
        // if (buff == null) return;
        glBindBuffer(ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(ARRAY_BUFFER, 0);
        
        // // System.out.println("drawElements buffer "+boundBuffer);
        // System.out.println("Requested count: "+count+"  Expected count: 256  Capacity count: "+(buff.capacity()/2)+"  Limit count: "+(buff.limit()/2));

        glDrawElements(mode, type, buff);
    }

    @Override
    public void lineWidth(float width) {
        report("void lineWidth");
        glLineWidth(width);
    }

    @Override
    public void frontFace(int dir) {
        report("void frontFace");
        glFrontFace(dir);
    }

    @Override
    public void cullFace(int mode) {
        report("void cullFace");
        glCullFace(mode);
    }

    @Override
    public void polygonOffset(float factor, float units) {
        report("void polygonOffset");
        glPolygonOffset(factor, units);
    }

    @Override
    public void pixelStorei(int pname, int param) {
        report("void pixelStorei");
        glPixelStorei(pname, param);
        
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer data) {
        report("void texImage2D");
        if (data == null) return;
        glTexImage2D(target, level, internalFormat, width, height, border, format, type, convertToByteBuffer(data));
    }

    @Override
    public void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
        report("void copyTexImage2D");
        glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
        
    }

    @Override
    public void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, Buffer data) {
        report("void texSubImage2D");
        if (data == null) return;
        // glTextSubImage2D(target, level, xOffset, yOffset, width, height, format, type, convertToByteBuffer(data));
    }

    @Override
    public void copyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
        report("void copyTexSubImage2D");
        glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height);
    }

    @Override
    public void compressedTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int imageSize, Buffer data) {
        report("void compressedTexImage2D");
        if (data == null) return;
        // glCompressedTexImage2D(target, level, internalFormat, width, height, border, imageSize, convertToByteBuffer(data));
    }

    @Override
    public void compressedTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int imageSize, Buffer data) {
        report("void compressedTexSubImage2D");
        if (data == null) return;
        
    }

    @Override
    public void texParameteri(int target, int pname, int param) {
        report("void texParameteri");
        glTexParameteri(target, pname, param);
    }

    @Override
    public void texParameterf(int target, int pname, float param) {
        report("void texParameterf");
        glTexParameterf(target, pname, param);
    }

    @Override
    public void texParameteriv(int target, int pname, IntBuffer params) {
        report("void texParameteriv");
        if (params == null) return;
        glTexParameteriv(target, pname, params);
    }

    @Override
    public void texParameterfv(int target, int pname, FloatBuffer params) {
        report("void texParameterfv");
        if (params == null) return;
        glTexParameterfv(target, pname, params);
    }

    @Override
    public void generateMipmap(int target) {
        report("void generateMipmap");
        glGenerateMipmap(target);
    }

    @Override
    public void genTextures(int n, IntBuffer textures) {
        report("void genTextures");
        if (textures == null) return;
        glGenTextures(textures);
    }

    @Override
    public void deleteTextures(int n, IntBuffer textures) {
        report("void deleteTextures");
        if (textures == null) return;
        glDeleteTextures(textures);
    }

    @Override
    public void getTexParameteriv(int target, int pname, IntBuffer params) {
        report("void getTexParameteriv");
        if (params == null) return;
        glGetTexParameteriv(target, pname, params);
    }

    @Override
    public void getTexParameterfv(int target, int pname, FloatBuffer params) {
        report("void getTexParameterfv");
        if (params == null) return;
        glGetTexParameterfv(target, pname, params);
    }

    @Override
    public boolean isTexture(int texture) {
        report("boolean isTexture");
        return glIsTexture(texture);
    }

    @Override
    protected void activeTextureImpl(int texture) {
        report("void activeTextureImpl");
        glActiveTexture(texture);
    }

    @Override
    protected void bindTextureImpl(int target, int texture) {
        report("void bindTextureImpl");
        glBindTexture(target, texture);
    }

    @Override
    public int createShader(int type) {
        report("int createShader");
        return glCreateShader(type);
    }

    @Override
    public void shaderSource(int shader, String source) {
        // System.out.println(source);
        report("void shaderSource");

        glShaderSource(shader, source);
    }

    @Override
    public void compileShader(int shader) {
        report("void compileShader");
        glCompileShader(shader);
    }

    @Override
    public void releaseShaderCompiler() {
        report("void releaseShaderCompiler");
        glReleaseShaderCompiler();
    }

    @Override
    public void deleteShader(int shader) {
        report("void deleteShader");
        glDeleteShader(shader);
    }

    @Override
    public void shaderBinary(int count, IntBuffer shaders, int binaryFormat, Buffer binary, int length) {
        report("void shaderBinary");
        if (shaders == null || binary == null) return;
        // glShaderBinary(count, shaders, binaryFormat, binary, length);
    }

    @Override
    public int createProgram() {
        report("int createProgram");
        return glCreateProgram();
    }

    @Override
    public void attachShader(int program, int shader) {
        report("void attachShader");
        glAttachShader(program, shader);
    }

    @Override
    public void detachShader(int program, int shader) {
        report("void detachShader");
        glDetachShader(program, shader);
    }

    @Override
    public void linkProgram(int program) {
        report("void linkProgram");
        glLinkProgram(program);
    }

    @Override
    public void useProgram(int program) {
        report("void useProgram");
        glUseProgram(program);
    }

    @Override
    public void deleteProgram(int program) {
        report("void deleteProgram");
        glDeleteProgram(program);
    }

    @Override
    public String getActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        report("String getActiveAttrib");
        if (size == null || type == null) return null;
        return glGetActiveAttrib(program, index, size, type);
    }

    @Override
    public int getAttribLocation(int program, String name) {
        report("int getAttribLocation");
        return glGetAttribLocation(program, name);
    }

    @Override
    public void bindAttribLocation(int program, int index, String name) {
        report("void bindAttribLocation");
        glBindAttribLocation(program, index, name);
    }

    @Override
    public int getUniformLocation(int program, String name) {
        report("int getUniformLocation");
        return glGetUniformLocation(program, name);
    }

    @Override
    public String getActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        report("String getActiveUniform");
        if (size == null || type == null) return null;
        return glGetActiveUniform(program, index, size, type);
    }

    @Override
    public void uniform1i(int location, int value) {
        report("void uniform1i");
        glUniform1i(location, value);
    }

    @Override
    public void uniform2i(int location, int value0, int value1) {
        report("void uniform2i");
        glUniform2i(location, value0, value1);
    }

    @Override
    public void uniform3i(int location, int value0, int value1, int value2) {
        report("void uniform3i");
        glUniform3i(location, value0, value1, value2);
    }

    @Override
    public void uniform4i(int location, int value0, int value1, int value2, int value3) {
        report("void uniform4i");
        glUniform4i(location, value0, value1, value2, value3);
    }

    @Override
    public void uniform1f(int location, float value) {
        report("void uniform1f");
        glUniform1f(location, value);
    }

    @Override
    public void uniform2f(int location, float value0, float value1) {
        report("void uniform2f");
        glUniform2f(location, value0, value1);
    }

    @Override
    public void uniform3f(int location, float value0, float value1, float value2) {
        report("void uniform3f");
        glUniform3f(location, value0, value1, value2);
    }

    @Override
    public void uniform4f(int location, float value0, float value1, float value2, float value3) {
        report("void uniform4f");
        glUniform4f(location, value0, value1, value2, value3);
    }

    @Override
    public void uniform1iv(int location, int count, IntBuffer v) {
        report("void uniform1iv");
        if (v == null) return;
        glUniform1iv(location, v);
    }

    @Override
    public void uniform2iv(int location, int count, IntBuffer v) {
        report("void uniform2iv");
        if (v == null) return;
        glUniform2iv(location, v);
    }

    @Override
    public void uniform3iv(int location, int count, IntBuffer v) {
        report("void uniform3iv");
        if (v == null) return;
        glUniform3iv(location, v);
    }

    @Override
    public void uniform4iv(int location, int count, IntBuffer v) {
        report("void uniform4iv");
        if (v == null) return;
        glUniform4iv(location, v);
    }

    @Override
    public void uniform1fv(int location, int count, FloatBuffer v) {
        report("void uniform1fv");
        if (v == null) return;
        glUniform1fv(location, v);
    }

    @Override
    public void uniform2fv(int location, int count, FloatBuffer v) {
        report("void uniform2fv");
        if (v == null) return;
        glUniform2fv(location, v);
    }

    @Override
    public void uniform3fv(int location, int count, FloatBuffer v) {
        report("void uniform3fv");
        if (v == null) return;
        glUniform3fv(location, v);
    }

    @Override
    public void uniform4fv(int location, int count, FloatBuffer v) {
        report("void uniform4fv");
        if (v == null) return;
        glUniform4fv(location, v);
    }

    @Override
    public void uniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix2fv");
        if (mat == null) return;
        glUniformMatrix2fv(location, transpose, mat);
    }

    @Override
    public void uniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix3fv");
        if (mat == null) return;
        glUniformMatrix3fv(location, transpose, mat);
    }

    @Override
    public void uniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer mat) {
        report("void uniformMatrix4fv");
        if (mat == null) return;
        glUniformMatrix4fv(location, transpose, mat);
    }

    @Override
    public void validateProgram(int program) {
        report("void validateProgram");
        glValidateProgram(program);
    }

    @Override
    public boolean isShader(int shader) {
        report("boolean isShader");
        return glIsShader(shader);
    }

    @Override
    public void getShaderiv(int shader, int pname, IntBuffer params) {
        report("void getShaderiv");
        if (params == null) return;
        glGetShaderiv(shader, pname, params);
    }

    @Override
    public void getAttachedShaders(int program, int maxCount, IntBuffer count, IntBuffer shaders) {
        report("void getAttachedShaders");
        if (count == null || shaders == null) return;
        glGetAttachedShaders(program, count, shaders);
    }

    @Override
    public String getShaderInfoLog(int shader) {
        report("String getShaderInfoLog");
        String log = glGetShaderInfoLog(shader);
        System.out.println(log);
        return log;
    }

    @Override
    public String getShaderSource(int shader) {
        report("String getShaderSource");
        return glGetShaderSource(shader);
    }

    @Override
    public void getShaderPrecisionFormat(int shaderType, int precisionType, IntBuffer range, IntBuffer precision) {
        report("void getShaderPrecisionFormat");
        if (range == null || precision == null) return;
        glGetShaderPrecisionFormat(shaderType, precisionType, range, precision);
    }

    @Override
    public void getVertexAttribfv(int index, int pname, FloatBuffer params) {
        report("void getVertexAttribfv");
        if (params == null) return;
        glGetVertexAttribfv(index, pname, params);
    }

    @Override
    public void getVertexAttribiv(int index, int pname, IntBuffer params) {
        report("void getVertexAttribiv");
        if (params == null) return;
        glGetVertexAttribiv(index, pname, params);
    }

    @Override
    public void getVertexAttribPointerv(int index, int pname, ByteBuffer data) {
        report("void getVertexAttribPointerv");
        if (data == null) return;
        // glGetVertexAttribPointerv(index, pname, data);
    }

    @Override
    public void getUniformfv(int program, int location, FloatBuffer params) {
        report("void getUniformfv");
        if (params == null) return;
        glGetUniformfv(program, location, params);
    }

    @Override
    public void getUniformiv(int program, int location, IntBuffer params) {
        report("void getUniformiv");
        if (params == null) return;
        glGetUniformiv(program, location, params);
    }

    @Override
    public boolean isProgram(int program) {
        report("boolean isProgram");
        return glIsProgram(program);
    }

    @Override
    public void getProgramiv(int program, int pname, IntBuffer params) {
        report("void getProgramiv");
        if (params == null) return;
        glGetProgramiv(program, pname, params);
    }

    @Override
    public String getProgramInfoLog(int program) {
        report("String getProgramInfoLog");
        return glGetProgramInfoLog(program);
    }

    @Override
    public void scissor(int x, int y, int w, int h) {
        report("void scissor");
        glScissor(x,y,w,h);
    }

    @Override
    public void sampleCoverage(float value, boolean invert) {
        report("void sampleCoverage");
        glSampleCoverage(value, invert);
    }

    @Override
    public void stencilFunc(int func, int ref, int mask) {
        report("void stencilFunc");
        glStencilFunc(func, ref, mask);
    }

    @Override
    public void stencilFuncSeparate(int face, int func, int ref, int mask) {
        report("void stencilFuncSeparate");
        glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
    public void stencilOp(int sfail, int dpfail, int dppass) {
        report("void stencilOp");
        glStencilOp(sfail, dpfail, dppass);
    }

    @Override
    public void stencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        report("void stencilOpSeparate");
        glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    @Override
    public void depthFunc(int func) {
        report("void depthFunc");
        glDepthFunc(func);
    }

    @Override
    public void blendEquation(int mode) {
        report("void blendEquation");
        glBlendEquation(mode);
    }

    @Override
    public void blendEquationSeparate(int modeRGB, int modeAlpha) {
        report("void blendEquationSeparate");
        glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void blendFunc(int src, int dst) {
        report("void blendFunc");
        glBlendFunc(src, dst);
    }

    @Override
    public void blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        report("void blendFuncSeparate");
        glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void blendColor(float red, float green, float blue, float alpha) {
        report("void blendColor");
        glBlendColor(red, green, blue, alpha);
    }

    @Override
    public void colorMask(boolean r, boolean g, boolean b, boolean a) {
        report("void colorMask");
        glColorMask(r, g, b, a);
    }

    @Override
    public void depthMask(boolean mask) {
        report("void depthMask");
        glDepthMask(mask);
    }

    @Override
    public void stencilMask(int mask) {
        report("void stencilMask");
        glStencilMask(mask);
    }

    @Override
    public void stencilMaskSeparate(int face, int mask) {
        report("void stencilMaskSeparate");
        glStencilMaskSeparate(face, mask);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        report("void clearColor");
        glClearColor(r,g,b,a);
    }

    @Override
    public void clearDepth(float d) {
        report("void clearDepth");
        glClearDepthf(d);
    }

    @Override
    public void clearStencil(int s) {
        report("void clearStencil");
        glClearStencil(s);
    }

    @Override
    public void clear(int buf) {
        report("void clear");
        glClear(buf);
    }

    @Override
    protected void bindFramebufferImpl(int target, int framebuffer) {
        report("void bindFramebufferImpl");
        glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void deleteFramebuffers(int n, IntBuffer framebuffers) {
        report("void deleteFramebuffers");
        if (framebuffers == null) return;
        glDeleteFramebuffers(framebuffers);
    }

    @Override
    public void genFramebuffers(int n, IntBuffer framebuffers) {
        report("void genFramebuffers");
        if (framebuffers == null) return;
        glGenFramebuffers(framebuffers);
    }

    @Override
    public void bindRenderbuffer(int target, int renderbuffer) {
        report("void bindRenderbuffer");
        glBindFramebuffer(target, renderbuffer);
    }

    @Override
    public void deleteRenderbuffers(int n, IntBuffer renderbuffers) {
        report("void deleteRenderbuffers");
        if (renderbuffers == null) return;
        glDeleteRenderbuffers(renderbuffers);
    }

    @Override
    public void genRenderbuffers(int n, IntBuffer renderbuffers) {
        report("void genRenderbuffers");
        if (renderbuffers == null) return;
        glGenRenderbuffers(renderbuffers);
    }

    @Override
    public void renderbufferStorage(int target, int internalFormat, int width, int height) {
        report("void renderbufferStorage");
        glRenderbufferStorage(target, internalFormat, width, height);
    }

    @Override
    public void framebufferRenderbuffer(int target, int attachment, int rendbufferTarget, int renderbuffer) {
        report("void framebufferRenderbuffer");
        glFramebufferRenderbuffer(target, attachment, rendbufferTarget, renderbuffer);
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int texTarget, int texture, int level) {
        report("void framebufferTexture2D");
        glFramebufferTexture2D(target, attachment, texTarget, texture, level);
    }

    @Override
    public int checkFramebufferStatus(int target) {
        report("int checkFramebufferStatus");
        return glCheckFramebufferStatus(target);
    }

    @Override
    public boolean isFramebuffer(int framebuffer) {
        report("boolean isFramebuffer");
        return glIsFramebuffer(framebuffer);
    }

    @Override
    public void getFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        report("void getFramebufferAttachmentParameteriv");
        if (params == null) return;
        glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    @Override
    public boolean isRenderbuffer(int renderbuffer) {
        report("boolean isRenderbuffer");
        return glIsRenderbuffer(renderbuffer);
    }

    @Override
    public void getRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        report("void getRenderbufferParameteriv");
        if (params == null) return;
        glGetRenderbufferParameteriv(INT, INT, params);
    }

    @Override
    public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        report("void blitFramebuffer");
        glBlitFramebuffer(srcX0, srcY0,  srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public void renderbufferStorageMultisample(int target, int samples, int format, int width, int height) {
        report("void renderbufferStorageMultisample");
        glRenderbufferStorageMultisample(target, samples, format, width, height);
    }

    @Override
    public void readBuffer(int buf) {
        report("void readBuffer");
        glReadBuffer(buf);
    }

    @Override
    public void drawBuffer(int buf) {
        report("void drawBuffer");
        // glDrawBuffer(buf);
    }
    
}
