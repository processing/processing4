package processing.vulkan;

import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PShapeOBJ;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;

public class PVKGraphics3D extends PGraphicsVulkan {
  public PVKGraphics3D() {
    super();
  }


  //////////////////////////////////////////////////////////////

  // RENDERER SUPPORT QUERIES


  @Override
  public boolean is2D() {
    return false;
  }


  @Override
  public boolean is3D() {
    return true;
  }


  //////////////////////////////////////////////////////////////

  // PROJECTION


  @Override
  protected void defaultPerspective() {
    perspective();
  }


  //////////////////////////////////////////////////////////////

  // CAMERA


  @Override
  protected void defaultCamera() {
    camera();
  }


  private void dumpStack() {
//    Thread.dumpStack();
  }


  //////////////////////////////////////////////////////////////

  // MATRIX MORE!


  @Override
  protected void begin2D() {
    pushProjection();
    ortho(-width/2f, width/2f, -height/2f, height/2f);
    pushMatrix();

    // Set camera for 2D rendering, it simply centers at (width/2, height/2)
    float centerX = width/2f;
    float centerY = height/2f;


    modelview.reset();
    modelview.translate(-centerX, -centerY);
    dumpStack();

    modelviewInv.set(modelview);
    modelviewInv.invert();

    camera.set(modelview);
    cameraInv.set(modelviewInv);

    updateProjmodelview();
  }


  @Override
  protected void end2D() {
    popMatrix();
    popProjection();
  }



  //////////////////////////////////////////////////////////////

  // SHAPE I/O


  static protected boolean isSupportedExtension(String extension) {
    return extension.equals("obj");
  }


  static protected PShape loadShapeImpl(PGraphics pg, String filename,
                                                      String extension) {
    PShapeOBJ obj = null;

    if (extension.equals("obj")) {
      obj = new PShapeOBJ(pg.parent, filename);
      int prevTextureMode = pg.textureMode;
      pg.textureMode = NORMAL;
      PShapeOpenGL p3d = PShapeOpenGL.createShape((PGraphicsOpenGL)pg, obj);
      pg.textureMode = prevTextureMode;
      return p3d;
    }
    return null;
  }
}


