package processing.vulkan;

import processing.GL2VK.GL2VK;
import processing.core.PSurface;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

public class PGraphicsVulkan extends PGraphicsOpenGL {

  public PGraphicsVulkan() {
    super();
  }

  @Override
  protected PGL createPGL(PGraphicsOpenGL pg) {
    return new PVK(pg);
  }

  @Override
  public PSurface createSurface() {
    return surface = new PSurfaceVK(this);
  }

  public void selectNode(int node) {
    ((PVK)pgl).selectNode(node);
  }

  public void enableAutoMode() {
    ((PVK)pgl).enableAutoMode();
  }

  public int getNodesCount() {
    return ((PVK)pgl).getNodesCount();
  }

  public void setMaxNodes(int v) {
    ((PVK)pgl).setMaxNodes(v);
  }


  public void bufferMultithreaded(boolean onoff) {
    ((PVK)pgl).bufferMultithreaded(onoff);
  }

  @Override
  public void beginDraw() {
    super.beginDraw();
    enableMultipleBuffers();
  }
}
