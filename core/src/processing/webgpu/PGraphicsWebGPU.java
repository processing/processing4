package processing.webgpu;

import processing.core.PGraphics;
import processing.core.PSurface;

public class PGraphicsWebGPU extends PGraphics {
    private long surfaceId = 0;

    @Override
    public PSurface createSurface() {
        return surface = new PSurfaceGLFW(this);
    }

    protected void initWebGPUSurface(long windowHandle, int width, int height, float scaleFactor) {
        surfaceId = PWebGPU.createSurface(windowHandle, width, height, scaleFactor);
        if (surfaceId == 0) {
            System.err.println("Failed to create WebGPU surface");
        }
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (surfaceId != 0) {
            PWebGPU.windowResized(surfaceId, pixelWidth, pixelHeight);
        }
    }

    @Override
    public void beginDraw() {
        super.beginDraw();
        checkSettings();
        System.out.println("Beginning draw on surfaceId: " + surfaceId);
        PWebGPU.beginDraw(surfaceId);
    }

    @Override
    public void flush() {
        super.flush();
        PWebGPU.flush(surfaceId);
    }

    @Override
    public void endDraw() {
        super.endDraw();
        PWebGPU.endDraw(surfaceId);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (surfaceId != 0) {
            PWebGPU.destroySurface(surfaceId);
            surfaceId = 0;
        }
        PWebGPU.exit();
    }

    @Override
    protected void backgroundImpl() {
        if (surfaceId == 0) {
            return;
        }
        PWebGPU.backgroundColor(surfaceId, backgroundR, backgroundG, backgroundB, backgroundA);
    }

    @Override
    protected void fillFromCalc() {
        super.fillFromCalc();
        if (surfaceId == 0) {
            return;
        }
        if (fill) {
            PWebGPU.setFill(surfaceId, fillR, fillG, fillB, fillA);
        } else {
            PWebGPU.noFill(surfaceId);
        }
    }

    @Override
    protected void strokeFromCalc() {
        super.strokeFromCalc();
        if (surfaceId == 0) {
            return;
        }
        if (stroke) {
            PWebGPU.setStrokeColor(surfaceId, strokeR, strokeG, strokeB, strokeA);
        } else {
            PWebGPU.noStroke(surfaceId);
        }
    }

    @Override
    public void strokeWeight(float weight) {
        super.strokeWeight(weight);
        if (surfaceId == 0) {
            return;
        }
        PWebGPU.setStrokeWeight(surfaceId, weight);
    }

    @Override
    public void noFill() {
        super.noFill();
        if (surfaceId == 0) {
            return;
        }
        PWebGPU.noFill(surfaceId);
    }

    @Override
    public void noStroke() {
        super.noStroke();
        if (surfaceId == 0) {
            return;
        }
        PWebGPU.noStroke(surfaceId);
    }

    @Override
    protected void rectImpl(float x1, float y1, float x2, float y2) {
        rectImpl(x1, y1, x2, y2, 0, 0, 0, 0);
    }

    @Override
    protected void rectImpl(float x1, float y1, float x2, float y2,
                            float tl, float tr, float br, float bl) {
        if (surfaceId == 0) {
            return;
        }
        // rectImpl receives corner coordinates, so let's convert to x,y,w,h
        PWebGPU.rect(surfaceId, x1, y1, x2 - x1, y2 - y1, tl, tr, br, bl);
    }
}
