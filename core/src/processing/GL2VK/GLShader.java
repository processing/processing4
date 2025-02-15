package processing.GL2VK;

import java.util.ArrayList;

import processing.GL2VK.ShaderSPIRVUtils.SPIRV;

//Shaders aren't actually anything significant, they're really temporary data structures
// to create a vulkan pipeline.
public class GLShader {
	private String source = "";
	public boolean successfulCompile = false;
	public int type;
	public SPIRV spirv = null;
	public String log = "";

	// This constructor is mostly for convenient testing.
	public GLShader(String source) {
		setSource(source);
	}

	// Use for vertex shaders only. See notes in glCompileShader
	// for why we're oddly putting this here.
	public ShaderAttribInfo attribInfo = null;

	// Once the vert and frag shaders are linked it will
	// be combined into one ArrayList
	public ArrayList<GLUniform> uniforms = new ArrayList<>();

	public GLShader(int type) {
		this.type = type;
	}

	public void setUniforms(ArrayList<GLUniform> uniforms) {
		this.uniforms = uniforms;
		for (GLUniform u : uniforms) {
			// I could and should do it the proper way to
			// ensure GL_VERTEX_SHADER is the same meaning for GLUniform
			// class but let's be real; it's an int with 2 different values.
			u.vertexFragment = type;
		}
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}