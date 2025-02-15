package processing.GL2VK;

import org.lwjgl.system.NativeResource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

public class ShaderSPIRVUtils {

    public static SPIRV compileShaderFile(String shaderFile, ShaderKind shaderKind) {
    	File f = new File(".");
    	String path = f.getAbsolutePath().replaceAll("\\\\", "/");
    	path = path.substring(0, path.length()-1);
    	System.out.println(path+shaderFile);
        return compileShaderAbsoluteFile(path+shaderFile, shaderKind);
    }
    
//    public static VertexAttribsBinding getVertexAttribPointers(String vertexShader, int binding) {
//    	File f = new File(".");
//    	String path = f.getAbsolutePath().replaceAll("\\\\", "/");
//    	path = path.substring(0, path.length()-1);
//    	System.out.println(path+vertexShader);
//    	try {
//            String source = new String(Files.readAllBytes(Paths.get(vertexShader)));
//            return new VertexAttribsBinding(binding, source);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    	return null;
//    }

    public static SPIRV compileShaderAbsoluteFile(String shaderFile, ShaderKind shaderKind) {
        try {
            String source = new String(Files.readAllBytes(Paths.get(shaderFile)));
            return compileShader(shaderFile, source, shaderKind);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SPIRV compileShader(String filename, String source, ShaderKind shaderKind) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL);

        if(result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException(shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }
    

    public static SPIRV compileShader(String source, ShaderKind shaderKind) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, "shader", "main", NULL);

        if(result == NULL) {
            throw new RuntimeException("No error information provided by compiler");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException(shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }


    public enum ShaderKind {

        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

        private final int kind;

        ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    public static final class SPIRV implements NativeResource {

        private final long handle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode) {
            this.handle = handle;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode() {
            return bytecode;
        }

        @Override
        public void free() {
            shaderc_result_release(handle);
            bytecode = null; // Help the GC
        }
    }

}