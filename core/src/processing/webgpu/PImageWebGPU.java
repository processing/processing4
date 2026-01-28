package processing.webgpu;

import processing.core.PImage;

public class PImageWebGPU extends PImage {

    protected long id = 0;

    public PImageWebGPU() {
        super();
    }

    public PImageWebGPU(int width, int height) {
        super(width, height);
    }

    public PImageWebGPU(int width, int height, int format) {
        super(width, height, format);
    }

    public long getId() {
        return id;
    }

    public void setId(long imageId) {
        this.id = imageId;
    }
}
