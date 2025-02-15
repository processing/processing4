package processing.vulkan;

import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PShapeSVG;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;

public class PVKGraphics2D extends PGraphicsVulkan {
  public PVKGraphics2D() {
    super();
  }

  //////////////////////////////////////////////////////////////

  // RENDERER SUPPORT QUERIES


  @Override
  public boolean is2D() {
    return true;
  }


  @Override
  public boolean is3D() {
    return false;
  }


  //////////////////////////////////////////////////////////////

  // HINTS


  @Override
  public void hint(int which) {
    if (which == ENABLE_STROKE_PERSPECTIVE) {
      showWarning("Strokes cannot be perspective-corrected in 2D.");
      return;
    }
    super.hint(which);
  }


  //////////////////////////////////////////////////////////////

  // PROJECTION


  @Override
  public void ortho() {
    showMethodWarning("ortho");
  }


  @Override
  public void ortho(float left, float right,
                    float bottom, float top) {
    showMethodWarning("ortho");
  }


  @Override
  public void ortho(float left, float right,
                    float bottom, float top,
                    float near, float far) {
    showMethodWarning("ortho");
  }


  @Override
  public void perspective() {
    showMethodWarning("perspective");
  }


  @Override
  public void perspective(float fov, float aspect, float zNear, float zFar) {
    showMethodWarning("perspective");
  }


  @Override
  public void frustum(float left, float right, float bottom, float top,
                      float znear, float zfar) {
    showMethodWarning("frustum");
  }


  @Override
  protected void defaultPerspective() {
    super.ortho(0, width, -height, 0, -1, +1);
  }


  //////////////////////////////////////////////////////////////

  // CAMERA


  @Override
  public void beginCamera() {
    showMethodWarning("beginCamera");
  }


  @Override
  public void endCamera() {
    showMethodWarning("endCamera");
  }


  @Override
  public void camera() {
    showMethodWarning("camera");
  }


  @Override
  public void camera(float eyeX, float eyeY, float eyeZ,
                     float centerX, float centerY, float centerZ,
                     float upX, float upY, float upZ) {
    showMethodWarning("camera");
  }


  @Override
  protected void defaultCamera() {
    eyeDist = 1;
    resetMatrix();
  }


  //////////////////////////////////////////////////////////////

  // MATRIX MORE!


  @Override
  protected void begin2D() {
    pushProjection();
    defaultPerspective();
    pushMatrix();
    defaultCamera();
  }


  @Override
  protected void end2D() {
    popMatrix();
    popProjection();
  }


  //////////////////////////////////////////////////////////////

  // SHAPE


  @Override
  public void shape(PShape shape) {
    if (shape.is2D()) {
      super.shape(shape);
    } else {
      showWarning("The shape object is not 2D, cannot be displayed with " +
                  "this renderer");
    }
  }


  @Override
  public void shape(PShape shape, float x, float y) {
    if (shape.is2D()) {
      super.shape(shape, x, y);
    } else {
      showWarning("The shape object is not 2D, cannot be displayed with " +
                  "this renderer");
    }
  }


  @Override
  public void shape(PShape shape, float a, float b, float c, float d) {
    if (shape.is2D()) {
      super.shape(shape, a, b, c, d);
    } else {
      showWarning("The shape object is not 2D, cannot be displayed with " +
                  "this renderer");
    }
  }


  @Override
  public void shape(PShape shape, float x, float y, float z) {
    showDepthWarningXYZ("shape");
  }


  @Override
  public void shape(PShape shape, float x, float y, float z,
                    float c, float d, float e) {
    showDepthWarningXYZ("shape");
  }



  //////////////////////////////////////////////////////////////

  // SHAPE I/O


  static protected boolean isSupportedExtension(String extension) {
    return extension.equals("svg") || extension.equals("svgz");
  }


  static protected PShape loadShapeImpl(PGraphics pg,
                                        String filename, String extension) {
    if (extension.equals("svg") || extension.equals("svgz")) {
      PShapeSVG svg = new PShapeSVG(pg.parent.loadXML(filename));
      return PShapeOpenGL.createShape((PGraphicsOpenGL) pg, svg);
    }
    return null;
  }


  //////////////////////////////////////////////////////////////

  // SCREEN TRANSFORMS


  @Override
  public float modelX(float x, float y, float z) {
    showDepthWarning("modelX");
    return 0;
  }


  @Override
  public float modelY(float x, float y, float z) {
    showDepthWarning("modelY");
    return 0;
  }


  @Override
  public float modelZ(float x, float y, float z) {
    showDepthWarning("modelZ");
    return 0;
  }



  //////////////////////////////////////////////////////////////

  // BEZIER VERTICES


  @Override
  public void bezierVertex(float x2, float y2, float z2,
                           float x3, float y3, float z3,
                           float x4, float y4, float z4) {
    showDepthWarningXYZ("bezierVertex");
  }


  //////////////////////////////////////////////////////////////

  // QUADRATIC BEZIER VERTICES


  @Override
  public void quadraticVertex(float x2, float y2, float z2,
                         float x4, float y4, float z4) {
    showDepthWarningXYZ("quadVertex");
  }


  //////////////////////////////////////////////////////////////

  // CURVE VERTICES


  @Override
  public void curveVertex(float x, float y, float z) {
    showDepthWarningXYZ("curveVertex");
  }


  //////////////////////////////////////////////////////////////

  // BOX


  @Override
  public void box(float w, float h, float d) {
    showMethodWarning("box");
  }


  //////////////////////////////////////////////////////////////

  // SPHERE


  @Override
  public void sphere(float r) {
    showMethodWarning("sphere");
  }


  //////////////////////////////////////////////////////////////

  // VERTEX SHAPES


  @Override
  public void vertex(float x, float y, float z) {
    showDepthWarningXYZ("vertex");
  }

  @Override
  public void vertex(float x, float y, float z, float u, float v) {
    showDepthWarningXYZ("vertex");
  }

  //////////////////////////////////////////////////////////////

  // MATRIX TRANSFORMATIONS

  @Override
  public void translate(float tx, float ty, float tz) {
    showDepthWarningXYZ("translate");
  }

  @Override
  public void rotateX(float angle) {
    showDepthWarning("rotateX");
  }

  @Override
  public void rotateY(float angle) {
    showDepthWarning("rotateY");
  }

  @Override
  public void rotateZ(float angle) {
    showDepthWarning("rotateZ");
  }

  @Override
  public void rotate(float angle, float vx, float vy, float vz) {
    showVariationWarning("rotate");
  }

  @Override
  public void applyMatrix(PMatrix3D source) {
    showVariationWarning("applyMatrix");
  }

  @Override
  public void applyMatrix(float n00, float n01, float n02, float n03,
                          float n10, float n11, float n12, float n13,
                          float n20, float n21, float n22, float n23,
                          float n30, float n31, float n32, float n33) {
    showVariationWarning("applyMatrix");
  }

  @Override
  public void scale(float sx, float sy, float sz) {
    showDepthWarningXYZ("scale");
  }

  //////////////////////////////////////////////////////////////

  // SCREEN AND MODEL COORDS

  @Override
  public float screenX(float x, float y, float z) {
    showDepthWarningXYZ("screenX");
    return 0;
  }

  @Override
  public float screenY(float x, float y, float z) {
    showDepthWarningXYZ("screenY");
    return 0;
  }

  @Override
  public float screenZ(float x, float y, float z) {
    showDepthWarningXYZ("screenZ");
    return 0;
  }

  @Override
  public PMatrix3D getMatrix(PMatrix3D target) {
    showVariationWarning("getMatrix");
    return target;
  }

  @Override
  public void setMatrix(PMatrix3D source) {
    showVariationWarning("setMatrix");
  }

  //////////////////////////////////////////////////////////////

  // LIGHTS

  @Override
  public void lights() {
    showMethodWarning("lights");
  }

  @Override
  public void noLights() {
    showMethodWarning("noLights");
  }

  @Override
  public void ambientLight(float red, float green, float blue) {
    showMethodWarning("ambientLight");
  }

  @Override
  public void ambientLight(float red, float green, float blue,
                           float x, float y, float z) {
    showMethodWarning("ambientLight");
  }

  @Override
  public void directionalLight(float red, float green, float blue,
                               float nx, float ny, float nz) {
    showMethodWarning("directionalLight");
  }

  @Override
  public void pointLight(float red, float green, float blue,
                         float x, float y, float z) {
    showMethodWarning("pointLight");
  }

  @Override
  public void spotLight(float red, float green, float blue,
                        float x, float y, float z,
                        float nx, float ny, float nz,
                        float angle, float concentration) {
    showMethodWarning("spotLight");
  }

  @Override
  public void lightFalloff(float constant, float linear, float quadratic) {
    showMethodWarning("lightFalloff");
  }

  @Override
  public void lightSpecular(float v1, float v2, float v3) {
    showMethodWarning("lightSpecular");
  }
}
