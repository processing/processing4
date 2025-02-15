package processing.vulkan;

import processing.opengl.FrameBuffer;
import processing.opengl.PGraphicsOpenGL;

public class VKFrameBuffer extends FrameBuffer {

  private boolean depthTestEnabled = true;

  // TODO: Dummy depth bit.

  public VKFrameBuffer(PGraphicsOpenGL pg) {
    super(pg);
  }


  public VKFrameBuffer(PGraphicsOpenGL pg, int w, int h, int samples, int colorBuffers,
              boolean depthTest, boolean screen) {
    super(pg, w, h, samples, colorBuffers, 0, 0, depthTest, screen);
    this.depthTestEnabled = depthTest;
  }


  public VKFrameBuffer(PGraphicsOpenGL pg, int w, int h) {
    super(pg, w, h, 1, 1, 0, 0, false, false);
  }


  public VKFrameBuffer(PGraphicsOpenGL pg, int w, int h, boolean screen) {
    super(pg, w, h, 1, 1, 0, 0, false, screen);
  }

  @Override
  public boolean hasDepthBuffer() {
    return true;
  }

  @Override
  public boolean hasStencilBuffer() {
    return true;
  }

}
