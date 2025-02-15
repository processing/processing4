package processing.GL2VK;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8_UINT;

import java.util.ArrayList;
import java.util.HashMap;


public class ShaderAttribInfo {

	// Each attrib has a type, size, and location
	public class AttribInfo {
		public int location = 0;
		public int format = 0;
		public int size = 0;
		public int offset = 0;
		
		public AttribInfo(int l, int f, int s, int off) {
			location = l;
			format = f;
			size = s;
			offset = off;
		}
	}
	
	public ShaderAttribInfo(String source) {
		loadShaderAttribs(source);
	}
	
	private void newAttribInfo(int l, int f, int s, int off) {
		locationToAttrib[l] = new AttribInfo(l,f,s,off);
	}
	
	// Stupid redundant info for gl backward compat
	public int bindingSize = 0;
	public AttribInfo[] locationToAttrib = new AttribInfo[1024];
	public HashMap<String, Integer> nameToLocation = new HashMap<String, Integer>();
	

	private void loadShaderAttribs(String shader) {
		
		// Split into array of lines
		String[] lines = shader.split("\n");
		int currAttribOffset = 0;
		
		
		int bracketDepth = 0;
		for (String s : lines) {
			
			// Bracket depth so we only get attribs from outside bodies.
			// You'll never see attribs/uniforms in brackets for example.
			bracketDepth += countChars(s, "{");
			bracketDepth -= countChars(s, "}");
			
			
			// Make sure we're not in any methods (like main)
			// and search for attribs (keyword "in")
			if (
					bracketDepth == 0 &&
					s.contains(" in ")
			) {
				// Lil filtering
				// Elements makes it easier to get each individual token.
				String[] elements = s.split(" ");
				// No spaces allowed.
				String line = s.replaceAll(" ", "");

				// Get location
				int index = line.indexOf("layout(location=");
				if (index == -1) continue;
				index += "layout(location=".length();
				int endIndex = line.indexOf(")", index);
				
				int location = Integer.parseInt(line.substring(index, endIndex));
//				System.out.println("Location: "+location);
				
				// Get type
				String type = "";
				String attribName = "";
				try {
					// Search for the element "in".
					// It is equivalant to "attrib" in OpenGL.
					for (int i = 0; i < elements.length; i++) {
						if (elements[i].equals("in")) {
							// Once we find in, remember what follows:
							// e.g. in vec3 variableName;
							type = elements[i+1];
							attribName = elements[i+2];
							// Remove ; at the end
							// And get the attribname
							if (attribName.charAt(attribName.length()-1) == ';') attribName = attribName.substring(0, attribName.length()-1);
//							System.out.println("type: "+type+"  attribName: "+attribName);
							break;
						}
					}
				}
				catch (IndexOutOfBoundsException e) {
//					System.err.println("Woops");
				}
				
				int size = typeToSize(type);
				int format = typeToFormat(type);
				
				newAttribInfo(location, format, size, currAttribOffset);
				
				bindingSize += size;
				currAttribOffset += size;
				
				nameToLocation.put(attribName, location);
				
				// Shouldn't be anything else for us to scan at this point.
				continue;
			}
		}
	}
	

	private static int countChars(String line, String c) {
		return line.length() - line.replace(c, "").length();
	}
	
	private static int typeToFormat(String val) {
		if (val.equals("float")) return VK_FORMAT_R32_SFLOAT;
		else if (val.equals("vec2")) return VK_FORMAT_R32G32_SFLOAT;
		else if (val.equals("vec3")) return VK_FORMAT_R32G32B32_SFLOAT;
		else if (val.equals("vec4")) return VK_FORMAT_R32G32B32A32_SFLOAT;
		else if (val.equals("int")) return VK_FORMAT_R32_SINT;
		else if (val.equals("ivec2")) return VK_FORMAT_R32G32_SINT;
		else if (val.equals("ivec3")) return VK_FORMAT_R32G32B32_SINT;
		else if (val.equals("ivec4")) return VK_FORMAT_R32G32B32A32_SINT;
		else if (val.equals("uint")) return VK_FORMAT_R32_UINT;
		else if (val.equals("uvec2")) return VK_FORMAT_R32G32_UINT;
		else if (val.equals("uvec3")) return VK_FORMAT_R32G32B32_UINT;
		else if (val.equals("uvec4")) return VK_FORMAT_R32G32B32A32_UINT;
		else if (val.equals("bool")) return VK_FORMAT_R8_UINT;
		else if (val.equals("bvec2")) return VK_FORMAT_R8G8_UINT;
		else if (val.equals("bvec3")) return VK_FORMAT_R8G8B8_UINT;
		else if (val.equals("bvec4")) return VK_FORMAT_R8G8B8A8_UINT;
		else if (val.equals("mat2")) return VK_FORMAT_R32G32_SFLOAT;
		else if (val.equals("mat3")) return VK_FORMAT_R32G32B32_SFLOAT;
		else if (val.equals("mat4")) return VK_FORMAT_R32G32B32A32_SFLOAT;
		else return -1;
	}
	
	
	
	private static int typeToSize(String val) {
		if (val.equals("float")) return 1 * Float.BYTES;
		else if (val.equals("vec2")) return 2 * Float.BYTES;
		else if (val.equals("vec3")) return 3 * Float.BYTES;
		else if (val.equals("vec4")) return 4 * Float.BYTES;
		else if (val.equals("int")) return 1 * Integer.BYTES;
		else if (val.equals("ivec2")) return 2 * Integer.BYTES;
		else if (val.equals("ivec3")) return 3 * Integer.BYTES;
		else if (val.equals("ivec4")) return 4 * Integer.BYTES;
		else if (val.equals("uint")) return 1 * Integer.BYTES;
		else if (val.equals("uvec2")) return 2 * Integer.BYTES;
		else if (val.equals("uvec3")) return 3 * Integer.BYTES;
		else if (val.equals("uvec4")) return 4 * Integer.BYTES;
		else if (val.equals("bool")) return 1;
		else if (val.equals("bvec2")) return 2;
		else if (val.equals("bvec3")) return 3;
		else if (val.equals("bvec4")) return 4;
		else if (val.equals("mat2")) return 2 * 2 * Float.BYTES;
		else if (val.equals("mat3")) return 3 * 3 * Float.BYTES;
		else if (val.equals("mat4")) return 4 * 4 * Float.BYTES;
		else return -1;
	}
	
	
}