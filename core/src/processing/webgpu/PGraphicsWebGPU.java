package processing.webgpu;

import processing.core.PGraphics;
import processing.core.PSurface;

public class PGraphicsWebGPU extends PGraphics {
    private long windowId = 0;

    @Override
    public PSurface createSurface() {
        return surface = new PSurfaceGLFW(this);
    }

    protected void initWebGPUSurface(long windowHandle, int width, int height, float scaleFactor) {
        windowId = PWebGPU.createSurface(windowHandle, width, height, scaleFactor);
        if (windowId == 0) {
            System.err.println("Failed to create WebGPU surface");
        }
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (windowId != 0) {
            PWebGPU.windowResized(windowId, pixelWidth, pixelHeight);
        }
    }

    @Override
    public void beginDraw() {
        super.beginDraw();
        checkSettings();
    }

    @Override
    public void endDraw() {
        super.endDraw();
        PWebGPU.update();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (windowId != 0) {
            PWebGPU.destroySurface(windowId);
            windowId = 0;
        }
        PWebGPU.exit();
    }

    @Override
    protected void backgroundImpl() {
        if (windowId == 0) {
            return;
        }
        PWebGPU.backgroundColor(windowId, backgroundR, backgroundG, backgroundB, backgroundA);
    }
}
