package processing.GL2VK;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import processing.GL2VK.ShaderSPIRVUtils.SPIRV;


// Buffer bindings in opengl look like this
// buffer 6
// buffer 7
// buffer 8
// Let's say we call
//  bindBuffer(6)
//  vertexAttribPointer(...)
//  bindBuffer(5)
//  vertexAttribPointer(...)
//  bindBuffer(8)
//  vertexAttribPointer(...)
// The question is, what would vulkan's bindings be?
// ...
// Whenever we bind a buffer, we need to add it to a list along
// with the actual buffer so we can later use it to vkcmdBindVertexArrays()
// so
// bindBuffer(6)
// enableVertexArrays()
// vertexAttribPointer(...)
// - vkbuffer[0] = buffer(6)
// - Create VertexAttribsBinding with binding 0
//
// bindBuffer(5)
// vertexAttribPointer(...)
// - vkbuffer[1] = buffer(5)
// - Create VertexAttribsBinding with binding 1
//
// bindBuffer(8)
// vertexAttribPointer(...)
// - vkbuffer[2] = buffer(8)
// - Create VertexAttribsBinding with binding 2
//
// We don't care what our vkbindings are, as long as it's in the correct order
// as we're passing the lists in when we call vkCmdBindVertexArrays().

// TODO: Properly fix up compileshaders
// TODO: Turn gl2vkBinding to buffer pointers so that we can use it in vkCmdBindVertexBuffers.
// It's very simple, when you call drawArrays in GL2VK, simply pass a list of buffers[].bufferID
// to the command.
// Maybe not so simple, because challenge to overcome: we need to somehow pass that big ol array
// through atomicLongs.

public class GL2VKPipeline {

  // In order to change textures, we need to bind different descriptorsets;
  // this class handles the creation of these descriptor sets and binds the particular image to it.
  private class TextureDescriptor {
    // For the descriptor sets
    private long descriptorPool = -1;
    // Need several, one for each frame.
    private long[] descriptorSets = null;


    public TextureDescriptor(TextureBuffer img) {
      // Must only be called when the pipeline is initiated
      if (currentState == -1) {
        throw new RuntimeException("TextureDescriptor must be called after pipeline has been initiated");
      }
      initDescritpors(samplerBindings.size());
      updateImage(img);
    }

    public long get() {
      return descriptorSets[system.getFrame()];
    }

    private void initDescritpors(int layoutBindingsSize) {
      try(MemoryStack stack = stackPush()) {
        // Create descriptor pool
        // First, we need poolsize
        VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.calloc(layoutBindingsSize, stack);

        // First, the samplers.
        for (int index = 0; index < layoutBindingsSize; index++) {
            poolSize.get(index).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            poolSize.get(index).descriptorCount(vkbase.swapChainImages.size());
            index++;
        }

        // Actually create the pool
        VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack);
        poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
        poolInfo.pPoolSizes(poolSize);
        poolInfo.maxSets(vkbase.swapChainImages.size());
        LongBuffer pDescriptorPool = stack.mallocLong(1);

        if(vkCreateDescriptorPool(system.device, poolInfo, null, pDescriptorPool) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create descriptor pool");
        }
        descriptorPool = pDescriptorPool.get(0);

        // Ok, time to create the set
        // Remember there's a separate descriptorset for each frame.
        // Use the pipeline's layout. Copy for each frame.
        LongBuffer layouts = stack.mallocLong(vkbase.swapChainImages.size());
        for(int i = 0;i < layouts.capacity();i++) {
            layouts.put(i, descriptorSetLayout);
        }

        // Allocation info
        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool(descriptorPool);
        allocInfo.pSetLayouts(layouts);


        LongBuffer pDescriptorSets = stack.mallocLong(vkbase.swapChainImages.size());

        if(vkAllocateDescriptorSets(vkbase.device, allocInfo, pDescriptorSets) != VK_SUCCESS) {
            throw new RuntimeException("Failed to allocate descriptor sets");
        }

        // Cool! We got our descriptor sets. Put it into our cool array.
        descriptorSets = new long[pDescriptorSets.capacity()];
        for (int i = 0; i < pDescriptorSets.capacity(); i++) {
          descriptorSets[i] = pDescriptorSets.get(i);
        }

      }
    }

    private void updateImage(TextureBuffer imgBuffer) {
      try(MemoryStack stack = stackPush()) {

        // Only need 1 info.
        // We don't need multiple because we don't have an array of images.
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(imgBuffer.imageView);
        imageInfo.sampler(imgBuffer.sampler);

        VkWriteDescriptorSet.Buffer samplerDescriptorWrite = VkWriteDescriptorSet.calloc(1, stack);
        samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        // TODO: Multiple samplers and binding the right texture to the right binding
        samplerDescriptorWrite.dstBinding(0);
        samplerDescriptorWrite.dstArrayElement(0);
        samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        samplerDescriptorWrite.descriptorCount(1);
        samplerDescriptorWrite.pImageInfo(imageInfo);

        // Remember, multiple descriptor sets.
        // Update all of them.
        for (int i = 0; i < descriptorSets.length; i++) {
          samplerDescriptorWrite.dstSet(descriptorSets[i]);

          vkUpdateDescriptorSets(system.device, samplerDescriptorWrite, null);
        }
      }
    }
  }

  private class PipelineState {
    public PipelineState(long g, long l) {
      this.graphicsPipeline = g;
      this.pipelineLayout = l;
    }

    public long graphicsPipeline = -1;

    // Public for use with push constants.
    public long pipelineLayout = -1;
  }

	private VulkanSystem system;
	private VKSetup vkbase;

	public SPIRV vertShaderSPIRV = null;
	public SPIRV fragShaderSPIRV = null;

	private long currentState = -1;
	private HashMap<Long, PipelineState> states = new HashMap<>();



  private long descriptorSetLayout = -1;

  // Pipeline's state
  public boolean depthTestEnable = false;
  public boolean depthWriteEnable = false;


  // We assign the offsets when we add the uniforms to the pipeline,
  // NOT during source code parsing (check notes in the ShaderAttribInfo.parseUniforms() method)
  private int currUniformOffset = 0;

	public ShaderAttribInfo attribInfo = null;

	private HashMap<Integer, VertexAttribsBinding> gl2vkBinding = new HashMap<>();
	private HashMap<String, Integer> attribNameToGLLocation = new HashMap<>();
	private int[] GLLocationToVKLocation = new int[1024];
	private HashMap<String, Integer> name2UniformLocation = new HashMap<>();
	public ArrayList<GLUniform> uniforms = new ArrayList<>();
	private ArrayList<Integer> samplerBindings = new ArrayList<>();

	// Textures are based on the OpenGL bindings, so use a standard array
	private TextureDescriptor[] textures = new TextureDescriptor[4096];

	private int boundBinding = 0;
	private int totalVertexAttribsBindings = 0;




	public GL2VKPipeline(VulkanSystem system) {
    	this.system = system;
    	this.vkbase = system.vkbase;
	}

	// Only use this constructor for testing purposes
	public GL2VKPipeline() {
	}


  private long pow(int input, int exp) {
    return (long)Math.pow(input, exp);
  }

  public long getHash() {
    return
        pow(2, depthTestEnable ? 1 : 0) *
        pow(3, depthWriteEnable ? 1 : 0);
  }

	public long getPipeline() {
	  return states.get(currentState).graphicsPipeline;
	}

  public long getLayout() {
    return states.get(currentState).pipelineLayout;
  }

	// Same from the tutorial
    private long createShaderModule(ByteBuffer spirvCode) {
        try(MemoryStack stack = stackPush()) {

            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);

            if(vkCreateShaderModule(vkbase.device, createInfo, null, pShaderModule) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shader module");
            }

            return pShaderModule.get(0);
        }
    }


    public boolean needsNewStateCreation() {
      return !states.containsKey(currentState);
    }

    public boolean initiated() {
      return currentState != -1;
    }

    // Checks if the state needs to be changed (because i.e. depth test enabled/disabled)
    // and if so sets currentState and returns true.
    public boolean updatedState() {
      if (currentState != getHash()) {
        currentState = getHash();
        return true;
      }
      else return false;
    }


    public void createGraphicsPipeline() {

        try(MemoryStack stack = stackPush()) {

            // Let's compile the GLSL shaders into SPIR-V at runtime using the shaderc library
            // Check ShaderSPIRVUtils class to see how it can be done
        	if (vertShaderSPIRV == null || fragShaderSPIRV == null) {
        		throw new RuntimeException("Shaders must be compiled before calling createGraphicsPipeline()");
        	}

        	// Do the descriptorset stuff
        	createDescriptorLayout();

          	// Completely unnecessary code
      		long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
      		long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());


//            long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
//            long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());

            ByteBuffer entryPoint = stack.UTF8("main");

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);

            VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

            vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
            vertShaderStageInfo.module(vertShaderModule);
            vertShaderStageInfo.pName(entryPoint);

            VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);

            fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
            fragShaderStageInfo.module(fragShaderModule);
            fragShaderStageInfo.pName(entryPoint);

            // ===> VERTEX STAGE <===

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            vertexInputInfo.pVertexBindingDescriptions(getBindingDescriptions());
            vertexInputInfo.pVertexAttributeDescriptions(getAttributeDescriptions());

            // ===> ASSEMBLY STAGE <===

            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            inputAssembly.primitiveRestartEnable(false);

            // ===> VIEWPORT & SCISSOR

            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(vkbase.swapChainExtent.width());
            viewport.height(vkbase.swapChainExtent.height());

            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
            scissor.extent(vkbase.swapChainExtent);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            viewportState.pViewports(viewport);
            viewportState.pScissors(scissor);

            // ===> RASTERIZATION STAGE <===

            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
            rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterizer.depthClampEnable(false);
            rasterizer.rasterizerDiscardEnable(false);
            rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
            rasterizer.lineWidth(1.0f);
//            rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
            rasterizer.cullMode(VK_CULL_MODE_NONE);
//            rasterizer.frontFace(VK_FRONT_FACE_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            // ===> MULTISAMPLING <===

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(false);
            multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
            depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(depthTestEnable);
            depthStencil.depthWriteEnable(depthWriteEnable);
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0.0f); // Optional
            depthStencil.maxDepthBounds(1.0f); // Optional
            depthStencil.stencilTestEnable(false);

            // ===> COLOR BLENDING <===

            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
            colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable(true);
            colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA); // Source factor
            colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA); // Destination factor
            colorBlendAttachment.colorBlendOp(VK_BLEND_OP_ADD); // Blend operation for color
            colorBlendAttachment.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE); // Source alpha factor
            colorBlendAttachment.dstAlphaBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA); // Destination alpha factor
            colorBlendAttachment.alphaBlendOp(VK_BLEND_OP_ADD); // Blend operation for alpha
            colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT |
                                                  VK_COLOR_COMPONENT_G_BIT |
                                                  VK_COLOR_COMPONENT_B_BIT |
                                                  VK_COLOR_COMPONENT_A_BIT); // Write mask

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
//            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachment);
//            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            // ===> PIPELINE LAYOUT CREATION <===

            // PUSH CONSTANTS
            int vertexSize = 0;
            int fragmentSize = 0;
            int totalSize = 0;

            // Need these just in case the offset is set to something stupidly high
            int maxOffsettedSizeVertex = 0;
            int maxOffsettedSizeFragment = 0;
            // Now we must compile our uniforms list into this pushConstants thing
            // All we really need to do is set the size.
            for (GLUniform uni : uniforms) {
            	if (uni.vertexFragment == GLUniform.VERTEX) {
            		vertexSize += uni.size;
            		totalSize += uni.size;
            		if (uni.offset+uni.size > maxOffsettedSizeVertex)
            			maxOffsettedSizeVertex = uni.offset+uni.size;
            	}
            	if (uni.vertexFragment == GLUniform.FRAGMENT) {
            		fragmentSize += uni.size;
                totalSize += uni.size;
            		if (uni.offset+uni.size > maxOffsettedSizeFragment)
            			maxOffsettedSizeFragment = uni.offset+uni.size;
            	}
            }

            if (maxOffsettedSizeVertex > vertexSize) vertexSize = maxOffsettedSizeVertex;
//            if (maxOffsettedSizeFragment > fragmentSize) fragmentSize = maxOffsettedSizeFragment;
            if (maxOffsettedSizeFragment > totalSize) totalSize = maxOffsettedSizeFragment;


//            vertexSize = Util.roundToMultiple8(vertexSize);


            int limit = system.getPushConstantsSizeLimit();
            if (totalSize > limit) {
            	Util.emergencyExit(
            			"Uniform variables totals up to "+totalSize+" bytes, greater than the push constant limit of "+limit+" bytes",
            			"on this gpu.",
            			"Unfortunately, uniform sizes greater than "+limit+" bytes is not supported yet.",
            			"The only solution for now is to remove some of the uniforms in your shader",
            			"(both vertex and fragment) to reduce uniform size, I'm sorry :("
    			);
            	// Program will exit after this.
            }

        	// Here, for each uniform, we specify a pushConstant
            int numBlocks = 0;
            if (vertexSize > 0) numBlocks++;
            if (fragmentSize > 0) numBlocks++;
            VkPushConstantRange.Buffer pushConstants = VkPushConstantRange.calloc(numBlocks, stack);

            // 0: vertex
            if (vertexSize > 0) {
	            pushConstants.get(0).offset(0);
	            pushConstants.get(0).size(vertexSize);
	            pushConstants.get(0).stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            }

            // No fragment variables? Forget about push constants
            // 1: fragment
            if (fragmentSize > 0) {
	            pushConstants.get(1).offset(vertexSize);
	            pushConstants.get(1).size(fragmentSize);
	            pushConstants.get(1).stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            }

            // Now pipeline layout info (only 1)
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            if (samplerBindings.size() > 0) {
              pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout));
            }

            if (numBlocks > 0) {
            	pipelineLayoutInfo.pPushConstantRanges(pushConstants);
            }

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

            if(vkCreatePipelineLayout(vkbase.device, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create pipeline layout");
            }


            /////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////



            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.layout(pPipelineLayout.get(0));
            pipelineInfo.renderPass(system.renderPass);
            pipelineInfo.subpass(0);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);

            if(vkCreateGraphicsPipelines(vkbase.device, VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create graphics pipeline");
            }

            // Create new state
            if (states.containsKey(getHash())) {
              System.out.println("WARNING  Pipelines state "+getHash()+" already exists");
            }

            PipelineState state = new PipelineState(pGraphicsPipeline.get(0), pPipelineLayout.get(0));

            states.put(getHash(), state);

            // ===> RELEASE RESOURCES <===
            vertShaderSPIRV.free();
            fragShaderSPIRV.free();

        		vkDestroyShaderModule(vkbase.device, vertShaderModule, null);
        		vkDestroyShaderModule(vkbase.device, fragShaderModule, null);

            vertShaderModule = -1;
            fragShaderModule = -1;
        }
    }


    // Hoohoohooo let's have some fun with descriptors.
    // TODO: at some point, normal descriptors and not just samplers.
    private void createDescriptorLayout() {
        try(MemoryStack stack = stackPush()) {

            int index = 0;
            int layoutBindingsSize = samplerBindings.size();
            VkDescriptorSetLayoutBinding.Buffer layoutBindings = VkDescriptorSetLayoutBinding.calloc(layoutBindingsSize, stack);

            // First, the samplers.
            for (Integer binding : samplerBindings) {
                layoutBindings.get(index).binding(binding);
                layoutBindings.get(index).descriptorCount(1);
                layoutBindings.get(index).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                layoutBindings.get(index).pImmutableSamplers(null);
                layoutBindings.get(index).stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            }

            // Actually, there is no next, it's just samplers for now.

            // Now let's create our thing !

            // Create descriptor layout
            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            // Set to our layout bindings from above.
            layoutInfo.pBindings(layoutBindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if(vkCreateDescriptorSetLayout(system.device, layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor set layout");
            }
            descriptorSetLayout = pDescriptorSetLayout.get(0);

            // Done! For now.
        }
    }


    // Remember, everytime you create a new pipeline, you should add the currently bound texture.
    public void addTextureIfAbsent(int binding, TextureBuffer img) {
      if (currentState == -1) return;

      // Don't add if it already exists
      if (textures[binding] == null) {
        textures[binding] = new TextureDescriptor(img);
      }
    }


    public long getCurrentTextureDescriptor(int texBinding) {
      return textures[texBinding].get();
    }


    public VkVertexInputBindingDescription.Buffer getBindingDescriptions() {
  		VkVertexInputBindingDescription.Buffer bindingDescriptions =
  		VkVertexInputBindingDescription.calloc(gl2vkBinding.size());

  		int i = 0;
    	for (VertexAttribsBinding vab : gl2vkBinding.values()) {
    		vab.updateBindingDescription(bindingDescriptions.get(i++));
    	}

    	return bindingDescriptions.rewind();
    }


    public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
  		VkVertexInputAttributeDescription.Buffer attributeDescriptions =
  		VkVertexInputAttributeDescription.calloc(attribInfo.nameToLocation.size());

  		int i = 0;
      	for (VertexAttribsBinding vab : gl2vkBinding.values()) {
      		vab.updateAttributeDescriptions(attributeDescriptions, i);
      		i += vab.getSize();
      	}

  		return attributeDescriptions;
    }


    // Used when glBindBuffer is called, so that we know to create a new binding
    // for our vertexattribs vulkan pipeline. This should be called in glVertexAttribPointer
    // function.
    public void bind(int glIndex, GraphicsBuffer buffer) {
    	boundBinding = glIndex;
    	// Automatically allocate new binding and increate binding count by one.
    	if (!gl2vkBinding.containsKey(boundBinding)) {
        	gl2vkBinding.put(glIndex, new VertexAttribsBinding(totalVertexAttribsBindings++, attribInfo));
    	}
    	// It all flowssss. In a very complicated, spaghetti'd way.
    	// Tell me a better way to do it though.
    	// Technically the actual buffer can change during the pipeline so
    	// allow that to be dyncamically updated.
    	gl2vkBinding.get(glIndex).buffer = buffer;
    }

    // This should only be used with debug mode
    public void bind(int glIndex) {
    	bind(glIndex, null);
    }

    // Create global variable so it can be cached and hence avoiding garbage collection
    private ArrayList<Long> bufferArray = new ArrayList<>();

    // This is a pretty bad solution, but efficient.
    // Loop through the list, making sure the position in the array
    // aligns with the myBinding value in VertexAttribsBinding.
    // We just naively add an item if the list isn't big enough
    public ArrayList<Long> getVKBuffers() {
    	for (VertexAttribsBinding binding : gl2vkBinding.values()) {
    		while (binding.myBinding > bufferArray.size()-1) {
    			bufferArray.add(0L);
    		}
    		bufferArray.set(binding.myBinding, binding.buffer.getCurrBuffer());
    	}

    	return bufferArray;
    }

    public void vertexAttribPointer(int vklocation, int count, int type, boolean normalized, int stride, int offset) {
    	// Remember, a gl buffer binding of 0 means no bound buffer,
    	// and by default in this class, means bind() hasn't been called.
    	if (boundBinding == 0) {
    		System.err.println("BUG WARNING  vertexAttribPointer called with no bound buffer.");
    		return;
    	}
    	gl2vkBinding.get(boundBinding).vertexAttribPointer(vklocation, count, type, normalized, stride, offset);
    }

    // Not actually used but cool to have
    public void vertexAttribPointer(int location) {
    	gl2vkBinding.get(boundBinding).vertexAttribPointer(location);
    }

    public int addAttribInfo(ShaderAttribInfo attribInfo, int startIndex) {
    	this.attribInfo = attribInfo;
        // Of course we need a way to get our locations from the name.
    	int count = 0;
        for (Entry<String, Integer> entry : attribInfo.nameToLocation.entrySet()) {
        	attribNameToGLLocation.put(entry.getKey(), startIndex);
        	GLLocationToVKLocation[startIndex] = entry.getValue();
        	startIndex++;
        	count++;
        }

        // Returns the iterations
        return count;
    }

    public int getGLAttribLocation(String name) {
    	if (!attribNameToGLLocation.containsKey(name)) return -1;
    	return attribNameToGLLocation.get(name);
    }

    public int getVKAttribLocation(int glAttribLocation) {
    	return GLLocationToVKLocation[glAttribLocation];
    }

    public void addUniforms(ArrayList<GLUniform> uniforms) {
        for (GLUniform uniform : uniforms) {
        	// Assign offset to the uniform
        	// But only if it hasn't been manually assigned already
        	// (via the layout(offset=...) token in the shader).
        	if (uniform.offset == -1) {
	        	uniform.offset = currUniformOffset;
        	}
        	else {
        		currUniformOffset = uniform.offset;
        	}
        	currUniformOffset += uniform.size;


        	// +1 because opengl objects start at index 1.
        	// You'll need to remember to sub one whenever you access this
        	// uniform arrayList.
        	this.uniforms.add(uniform);
        	name2UniformLocation.put(uniform.name, this.uniforms.size());
        }
    }

    public void addSamplers(ArrayList<Integer> bindings) {
        for (Integer binding : bindings) {
            samplerBindings.add(binding);
        }
    }

    // Remember we start from 1, not 0.
    public GLUniform getUniform(int index) {
    	return uniforms.get(index-1);
    }

    public int getUniformLocation(String name) {
    	if (!name2UniformLocation.containsKey(name)) return -1;
    	return name2UniformLocation.get(name);
    }

    // Depricated but leave these in for the unit tests
	public void compileVertex(String source) {
	//      vertShaderSPIRV = compileShaderFile("resources/shaders/09_shader_base.vert", VERTEX_SHADER);
	  attribInfo = new ShaderAttribInfo(source);
	}

	public void compileFragment(String source) {
//    	  fragShaderSPIRV = compileShaderFile("resources/shaders/09_shader_base.frag", FRAGMENT_SHADER);
	}


  public void clean() {
    for (PipelineState state: states.values()) {
      vkDestroyPipeline(system.device, state.graphicsPipeline, null);
      vkDestroyPipelineLayout(system.device, state.pipelineLayout, null);
      vkDestroyDescriptorSetLayout(system.device, descriptorSetLayout, null);

    }
  }
}