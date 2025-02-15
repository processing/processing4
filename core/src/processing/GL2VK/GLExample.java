package processing.GL2VK;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
//import org.joml.*;
//import org.joml.Math;

public class GLExample {

    // ======= CLASSES ======= //
//    private static class Vertex {
//        private static final int SIZEOF = (2 + 3) * Float.BYTES;
//
//    	private Vector2fc pos;
//    	private Vector3fc color;
//
//    	public Vertex(float x, float y, float r, float g, float b) {
//    		this.pos = new Vector2f(x, y);
//    		this.color = new Vector3f(r,g,b);
//    	}
//
//    	// NOTES FOR VERTEX BINDING:
//    	// in OpenGL, bindBuffer specifies which buffer index to bind.
//    	// This index can be used in vulkan's bindingDescription.binding(index)
//    	// If we want an interleaved buffer: then
//    	// bindBuffer(1);
//    	// vertexAttribPointer("pos" ... )
//    	// bindBuffer(1)
//    	// vertexAttribPointer("color" ... )
//    	//
//    	// If we want a separate buffer:
//    	// bindBuffer(1);
//    	// vertexAttribPointer("pos" ... )
//    	// bindBuffer(2)
//    	// vertexAttribPointer("color" ... )
//    	//
//    	// All we do is create a new bindingdescription whenever a new
//    	// buffer is bound when vertexAttribPointer is called.
//	}
//
//  public static void main(String[] args) {
//    try {
//      GL2VK gl = new GL2VK(1200, 800);
////      triangles(gl);
////      trianglesSeparate(gl);
////      throttleTest(gl);
////      indices(gl);
////      indicesUniform(gl);
////      coolIndicies(gl);
//
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      System.exit(1);
//    }
//	}
//
//
//	private static void createIndicesSquare(ByteBuffer vertexBuffer, ByteBuffer colorBuffer, ByteBuffer indexBuffer) {
//
//    	vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
//    	colorBuffer.order(ByteOrder.LITTLE_ENDIAN);
//    	indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
//
////        {{-0.5f, -0.5f}, {1.0f, 0.0f, 0.0f}},
////        {{0.5f, -0.5f}, {0.0f, 1.0f, 0.0f}},
////        {{0.5f, 0.5f}, {0.0f, 0.0f, 1.0f}},
////        {{-0.5f, 0.5f}, {1.0f, 1.0f, 1.0f}}
//    	vertexBuffer.putFloat(-0.5f);
//    	vertexBuffer.putFloat(-0.5f);
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(0f);
//
//    	vertexBuffer.putFloat(0.5f);
//    	vertexBuffer.putFloat(-0.5f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(0f);
//
//    	vertexBuffer.putFloat(0.5f);
//    	vertexBuffer.putFloat(0.5f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//
//    	vertexBuffer.putFloat(-0.5f);
//    	vertexBuffer.putFloat(0.5f);
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//
//    	indexBuffer.putShort((short)0);
//    	indexBuffer.putShort((short)1);
//    	indexBuffer.putShort((short)2);
//    	indexBuffer.putShort((short)2);
//    	indexBuffer.putShort((short)3);
//    	indexBuffer.putShort((short)0);
//
//    	vertexBuffer.rewind();
//    	colorBuffer.rewind();
//    	indexBuffer.rewind();
//	}
//
//	private static void createIndicesSquareProcessingShader(ByteBuffer vertexBuffer, ByteBuffer colorBuffer, ByteBuffer indexBuffer) {
//
//    	vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
//    	colorBuffer.order(ByteOrder.LITTLE_ENDIAN);
//    	indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
//
////        {{-0.5f, -0.5f}, {1.0f, 0.0f, 0.0f}},
////        {{0.5f, -0.5f}, {0.0f, 1.0f, 0.0f}},
////        {{0.5f, 0.5f}, {0.0f, 0.0f, 1.0f}},
////        {{-0.5f, 0.5f}, {1.0f, 1.0f, 1.0f}}
//    	vertexBuffer.putFloat(-0.5f);
//    	vertexBuffer.putFloat(-0.5f);
//    	vertexBuffer.putFloat(0f);
//    	vertexBuffer.putFloat(1f);
//
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//
//    	vertexBuffer.putFloat(0.5f);
//    	vertexBuffer.putFloat(-0.5f);
//    	vertexBuffer.putFloat(0f);
//    	vertexBuffer.putFloat(1f);
//
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//
//    	vertexBuffer.putFloat(0.5f);
//    	vertexBuffer.putFloat(0.5f);
//    	vertexBuffer.putFloat(0f);
//    	vertexBuffer.putFloat(1f);
//
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(1f);
//
//    	vertexBuffer.putFloat(-0.5f);
//    	vertexBuffer.putFloat(0.5f);
//    	vertexBuffer.putFloat(0f);
//    	vertexBuffer.putFloat(1f);
//
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(0f);
//    	colorBuffer.putFloat(1f);
//    	colorBuffer.putFloat(1f);
//
//    	indexBuffer.putShort((short)0);
//    	indexBuffer.putShort((short)1);
//    	indexBuffer.putShort((short)2);
//    	indexBuffer.putShort((short)2);
//    	indexBuffer.putShort((short)3);
//    	indexBuffer.putShort((short)0);
//
//    	vertexBuffer.rewind();
//    	colorBuffer.rewind();
//    	indexBuffer.rewind();
//	}
//
//
//
//
//	public static void coolIndicies(GL2VK gl) {
//
//		// Create the data
//    	ByteBuffer vertexBuffer = ByteBuffer.allocate(Float.BYTES * 6 * 4);
//    	ByteBuffer colorBuffer = ByteBuffer.allocate(Float.BYTES * 6 * 4);
//    	ByteBuffer indexBuffer = ByteBuffer.allocate(Short.BYTES * 6);
//    	createIndicesSquareProcessingShader(vertexBuffer, colorBuffer, indexBuffer);
//
//    	// Gen buffers
//    	IntBuffer out = IntBuffer.allocate(3);
//    	gl.glGenBuffers(3, out);
//    	int glVertBuff = out.get(0);
//    	int glColBuff = out.get(1);
//    	int glIndexBuff = out.get(2);
//
//
//    	// Create our gpu program
//    	int program = gl.glCreateProgram();
//    	int vertShader = gl.glCreateShader(GL2VK.GL_VERTEX_SHADER);
//    	int fragShader = gl.glCreateShader(GL2VK.GL_FRAGMENT_SHADER);
//
//    	// Shader source
//    	gl.glShaderSource(vertShader, Util.readFile("src/processing/opengl/shaders/ColorVert.glsl"));
//    	gl.glShaderSource(fragShader, Util.readFile("src/processing/opengl/shaders/ColorFrag.glsl"));
//    	// Compile the shaders
//    	gl.glCompileShader(vertShader);
//    	gl.glCompileShader(fragShader);
//    	// Check shaders
//		IntBuffer compileStatus = IntBuffer.allocate(1);
//		gl.glGetShaderiv(vertShader, GL2VK.GL_COMPILE_STATUS, compileStatus);
//		if (compileStatus.get(0) == GL2VK.GL_FALSE) {
//			System.out.println(gl.glGetShaderInfoLog(vertShader));
//			System.exit(1);
//		}
//		gl.glGetShaderiv(fragShader, GL2VK.GL_COMPILE_STATUS, compileStatus);
//		if (compileStatus.get(0) == GL2VK.GL_FALSE) {
//			System.out.println(gl.glGetShaderInfoLog(fragShader));
//			System.exit(1);
//		}
//    	// Attach the shaders
//    	gl.glAttachShader(program, vertShader);
//    	gl.glAttachShader(program, fragShader);
//    	// Don't need em anymore
//    	gl.glDeleteShader(vertShader);
//    	gl.glDeleteShader(fragShader);
//
//    	gl.glLinkProgram(program);
//
//
//		// Setup up attribs
//		int position = gl.glGetAttribLocation(program, "position");
//		int color = gl.glGetAttribLocation(program, "color");
//
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, glVertBuff);
//    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, vertexBuffer.capacity(), vertexBuffer, 0);
//		gl.glVertexAttribPointer(position, 4, GL2VK.GL_FLOAT, false, 4*4, 0);
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, glColBuff);
//    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, colorBuffer.capacity(), colorBuffer, 0);
//		gl.glVertexAttribPointer(color, 4, GL2VK.GL_FLOAT, false, 4*4, 0);
//
//    	gl.glBindBuffer(GL2VK.GL_INDEX_BUFFER, glIndexBuff);
//    	gl.glBufferData(GL2VK.GL_INDEX_BUFFER, indexBuffer.capacity(), indexBuffer, 0);
//
//		gl.glUseProgram(program);
//
//		int transformMatrix = gl.getUniformLocation(program, "transformMatrix");
//		if (transformMatrix == -1) {
//			System.out.println("Missing transformMatrix!");
//			System.exit(1);
//		}
//
//
//		boolean multithreaded = true;
//		int threadIndex = 0;
//		float qtime = 0f;
//
//		Matrix4f transform = new Matrix4f();
//
//    	while (!gl.shouldClose()) {
//    		transform.identity();
//    		transform.rotateZ((qtime * Math.toRadians(90)));
////    		transform.lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
////    		transform.perspective((float) Math.toRadians(45),
////                    (float)1200 / (float)00, 0.1f, 10.0f);
////    		transform.m11(transform.m11() * -1);
//
//    		qtime += 0.02f;
//
//    		gl.beginRecord();
//
//
//    		if (multithreaded) gl.selectNode((threadIndex++)%gl.getNodesCount());
//    		else gl.selectNode(0);
//
////        ByteBuffer buff = ByteBuffer.allocateDirect(64);
////    		buff.order(ByteOrder.LITTLE_ENDIAN);
////    		transform.get(buff);
//
//        FloatBuffer buff = ByteBuffer.allocateDirect(64).asFloatBuffer();
//        transform.get(buff);
//
//    		gl.glUniformMatrix4fv(transformMatrix, 1, false, buff);
//
//        	gl.glBindBuffer(GL2VK.GL_INDEX_BUFFER, glIndexBuff);
//
//    		gl.glDrawElements(0, 6, GL2VK.GL_UNSIGNED_SHORT, 0);
//    		gl.endRecord();
//
////    		frameWait();
//    	}
//    	gl.close();
//	}
//
//
//
//	public static void indicesUniform(GL2VK gl) {
//
//		// Create the data
//    	ByteBuffer vertexBuffer = ByteBuffer.allocate(Float.BYTES * 6 * 2);
//    	ByteBuffer colorBuffer = ByteBuffer.allocate(Float.BYTES * 6 * 3);
//    	ByteBuffer indexBuffer = ByteBuffer.allocate(Short.BYTES * 6);
//    	createIndicesSquare(vertexBuffer, colorBuffer, indexBuffer);
//
//    	// Gen buffers
//    	IntBuffer out = IntBuffer.allocate(3);
//    	gl.glGenBuffers(3, out);
//    	int glVertBuff = out.get(0);
//    	int glColBuff = out.get(1);
//    	int glIndexBuff = out.get(2);
//
//
//    	// Create our gpu program
//    	int program = gl.glCreateProgram();
//    	int vertShader = gl.glCreateShader(GL2VK.GL_VERTEX_SHADER);
//    	int fragShader = gl.glCreateShader(GL2VK.GL_FRAGMENT_SHADER);
//
//    	// Shader source
//    	gl.glShaderSource(vertShader, Util.readFile("resources/shaders/uniform.vert"));
//    	gl.glShaderSource(fragShader, Util.readFile("resources/shaders/uniform.frag"));
//    	// Compile the shaders
//    	gl.glCompileVKShader(vertShader);
//    	gl.glCompileVKShader(fragShader);
//    	// Check shaders
//		IntBuffer compileStatus = IntBuffer.allocate(1);
//		gl.glGetShaderiv(vertShader, GL2VK.GL_COMPILE_STATUS, compileStatus);
//		if (compileStatus.get(0) == GL2VK.GL_FALSE) {
//			System.out.println(gl.glGetShaderInfoLog(vertShader));
//			System.exit(1);
//		}
//		gl.glGetShaderiv(fragShader, GL2VK.GL_COMPILE_STATUS, compileStatus);
//		if (compileStatus.get(0) == GL2VK.GL_FALSE) {
//			System.out.println(gl.glGetShaderInfoLog(fragShader));
//			System.exit(1);
//		}
//    	// Attach the shaders
//    	gl.glAttachShader(program, vertShader);
//    	gl.glAttachShader(program, fragShader);
//    	// Don't need em anymore
//    	gl.glDeleteShader(vertShader);
//    	gl.glDeleteShader(fragShader);
//
//    	gl.glLinkProgram(program);
//
//
//		// Setup up attribs
//		int position = gl.glGetAttribLocation(program, "inPosition");
//		int color = gl.glGetAttribLocation(program, "inColor");
//
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, glVertBuff);
//    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, vertexBuffer.capacity(), vertexBuffer, 0);
//		gl.glVertexAttribPointer(position, 2, GL2VK.GL_FLOAT, false, 2*4, 0);
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, glColBuff);
//    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, colorBuffer.capacity(), colorBuffer, 0);
//		gl.glVertexAttribPointer(color, 3, GL2VK.GL_FLOAT, false, 3*4, 0);
//
//    	gl.glBindBuffer(GL2VK.GL_INDEX_BUFFER, glIndexBuff);
//    	gl.glBufferData(GL2VK.GL_INDEX_BUFFER, indexBuffer.capacity(), indexBuffer, 0);
//
//
//
//
//		gl.glUseProgram(program);
//
//		boolean multithreaded = true;
//		int threadIndex = 0;
//
//		double qtime = 0d;
//
//
//		int u_pos = gl.getUniformLocation(program, "u_pos");
//		int u_brightness = gl.getUniformLocation(program, "u_brightness");
//		int u_pos_secondary = gl.getUniformLocation(program, "u_pos_secondary");
//		int u_r = gl.getUniformLocation(program, "u_r");
//		int u_g = gl.getUniformLocation(program, "u_g");
//		int u_b = gl.getUniformLocation(program, "u_b");
//		int extra_red = gl.getUniformLocation(program, "extra_red");
//
//		if (u_pos == -1) System.out.println("UHOH u_pos -1");
//		if (u_pos_secondary == -1) System.out.println("UHOH u_pos_secondary -1");
//		if (u_r == -1) System.out.println("UHOH u_r -1");
//
//
//    	while (!gl.shouldClose()) {
//    		gl.beginRecord();
//
//    		if (multithreaded) gl.selectNode((threadIndex++)%gl.getNodesCount());
//    		else gl.selectNode(0);
//
//    		qtime += 0.1d;
//
//        	gl.glBindBuffer(GL2VK.GL_INDEX_BUFFER, glIndexBuff);
//
//        	gl.glUniform1f(u_r, 1f);
//        	gl.glUniform1f(u_g, 1f);
//        	gl.glUniform1f(u_b, 1f);
////        	gl.glUniform1f(u_brightness, (float)Math.sin(qtime)*0.5f+0.5f);
//        	gl.glUniform1f(u_brightness, (float)Math.sin(qtime)*0.5f+0.5f);
//        	gl.glUniform1f(extra_red, 1f);
//
//
//    		gl.glUniform2f(u_pos, (float)Math.sin(qtime)*0.5f, (float)Math.cos(qtime)*0.5f);
////    		gl.glUniform2f(u_pos_secondary, 0, (float)Math.cos(qtime*2.238f)*0.2f);
////    		gl.glUniform2f(u_pos_secondary, 0, (float)Math.cos(qtime*2.238f)*0.2f);
//
//    		gl.glDrawElements(0, 6, GL2VK.GL_UNSIGNED_SHORT, 0);
//    		gl.endRecord();
//
////    		frameWait();
//    	}
//    	gl.close();
//	}
//
//
//
//	// Draw a square with indicies
//	public static void indices(GL2VK gl) {
//
//		// Create the data
//    	ByteBuffer vertexBuffer = ByteBuffer.allocate(Float.BYTES * 6 * 2);
//    	ByteBuffer colorBuffer = ByteBuffer.allocate(Float.BYTES * 6 * 3);
//    	ByteBuffer indexBuffer = ByteBuffer.allocate(Short.BYTES * 6);
//    	createIndicesSquare(vertexBuffer, colorBuffer, indexBuffer);
//
//    	// Gen buffers
//    	IntBuffer out = IntBuffer.allocate(3);
//    	gl.glGenBuffers(3, out);
//    	int glVertBuff = out.get(0);
//    	int glColBuff = out.get(1);
//    	int glIndexBuff = out.get(2);
//
//
//    	// Create our gpu program
//    	int program = gl.glCreateProgram();
//    	int vertShader = gl.glCreateShader(GL2VK.GL_VERTEX_SHADER);
//    	int fragShader = gl.glCreateShader(GL2VK.GL_FRAGMENT_SHADER);
//
//    	// Shader source
//    	gl.glShaderSource(vertShader, Util.readFile("resources/shaders/shader.vert"));
//    	gl.glShaderSource(fragShader, Util.readFile("resources/shaders/shader.frag"));
//    	// Compile the shaders
//    	gl.glCompileVKShader(vertShader);
//    	gl.glCompileVKShader(fragShader);
//    	// Attach the shaders
//    	gl.glAttachShader(program, vertShader);
//    	gl.glAttachShader(program, fragShader);
//    	// Don't need em anymore
//    	gl.glDeleteShader(vertShader);
//    	gl.glDeleteShader(fragShader);
//
//    	gl.glLinkProgram(program);
//
//
//		// Setup up attribs
//		int position = gl.glGetAttribLocation(program, "inPosition");
//		int color = gl.glGetAttribLocation(program, "inColor");
//
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, glVertBuff);
//    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, vertexBuffer.capacity(), vertexBuffer, 0);
//		gl.glVertexAttribPointer(position, 2, GL2VK.GL_FLOAT, false, 2*4, 0);
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, glColBuff);
//    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, colorBuffer.capacity(), colorBuffer, 0);
//		gl.glVertexAttribPointer(color, 3, GL2VK.GL_FLOAT, false, 3*4, 0);
//
//    	gl.glBindBuffer(GL2VK.GL_INDEX_BUFFER, glIndexBuff);
//    	gl.glBufferData(GL2VK.GL_INDEX_BUFFER, indexBuffer.capacity(), indexBuffer, 0);
//
//
//
//
//		gl.glUseProgram(program);
//
//		boolean multithreaded = true;
//		int threadIndex = 0;
//
//    	while (!gl.shouldClose()) {
//    		gl.beginRecord();
//
//    		if (multithreaded) gl.selectNode((threadIndex++)%gl.getNodesCount());
//    		else gl.selectNode(0);
//
//        	gl.glBindBuffer(GL2VK.GL_INDEX_BUFFER, glIndexBuff);
//    		gl.glDrawElements(0, 6, GL2VK.GL_UNSIGNED_SHORT, 0);
//    		gl.endRecord();
//
//    		frameWait();
//    	}
//    	gl.close();
//	}
//
//
//
//
//	private static void createVertices(Vertex[] buffer) {
//
//		final float TRIANGLE_SIZE = 0.04f;
//
//		int l = buffer.length;
//		for (int i = 0; i < l; i+=3) {
//			float r = (float)Math.random();
//			float g = (float)Math.random();
//			float b = (float)Math.random();
//
//			float x1 = (float)Math.random()*2f-1f;
//			float y1 = (float)Math.random()*2f-1f;
//			float x2 = x1+TRIANGLE_SIZE;
//			float y2 = y1;
//			float x3 = x1+TRIANGLE_SIZE/2f;
//			float y3 = y1+TRIANGLE_SIZE;
//			buffer[i] = new Vertex(x1,y1,r,g,b);
//			if (i+1 < l) buffer[i+1] = new Vertex(x2,y2,r,g,b);
//			if (i+2 < l) buffer[i+2] = new Vertex(x3,y3,r,g,b);
//		}
//	}
//
//    private static void memcpy(ByteBuffer buffer, Vertex[] vertices) {
//    	buffer.order(ByteOrder.LITTLE_ENDIAN);
//        for(Vertex vertex : vertices) {
//            buffer.putFloat(vertex.pos.x());
//            buffer.putFloat(vertex.pos.y());
//
//            buffer.putFloat(vertex.color.x());
//            buffer.putFloat(vertex.color.y());
//            buffer.putFloat(vertex.color.z());
//        }
//    }
//
//
//    private static void copyVertex(ByteBuffer buffer, Vertex[] vertices) {
//    	buffer.order(ByteOrder.LITTLE_ENDIAN);
//        for(Vertex vertex : vertices) {
//            buffer.putFloat(vertex.pos.x());
//            buffer.putFloat(vertex.pos.y());
//        }
//    }
//
//
//    private static void copyColor(ByteBuffer buffer, Vertex[] vertices) {
//    	buffer.order(ByteOrder.LITTLE_ENDIAN);
//        for(Vertex vertex : vertices) {
//            buffer.putFloat(vertex.color.x());
//            buffer.putFloat(vertex.color.y());
//            buffer.putFloat(vertex.color.z());
//        }
//    }
//
//
//	public static void triangles(GL2VK gl) {
//	  Vertex[] vertices = new Vertex[1000];
//    	createVertices(vertices);
//
//    	// Gen buffers
//    	IntBuffer out = IntBuffer.allocate(1);
//    	gl.glGenBuffers(1, out);
//    	int vertexBuffer = out.get(0);
//
//    	// Create our gpu program
//    	int program = gl.glCreateProgram();
//    	int vertShader = gl.glCreateShader(GL2VK.GL_VERTEX_SHADER);
//    	int fragShader = gl.glCreateShader(GL2VK.GL_FRAGMENT_SHADER);
//
//    	// Shader source
//    	gl.glShaderSource(vertShader, Util.readFile("resources/shaders/shader.vert"));
//    	gl.glShaderSource(fragShader, Util.readFile("resources/shaders/shader.frag"));
//    	// Compile the shaders
//    	gl.glCompileVKShader(vertShader);
//    	gl.glCompileVKShader(fragShader);
//    	// Attach the shaders
//    	gl.glAttachShader(program, vertShader);
//    	gl.glAttachShader(program, fragShader);
//    	// Don't need em anymore
//    	gl.glDeleteShader(vertShader);
//    	gl.glDeleteShader(fragShader);
//
//    	gl.glLinkProgram(program);
//
//
//
//    	int size = vertices.length*Vertex.SIZEOF;
//    	ByteBuffer buff = ByteBuffer.allocate(size);
//
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, vertexBuffer);
//
//		// Setup up attribs
//		int position = gl.glGetAttribLocation(program, "inPosition");
//		int color = gl.glGetAttribLocation(program, "inColor");
//		gl.glVertexAttribPointer(position, 2, GL2VK.GL_FLOAT, false, 5*4, 0);
//		gl.glVertexAttribPointer(color, 3, GL2VK.GL_FLOAT, false, 5*4, 2*4);
//
//		gl.glUseProgram(program);
//
//
//    	boolean multithreaded = false;
//    	int threadIndex = 0;
//
//    	while (!gl.shouldClose()) {
//        	// Buffer data
//        	createVertices(vertices);
//        	buff.rewind();
//    		memcpy(buff, vertices);
//        	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, vertexBuffer);
//        	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, size, buff, 0);
//
//    		gl.beginRecord();
//
//    		if (multithreaded) gl.selectNode((threadIndex++)%gl.getNodesCount());
//    		else gl.selectNode(0);
//
//    		gl.glDrawArrays(0, 0, vertices.length);
//    		gl.endRecord();
//
//    		frameWait();
//    	}
//    	gl.close();
//	}
//
//
//
//	public static void trianglesSeparate(GL2VK gl) {
//	  Vertex[] vertices = new Vertex[1000];
//    	createVertices(vertices);
//
//    	// Gen buffers
//    	IntBuffer out = IntBuffer.allocate(2);
//    	gl.glGenBuffers(2, out);
//    	int vertexBuffer = out.get(0);
//    	int colorBuffer = out.get(1);
//
//    	// Create our gpu program
//    	int program = gl.glCreateProgram();
//    	int vertShader = gl.glCreateShader(GL2VK.GL_VERTEX_SHADER);
//    	int fragShader = gl.glCreateShader(GL2VK.GL_FRAGMENT_SHADER);
//
//    	// Shader source
//    	gl.glShaderSource(vertShader, Util.readFile("resources/shaders/shader.vert"));
//    	gl.glShaderSource(fragShader, Util.readFile("resources/shaders/shader.frag"));
//    	// Compile the shaders
//    	gl.glCompileVKShader(vertShader);
//    	gl.glCompileVKShader(fragShader);
//    	// Attach the shaders
//    	gl.glAttachShader(program, vertShader);
//    	gl.glAttachShader(program, fragShader);
//    	// Don't need em anymore
//    	gl.glDeleteShader(vertShader);
//    	gl.glDeleteShader(fragShader);
//
//    	gl.glLinkProgram(program);
//
//
//
//    	ByteBuffer vertexBuff = ByteBuffer.allocate(2 * Float.BYTES * vertices.length);
//    	ByteBuffer colorBuff = ByteBuffer.allocate(3 * Float.BYTES * vertices.length);
//
//
//		// Setup up attribs
//		int position = gl.glGetAttribLocation(program, "inPosition");
//		int color = gl.glGetAttribLocation(program, "inColor");
//
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, vertexBuffer);
//		gl.glVertexAttribPointer(position, 2, GL2VK.GL_FLOAT, false, 2*4, 0);
//    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, colorBuffer);
//		gl.glVertexAttribPointer(color, 3, GL2VK.GL_FLOAT, false, 3*4, 0);
//
//		gl.glUseProgram(program);
//
//
//    	boolean multithreaded = false;
//    	int threadIndex = 0;
//
//    	while (!gl.shouldClose()) {
//        	// Buffer vertices
//        	createVertices(vertices);
//        	vertexBuff.rewind();
//        	copyVertex(vertexBuff, vertices);
//        	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, vertexBuffer);
//        	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, 2 * Float.BYTES * vertices.length, vertexBuff, 0);
//
//        	colorBuff.rewind();
//        	copyColor(colorBuff, vertices);
//        	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, colorBuffer);
//        	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, 3 * Float.BYTES * vertices.length, colorBuff, 0);
////
////        	vertexBuff.rewind();
////
////        	while (vertexBuff.hasRemaining()) {
////        		System.out.println(vertexBuff.getFloat());
////        	}
//
//    		gl.beginRecord();
//
//    		if (multithreaded) gl.selectNode((threadIndex++)%gl.getNodesCount());
//    		else gl.selectNode(0);
//
//    		gl.glDrawArrays(0, 0, vertices.length);
//    		gl.endRecord();
//
//    		frameWait();
//    	}
//    	gl.close();
//	}
//
//
//
//	private static void frameWait() {
//		try {
//			Thread.sleep(16);
//		} catch (InterruptedException e) {
//		}
//	}
//
//
//	public static void throttleTest(GL2VK gl) {
//		final int PARTS = 5;
//		Vertex[] vertices = new Vertex[3];
//		int[] vertexBuffer = new int[PARTS];
//    	createVertices(vertices);
//
//    	// Gen buffers
//    	IntBuffer out = IntBuffer.allocate(PARTS);
//    	gl.glGenBuffers(PARTS, out);
//    	vertexBuffer = out.array();
//
//    	int buffindex = 0;
//
//    	int size = vertices.length*Vertex.SIZEOF;
//    	ByteBuffer buff = ByteBuffer.allocate(size);
//
//    	for (int i = 0; i < PARTS; i++) {
//	    	// Buffer data
//	    	createVertices(vertices);
//	    	buff.rewind();
//	    	memcpy(buff, vertices);
//
//	    	gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, vertexBuffer[i]);
//	    	gl.glBufferData(GL2VK.GL_VERTEX_BUFFER, size, buff, 0);
//    	}
//
//    	boolean multithreaded = true;
//
//    	while (!gl.shouldClose()) {
//
//    		gl.beginRecord();
//
//    		// Throttle
//        	for (int i = 0; i < 10000; i++) {
//        		if (multithreaded) gl.selectNode((i/10)%gl.getNodesCount());
//        		else gl.selectNode(0);
//
//        		gl.glBindBuffer(GL2VK.GL_VERTEX_BUFFER, vertexBuffer[buffindex]);
//	    		gl.glDrawArrays(0, 0, vertices.length);
//        	}
//
//    		buffindex++;
//    		if (buffindex >= PARTS) buffindex = 0;
//
//
//    		gl.endRecord();
////    		frameWait();
//    	}
//    	gl.close();
//	}

}
