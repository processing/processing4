package processing.GL2VK;

import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

import java.util.HashSet;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

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
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16_SNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SNORM;


// ABOUT BINDINGS:
// Bindings are just like glBindBuffer before calling vertexAttribPointer.
// Bindings are indexes to those buffers.
// For example
// Binding 0: vertexBuffer[0]
// Binding 1: vertexBuffer[1]
// Remember that each of those bindings are tied to the attributes
// For example, attribute (location=0) can be attached to binding 0
// Then attribute (location=1) can be attached to binding 1
// And
// When you want interleaved, it's like this
// Attribute (location=0) attached to binding 0
// Attribute (location=1) attached to binding 0
// Both attached to binding 0.
// Of course, if you want separate buffers per attribute, you'll need to
// assign them different buffers each.


// In our main program, here's what's happening surface level v underneath the hood:

// EXAMPLE 1
// Surface:
// glbindBuffer(PGL.ARRAY_BUFFER, vertexVboId);
// glvertexAttribPointer(vertLoc, VERT_CMP_COUNT, PGL.FLOAT, false, vertexStride, vertexOffset);
// glbindBuffer(PGL.ARRAY_BUFFER, colorVboId);
// glvertexAttribPointer(colorLoc, CLR_CMP_COUNT, PGL.FLOAT, false, colorStride, colorOffset);

// Under the hood:
// - New buffer bound, create new VertexAttribsBinding object
// - Set VertexAttribsBinding object's attribs.
// - New buffer bound, create new VertexAttribsBinding object
// - Set VertexAttribsBinding object's attribs.
// - When pipeline gets created, we go through each VertexAttribsBinding object
//   and join the vertexAttributeDescriptions and combine the vertexbindings of each object

// EXAMPLE 2
// Surface:
// glbufferData(PGL.ARRAY_BUFFER, Float.BYTES * attribs.length, attribBuffer, PGL.DYNAMIC_DRAW);
// glvertexAttribPointer(vertLoc, VERT_CMP_COUNT, PGL.FLOAT, false, stride, vertexOffset);
// glvertexAttribPointer(colorLoc, CLR_CMP_COUNT, PGL.FLOAT, false, stride, colorOffset);


// Under the hood:
// - New buffer bound, create new VertexAttribsBinding object
// - Set VertexAttribsBinding object's attribs.
// - Set VertexAttribsBinding object's attribs.
// - When pipeline gets created, we go through each VertexAttribsBinding object
//   and join the vertexAttributeDescriptions and combine the vertexbindings of each object



public class VertexAttribsBinding {
	public int myBinding = 0;
	private int bindingStride = 0;

	// Need the whole buffer and not just the id because the id could change at any time.
	public GraphicsBuffer buffer = null;
	private ShaderAttribInfo attribInfo;

	private int stateHash = 0;

	private HashSet<Integer> usedLocations = new HashSet<>();

	public VertexAttribsBinding(int binding, ShaderAttribInfo attribInfo) {
		this.myBinding = binding;
		this.attribInfo = attribInfo;
		this.bindingStride = attribInfo.bindingSize;
	}




	// As ugly as it is to modify values straight from attribInfo, realistically these
	// values will not be changed to anything different if the program uses vertexAttribPointer
	// correctly.
	// NOTE: Passing the return value from glGetAttribLocation will NOT work because how it works
	// in gl:
	// Shader 0:
	// 1: attrib 0
	// 2: attrib 1
	// Shader 1:
	// 3: attrib 0
	// 4: attrib 1
	// 5: attrib 2
	// Instead you will need to pass the attrib number belonging to the shader, i.e. attrib 1,2,3
	// OpenGL is truly a tangled mess.
	public void vertexAttribPointer(int vklocation, int count, int type, boolean normalized, int stride, int offset) {


	  int actualSize = 0;
	  int vktype = -1;
    switch (type) {
    case GL2VK.GL_UNSIGNED_BYTE:
//	      System.out.println("GL_UNSIGNED_BYTE");

      // UNORM: 0 to 1
      if (normalized) {
        if (count == 1) {
          vktype = VK_FORMAT_R8_UNORM;
        }
        else if (count == 2) {
          vktype = VK_FORMAT_R8G8_UNORM;
        }
        else if (count == 3) {
          vktype = VK_FORMAT_R8G8B8_UNORM;
        }
        else if (count == 4) {
          vktype = VK_FORMAT_R8G8B8A8_UNORM;
        }
      }
      else {
        System.err.println("vertexAttribPointer: not supported (unnormalized unsigned byte)");
      }
      actualSize = count*Byte.BYTES;
      break;
    case GL2VK.GL_UNSIGNED_SHORT:
//	      System.out.println("SHORT");

      // UNORM: 0 to 1
      if (normalized) {
        if (count == 1) {
          vktype = VK_FORMAT_R16_UNORM;
        }
        else if (count == 2) {
          vktype = VK_FORMAT_R16G16_UNORM;
        }
        else if (count == 3) {
          vktype = VK_FORMAT_R16G16B16_UNORM;
        }
        else if (count == 4) {
          vktype = VK_FORMAT_R16G16B16A16_UNORM;
        }
      }
      else {
        System.err.println("vertexAttribPointer: not supported (unnormalized unsigned short)");
      }
      actualSize = count*Short.BYTES;
      break;
    case GL2VK.GL_UNSIGNED_INT:
//	      System.out.println("GL_UNSIGNED_INT");
      System.err.println("vertexAttribPointer: Unsigned int not supported");
      actualSize = count*Integer.BYTES;
      break;
    case GL2VK.GL_INT:
//	      System.out.println("GL_INT");
      System.err.println("vertexAttribPointer: signed int not supported");
      actualSize = count*Integer.BYTES;
      break;
    case GL2VK.GL_BYTE:
//	      System.out.println("GL_BYTE");

      // SNORM: -1 to 1
      if (normalized) {
        if (count == 1) {
          vktype = VK_FORMAT_R8_SNORM;
        }
        else if (count == 2) {
          vktype = VK_FORMAT_R8G8_SNORM;
        }
        else if (count == 3) {
          vktype = VK_FORMAT_R8G8B8_SNORM;
        }
        else if (count == 4) {
          vktype = VK_FORMAT_R8G8B8A8_SNORM;
        }
      }
      else {
        System.err.println("vertexAttribPointer: not supported (unnormalized signed byte)");
      }
      actualSize = count*Byte.BYTES;
      break;
    case GL2VK.GL_SHORT:

      // SNORM: -1 to 1
      if (normalized) {
        if (count == 1) {
          vktype = VK_FORMAT_R16_SNORM;
        }
        else if (count == 2) {
          vktype = VK_FORMAT_R16G16_SNORM;
        }
        else if (count == 3) {
          vktype = VK_FORMAT_R16G16B16_SNORM;
        }
        else if (count == 4) {
          vktype = VK_FORMAT_R16G16B16A16_SNORM;
        }
      }
      else {
        System.err.println("vertexAttribPointer: not supported (unnormalized signed short)");
      }
//	      System.out.println("GL_SHORT");
      actualSize = count*Short.BYTES;
      break;
    case GL2VK.GL_FLOAT:
//      System.out.println("GL_FLOAT");
      if (normalized) {
        System.err.println("vertexAttribPointer: not supported (normalized float)");
      }
      else {
        if (count == 1) {
          vktype = VK_FORMAT_R32_SFLOAT;
        }
        else if (count == 2) {
          vktype = VK_FORMAT_R32G32_SFLOAT;
        }
        else if (count == 3) {
          vktype = VK_FORMAT_R32G32B32_SFLOAT;
        }
        else if (count == 4) {
          vktype = VK_FORMAT_R32G32B32A32_SFLOAT;
        }
      }
      actualSize = count*Float.BYTES;
      break;
    case GL2VK.GL_BOOL:
//	      System.out.println("GL_BOOL");
        System.err.println("vertexAttribPointer: not supported (boolean)");
      actualSize = count*1;
      break;
    }

    // Something I did not spot in the OpenGL specification:
    // "If stride is 0, the generic vertex attributes are understood to be tightly packed in the array"
    if (stride == 0) {
      stride = actualSize;
    }


    attribInfo.locationToAttrib[vklocation].size = actualSize;
    attribInfo.locationToAttrib[vklocation].format = vktype;
    attribInfo.locationToAttrib[vklocation].offset = offset;
    bindingStride = stride;

		// We're using those attribs
		usedLocations.add(vklocation);

		// What's set by this function determines the state of the pipeline (if
		// it changes at any point, we need to recreate the pipeline with new
		// vertex bindings)

		// TODO: Remove this, move it to getStateHash() instead.
		stateHash += (vklocation+1L)*usedLocations.size()*100L + count*2 + offset*3;
	}

	// Mostly used for testing purposes
	public void vertexAttribPointer(int location) {
		// We're using those attribs
		usedLocations.add(location);

		// TODO: Remove this, move it to getStateHash() instead.
		stateHash += (location+1L)*usedLocations.size()*100L +
				attribInfo.locationToAttrib[location].size*2 +
				attribInfo.locationToAttrib[location].offset*3;
	}

	// TODO: Calculate hash state on the spot instead of relying on possibly invalid states.
	public int getHashState() {
		return stateHash;
	}

	public void updateAttributeDescriptions(VkVertexInputAttributeDescription.Buffer attribDescrptions, int index) {
		for (Integer loc : usedLocations) {
			VkVertexInputAttributeDescription description = attribDescrptions.get(index++);
			description.binding(myBinding);
			description.location(loc);
			description.format(attribInfo.locationToAttrib[loc].format);
			description.offset(attribInfo.locationToAttrib[loc].offset);
		}
	}

	public int getSize() {
		return usedLocations.size();
	}

	public void updateBindingDescription(VkVertexInputBindingDescription bindingDescription) {
		bindingDescription.binding(myBinding);
		bindingDescription.stride(bindingStride);
		bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
	}

}
