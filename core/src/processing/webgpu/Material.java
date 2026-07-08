package processing.webgpu;

public class Material {

    private long id;

    private Material(long id) {
        this.id = id;
    }

    public static Material pbr() {
        return new Material(PWebGPU.materialCreatePbr());
    }

    public static Material unlit() {
        Material mat = pbr();
        mat.set("unlit", 1.0f);
        return mat;
    }

    public long id() {
        return id;
    }

    public void set(String name, float value) {
        PWebGPU.materialSetFloat(id, name, value);
    }

    public void set(String name, float r, float g, float b, float a) {
        PWebGPU.materialSetFloat4(id, name, r, g, b, a);
    }

    public void destroy() {
        if (id != 0) {
            PWebGPU.materialDestroy(id);
            id = 0;
        }
    }
}
