package processing.GL2VK;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;




// Poptential useful extensions:

// VK_KHR_dynamic_rendering -
// Create a single render pass without requiring different render passes
// per pipeline or something

// enableVertexAttribArray is important because it tells opengl what
// vertex buffers to include for drawing with in commands like drawArrays.


public class VulkanSystem {



	public static final int MAX_FRAMES_IN_FLIGHT = 2;



    // ======= FIELDS ======= //

    public VkDevice device;




    public long renderPass;

    public long colorAttachmentVal;

    private List<VkCommandBuffer> commandBuffers;
    public List<Long> swapChainFramebuffers;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    boolean framebufferResize;

    public VKSetup vkbase;

  	private int selectedNode = 0;
  	private ThreadNode[] threadNodes = new ThreadNode[8];

    public long depthImage;
    public long depthImageMemory;
    public long depthImageView;


    // ======= METHODS ======= //

    public void run() {
//        initVulkan();
    }



    public void initVulkan(int width, int height, int threadNodeSize) {
    	vkbase = new VKSetup();
    	vkbase.initBase(width, height);
    	device = vkbase.device;

        createCommandPool();
        createRenderPass();
//        createGraphicsPipeline();
        createDepthBuffer();
        createFramebuffers();
        createCommandBuffers();
        createSyncObjects();
        createThreadNodes(threadNodeSize);
    }

    public boolean shouldClose() {
        glfwPollEvents();
    	return glfwWindowShouldClose(vkbase.window);
    }


    public void cleanupNodes() {
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            vkWaitForFences(device, inFlightFrames.get(i).pFence(), true, Util.UINT64_MAX);
        }

    	for (ThreadNode n : threadNodes) {
    		n.killAndCleanup();
    	}
    }

    public void cleanupRest() {
    	vkbase.destroyOtherThings();

        cleanupSwapChain();

        inFlightFrames.forEach(frame -> {

            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        inFlightFrames.clear();

        vkDestroyCommandPool(device, vkbase.commandPool, null);

        vkDestroyDevice(device, null);

        if(VKSetup.ENABLE_VALIDATION_LAYERS) {
            VKSetup.destroyDebugUtilsMessengerEXT(vkbase.instance, vkbase.debugMessenger, null);
        }

        vkDestroySurfaceKHR(vkbase.instance, vkbase.vkwindow.surface, null);

        vkDestroyInstance(vkbase.instance, null);

        glfwDestroyWindow(vkbase.window);

        glfwTerminate();
    }


    private void createThreadNodes(int v) {
      threadNodes = new ThreadNode[v];
    	for (int i = 0; i < threadNodes.length; i++) {
    		threadNodes[i] = new ThreadNode(this, i);
    	}
    }

    private void createThreadNodes() {
      int availableProcessors = Runtime.getRuntime().availableProcessors();
      createThreadNodes(availableProcessors);
    }



    private void createRenderPass() {

        try(MemoryStack stack = stackPush()) {

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(2, stack);

            // Color attachments

            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(vkbase.swapChainImageFormat);
            colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
//            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_LOAD);
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
//            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            int y = attachments.get(0).samples();
            VkAttachmentReference colorAttachmentRef = attachmentRefs.get(0);
            colorAttachmentRef.attachment(0);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

         // Depth-Stencil attachments

            VkAttachmentDescription depthAttachment = attachments.get(1);
            depthAttachment.format(vkbase.findDepthFormat());
            depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(1);
            depthAttachmentRef.attachment(1);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));
            subpass.pDepthStencilAttachment(depthAttachmentRef);


            VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpass);
            renderPassInfo.pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);

            if(vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass");
            }

            renderPass = pRenderPass.get(0);
        }
    }


    private void createSwapChainObjects() {
        vkbase.createSwapChain();
        vkbase.createImageViews();
        createRenderPass();
//        createGraphicsPipeline();
        createFramebuffers();
        createCommandBuffers();
    }

    public void recreateSwapChain() {
    	System.err.println("BUG WARNING  recreateSwapChain is temporarily disabled.");
//        try(MemoryStack stack = stackPush()) {
//
//            IntBuffer width = stack.ints(0);
//            IntBuffer height = stack.ints(0);
//
//            while(width.get(0) == 0 && height.get(0) == 0) {
//                glfwGetFramebufferSize(vkbase.window, width, height);
//                glfwWaitEvents();
//            }
//        }
//
//        vkDeviceWaitIdle(device);
//
//        cleanupSwapChain();
//
//        createSwapChainObjects();
    }

    private void createFramebuffers() {

        swapChainFramebuffers = new ArrayList<>(vkbase.swapChainImageViews.size());

        try(MemoryStack stack = stackPush()) {

            LongBuffer attachments = stack.longs(VK_NULL_HANDLE, depthImageView);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(vkbase.swapChainExtent.width());
            framebufferInfo.height(vkbase.swapChainExtent.height());
            framebufferInfo.layers(1);

            for(long imageView : vkbase.swapChainImageViews) {

                attachments.put(0, imageView);

                framebufferInfo.pAttachments(attachments);

                if(vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer");
                }

                swapChainFramebuffers.add(pFramebuffer.get(0));
            }
            colorAttachmentVal = attachments.get(0);
        }
    }


    private void createDepthBuffer() {
      try(MemoryStack stack = stackPush()) {
          int depthFormat = vkbase.findDepthFormat();

          LongBuffer pDepthImage = stack.mallocLong(1);
          LongBuffer pDepthImageMemory = stack.mallocLong(1);

          vkbase.createImage(
                  vkbase.swapChainExtent.width(), vkbase.swapChainExtent.height(),
                  depthFormat,
                  VK_IMAGE_TILING_OPTIMAL,
                  VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                  VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                  pDepthImage,
                  pDepthImageMemory);

          depthImage = pDepthImage.get(0);
          depthImageMemory = pDepthImageMemory.get(0);

          depthImageView = vkbase.createImageView(depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT);

          // Explicitly transitioning the depth image
          vkbase.transitionImageLayout(depthImage, depthFormat,
                  VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

      }
    }



    private void createCommandBuffers() {

        final int commandBuffersCount = swapChainFramebuffers.size();

        commandBuffers = new ArrayList<>(commandBuffersCount);

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.commandPool(vkbase.commandPool);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandBufferCount(commandBuffersCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);

            if(vkAllocateCommandBuffers(device, allocInfo, pCommandBuffers) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers");
            }

            for(int i = 0;i < commandBuffersCount;i++) {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device));
            }
        }
    }



    private void createSyncObjects() {

        inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(vkbase.swapChainImages.size());

        try(MemoryStack stack = stackPush()) {

            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for(int i = 0;i < MAX_FRAMES_IN_FLIGHT;i++) {

                if(vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
                || vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS
                || vkCreateFence(device, fenceInfo, null, pFence) != VK_SUCCESS) {

                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }

                inFlightFrames.add(new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
            }

        }
    }

    private void createCommandPool() {
    	vkbase.createCommandPool();
    }


    public int getPushConstantsSizeLimit() {
    	return vkbase.pushConstantsSizeLimit;
    }



    public VkCommandBuffer currentCommandBuffer = null;
//    private IntBuffer currentImageIndex = null;
    private int currentImageIndex = 0;
    public VkRenderPassBeginInfo renderPassInfo = null;

    public void beginRecord() {
    	// All the stuff that was before recordCommandBuffer()
        try(MemoryStack stack = stackPush()) {

        	// Frames in flight stuff
            Frame thisFrame = inFlightFrames.get(currentFrame);

//            Util.beginTmr();
            vkWaitForFences(device, thisFrame.pFence(), true, Util.UINT64_MAX);
//            Util.endTmr("wait 1");

            IntBuffer currentImageIndex = stack.mallocInt(1);

            int vkResult = vkAcquireNextImageKHR(device, vkbase.swapChain, Util.UINT64_MAX,
                    thisFrame.imageAvailableSemaphore(), VK_NULL_HANDLE, currentImageIndex);


            // Window resizing
            if(vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
                recreateSwapChain();
                return;
            } else if(vkResult != VK_SUCCESS) {
                throw new RuntimeException("Cannot get image");
            }

            final int imageIndex = currentImageIndex.get(0);
            this.currentImageIndex = currentImageIndex.get(0);

            // Fence wait for images in flight.
//            Util.beginTmr();
//            if(imagesInFlight.containsKey(imageIndex)) {
//                vkWaitForFences(device, imagesInFlight.get(imageIndex).fence(), true, Util.UINT64_MAX);
//            }
//            Util.endTmr("wait 2");

            imagesInFlight.put(imageIndex, thisFrame);

        }

        currentCommandBuffer = commandBuffers.get(currentFrame);

    	// Now to the command buffer stuff.
        vkResetCommandBuffer(currentCommandBuffer, 0);
        final int imageIndex = this.currentImageIndex;

        try(MemoryStack stack = stackPush()) {

	        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
	        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

	        renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
	        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);

	        renderPassInfo.renderPass(renderPass);

	        VkRect2D renderArea = VkRect2D.calloc(stack);
	        renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
	        renderArea.extent(vkbase.swapChainExtent);
	        renderPassInfo.renderArea(renderArea);

	        VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
	        clearValues.get(0).color().float32(stack.floats(0.8f, 0.8f, 0.8f, 1.0f));
          clearValues.get(1).depthStencil().set(1.0f, 0);
	        renderPassInfo.pClearValues(clearValues);

            if(vkBeginCommandBuffer(currentCommandBuffer, beginInfo) != VK_SUCCESS) {
                throw new RuntimeException("Failed to begin recording command buffer");
            }


            renderPassInfo.framebuffer(swapChainFramebuffers.get(imageIndex));

            vkCmdBeginRenderPass(currentCommandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

//            vkCmdBindPipeline(currentCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
        }

        // And then begin our thread nodes (secondary command buffers)
        // TODO: other thread nodes
    	for (ThreadNode n : threadNodes) {
    		n.beginRecord(currentFrame, imageIndex);
    	}

    }


    public void endRecord() {
    	// Before we can end recording, we need to think about our secondary command buffers


    	for (ThreadNode n : threadNodes) {
	    	n.endRecord();
    	}
    	for (ThreadNode n : threadNodes) {
    		n.await();
    	}

    	// TODO: TEST NODE
    	try(MemoryStack stack = stackPush()) {
    		// TODO: avoid garbage collection by making it assign list only once.
	    	List<VkCommandBuffer> cmdbuffers = new ArrayList<>();

	    	for (ThreadNode n : threadNodes) {
	    		cmdbuffers.add(n.getBuffer());
	    	}

	    	vkCmdExecuteCommands(currentCommandBuffer, Util.asPointerBuffer(stack, cmdbuffers));
    	}

        vkCmdEndRenderPass(currentCommandBuffer);

        if(vkEndCommandBuffer(currentCommandBuffer) != VK_SUCCESS) {
            throw new RuntimeException("Failed to record command buffer");
        }

        submitAndPresent();
    }

    public void selectNode(int node) {
    	selectedNode = node;
    }

    public void updateNodePipeline(long pipeline) {
    	if (pipeline != threadNodes[selectedNode].currentPipeline) {
        	threadNodes[selectedNode].bindPipeline(pipeline);
    	}
    }

    public void setMaxNodes(int v) {
      createThreadNodes(v);
    }

    public int getNodesCount() {
    	return threadNodes.length;
    }


    public void copyBufferFast(VkCommandBuffer cmdbuffer, long srcBuffer, long dstBuffer, long size) {
        try(MemoryStack stack = stackPush()) {
            {
                VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
                copyRegion.size(size);
                vkCmdCopyBuffer(cmdbuffer, srcBuffer, dstBuffer, copyRegion);
            }
        }
    }

    public void submitAndPresent() {
        try(MemoryStack stack = stackPush()) {

            Frame thisFrame = inFlightFrames.get(currentFrame);

	    	// Queue submission (to be carried out after recording a command buffer)
	        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
	        submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

	        submitInfo.waitSemaphoreCount(1);
	        submitInfo.pWaitSemaphores(thisFrame.pImageAvailableSemaphore());
	        submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

	        submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());

	        submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(currentFrame)));

	        vkResetFences(device, thisFrame.pFence());

	        int vkResult = 0;
	        if((vkResult = vkQueueSubmit(vkbase.graphicsQueue, submitInfo, thisFrame.fence())) != VK_SUCCESS) {
	            vkResetFences(device, thisFrame.pFence());
	            throw new RuntimeException("Failed to submit draw command buffer: " + vkResult);
	        }

	        // Presenting image from swapchain (to be done after submission)
	        // and also some waiting.
	        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
	        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

	        presentInfo.pWaitSemaphores(thisFrame.pRenderFinishedSemaphore());

	        presentInfo.swapchainCount(1);
	        presentInfo.pSwapchains(stack.longs(vkbase.swapChain));

	        IntBuffer imgIndexBuffer = stack.mallocInt(1);
	        imgIndexBuffer.put(0, currentImageIndex);
	        presentInfo.pImageIndices(imgIndexBuffer);

	        vkResult = vkQueuePresentKHR(vkbase.presentQueue, presentInfo);

	        // Window resizing
	        if(vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR || framebufferResize) {
	            framebufferResize = false;
	            recreateSwapChain();
	        } else if(vkResult != VK_SUCCESS) {
	            throw new RuntimeException("Failed to present swap chain image");
	        }

	        // update current frame.
	        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
        }
    }

    public int getFrame() {
      return currentFrame;
    }



    public void cleanupSwapChain() {
        swapChainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));
        try(MemoryStack stack = stackPush()) {vkFreeCommandBuffers(device, vkbase.commandPool, Util.asPointerBuffer(stack, commandBuffers));}

        vkDestroyRenderPass(device, renderPass, null);

        vkbase.swapChainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));

        vkDestroySwapchainKHR(device, vkbase.swapChain, null);

        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);
    }



    ////////////////
    // NODE COMMANDS
    ////////////////
    public void nodeDrawArrays(ArrayList<Long> buffers, int size, int first) {
    	threadNodes[selectedNode].drawArrays(buffers, size, first);
    }

    public void nodeBindPipeline(long pipeline) {
      threadNodes[selectedNode].bindPipeline(pipeline);
    }

    public void nodeBindDescriptorSet(long pipelineLayout, long descriptorSet) {
      threadNodes[selectedNode].bindDescriptorSet(pipelineLayout, descriptorSet);
    }

    public void nodeDrawIndexed(int indiciesSize, long indiciesBuffer, ArrayList<Long> vertexBuffers, int offset, int vertexOffset, int type) {
//      System.out.println("Using thread "+selectedNode);
    	threadNodes[selectedNode].drawIndexed(indiciesSize, indiciesBuffer, vertexBuffers, offset, vertexOffset, type);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, ByteBuffer buffer) {
    	// We need a size because the buffer must be in multiples of 8, but we may have a half-filled long of one float.
    	threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, buffer); // TODO
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, FloatBuffer buffer) {
      // We need a size because the buffer must be in multiples of 8, but we may have a half-filled long of one float.
      threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, buffer);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, float val) {
    	threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, float val0, float val1) {
    	threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val0, val1);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, float val0, float val1, float val2) {
    	threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val0, val1, val2);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, float val0, float val1, float val2, float val3) {
    	threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val0, val1, val2, val3);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, int val) {
      threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, int val0, int val1) {
      threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val0, val1);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, int val0, int val1, int val2) {
      threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val0, val1, val2);
    }

    public void nodePushConstants(long pipelineLayout, int vertexOfFragment, int offset, int val0, int val1, int val2, int val3) {
      threadNodes[selectedNode].pushConstant(pipelineLayout, vertexOfFragment, offset, val0, val1, val2, val3);
    }

    public void nodeBufferData(GraphicsBuffer graphicsBuffer, int size, ByteBuffer buffer, int instance) {
      threadNodes[selectedNode].bufferData(graphicsBuffer, size, buffer, instance);
    }

    public void nodeBufferData(GraphicsBuffer graphicsBuffer, int size, FloatBuffer buffer, int instance) {
      threadNodes[selectedNode].bufferData(graphicsBuffer, size, buffer, instance);
    }

    public void nodeBufferData(GraphicsBuffer graphicsBuffer, int size, LongBuffer buffer, int instance) {
      threadNodes[selectedNode].bufferData(graphicsBuffer, size, buffer, instance);
    }

    public void nodeBufferData(GraphicsBuffer graphicsBuffer, int size, ShortBuffer buffer, int instance) {
      threadNodes[selectedNode].bufferData(graphicsBuffer, size, buffer, instance);
    }

    public void nodeBufferData(GraphicsBuffer graphicsBuffer, int size, IntBuffer buffer, int instance) {
      threadNodes[selectedNode].bufferData(graphicsBuffer, size, buffer, instance);
    }

    public void nodeClearColor(float r, float g, float b, float a) {
      threadNodes[selectedNode].clearColor(r, g, b, a);
    }







    /////////////////////////////////////////////////
    /////////////////////////////////////////////////



}