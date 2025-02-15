package processing.vulkan;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

import processing.GL2VK.GL2VK;
import processing.GL2VK.TextureBuffer;
import processing.GL2VK.Util;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;
import processing.opengl.PGL.TessellatorCallback;
public class PVK extends PGL implements PJOGLInterface {
  static {
    FALSE = GL.GL_FALSE;
    TRUE  = GL.GL_TRUE;

    INT            = GL2ES2.GL_INT;
    BYTE           = GL.GL_BYTE;
    SHORT          = GL.GL_SHORT;
    FLOAT          = GL.GL_FLOAT;
    BOOL           = GL2ES2.GL_BOOL;
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

    READ_ONLY  = GL2ES3.GL_READ_ONLY;
    WRITE_ONLY = GL.GL_WRITE_ONLY;
    READ_WRITE = GL2ES3.GL_READ_WRITE;

    TESS_WINDING_NONZERO = GLU.GLU_TESS_WINDING_NONZERO;
    TESS_WINDING_ODD     = GLU.GLU_TESS_WINDING_ODD;
    TESS_EDGE_FLAG       = GLU.GLU_TESS_EDGE_FLAG;

    GENERATE_MIPMAP_HINT = GL.GL_GENERATE_MIPMAP_HINT;
    FASTEST              = GL.GL_FASTEST;
    NICEST               = GL.GL_NICEST;
    DONT_CARE            = GL.GL_DONT_CARE;

    VENDOR                   = GL.GL_VENDOR;
    RENDERER                 = GL.GL_RENDERER;
    VERSION                  = GL.GL_VERSION;
    EXTENSIONS               = GL.GL_EXTENSIONS;
    SHADING_LANGUAGE_VERSION = GL2ES2.GL_SHADING_LANGUAGE_VERSION;

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
    PIXEL_PACK_BUFFER    = GL2ES3.GL_PIXEL_PACK_BUFFER;

    MAX_VERTEX_ATTRIBS  = GL2ES2.GL_MAX_VERTEX_ATTRIBS;

    STATIC_DRAW  = GL.GL_STATIC_DRAW;
    DYNAMIC_DRAW = GL.GL_DYNAMIC_DRAW;
    STREAM_DRAW  = GL2ES2.GL_STREAM_DRAW;
    STREAM_READ  = GL2ES3.GL_STREAM_READ;

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
    TEXTURE_RECTANGLE = GL2GL3.GL_TEXTURE_RECTANGLE;

    TEXTURE_BINDING_2D        = GL.GL_TEXTURE_BINDING_2D;
    TEXTURE_BINDING_RECTANGLE = GL2GL3.GL_TEXTURE_BINDING_RECTANGLE;

    MAX_TEXTURE_SIZE           = GL.GL_MAX_TEXTURE_SIZE;
    TEXTURE_MAX_ANISOTROPY     = GL.GL_TEXTURE_MAX_ANISOTROPY_EXT;
    MAX_TEXTURE_MAX_ANISOTROPY = GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;

    MAX_VERTEX_TEXTURE_IMAGE_UNITS   = GL2ES2.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
    MAX_TEXTURE_IMAGE_UNITS          = GL2ES2.GL_MAX_TEXTURE_IMAGE_UNITS;
    MAX_COMBINED_TEXTURE_IMAGE_UNITS = GL2ES2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

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
    TEXTURE_WRAP_R     = GL2ES2.GL_TEXTURE_WRAP_R;

    TEXTURE_CUBE_MAP = GL.GL_TEXTURE_CUBE_MAP;
    TEXTURE_CUBE_MAP_POSITIVE_X = GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
    TEXTURE_CUBE_MAP_POSITIVE_Y = GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
    TEXTURE_CUBE_MAP_POSITIVE_Z = GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
    TEXTURE_CUBE_MAP_NEGATIVE_X = GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
    TEXTURE_CUBE_MAP_NEGATIVE_Y = GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
    TEXTURE_CUBE_MAP_NEGATIVE_Z = GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;

    VERTEX_SHADER        = GL2ES2.GL_VERTEX_SHADER;
    FRAGMENT_SHADER      = GL2ES2.GL_FRAGMENT_SHADER;
    INFO_LOG_LENGTH      = GL2ES2.GL_INFO_LOG_LENGTH;
    SHADER_SOURCE_LENGTH = GL2ES2.GL_SHADER_SOURCE_LENGTH;
    COMPILE_STATUS       = GL2ES2.GL_COMPILE_STATUS;
    LINK_STATUS          = GL2ES2.GL_LINK_STATUS;
    VALIDATE_STATUS      = GL2ES2.GL_VALIDATE_STATUS;
    SHADER_TYPE          = GL2ES2.GL_SHADER_TYPE;
    DELETE_STATUS        = GL2ES2.GL_DELETE_STATUS;

    FLOAT_VEC2   = GL2ES2.GL_FLOAT_VEC2;
    FLOAT_VEC3   = GL2ES2.GL_FLOAT_VEC3;
    FLOAT_VEC4   = GL2ES2.GL_FLOAT_VEC4;
    FLOAT_MAT2   = GL2ES2.GL_FLOAT_MAT2;
    FLOAT_MAT3   = GL2ES2.GL_FLOAT_MAT3;
    FLOAT_MAT4   = GL2ES2.GL_FLOAT_MAT4;
    INT_VEC2     = GL2ES2.GL_INT_VEC2;
    INT_VEC3     = GL2ES2.GL_INT_VEC3;
    INT_VEC4     = GL2ES2.GL_INT_VEC4;
    BOOL_VEC2    = GL2ES2.GL_BOOL_VEC2;
    BOOL_VEC3    = GL2ES2.GL_BOOL_VEC3;
    BOOL_VEC4    = GL2ES2.GL_BOOL_VEC4;
    SAMPLER_2D   = GL2ES2.GL_SAMPLER_2D;
    SAMPLER_CUBE = GL2ES2.GL_SAMPLER_CUBE;

    LOW_FLOAT    = GL2ES2.GL_LOW_FLOAT;
    MEDIUM_FLOAT = GL2ES2.GL_MEDIUM_FLOAT;
    HIGH_FLOAT   = GL2ES2.GL_HIGH_FLOAT;
    LOW_INT      = GL2ES2.GL_LOW_INT;
    MEDIUM_INT   = GL2ES2.GL_MEDIUM_INT;
    HIGH_INT     = GL2ES2.GL_HIGH_INT;

    CURRENT_VERTEX_ATTRIB = GL2ES2.GL_CURRENT_VERTEX_ATTRIB;

    VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
    VERTEX_ATTRIB_ARRAY_ENABLED        = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_ENABLED;
    VERTEX_ATTRIB_ARRAY_SIZE           = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_SIZE;
    VERTEX_ATTRIB_ARRAY_STRIDE         = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_STRIDE;
    VERTEX_ATTRIB_ARRAY_TYPE           = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_TYPE;
    VERTEX_ATTRIB_ARRAY_NORMALIZED     = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
    VERTEX_ATTRIB_ARRAY_POINTER        = GL2ES2.GL_VERTEX_ATTRIB_ARRAY_POINTER;

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
    FUNC_MIN              = GL2ES3.GL_MIN;
    FUNC_MAX              = GL2ES3.GL_MAX;
    FUNC_REVERSE_SUBTRACT = GL.GL_FUNC_REVERSE_SUBTRACT;
    FUNC_SUBTRACT         = GL.GL_FUNC_SUBTRACT;

    DITHER = GL.GL_DITHER;

    CONSTANT_COLOR           = GL2ES2.GL_CONSTANT_COLOR;
    CONSTANT_ALPHA           = GL2ES2.GL_CONSTANT_ALPHA;
    ONE_MINUS_CONSTANT_COLOR = GL2ES2.GL_ONE_MINUS_CONSTANT_COLOR;
    ONE_MINUS_CONSTANT_ALPHA = GL2ES2.GL_ONE_MINUS_CONSTANT_ALPHA;
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
    COLOR_ATTACHMENT1  = GL2ES2.GL_COLOR_ATTACHMENT1;
    COLOR_ATTACHMENT2  = GL2ES2.GL_COLOR_ATTACHMENT2;
    COLOR_ATTACHMENT3  = GL2ES2.GL_COLOR_ATTACHMENT3;
    RENDERBUFFER       = GL.GL_RENDERBUFFER;
    DEPTH_ATTACHMENT   = GL.GL_DEPTH_ATTACHMENT;
    STENCIL_ATTACHMENT = GL.GL_STENCIL_ATTACHMENT;
    READ_FRAMEBUFFER   = GL.GL_READ_FRAMEBUFFER;
    DRAW_FRAMEBUFFER   = GL.GL_DRAW_FRAMEBUFFER;

    DEPTH24_STENCIL8 = GL.GL_DEPTH24_STENCIL8;

    DEPTH_COMPONENT   = GL2ES2.GL_DEPTH_COMPONENT;
    DEPTH_COMPONENT16 = GL.GL_DEPTH_COMPONENT16;
    DEPTH_COMPONENT24 = GL.GL_DEPTH_COMPONENT24;
    DEPTH_COMPONENT32 = GL.GL_DEPTH_COMPONENT32;

    STENCIL_INDEX  = GL2ES2.GL_STENCIL_INDEX;
    STENCIL_INDEX1 = GL.GL_STENCIL_INDEX1;
    STENCIL_INDEX4 = GL.GL_STENCIL_INDEX4;
    STENCIL_INDEX8 = GL.GL_STENCIL_INDEX8;

    DEPTH_STENCIL = GL.GL_DEPTH_STENCIL;

    FRAMEBUFFER_COMPLETE                      = GL.GL_FRAMEBUFFER_COMPLETE;
    FRAMEBUFFER_UNDEFINED                     = GL2ES3.GL_FRAMEBUFFER_UNDEFINED;
    FRAMEBUFFER_INCOMPLETE_ATTACHMENT         = GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
    FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
    FRAMEBUFFER_INCOMPLETE_DIMENSIONS         = GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
    FRAMEBUFFER_INCOMPLETE_FORMATS            = GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS;
    FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        = GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
    FRAMEBUFFER_INCOMPLETE_READ_BUFFER        = GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
    FRAMEBUFFER_UNSUPPORTED                   = GL.GL_FRAMEBUFFER_UNSUPPORTED;
    FRAMEBUFFER_INCOMPLETE_MULTISAMPLE        = GL.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
    FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS      = GL3ES3.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;

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

    MULTISAMPLE    = GL.GL_MULTISAMPLE;
    LINE_SMOOTH    = GL.GL_LINE_SMOOTH;
    POLYGON_SMOOTH = GL2GL3.GL_POLYGON_SMOOTH;

    SYNC_GPU_COMMANDS_COMPLETE = GL3ES3.GL_SYNC_GPU_COMMANDS_COMPLETE;
    ALREADY_SIGNALED           = GL3ES3.GL_ALREADY_SIGNALED;
    CONDITION_SATISFIED        = GL3ES3.GL_CONDITION_SATISFIED;
  }

  private HashMap<Integer, String> glEnumToStrings = new HashMap<>();

  private GL2VK gl2vk;


  /** GLU interface **/
  // For backward compatibility idk
  // I hope this doesn't require openGL
  public GLU glu;

  public PVK(PGraphicsOpenGL pg, GL2VK gl2vk) {
    this.gl2vk = gl2vk;
    this.graphics = pg;
    glu = new GLU();
  }

  public PVK(PGraphicsOpenGL pg) {
    this.graphics = pg;
    glu = new GLU();
  }

  {
    prepareStrings();
    if (glColorTex == null) {
      glColorFbo = allocateIntBuffer(1);
      glColorTex = allocateIntBuffer(2);
      glDepthStencil = allocateIntBuffer(1);
      glDepth = allocateIntBuffer(1);
      glStencil = allocateIntBuffer(1);

      glMultiFbo = allocateIntBuffer(1);
      glMultiColor = allocateIntBuffer(1);
      glMultiDepthStencil = allocateIntBuffer(1);
      glMultiDepth = allocateIntBuffer(1);
      glMultiStencil = allocateIntBuffer(1);
    }

    byteBuffer = allocateByteBuffer(1);
    intBuffer = allocateIntBuffer(1);
    viewBuffer = allocateIntBuffer(4);
  }

  public void setGL2VK(GL2VK gl2vk) {
    this.gl2vk = gl2vk;
  }

  public void cleanup() {
    gl2vk.close();
  }

  public boolean shouldClose() {
    return gl2vk.shouldClose();
  }

  public void beginRecord() {
    report("beginRecord");
    gl2vk.beginRecord();
  }

  public void endRecord() {
    report("endRecord");
    gl2vk.endRecord();
  }

  public void selectNode(int node) {
    gl2vk.disableAutoMode();
    gl2vk.selectNode(node);
  }

  public void enableAutoMode() {
    gl2vk.enableAutoMode();
  }

  public int getNodesCount() {
    return gl2vk.getNodesCount();
  }

  public void setMaxNodes(int v) {
    gl2vk.setMaxNodes(v);
  }


  public void bufferMultithreaded(boolean onoff) {
    gl2vk.bufferMultithreaded(onoff);
  }

  private void report(String func) {
//    System.out.println(func);
  }


  private void prepareStrings() {
    // OpenGL strings only really have this
    // information being loaded into the
    // hashmap below.

    // TODO: Query vulkan
    glEnumToStrings.put(VENDOR, "Intel");
    glEnumToStrings.put(RENDERER, "Intel(R) UHD Graphics 620");

    // For obvious reasons, this needs to be spoofed as some version of OpenGL.
    // Let's just use the version on my Surface Book 2.
    glEnumToStrings.put(VERSION, "4.6.0 - Build 30.0.101.1339");

    // TODO:
    // Convert Vulkan extensions to comparibly equivalent OpenGL
    // extensions and hope for the best this doesn't break anything.
    glEnumToStrings.put(EXTENSIONS, "GL_3DFX_texture_compression_FXT1 GL_AMD_depth_clamp_separate GL_AMD_vertex_shader_layer GL_AMD_vertex_shader_viewport_index GL_ARB_ES2_compatibility GL_ARB_ES3_1_compatibility GL_ARB_ES3_compatibility GL_ARB_arrays_of_arrays GL_ARB_base_instance GL_ARB_bindless_texture GL_ARB_blend_func_extended GL_ARB_buffer_storage GL_ARB_cl_event GL_ARB_clear_buffer_object GL_ARB_clear_texture GL_ARB_clip_control GL_ARB_color_buffer_float GL_ARB_compressed_texture_pixel_storage GL_ARB_compute_shader GL_ARB_conditional_render_inverted GL_ARB_conservative_depth GL_ARB_copy_buffer GL_ARB_copy_image GL_ARB_cull_distance GL_ARB_debug_output GL_ARB_depth_buffer_float GL_ARB_depth_clamp GL_ARB_depth_texture GL_ARB_derivative_control GL_ARB_direct_state_access GL_ARB_draw_buffers GL_ARB_draw_buffers_blend GL_ARB_draw_elements_base_vertex GL_ARB_draw_indirect GL_ARB_draw_instanced GL_ARB_enhanced_layouts GL_ARB_explicit_attrib_location GL_ARB_explicit_uniform_location GL_ARB_fragment_coord_conventions GL_ARB_fragment_layer_viewport GL_ARB_fragment_program GL_ARB_fragment_program_shadow GL_ARB_fragment_shader GL_ARB_fragment_shader_interlock GL_ARB_framebuffer_no_attachments GL_ARB_framebuffer_object GL_ARB_framebuffer_sRGB GL_ARB_geometry_shader4 GL_ARB_get_program_binary GL_ARB_get_texture_sub_image GL_ARB_gl_spirv GL_ARB_gpu_shader5 GL_ARB_gpu_shader_fp64 GL_ARB_half_float_pixel GL_ARB_half_float_vertex GL_ARB_indirect_parameters GL_ARB_instanced_arrays GL_ARB_internalformat_query GL_ARB_internalformat_query2 GL_ARB_invalidate_subdata GL_ARB_map_buffer_alignment GL_ARB_map_buffer_range GL_ARB_multi_bind GL_ARB_multi_draw_indirect GL_ARB_multisample GL_ARB_multitexture GL_ARB_occlusion_query GL_ARB_occlusion_query2 GL_ARB_pipeline_statistics_query GL_ARB_pixel_buffer_object GL_ARB_point_parameters GL_ARB_point_sprite GL_ARB_polygon_offset_clamp GL_ARB_post_depth_coverage GL_ARB_program_interface_query GL_ARB_provoking_vertex GL_ARB_query_buffer_object GL_ARB_robust_buffer_access_behavior GL_ARB_robustness GL_ARB_robustness_isolation GL_ARB_sample_shading GL_ARB_sampler_objects GL_ARB_seamless_cube_map GL_ARB_seamless_cubemap_per_texture GL_ARB_separate_shader_objects GL_ARB_shader_atomic_counter_ops GL_ARB_shader_atomic_counters GL_ARB_shader_bit_encoding GL_ARB_shader_draw_parameters GL_ARB_shader_group_vote GL_ARB_shader_image_load_store GL_ARB_shader_image_size GL_ARB_shader_objects GL_ARB_shader_precision GL_ARB_shader_stencil_export GL_ARB_shader_storage_buffer_object GL_ARB_shader_subroutine GL_ARB_shader_texture_image_samples GL_ARB_shading_language_100 GL_ARB_shading_language_420pack GL_ARB_shading_language_packing GL_ARB_shadow GL_ARB_spirv_extensions GL_ARB_stencil_texturing GL_ARB_sync GL_ARB_tessellation_shader GL_ARB_texture_barrier GL_ARB_texture_border_clamp GL_ARB_texture_buffer_object GL_ARB_texture_buffer_object_rgb32 GL_ARB_texture_buffer_range GL_ARB_texture_compression GL_ARB_texture_compression_bptc GL_ARB_texture_compression_rgtc GL_ARB_texture_cube_map GL_ARB_texture_cube_map_array GL_ARB_texture_env_add GL_ARB_texture_env_combine GL_ARB_texture_env_crossbar GL_ARB_texture_env_dot3 GL_ARB_texture_filter_anisotropic GL_ARB_texture_float GL_ARB_texture_gather GL_ARB_texture_mirror_clamp_to_edge GL_ARB_texture_mirrored_repeat GL_ARB_texture_multisample GL_ARB_texture_non_power_of_two GL_ARB_texture_query_levels GL_ARB_texture_query_lod GL_ARB_texture_rectangle GL_ARB_texture_rg GL_ARB_texture_rgb10_a2ui GL_ARB_texture_stencil8 GL_ARB_texture_storage GL_ARB_texture_storage_multisample GL_ARB_texture_swizzle GL_ARB_texture_view GL_ARB_timer_query GL_ARB_transform_feedback2 GL_ARB_transform_feedback3 GL_ARB_transform_feedback_instanced GL_ARB_transform_feedback_overflow_query GL_ARB_transpose_matrix GL_ARB_uniform_buffer_object GL_ARB_vertex_array_bgra GL_ARB_vertex_array_object GL_ARB_vertex_attrib_64bit GL_ARB_vertex_attrib_binding GL_ARB_vertex_buffer_object GL_ARB_vertex_program GL_ARB_vertex_shader GL_ARB_vertex_type_10f_11f_11f_rev GL_ARB_vertex_type_2_10_10_10_rev GL_ARB_viewport_array GL_ARB_window_pos GL_ATI_separate_stencil GL_EXT_abgr GL_EXT_bgra GL_EXT_blend_color GL_EXT_blend_equation_separate GL_EXT_blend_func_separate GL_EXT_blend_minmax GL_EXT_blend_subtract GL_EXT_clip_volume_hint GL_EXT_compiled_vertex_array GL_EXT_direct_state_access GL_EXT_draw_buffers2 GL_EXT_draw_range_elements GL_EXT_fog_coord GL_EXT_framebuffer_blit GL_EXT_framebuffer_multisample GL_EXT_framebuffer_object GL_EXT_geometry_shader4 GL_EXT_gpu_program_parameters GL_EXT_gpu_shader4 GL_EXT_memory_object GL_EXT_memory_object_win32 GL_EXT_multi_draw_arrays GL_EXT_packed_depth_stencil GL_EXT_packed_float GL_EXT_packed_pixels GL_EXT_polygon_offset_clamp GL_EXT_rescale_normal GL_EXT_secondary_color GL_EXT_semaphore GL_EXT_semaphore_win32 GL_EXT_separate_specular_color GL_EXT_shader_framebuffer_fetch GL_EXT_shader_integer_mix GL_EXT_shadow_funcs GL_EXT_stencil_two_side GL_EXT_stencil_wrap GL_EXT_texture3D GL_EXT_texture_array GL_EXT_texture_compression_s3tc GL_EXT_texture_edge_clamp GL_EXT_texture_env_add GL_EXT_texture_env_combine GL_EXT_texture_filter_anisotropic GL_EXT_texture_integer GL_EXT_texture_lod_bias GL_EXT_texture_rectangle GL_EXT_texture_sRGB GL_EXT_texture_sRGB_decode GL_EXT_texture_shared_exponent GL_EXT_texture_snorm GL_EXT_texture_storage GL_EXT_texture_swizzle GL_EXT_timer_query GL_EXT_transform_feedback GL_IBM_texture_mirrored_repeat GL_INTEL_conservative_rasterization GL_INTEL_fragment_shader_ordering GL_INTEL_framebuffer_CMAA GL_INTEL_map_texture GL_INTEL_multi_rate_fragment_shader GL_INTEL_performance_query GL_KHR_blend_equation_advanced GL_KHR_blend_equation_advanced_coherent GL_KHR_context_flush_control GL_KHR_debug GL_KHR_no_error GL_KHR_shader_subgroup GL_KHR_shader_subgroup_arithmetic GL_KHR_shader_subgroup_ballot GL_KHR_shader_subgroup_basic GL_KHR_shader_subgroup_clustered GL_KHR_shader_subgroup_quad GL_KHR_shader_subgroup_shuffle GL_KHR_shader_subgroup_shuffle_relative GL_KHR_shader_subgroup_vote GL_KHR_texture_compression_astc_hdr GL_KHR_texture_compression_astc_ldr GL_NV_blend_square GL_NV_conditional_render GL_NV_primitive_restart GL_NV_texgen_reflection GL_SGIS_generate_mipmap GL_SGIS_texture_edge_clamp GL_SGIS_texture_lod GL_SUN_multi_draw_arrays GL_WIN_swap_hint WGL_EXT_swap_control");

    // This needs to be spoofed too.
    glEnumToStrings.put(SHADING_LANGUAGE_VERSION, "4.60 - Build 30.0.101.1339");
  }

  @Override
  public Object getNative() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setCaps(GLCapabilities caps) {
    // TODO Auto-generated method stub

  }

  public GLCapabilitiesImmutable getCaps() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean needSharedObjectSync() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setFps(float fps) {
    // TODO Auto-generated method stub

  }

  public void getGL(GLAutoDrawable glDrawable) {
//    setThread(Thread.currentThread());
  }

  @Override
  public boolean threadIsCurrent()  {
    // idc
    return true;
  }

  @Override
  protected int[] getGLVersion() {
    int[] res = {4, 6, 0};
    return res;
  }

  public void init(GLAutoDrawable glDrawable) {
    // TODO Auto-generated method stub

  }

  public void showme(String method, int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void flush() {
    // TODO Auto-generated method stub

  }

  @Override
  public void finish() {
    // TODO Auto-generated method stub

  }

  @Override
  public void hint(int target, int hint) {
    // TODO Auto-generated method stub

  }

  @Override
  public void enable(int value) {
    int gl2vkValue = -1;
    if (value == DEPTH_TEST) {
      gl2vkValue = GL2VK.DEPTH_TEST;
    }
    else if (value == BLEND) {
      gl2vkValue = GL2VK.BLEND;
    }
    else if (value == MULTISAMPLE) {
      gl2vkValue = GL2VK.MULTISAMPLE;
    }
    else if (value == POLYGON_SMOOTH) {
      gl2vkValue = GL2VK.POLYGON_SMOOTH;
    }
    else if (value == CULL_FACE) {
      gl2vkValue = GL2VK.CULL_FACE;
    }

    gl2vk.glEnable(gl2vkValue);
  }

  @Override
  public void disable(int value) {
    if (value == DEPTH_TEST) {

    }
    else if (value == BLEND) {

    }
    else if (value == MULTISAMPLE) {

    }
    else if (value == POLYGON_SMOOTH) {

    }
    else if (value == CULL_FACE) {

    }
//    gl2vk.glDisable(value);
  }

  @Override
  public void getBooleanv(int value, IntBuffer data) {
    // TODO Auto-generated method stub
    System.out.println("getBooleanv UNKNOWN "+value);
  }

  @Override
  // TODO: actual values instead of placeholder values.
  // Have fun with that.
  public void getIntegerv(int value, IntBuffer data) {
//    MAX_TEXTURE_SIZE 16384
//    MAX_SAMPLES 16
//    MAX_TEXTURE_MAX_ANISOTROPY 16.0

    if (value == MAX_TEXTURE_SIZE) {
      data.put(0, 16384);
    }
    else if (value == MAX_SAMPLES) {
      data.put(0, 16);
    }
    else if (value == MAX_TEXTURE_IMAGE_UNITS) {
      data.put(0, 24);
    }
    else {
      System.out.println("getIntegerv UNKNOWN "+value);
    }
  }

  @Override
  public void getFloatv(int value, FloatBuffer data) {
//    MAX_TEXTURE_SIZE 16384
//    MAX_SAMPLES 16
//    MAX_TEXTURE_MAX_ANISOTROPY 16.0

    if (value == MAX_TEXTURE_MAX_ANISOTROPY) {
      data.put(0, 16f);
    }
    else {
      System.out.println("getFloatv UNKNOWN "+value);
    }
  }

  @Override
  public boolean isEnabled(int value) {
    // TODO Auto-generated method stub
    return false;
  }

  @SuppressWarnings("deprecation")
  private FontMetrics getFontMetrics(Font font) {  // ignore
        report("getFontMetrics");
    return Toolkit.getDefaultToolkit().getFontMetrics(font);
  }


  @Override
  protected int getTextWidth(Object font, char[] buffer, int start, int stop) {
        report("getTextWidth");
    // maybe should use one of the newer/fancier functions for this?
    int length = stop - start;
    FontMetrics metrics = getFontMetrics((Font) font);
    return metrics.charsWidth(buffer, start, length);
  }



  @Override
  public int getError() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String errorString(int err) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void genBuffers(int n, IntBuffer buffers) {
    report("genBuffers");
    gl2vk.glGenBuffers(n, buffers);
  }

  @Override
  public void deleteBuffers(int n, IntBuffer buffers) {
    // TODO: Deletebuffers
  }

  @Override
  public void bindBuffer(int target, int buffer) {
    report("bindBuffer");
    gl2vk.glBindBuffer(target, buffer);
  }


  public static ByteBuffer convertToByteBuffer(Buffer outputBuffer) {
    if (outputBuffer == null) return null;

    ByteBuffer byteBuffer = null;
    if (outputBuffer instanceof ByteBuffer) {
      System.out.println("ByteBuffer");
        byteBuffer = (ByteBuffer) outputBuffer;
    } else if (outputBuffer instanceof CharBuffer) {
      System.out.println("CharBuffer");
        byteBuffer = ByteBuffer.allocate(outputBuffer.capacity());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asCharBuffer().put((CharBuffer) outputBuffer);
    } else if (outputBuffer instanceof ShortBuffer) {
      System.out.println("ShortBuffer");
        byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asShortBuffer().put((ShortBuffer) outputBuffer);
    } else if (outputBuffer instanceof IntBuffer) {
      System.out.println("IntBuffer");
        byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asIntBuffer().put((IntBuffer) outputBuffer);
    } else if (outputBuffer instanceof LongBuffer) {
      System.out.println("LongBuffer");
        byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asLongBuffer().put((LongBuffer) outputBuffer);
    } else if (outputBuffer instanceof FloatBuffer) {
      System.out.println("FloatBuffer");
        byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asFloatBuffer().put((FloatBuffer) outputBuffer);
    } else if (outputBuffer instanceof DoubleBuffer) {
      System.out.println("DoubleBuffer");
        byteBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asDoubleBuffer().put((DoubleBuffer) outputBuffer);
    }
    else {
      System.err.println("Unknown buffer "+outputBuffer.getClass().toString());
    }
    return byteBuffer;
  }




  @Override
  public void bufferData(int target, int size, Buffer data, int usage) {
    int gl2vktarget = -1;
    if (target == ARRAY_BUFFER) {
      gl2vktarget = GL2VK.GL_VERTEX_BUFFER;
    }
    else if (target == ELEMENT_ARRAY_BUFFER) {
      gl2vktarget = GL2VK.GL_INDEX_BUFFER;
    }
    else {
      System.err.println("bufferData: Dunno what to do with target "+target);
    }

    int gl2vkusage = -1;
    if (usage == STREAM_DRAW) {
      gl2vkusage = GL2VK.STREAM_DRAW;
    } else if (usage == STREAM_READ) {
      gl2vkusage = GL2VK.STREAM_READ;
    } else if (usage == STATIC_DRAW) {
      gl2vkusage = GL2VK.STATIC_DRAW;
    } else if (usage == DYNAMIC_DRAW) {
      gl2vkusage = GL2VK.DYNAMIC_DRAW;
    }
    else {
      System.err.println("bufferData: Unknown usage "+usage);
    }


    report("bufferData");

//    ByteBuffer databyte = convertToByteBuffer(data);


//    System.out.println();
//  if (databyte != null) {
////    newData = ByteBuffer.allocateDirect(data.capacity());
////    newData.order(ByteOrder.LITTLE_ENDIAN);
//    databyte.rewind();
//
//    int max = databyte.capacity();
//    if (32 < max) max = 64;
//
//    for (int i = 0; i < max; i+=4) {
//      float f = databyte.getFloat();
////      if (f > 1.0f) f /= 512f;
//      System.out.print(f+" ");
////      newData.putFloat(f);
//    }
//    databyte.rewind();
//    System.out.println();
//
//    for (int i = 0; i < max; i+=4) {
//      short x = databyte.getShort();
////      if (f > 1.0f) f /= 512f;
//      System.out.print(x+" ");
////      newData.putFloat(f);
//    }
//    databyte.rewind();
////    newData.rewind();
//    System.out.println();

    if (data instanceof ByteBuffer) {
      gl2vk.glBufferData(gl2vktarget, size, (ByteBuffer)data, gl2vkusage);
    } else if (data instanceof CharBuffer) {
      gl2vk.glBufferData(gl2vktarget, size, (ByteBuffer)data, gl2vkusage);
    } else if (data instanceof ShortBuffer) {
      gl2vk.glBufferData(gl2vktarget, size, (ShortBuffer)data, gl2vkusage);
    } else if (data instanceof IntBuffer) {
      gl2vk.glBufferData(gl2vktarget, size, (IntBuffer)data, gl2vkusage);
    } else if (data instanceof LongBuffer) {
      System.err.println("bufferData: LongBuffer not support");
    } else if (data instanceof FloatBuffer) {
      gl2vk.glBufferData(gl2vktarget, size, (FloatBuffer)data, gl2vkusage);
    } else if (data instanceof DoubleBuffer) {
      System.err.println("bufferData: DoubleBuffer not support");
    } else if (data == null) {
      // Just create the buffer
      gl2vk.glBufferData(gl2vktarget, size, gl2vkusage);
    }
    else {
      System.err.println("bufferData: Unknown buffer type "+data.getClass().getName());
    }
  }

  @Override
  public void bufferSubData(int target, int offset, int size, Buffer data) {
//    TODO
  }

  @Override
  public void isBuffer(int buffer) {
    // TODO

  }

  // Because we don't have our GL anymore, we need to return an emulated list of
  // OpenGL's int->names. The emulated list is right here in this class.
  @Override
  public String getString(int name) {
    return glEnumToStrings.get(name);
  }

  @Override
  protected boolean isES() {
    return false;
  }

  @Override
  public void getBufferParameteriv(int target, int value, IntBuffer data) {
    // TODO Auto-generated method stub

  }

  @Override
  public ByteBuffer mapBuffer(int target, int access) {
    return gl2vk.glMapBuffer(target, access);
  }

  @Override
  public ByteBuffer mapBufferRange(int target, int offset, int length,
                                   int access) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void unmapBuffer(int target) {
    gl2vk.glUnmapBuffer(target);

  }

  @Override
  public long fenceSync(int condition, int flags) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void deleteSync(long sync) {
    // TODO Auto-generated method stub

  }

  @Override
  public int clientWaitSync(long sync, int flags, long timeout) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void depthRangef(float n, float f) {
    // TODO Auto-generated method stub

  }

  @Override
  public void viewport(int x, int y, int w, int h) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib1f(int index, float value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib2f(int index, float value0, float value1) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib3f(int index, float value0, float value1,
                             float value2) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib4f(int index, float value0, float value1,
                             float value2, float value3) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib1fv(int index, FloatBuffer values) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib2fv(int index, FloatBuffer values) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib3fv(int index, FloatBuffer values) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttrib4fv(int index, FloatBuffer values) {
    // TODO Auto-generated method stub

  }

  @Override
  public void vertexAttribPointer(int index, int size, int type,
                                  boolean normalized, int stride, int offset) {
    report("vertexAttribPointer");

    int gl2vktype = 0;
    if (type == UNSIGNED_BYTE)
      gl2vktype = GL2VK.GL_UNSIGNED_BYTE;
    else if (type == FLOAT)
      gl2vktype = GL2VK.GL_FLOAT;
    else if (type ==  UNSIGNED_SHORT)
      gl2vktype = GL2VK.GL_UNSIGNED_SHORT;
    else if (type == UNSIGNED_INT)
      gl2vktype = GL2VK.GL_UNSIGNED_INT;
    else if (type == INT)
      gl2vktype = GL2VK.GL_INT;
    else if (type == BYTE)
      gl2vktype = GL2VK.GL_BYTE;
    else if (type == SHORT)
      gl2vktype = GL2VK.GL_SHORT;
    else if (type == BOOL)
      gl2vktype = GL2VK.GL_BOOL;
    else System.out.println("WARNING  Unknown type "+type);

    gl2vk.glVertexAttribPointer(index, size, gl2vktype, normalized, stride, offset);

  }

  @Override
  public void enableVertexAttribArray(int index) {
    // Unneeded and unused in gl2vk

    gl2vk.glEnableVertexAttribArray(index);
  }

  @Override
  public void disableVertexAttribArray(int index) {
    // Unneeded and unused in gl2vk
    gl2vk.glDisableVertexAttribArray(index);
  }

  @Override
  public void drawArraysImpl(int mode, int first, int count) {
    report("drawArrays");
    gl2vk.glDrawArrays(mode, first, count);
  }

  @Override
  public void drawElementsImpl(int mode, int count, int type, int offset) {
    int gl2vktype = 0;
    if (type == UNSIGNED_BYTE) {
      gl2vktype = GL2VK.GL_UNSIGNED_BYTE;
    }
    else if (type == UNSIGNED_INT) {
      gl2vktype = GL2VK.GL_UNSIGNED_INT;
    }
    else if (type == UNSIGNED_SHORT) {
      gl2vktype = GL2VK.GL_UNSIGNED_SHORT;
    }
    report("drawElements");
    gl2vk.glDrawElements(mode, count, gl2vktype, offset);
  }

  @Override
  public void lineWidth(float width) {
    // TODO Auto-generated method stub
  }

  @Override
  public void frontFace(int dir) {
    // TODO Auto-generated method stub
  }

  @Override
  public void cullFace(int mode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void polygonOffset(float factor, float units) {
    // TODO Auto-generated method stub

  }

  @Override
  public void pixelStorei(int pname, int param) {
    // TODO Auto-generated method stub

  }

  @Override
  public void texImage2D(int target, int level, int internalFormat, int width,
                         int height, int border, int format, int type,
                         Buffer data) {
    gl2vk.glTexImage2D(target, level, internalFormat, width, height, border, format, type, data);
  }

  @Override
  public void copyTexImage2D(int target, int level, int internalFormat, int x,
                             int y, int width, int height, int border) {
    // TODO Auto-generated method stub

  }

  @Override
  public void texSubImage2D(int target, int level, int xOffset, int yOffset,
                            int width, int height, int format, int type,
                            Buffer data) {
    report("texSubImage2D");

    // Don't buffer the white 16x16 memory-saver slow-af clear texture.
    // TODO: automatically detect incoming wave of clear textures, and then
    // submit vkCmdClearImage command.
    if (!(width == 16 && height == 16)) {
      gl2vk.glTexSubImage2D(target, level, xOffset, yOffset, width, height, format, type, data);
    }
  }

  @Override
  public void copyTexSubImage2D(int target, int level, int xOffset, int yOffset,
                                int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void compressedTexImage2D(int target, int level, int internalFormat,
                                   int width, int height, int border,
                                   int imageSize, Buffer data) {
    // TODO Auto-generated method stub

  }

  @Override
  public void compressedTexSubImage2D(int target, int level, int xOffset,
                                      int yOffset, int width, int height,
                                      int format, int imageSize, Buffer data) {
    // TODO Auto-generated method stub

  }

  @Override
  public void texParameteri(int target, int pname, int param) {
    // TODO Auto-generated method stub

  }

  @Override
  public void texParameterf(int target, int pname, float param) {
    // TODO Auto-generated method stub

  }

  @Override
  public void texParameteriv(int target, int pname, IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void texParameterfv(int target, int pname, FloatBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void generateMipmap(int target) {
    // TODO Auto-generated method stub

  }

  @Override
  public void genTextures(int n, IntBuffer textures) {
    report("genTextures");
    gl2vk.glGenTextures(n, textures);
  }

  @Override
  public void deleteTextures(int n, IntBuffer textures) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getTexParameteriv(int target, int pname, IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getTexParameterfv(int target, int pname, FloatBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isTexture(int texture) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int createShader(int type) {
    report("createShader");
    int gl2vktype = 0;
    if (type == VERTEX_SHADER) {
      gl2vktype = GL2VK.GL_VERTEX_SHADER;
    }
    else {
      gl2vktype = GL2VK.GL_FRAGMENT_SHADER;
    }
    return gl2vk.glCreateShader(gl2vktype);
  }

  @Override
  public void shaderSource(int shader, String source) {
    report("shaderSource");
    gl2vk.glShaderSource(shader, source);
  }

  @Override
  public void compileShader(int shader) {
    report("compileShader");
    gl2vk.glCompileShader(shader);
  }

  @Override
  public void releaseShaderCompiler() {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteShader(int shader) {
    report("deleteShader");
    gl2vk.glDeleteShader(shader);

  }

  @Override
  public void shaderBinary(int count, IntBuffer shaders, int binaryFormat,
                           Buffer binary, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public int createProgram() {
    report("createProgram");
    return gl2vk.glCreateProgram();
  }

  @Override
  public void attachShader(int program, int shader) {
    report("attachShader");
    gl2vk.glAttachShader(program, shader);
  }

  @Override
  public void detachShader(int program, int shader) {
    // TODO Auto-generated method stub

  }

  @Override
  public void linkProgram(int program) {
    report("linkProgram");
    gl2vk.glLinkProgram(program);
  }

  @Override
  public void useProgram(int program) {
    report("useProgram");
    gl2vk.glUseProgram(program);
  }

  @Override
  public void deleteProgram(int program) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getActiveAttrib(int program, int index, IntBuffer size,
                                IntBuffer type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getAttribLocation(int program, String name) {
    report("getAttribLocation");
    return gl2vk.glGetAttribLocation(program, name);
  }

  @Override
  public void bindAttribLocation(int program, int index, String name) {

  }

  @Override
  public int getUniformLocation(int program, String name) {
    report("getUniformLocation");
    return gl2vk.glGetUniformLocation(program, name);
  }

  @Override
  public String getActiveUniform(int program, int index, IntBuffer size,
                                 IntBuffer type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void uniform1i(int location, int value) {
    report("uniform1i");
//    gl2vk.glUniform1i(location, value);
  }

  @Override
  public void uniform2i(int location, int value0, int value1) {
    report("uniform2i");
    gl2vk.glUniform2i(location, value0, value1);
  }

  @Override
  public void uniform3i(int location, int value0, int value1, int value2) {
    report("uniform3i");
    gl2vk.glUniform3i(location, value0, value1, value2);
  }

  @Override
  public void uniform4i(int location, int value0, int value1, int value2,
                        int value3) {
    report("uniform4i");
    gl2vk.glUniform4i(location, value0, value1, value2, value3);
  }

  @Override
  public void uniform1f(int location, float value) {
    report("uniform1f");
    gl2vk.glUniform1f(location, value);

  }

  @Override
  public void uniform2f(int location, float value0, float value1) {
    report("uniform2f");
//    gl2vk.glUniform2f(location, value0, value1);

  }

  @Override
  public void uniform3f(int location, float value0, float value1,
                        float value2) {
    report("uniform3f");
    gl2vk.glUniform3f(location, value0, value1, value2);

  }

  @Override
  public void uniform4f(int location, float value0, float value1, float value2,
                        float value3) {
    report("uniform4f");
    gl2vk.glUniform4f(location, value0, value1, value2, value3);

  }

  @Override
  public void uniform1iv(int location, int count, IntBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform2iv(int location, int count, IntBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform3iv(int location, int count, IntBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform4iv(int location, int count, IntBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform1fv(int location, int count, FloatBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform2fv(int location, int count, FloatBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform3fv(int location, int count, FloatBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniform4fv(int location, int count, FloatBuffer v) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniformMatrix2fv(int location, int count, boolean transpose,
                               FloatBuffer mat) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniformMatrix3fv(int location, int count, boolean transpose,
                               FloatBuffer mat) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uniformMatrix4fv(int location, int count, boolean transpose,
                               FloatBuffer mat) {

    report("uniformMatrix4fv");
    gl2vk.glUniformMatrix4fv(location, count, transpose, mat);

//    mat.rewind();
//    for (int y = 0; y < 4; y++) {
//      for (int x = 0; x < 4; x++) {
//        System.out.print(" "+mat.get());
//      }
//      System.out.println();
//    }
//
//    System.out.println();
//
//    mat.rewind();
  }

  @Override
  public void validateProgram(int program) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isShader(int shader) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public void getShaderiv(int shader, int pname, IntBuffer params) {
    report("getShaderiv");
    int gl2vkpname = 0;
    if (pname == COMPILE_STATUS) {
      gl2vkpname = GL2VK.GL_COMPILE_STATUS;
    }
    else if (pname == INFO_LOG_LENGTH) {
      gl2vkpname = GL2VK.GL_INFO_LOG_LENGTH;
    }
    gl2vk.glGetShaderiv(shader, gl2vkpname, params);
  }

  @Override
  public void getAttachedShaders(int program, int maxCount, IntBuffer count,
                                 IntBuffer shaders) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getShaderInfoLog(int shader) {
    report("getShaderInfoLog");
    return gl2vk.glGetShaderInfoLog(shader);
  }

  @Override
  public String getShaderSource(int shader) {
    report("getShaderSource");
    return gl2vk.glGetShaderSource(shader);
  }

  @Override
  public void getShaderPrecisionFormat(int shaderType, int precisionType,
                                       IntBuffer range, IntBuffer precision) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getVertexAttribfv(int index, int pname, FloatBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getVertexAttribiv(int index, int pname, IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getVertexAttribPointerv(int index, int pname, ByteBuffer data) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getUniformfv(int program, int location, FloatBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void getUniformiv(int program, int location, IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isProgram(int program) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void getProgramiv(int program, int pname, IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getProgramInfoLog(int program) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void scissor(int x, int y, int w, int h) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sampleCoverage(float value, boolean invert) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stencilFunc(int func, int ref, int mask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stencilFuncSeparate(int face, int func, int ref, int mask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stencilOp(int sfail, int dpfail, int dppass) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
    // TODO Auto-generated method stub

  }

  @Override
  public void depthFunc(int func) {
    // TODO Auto-generated method stub

  }

  @Override
  public void blendEquation(int mode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void blendEquationSeparate(int modeRGB, int modeAlpha) {
    // TODO Auto-generated method stub

  }

  @Override
  public void blendFunc(int src, int dst) {
    // TODO Auto-generated method stub

  }

  @Override
  public void blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
                                int dstAlpha) {
    // TODO Auto-generated method stub

  }

  @Override
  public void blendColor(float red, float green, float blue, float alpha) {
    // TODO Auto-generated method stub

  }

  @Override
  public void colorMask(boolean r, boolean g, boolean b, boolean a) {
    // TODO Auto-generated method stub

  }

  @Override
  public void depthMask(boolean mask) {
    gl2vk.glDepthMask(mask);
  }

  @Override
  public void stencilMask(int mask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stencilMaskSeparate(int face, int mask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearColor(float r, float g, float b, float a) {
    // TODO Auto-generated method stub
    gl2vk.glClearColor(r, g, b, a);
  }

  @Override
  public void clearDepth(float d) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearStencil(int s) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clear(int buf) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteFramebuffers(int n, IntBuffer framebuffers) {
    // TODO Auto-generated method stub

  }

  @Override
  public void genFramebuffers(int n, IntBuffer framebuffers) {
    // TODO Auto-generated method stub

  }

  @Override
  public void bindRenderbuffer(int target, int renderbuffer) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteRenderbuffers(int n, IntBuffer renderbuffers) {
    // TODO Auto-generated method stub

  }

  @Override
  public void genRenderbuffers(int n, IntBuffer renderbuffers) {
    // TODO Auto-generated method stub

  }

  @Override
  public void renderbufferStorage(int target, int internalFormat, int width,
                                  int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void framebufferRenderbuffer(int target, int attachment, int rbt,
                                      int renderbuffer) {
    // TODO Auto-generated method stub

  }

  @Override
  public void framebufferTexture2D(int target, int attachment, int texTarget,
                                   int texture, int level) {
    // TODO Auto-generated method stub

  }

  @Override
  public int checkFramebufferStatus(int target) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isFramebuffer(int framebuffer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void getFramebufferAttachmentParameteriv(int target, int attachment,
                                                  int name, IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isRenderbuffer(int renderbuffer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void getRenderbufferParameteriv(int target, int name,
                                         IntBuffer params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1,
                              int dstX0, int dstY0, int dstX1, int dstY1,
                              int mask, int filter) {
    // TODO Auto-generated method stub

  }

  @Override
  public void renderbufferStorageMultisample(int target, int samples,
                                             int format, int width,
                                             int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void readBuffer(int buf) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawBuffer(int buf) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void setFrameRate(float fps) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void initSurface(int antialias) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void reinitSurface() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void registerListeners() {
    // TODO Auto-generated method stub

  }

  @Override
  protected int getDepthBits() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected int getStencilBits() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected float getPixelScale() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected void getGL(PGL pgl) {
  }

  @Override
  protected boolean canDraw() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void requestFocus() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void requestDraw() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void swapBuffers() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void initFBOLayer() {
    // TODO Auto-generated method stub

  }

  @Override
  protected int getGLSLVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected String getGLSLVersionSuffix() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Object getDerivedFont(Object font, float size) {
        report("getDerivedFont");
    return ((Font) font).deriveFont(size);
  }

//Tessellator


   @Override
   protected Tessellator createTessellator(TessellatorCallback callback) {
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


   @Override
   protected String tessError(int err) {
     return glu.gluErrorString(err);
   }

  @Override
  protected FontOutline createFontOutline(char ch, Object font) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void viewportImpl(int x, int y, int w, int h) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void readPixelsImpl(int x, int y, int width, int height, int format,
                                int type, Buffer buffer) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void readPixelsImpl(int x, int y, int width, int height, int format,
                                int type, long offset) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void activeTextureImpl(int texture) {
    // TODO Auto-generated method stub
  }

  @Override
  protected void bindTextureImpl(int target, int texture) {
    report("bindTextureImpl");
    gl2vk.glBindTexture(texture);
  }

  @Override
  protected void bindFramebufferImpl(int target, int framebuffer) {
    // TODO Auto-generated method stub

  }

}
