package processing.opengl;

import processing.core.PSurface;

public class PGraphicsOpenGLANGLE extends PGraphicsOpenGL {
    public PGraphicsOpenGLANGLE() {
        super();
    }

    @Override
    protected PGL createPGL(PGraphicsOpenGL pg) {
        return new PGLANGLE(pg);
    }
    
    @Override
    // Java only
    public PSurface createSurface() {  // ignore
        return surface = new PSurfaceANGLE(this);
    }

}
