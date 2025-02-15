package processing.GL2VK;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT16;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VkPhysicalDeviceIndexTypeUint8FeaturesKHR.INDEXTYPEUINT8;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkResetCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkCmdClearAttachments;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkClearRect;

//import helloVulkan.VKSetup.QueueFamilyIndices;

// TODO: time active vs. time idle query information
// TODO: average % of cmdQueue used in a frame.

public class ThreadNode {
	final static boolean DEBUG = false;

	// ThreadNode commands
	public final static int NO_CMD = 0;
	public final static int CMD_DRAW_ARRAYS = 1;
	public final static int CMD_DRAW_INDEXED = 2;
	public final static int CMD_BEGIN_RECORD = 3;
	public final static int CMD_END_RECORD = 4;
	public final static int CMD_KILL = 5;
	public final static int CMD_BUFFER_DATA = 6;
	public final static int CMD_BIND_PIPELINE = 7;
	public final static int CMD_PUSH_CONSTANT = 8;
  public final static int CMD_BUFFER_FLOAT_DATA = 9;
  public final static int CMD_BUFFER_BYTE_DATA = 10;
  public final static int CMD_BUFFER_SHORT_DATA = 11;
  public final static int CMD_BUFFER_LONG_DATA = 12;
  public final static int CMD_BUFFER_INT_DATA = 13;
  public final static int CMD_CLEAR = 14;
  public final static int CMD_BIND_DESCRIPTOR = 15;

	// ThreadNode state statuses
	public final static int STATE_INACTIVE = 0;
	public final static int STATE_SLEEPING = 1;
	public final static int STATE_RUNNING = 2;
	public final static int STATE_ENTERING_SLEEP = 3;
	public final static int STATE_KILLED = 4;
	public final static int STATE_WAKING = 5;
	public final static int STATE_SLEEPING_INTERRUPTED = 6;
	public final static int STATE_NEXT_CMD = 7;
	public final static int STATE_LINGERING = 8;
	public final static int STATE_LINGERING_LAST_CHANCE = 9;


	// CURRENT BUGS:
	// BUG WARNING signalled out of sleep with no work available.
	// Let's say we're calling endCommands:
	// - (1) executing cmd
	// - (0) Set cmd id
	// - (1) Oh look! A new command to execute.
	// - (1) Done, let's go to sleep
	// - (0) wakeThread (we're under the assumption that the thread hasn't done the work yet)
	// - (1) Woke up, but wait! There isn't any work for me to do!
	// Solution: check cmd is set to 0 or not

	// Other bug:
	// looplock'd waiting for a thread that won't respond (state 1)
	//

	// To avoid clashing from the main thread accessing the front of the queue while the
	// other thread is accessing the end of the queue, best solution is to make this big
	// enough lol.
	protected final static int MAX_QUEUE_LENGTH = 100000;

	// In order to avoid expensive interrupt calls waking a thread up from sleep, we can
	// busy loop for a little while after we have no more tasks to do. If we get another task
	// during this lingering period, great! We avoided an expensive interrupt() call. If not,
	// then we go to sleep, and eventually get woken up by interrupt().
	// How long does this linger time last?
	// Well it's up to you, this value is in microseconds.
  protected final static long LINGER_TIME = 500L;

	protected VulkanSystem system;
	protected VKSetup vkbase;
	protected int myID = 0;

	protected VkCommandBuffer[] cmdbuffers;

	// NOT to be set by main thread
	protected AtomicInteger currentFrame = new AtomicInteger(0);
	protected AtomicInteger currentImage = new AtomicInteger(0);
	protected long commandPool;

	protected AtomicInteger threadState = new AtomicInteger(STATE_INACTIVE);
	protected AtomicBoolean openCmdBuffer = new AtomicBoolean(false);

	// Read-only begin info for beginning our recording of commands
	// (what am i even typing i need sleep)
	// One for each frames in flight
	protected VkCommandBufferBeginInfo[] beginInfos;
	// Just to keep it from being garbage collected or something
	private VkCommandBufferInheritanceInfo[] inheritanceInfos;


	// There are two seperate indexes, one for our thread (main thread) and one for this thread.
	// We add item to queue (cmdindex = 0 -> 1) and then eventually thread updates its own index
	// as it works on cmd   (myIndex  = 0 -> 1)
	protected int cmdindex = 0;  // Start at one because thread will have already consumed index 0
	protected Thread thread;

	// Accessed by 2 threads so volatile (i was told that volatile avoids outdated caching issues)
	// (source: the internet, the most truthful place, i think)
//	private volatile CMD[] cmdqueue;


	// INNER COMMAND TYPES
	// Originally was gonna create some classes which extend this class,
	// but this would mean garbage collection for each command we call.
	// The other option is to put all arguments from every command into
	// the one cmd class, which isn't the most readable or memory efficient,
	// but we care about going FAST.
	protected AtomicIntegerArray cmdID = new AtomicIntegerArray(MAX_QUEUE_LENGTH);
	protected AtomicLongArray[] cmdLongArgs = new AtomicLongArray[128];
	protected AtomicIntegerArray[] cmdIntArgs = new AtomicIntegerArray[128];
	public long currentPipeline = 0L;

	private static final int MAX_BUFFERS = 4096;

	protected int floatBuffersIndex = 0;
  protected int shortBuffersIndex = 0;
  protected int byteBuffersIndex = 0;
  protected int longBuffersIndex = 0;
  protected int intBuffersIndex = 0;
  protected int graphicsBufferIndex = 0;
  protected GraphicsBuffer[] graphicsBuffers = new GraphicsBuffer[MAX_BUFFERS];
	protected FloatBuffer[] floatBuffers = new FloatBuffer[MAX_BUFFERS];
  protected ShortBuffer[] shortBuffers = new ShortBuffer[MAX_BUFFERS];
  protected IntBuffer[]   intBuffers = new IntBuffer[MAX_BUFFERS];
  protected ByteBuffer[]  byteBuffers  = new ByteBuffer[MAX_BUFFERS];
  protected LongBuffer[]  longBuffers  = new LongBuffer[MAX_BUFFERS];

	// Some benchmarking variables.
	private static int  interruptsCalled = 0;
	private static long interruptPenalty = 0L;
  private static long oddStateStallPenalty = 0L;
  private static long threadFinishWait = 0L;
  private static long fullQueueStallPenalty = 0L;



	public ThreadNode(VulkanSystem vk, int id) {
		// Variables setup
		system = vk;
		vkbase = vk.vkbase;
		myID = id;

		// Initialise cmdqueue and all its objects
		for (int i = 0; i < MAX_QUEUE_LENGTH; i++) {
			cmdID.set(i, 0);
		}

		createObjects();
		startThread();
	}


	protected void println(String message) {
		if (DEBUG) {
			System.out.println("("+myID+") "+message);
		}
	}

	private void createObjects() {
		// Create command pool
        try(MemoryStack stack = stackPush()) {
	        VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
	        poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
	        poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
	        poolInfo.queueFamilyIndex(vkbase.queueIndicies.graphicsFamily);

	        // create our command pool vk
	        LongBuffer pCommandPool = stack.mallocLong(1);
	        if (vkCreateCommandPool(vkbase.device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
	            throw new RuntimeException("Failed to create command pool");
	        };
	        commandPool = pCommandPool.get(0);
        }

        final int commandBuffersCount = VulkanSystem.MAX_FRAMES_IN_FLIGHT;

        // Create secondary command buffer
        try(MemoryStack stack = stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.commandPool(commandPool);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_SECONDARY);
            allocInfo.commandBufferCount(commandBuffersCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);

            if(vkAllocateCommandBuffers(vkbase.device, allocInfo, pCommandBuffers) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers");
            }

            cmdbuffers = new VkCommandBuffer[commandBuffersCount];
            for(int i = 0; i < commandBuffersCount; i++) {
            	cmdbuffers[i] = new VkCommandBuffer(pCommandBuffers.get(i), vkbase.device);
            }
        }

        int imagesSize = system.swapChainFramebuffers.size();
        // Create readonly beginInfo structs.
        beginInfos = new VkCommandBufferBeginInfo[imagesSize];
        inheritanceInfos = new VkCommandBufferInheritanceInfo[imagesSize];
        for(int i = 0; i < imagesSize; i++) {
//        	 Inheritance because for some reason we need that
	        inheritanceInfos[i] = VkCommandBufferInheritanceInfo.create();
	        inheritanceInfos[i].sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
			inheritanceInfos[i].renderPass(system.renderPass);
			// Secondary command buffer also use the currently active framebuffer
			inheritanceInfos[i].framebuffer(system.swapChainFramebuffers.get(i));

			beginInfos[i] = VkCommandBufferBeginInfo.create();
			beginInfos[i].sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			beginInfos[i].flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
			beginInfos[i].pInheritanceInfo(inheritanceInfos[i]);
        }
	}

	protected void startThread() {
		// Inside of thread, we run logic which checks for items in the queue
		// and then executes the vk commands that correspond to the int
		thread = new Thread(new Runnable() {
	          public void run() {
	        	  int myIndex = 0;

        		  long sleepTime = 0L;
        		  long runTime = 0L;

        		  boolean pipelineBound = false;

        		  long lingerTimer = 0;
        		  long lingerTimestampBefore = 0L;

        		  ByteBuffer pushConstantBuffer = null;

	        	  // Loop until receive KILL_THREAD cmd
	        	  while (true) {
    				  long runbefore = System.nanoTime();
	        		  boolean goToSleepMode = false;
	        		  boolean kill = false;

	        		  int index = (myIndex++)%MAX_QUEUE_LENGTH;

	        		  VkCommandBuffer cmdbuffer = cmdbuffers[currentFrame.get()];


//	        		  if (threadState.get() == STATE_WAKING) {
//	        			  if (id == NO_CMD) System.err.println(myID+" NO_CMD warning");
//        				  threadState.set(STATE_RUNNING);
//	        		  }
//	        		  Util.beginTmr();


	        		  // ======================
	        		  // CMD EXECUTOR
	        		  // ======================
	        		  switch (cmdID.getAndSet(index, 0)) {
	        		  case NO_CMD:
	        		    // Only go to sleep if we've been lingering for long enough.
	        		    if ((lingerTimer/1000L) < LINGER_TIME) {
	        		      // Keep running
                    threadState.set(STATE_LINGERING);
	        		      lingerTimer += System.nanoTime()-lingerTimestampBefore;
                    lingerTimestampBefore = System.nanoTime();

                    if ((lingerTimer/1000L) >= LINGER_TIME) {
                      threadState.set(STATE_LINGERING_LAST_CHANCE);
                    }
	        		    }
	        		    else {
//                    System.out.println("GOING TO SLEEP");
	                  threadState.set(STATE_ENTERING_SLEEP);
	                  goToSleepMode = true;
	        		    }

	        			  myIndex--;
	        			  break;
	        		  case CMD_DRAW_ARRAYS: {
	        			  threadState.set(STATE_RUNNING);
	        			  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

	        			  println("CMD_DRAW_ARRAYS (index "+index+")");
	        			  int size = cmdIntArgs[0].get(index);
	        			  int first = cmdIntArgs[1].get(index);
	        			  int numBuffers = cmdIntArgs[2].get(index);

	        			  try(MemoryStack stack = stackPush()) {
	        			      LongBuffer vertexBuffers = stack.callocLong(numBuffers);
	        			      LongBuffer offsets = stack.callocLong(numBuffers);

	        			      // Longargs 1-x are buffers.
	        			      for (int i = 0; i < numBuffers; i++) {
	        			    	  vertexBuffers.put(i, cmdLongArgs[i].get(index));
		        			      offsets.put(i, 0);
	        			      }
	        			      vkCmdBindVertexBuffers(cmdbuffer, 0, vertexBuffers, offsets);

	        			      vkCmdDraw(cmdbuffer, size, 1, first, 0);
	        		      }
	        			  break;
	        		  }
	        			  // Probably the most important command
	        		  case CMD_BEGIN_RECORD:
	        			  	threadState.set(STATE_RUNNING);
	                  lingerTimer = 0L;
                    lingerTimestampBefore = System.nanoTime();

	        			  	sleepTime = 0;
	        			  	runTime = 0;
	        			  	println("CMD_BEGIN_RECORD");

        			  		currentImage.set(cmdIntArgs[0].get(index));
        			  		currentFrame.set(cmdIntArgs[1].get(index));
        			  		cmdbuffer = cmdbuffers[currentFrame.get()];

	        			  	if (openCmdBuffer.get() == false) {
	        			  		vkResetCommandBuffer(cmdbuffer, 0);
		        		      	// Begin recording

		        		      	// In case you're wondering, beginInfo index is currentImage, not
		        		      	// the currentFrame, because it holds which framebuffer (image) we're
		        		      	// using (confusing i know)
		        	            if(vkBeginCommandBuffer(cmdbuffer, beginInfos[currentImage.get()]) != VK_SUCCESS) {
		        	                throw new RuntimeException("Failed to begin recording command buffer");
		        	            }
	        			  	}
	        			  	// Bug detected
	        			  	else System.err.println("("+myID+") Attempt to begin an already open command buffer.");

	        	            openCmdBuffer.set(true);
//	        	            vkCmdBindPipeline(cmdbuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, system.graphicsPipeline);
	        	            pipelineBound = false;
	        	            break;
	        		  case CMD_END_RECORD:
	        			  	threadState.set(STATE_RUNNING);
	                  lingerTimer = 0L;
                    lingerTimestampBefore = System.nanoTime();

	        			  	println("CMD_END_RECORD (index "+index+")");

	        			  	if (openCmdBuffer.get() == true) {
							    if(vkEndCommandBuffer(cmdbuffer) != VK_SUCCESS) {
							        throw new RuntimeException("Failed to record command buffer");
							    }
	        			  	}
	        			  	else System.err.println("("+myID+") Attempt to close an already closed command buffer.");

	        	            openCmdBuffer.set(false);
	        	            // We should also really go into sleep mode now
	        	            threadState.set(STATE_ENTERING_SLEEP);
	        	            goToSleepMode = true;

	        	            // RATIO
//	        	            System.out.println("("+myID+") Sleep time "+(sleepTime/1000L)+"us  Run time "+(runTime/1000L)+"us");

//	        	            System.out.println("("+myID+") Active-to-sleep ratio "+(int)((((double)runTime)/((double)(runTime+sleepTime)))*100d)+"%");

						    break;
	        		  case CMD_KILL:
	        			  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

	        			  goToSleepMode = false;
	        			  kill = true;
	        			  break;

	        		  // This goes pretty much unused.
	        		  case CMD_BUFFER_DATA:
	        			  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
	        			  println("CMD_BUFFER_DATA (index "+index+")");

//	        			  vkCmdEndRenderPass(system.currentCommandBuffer);
	        			  system.copyBufferFast(cmdbuffer, cmdLongArgs[0].get(index), cmdLongArgs[1].get(index), cmdIntArgs[0].get(index));
//	        			  vkCmdBeginRenderPass(system.currentCommandBuffer, system.renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
	        			  break;

	        		  case CMD_BIND_PIPELINE: {
	        			  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

	        			  println("CMD_BIND_PIPELINE (index "+index+")");

	        			  long pipeline = cmdLongArgs[0].get(index);

      	          vkCmdBindPipeline(cmdbuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
	        			  break;
	        		  }

	        		  case CMD_BIND_DESCRIPTOR: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

                  println("CMD_BIND_DESCRIPTOR (index "+index+")");

                  long pipelineLayout = cmdLongArgs[0].get(index);
                  long descriptorSet = cmdLongArgs[1].get(index);

                  try(MemoryStack stack = stackPush()) {
                    vkCmdBindDescriptorSets(cmdbuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                            pipelineLayout, 0, stack.longs(descriptorSet), null);
                  }
	        		    break;
	        		  }


	        		  case CMD_DRAW_INDEXED: {
	        			  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

	        			  println("CMD_DRAW_INDEXED (index "+index+")");
	        			  // Int0: indiciesSize
	        			  // Long0: indiciesBuffer
	        			  // Int1: numBuffers
	        			  // LongX: vertexBuffers
	        			  // Int2: offset

	        			  int indiciesSize = cmdIntArgs[0].get(index);
	        			  int numBuffers = cmdIntArgs[1].get(index);
	        			  long indicesBuffer = cmdLongArgs[0].get(index);

	        			  int type   = cmdIntArgs[2].get(index);
                  int offset = cmdIntArgs[3].get(index);
                  int vertexOffset =cmdIntArgs[4].get(index);



//                  System.out.println("expected type: "+VK_INDEX_TYPE_UINT16+"  type: "+vkType+"  offset: "+offset+"  indicesSize: "+indiciesSize+"  numBuffers: "+numBuffers+"  indicesBuffer: "+indicesBuffer);


//                  long before = System.nanoTime();
	        			  try(MemoryStack stack = stackPush()) {
	        			      LongBuffer vertexBuffers = stack.callocLong(numBuffers);
	        			      LongBuffer offsets = stack.callocLong(numBuffers);


	        			      // Longargs 1-x are buffers.
	        			      for (int i = 0; i < numBuffers; i++) {
	        			    	  vertexBuffers.put(i, cmdLongArgs[i+1].get(index));
		        			      offsets.put(i, 0);
	        			      }
	        			      vertexBuffers.rewind();
	        			      offsets.rewind();

	        			      vkCmdBindVertexBuffers(cmdbuffer, 0, vertexBuffers, offsets);

//                      vkCmdBindIndexBuffer(cmdbuffer, indicesBuffer, offset, type);
//                      vkCmdDrawIndexed(cmdbuffer, indiciesSize, 1, 0, 0, 0);
	        			      vkCmdBindIndexBuffer(cmdbuffer, indicesBuffer, offset, type);
	        			      vkCmdDrawIndexed(cmdbuffer, indiciesSize, 1, 0, vertexOffset, 0);
	        			  }
//	        			  System.out.println("drawindexed elapsed time "+(System.nanoTime()-before)+"ns");

	        			  break;
	        		  }
	        		  case CMD_PUSH_CONSTANT: {
	        			  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

	        			  println("CMD_PUSH_CONSTANT (index "+index+")");
	        			  // Long0:   pipelineLayout
	        			  // Int0:    Size/offset/vertexOrFragment
	        			  // Long1-X: bufferData (needs to be reconstructed

	        			  long pipelineLayout = cmdLongArgs[0].get(index);

	        			  // Layout: ssssssssoooooooooooooooovvvvvvvv
	        			  int arg0 = cmdIntArgs[0].get(index);
	        			  int size             = ((arg0 >> 24) & 0x000000FF);
	        			  int offset           = ((arg0 >> 8) & 0x0000FFFF);
	        			  int vertexOrFragment = (arg0 & 0x000000FF);

	        			  if (size <= 0) {
	        			    System.err.println("CONCURRENCY ISSUE");
                    System.err.println("arg0 "+Integer.toHexString(arg0));
                    System.err.println("size "+size);
                    System.err.println("offset "+offset);
                    System.err.println("vertexOrFragment "+vertexOrFragment);
	        			  }

	        			  // Needs to be in multiples of 8 for long buffer.
	        			  // TODO: actually sort out.
	        			  // buffer size = 12, we need to expand to 16.
	        			  // This equasion would set it to 8.
//	        			  size = ((size/8))*8;

	        			  // Create buffer
	        			  // It's a gobal variable so we don't have to recreate the variable each
	        			  // time
	        			  if (pushConstantBuffer == null || pushConstantBuffer.capacity() != size) {
	        				  pushConstantBuffer = BufferUtils.createByteBuffer(size);
	        				  pushConstantBuffer.order(ByteOrder.LITTLE_ENDIAN);
	        			  }

	        			  // Now we have to reconstruct the buffer from the longs
	        			  pushConstantBuffer.rewind();
	        			  int arg = 1;

	        			  // Long story short, we might store 8 bytes in a 4-byte buffer,
	        			  // which would cause an exception.
	        			  // We have a special case to write the bytes in multiples of 8,
	        			  // then write the remaining 4 bytes
	        			  int ssize = size;
	        			  if (size % 8 == 4) ssize -= 4;
	        			  for (int i = 0; i < ssize; i += Long.BYTES) {
	        				  pushConstantBuffer.putLong(cmdLongArgs[arg++].get(index));
	        			  }
	        			  if (size % 8 == 4) {
	        				  int val = (int)cmdLongArgs[arg++].get(index);
	        				  pushConstantBuffer.putInt(val);
	        			  }
	        			  pushConstantBuffer.rewind();


	        			  // Get the vk type
	        			  int vkType = 0;
	        			  if (vertexOrFragment == GLUniform.VERTEX) {
	        				  vkType = VK_SHADER_STAGE_VERTEX_BIT;
	        			  }
	        			  else if (vertexOrFragment == GLUniform.FRAGMENT) {
	        				  vkType = VK_SHADER_STAGE_FRAGMENT_BIT;
	        			  }
	        			  else {
	        				  vkType = VK_SHADER_STAGE_VERTEX_BIT;
	        			  }

	        			  vkCmdPushConstants(cmdbuffer, pipelineLayout, vkType, offset, pushConstantBuffer);
	        		  }
	        			  break;
	        		  case CMD_BUFFER_FLOAT_DATA: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();


	        		    int size = cmdIntArgs[0].get(index);
                  int bufferIndex = cmdIntArgs[1].get(index);
                  int instance = cmdIntArgs[2].get(index);
                  int graphicsIndex = cmdIntArgs[3].get(index);

	        		    graphicsBuffers[graphicsIndex].bufferDataImmediate(floatBuffers[bufferIndex], size, instance);
                  break;
	        		  }
                case CMD_BUFFER_BYTE_DATA: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

                  int size = cmdIntArgs[0].get(index);
                  int bufferIndex = cmdIntArgs[1].get(index);
                  int instance = cmdIntArgs[2].get(index);
                  int graphicsIndex = cmdIntArgs[3].get(index);

                  graphicsBuffers[graphicsIndex].bufferDataImmediate(byteBuffers[bufferIndex], size, instance);
                  break;
                }
                case CMD_BUFFER_SHORT_DATA: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

                  int size = cmdIntArgs[0].get(index);
                  int bufferIndex = cmdIntArgs[1].get(index);
                  int instance = cmdIntArgs[2].get(index);
                  int graphicsIndex = cmdIntArgs[3].get(index);

                  graphicsBuffers[graphicsIndex].bufferDataImmediate(shortBuffers[bufferIndex], size, instance);
                  break;
                }
                case CMD_BUFFER_INT_DATA: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

                  int size = cmdIntArgs[0].get(index);
                  int bufferIndex = cmdIntArgs[1].get(index);
                  int instance = cmdIntArgs[2].get(index);
                  int graphicsIndex = cmdIntArgs[3].get(index);

                  graphicsBuffers[graphicsIndex].bufferDataImmediate(intBuffers[bufferIndex], size, instance);
                  break;
                }
                case CMD_BUFFER_LONG_DATA: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

                  int size = cmdIntArgs[0].get(index);
                  int bufferIndex = cmdIntArgs[1].get(index);
                  int instance = cmdIntArgs[2].get(index);
                  int graphicsIndex = cmdIntArgs[3].get(index);

                  graphicsBuffers[graphicsIndex].bufferDataImmediate(longBuffers[bufferIndex], size, instance);
                  break;
                }
                case CMD_CLEAR: {
                  threadState.set(STATE_RUNNING);
                  lingerTimer = 0L;
                  lingerTimestampBefore = System.nanoTime();

                  try(MemoryStack stack = stackPush()) {
                    VkClearAttachment.Buffer attachments = VkClearAttachment.calloc(1, stack);
                    attachments.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

                    float r = Float.intBitsToFloat(cmdIntArgs[0].get(index));
                    float g = Float.intBitsToFloat(cmdIntArgs[1].get(index));
                    float b = Float.intBitsToFloat(cmdIntArgs[2].get(index));
                    float a = Float.intBitsToFloat(cmdIntArgs[3].get(index));

                    VkClearValue clearValues = VkClearValue.calloc(stack);
                    clearValues.color().float32(stack.floats(r,g,b,a));
                    attachments.clearValue(clearValues);

                    VkRect2D rect = VkRect2D.calloc(stack);
                    rect.offset(VkOffset2D.calloc(stack).set(0, 0));
                    rect.extent(vkbase.swapChainExtent);

                    VkClearRect.Buffer clearRect = VkClearRect.calloc(1, stack);
                    clearRect.rect(rect); // Size of the area to clear
                    clearRect.baseArrayLayer(0); // For 2D images
                    clearRect.layerCount(1); // Clear one layer

                    vkCmdClearAttachments(cmdbuffer, attachments, clearRect);
                  }
                }


	        		  }

	        		  // ======================


	        		  if (kill) {
	        			  threadState.set(STATE_KILLED);
	        			  // Kills the thread
	        			  break;
	        		  }

	        		  runTime += System.nanoTime()-runbefore;
	        		  // No more tasks to do? Take a lil nap.
	        		  if (goToSleepMode) {
	        			  println("NOW SLEEPING");
        				  long before = System.nanoTime();
	        			  try {
	        				  // Sleep for an indefinite amount of time
	        				  // (we gonna interrupt the thread later)
	        				  threadState.set(STATE_SLEEPING);
	        				  Thread.sleep(999999);
	        			  }
	        			  catch (InterruptedException e) {
	        				  threadState.set(STATE_WAKING);
	                  lingerTimer = 0;
	        				  println("WAKEUP");
	        			  }
        				  sleepTime += System.nanoTime()-before;
	        		  }
	        	  }
	        	  threadState.set(STATE_KILLED);

	          }
		}
		);
		thread.start();
	}
	int stalltime = 0;

	private int getNextCMDIndex() {
		int ret = cmdindex;

		long before = System.nanoTime();
		int stallloopcounts = 0;
		while (cmdID.get(ret) != NO_CMD) {
			// We're forced to wait until the thread has caught up with some of the queue
//			try {
//				Thread.sleep(0);
//			} catch (InterruptedException e) {
//			}
//			println("WARNING  queue clash, cmdid is "+ret);
		  stallloopcounts++;
		}
		long stall = System.nanoTime()-before;
		if (stall > 100) fullQueueStallPenalty += stall;
		cmdindex++;
		if (cmdindex >= MAX_QUEUE_LENGTH) {
		  cmdindex = 0;
		}
		return ret;
	}

	private void setIntArg(int argIndex, int queueIndex, int value) {
		if (cmdIntArgs[argIndex] == null) {
			cmdIntArgs[argIndex] = new AtomicIntegerArray(MAX_QUEUE_LENGTH);
		}
		cmdIntArgs[argIndex].set(queueIndex, value);
	}

	private void setLongArg(int argIndex, int queueIndex, long value) {
		if (cmdLongArgs[argIndex] == null) {
			cmdLongArgs[argIndex] = new AtomicLongArray(MAX_QUEUE_LENGTH);
		}
		cmdLongArgs[argIndex].set(queueIndex, value);
	}


  public static void getTimedReport() {
    System.out.println("------------------------");

    System.out.println("Interrupts called         "+interruptsCalled);
    System.out.println("Interrupt penalty         "+(interruptPenalty/1000L)+"us");
    System.out.println("Unlucky stall penalty     "+(oddStateStallPenalty/1000L)+"us");
    System.out.println("Full queue stall penalty  "+(fullQueueStallPenalty/1000L)+"us");
    System.out.println("Finish up wait            "+(threadFinishWait/1000L)+"us");

    // You should only call this method once lol.
    interruptsCalled = 0;
    interruptPenalty = 0L;
    oddStateStallPenalty = 0L;
    threadFinishWait = 0L;
    fullQueueStallPenalty = 0L;
  }


	protected void wakeThread(int cmdindex) {

	  // Thread's already awake if we're lingering. Skip the interrupt() call!
	  if (threadState.get() == STATE_LINGERING) {
	    return;
	  }

		// There's a bug if we just call wakethread after setting cmdIndex.
		// I'm going to copy+paste it here:
		// Let's say we're calling endCommands:
		// - (1) executing cmd
		// - (0) Set cmd id
		// - (1) Oh look! A new command to execute.
		// - (1) Done, let's go to sleep
		// - (0) wakeThread (we're under the assumption that the thread hasn't done the work yet)
		// - (1) Woke up, but wait! There isn't any work for me to do!
		// Solution: Check cmdid is non-zero, because thread sets it to 0 as soon as it executes
		// the command
		if (cmdID.get(cmdindex) == NO_CMD) {
			return;
		}



		// Only need to interrupt if sleeping.
		// We call it here because if wakeThread is called, then a command was called, and
		// when a command was called, that means we should definitely not be asleep
		// (avoids concurrency issues with await()
		// If it's on STATE_NEXT_CMD, it means that it might have an outdated cmdid, which we can fix
		// by simply interrupting it as soon as it eventually goes into sleep mode

		// NOTE: With the new lingering mode, it means that STATE_NEXT_CMD is deprecated.

		// Here's the new solution:
		// If we're on the verge of going from lingering to entering state, we must wait for either 1 of 2 outcomes:
		// - worker thread has read the updated cmdid and will update its state to STATE_RUNNING, hence we can continue
		//   without calling interrupt()
		// OR
		// - worker thread has read an outdated state (0 NO_CMD) and is now going to sleep, so we need to
		//   call interrupt() (bummer).


		if (threadState.get() == STATE_ENTERING_SLEEP || threadState.get() == STATE_LINGERING_LAST_CHANCE) {  // || threadState.get() == STATE_NEXT_CMD

			// Uhoh, unlucky. This means we just gotta wait until we're entering sleep state then wake up.
		  long before = System.nanoTime();
			while (threadState.get() != STATE_SLEEPING && threadState.get() != STATE_RUNNING) {
				// Busy loop
			}
			oddStateStallPenalty += System.nanoTime()-before;

			// Running? We can happily continue and skip the interrupt() :D
			if (threadState.get() == STATE_RUNNING) {
			  return;
			}

			println("INTERRUPT");
			threadState.set(STATE_SLEEPING_INTERRUPTED);

			// Before we call interrupt let's do a lil benchmarking
			before = System.nanoTime();

			// Actually call interrupt
			thread.interrupt();

			interruptPenalty += System.nanoTime()-before;
			interruptsCalled++;
		}

		if (threadState.get() == STATE_SLEEPING) {
			println("INTERRUPT");

			// We need to set status for only one interrupt otherwise we will keep calling
			// interrupt interrupt interrupt interrupt interrupt interrupt interrupt interrupt
			// and it seems to be stored in some sort of queue. That means, when the thread tries
			// to go back to sleep, it immediately wakes up because those interrupts are still in
			// the queue. We tell it "it's been interrupted once, don't bother it any further."
			threadState.set(STATE_SLEEPING_INTERRUPTED);

      // Before we call interrupt let's do a lil benchmarking
      long before = System.nanoTime();

      // Actually call interrupt
      thread.interrupt();

      interruptPenalty += System.nanoTime()-before;;
      interruptsCalled++;
		}

		// We also need to consider the case for when a thread is ABOUT to enter sleep mode.
		// Cus we can call interrupt() all we want, it's not going to stop the thread from
		// entering sleep mode.

//		sleeping.set(false);
	}


    public void drawArrays(ArrayList<Long> buffers, int size, int first) {
        int index = getNextCMDIndex();
        println("call CMD_DRAW_ARRAYS (index "+index+")");

        for (int i = 0; i < buffers.size(); i++) {
        	setLongArg(i, index, buffers.get(i));
        }

    		setIntArg(0, index, size);
    		setIntArg(1, index, first);
    		setIntArg(2, index, buffers.size());
        // Remember, last thing we should set is cmdID, set it before and
        // our thread may begin executing drawArrays without all the commands
        // being properly set.
        cmdID.set(index, CMD_DRAW_ARRAYS);
        wakeThread(index);
    }


    public void drawIndexed(int indiciesSize, long indiciesBuffer, ArrayList<Long> vertexBuffers, int offset, int vertexOffset, int type) {
//        long before = System.nanoTime();
        int index = getNextCMDIndex();
        println("call CMD_DRAW_INDEXED (index "+index+")");


		  // Int0: indiciesSize
		  // Long0: indiciesBuffer
		  // Int1: numBuffers
		  // LongX: vertexBuffers
		  // Int2: offset/type (higher bits type, lower bits offset)

        setIntArg(0, index, indiciesSize);
        setLongArg(0, index, indiciesBuffer);
        setIntArg(1, index, vertexBuffers.size());


        // Replace last 2 bits with type
        switch (type) {
        case GL2VK.GL_UNSIGNED_BYTE:
          // 1
          setIntArg(2, index, INDEXTYPEUINT8);
          break;
        case GL2VK.GL_UNSIGNED_INT:
          // 2
          setIntArg(2, index, VK_INDEX_TYPE_UINT32);
          break;
        case GL2VK.GL_UNSIGNED_SHORT:
          // 3
          setIntArg(2, index, VK_INDEX_TYPE_UINT16);
          break;
        }


        setIntArg(3, index, offset);
        setIntArg(4, index, vertexOffset);
        for (int i = 0; i < vertexBuffers.size(); i++) {
        	setLongArg(i+1, index, vertexBuffers.get(i));
        }
        cmdID.set(index, CMD_DRAW_INDEXED);
        wakeThread(index);
//        System.out.println("issue drawindexed elapsed time "+(System.nanoTime()-before)+"ns");
    }


    // TODO: what would be much more efficient and perhaps easier would be to pass
    // the literal uniform arguments e.g.
    // mat4, vec2, another vec2
    // We would need a class that contains the args we wanna pass tho.
    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, ByteBuffer buffer) {
    	int index = getNextCMDIndex();
    	println("call CMD_PUSH_CONSTANT (index "+index+")");
		  // Long0:   pipelineLayout
		  // Int0:    Size/offset/vertexOrFragment
		  // Long1-X: bufferData (needs to be reconstructed
    	setLongArg(0, index, pipelineLayout);

    	int size = buffer.capacity();

    	// Let's combine it into a single int argument to reduce memory usage
    	// Layout: ssssssssoooooooooooooooovvvvvvvv
    	// Remember that none of these should ever be bigger than their limits
    	int arg0 = 0;
    	arg0 |= size << 24;
    	arg0 |= ((offset << 8) & 0x00FFFF00);
    	arg0 |= (vertexOrFragment & 0x000000FF);
    	setIntArg(0, index, arg0);

    	// Now, we need to do the unhinged:
    	// Stuff an entire buffer into the long args.
    	// If we use the entire 256 bytes of pushConstant space,
    	// we will need 32 long args altogether so it's not too bad I guess??
    	int arg = 1;
    	for (int i = 0; i < size; i += Long.BYTES) {
    		setLongArg(arg++, index, buffer.getLong());
    	}

        cmdID.set(index, CMD_PUSH_CONSTANT);
        wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, FloatBuffer buffer) {

      int index = getNextCMDIndex();
      println("call CMD_PUSH_CONSTANT (index "+index+")");
      // Long0:   pipelineLayout
      // Int0:    Size/offset/vertexOrFragment
      // Long1-X: bufferData (needs to be reconstructed
      setLongArg(0, index, pipelineLayout);

      int size = buffer.capacity()*4;

      // Let's combine it into a single int argument to reduce memory usage
      // Layout: ssssssssoooooooooooooooovvvvvvvv
      // Remember that none of these should ever be bigger than their limits
      int arg0 = 0;
      arg0 |= size << 24;
      arg0 |= ((offset << 8) & 0x00FFFF00);
      arg0 |= (vertexOrFragment & 0x000000FF);
      setIntArg(0, index, arg0);


      // Now, we need to do the unhinged:
      // Stuff an entire buffer into the long args.
      // If we use the entire 256 bytes of pushConstant space,
      // we will need 32 long args altogether so it's not too bad I guess??
      buffer.rewind();
      int arg = 1;
      for (int i = 0; i < size; i += Long.BYTES) {
        float val1 = buffer.get();
        float val2 = buffer.get();
//        if (i+4 < size) val2 = buffer.get();

        setLongArg(arg++, index, (Float.floatToIntBits(val1) & 0xFFFFFFFFL) | ((Float.floatToIntBits(val2) & 0xFFFFFFFFL) << 32));
      }

      cmdID.set(index, CMD_PUSH_CONSTANT);
      wakeThread(index);
    }

    private int getNextIndexForPushConstant(long pipelineLayout, int vertexOrFragment, int offset, int size) {
    	int index = getNextCMDIndex();

    	println("call CMD_PUSH_CONSTANT (index "+index+")");
		  // Long0:   pipelineLayout
		  // Int0:    Size/offset/vertexOrFragment
		  // Long1-X: bufferData (needs to be reconstructed
	  	setLongArg(0, index, pipelineLayout);

	  	// Let's combine it into a single int argument to reduce memory usage
	  	// Layout: ssssssssoooooooooooooooovvvvvvvv
	  	// Remember that none of these should ever be bigger than their limits
	  	int arg0 = 0;
	  	arg0 |= size << 24;
	  	arg0 |= ((offset << 8) & 0x00FFFF00);
	  	arg0 |= (vertexOrFragment & 0x000000FF);
	  	setIntArg(0, index, arg0);

	  	return index;
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, float val) {
    		int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 4);

    		setLongArg(1, index, Float.floatToIntBits(val) & 0xFFFFFFFFL);

        cmdID.set(index, CMD_PUSH_CONSTANT);
        wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, float val0, float val1) {
    		int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 8);

    		setLongArg(1, index, (Float.floatToIntBits(val0) & 0xFFFFFFFFL) | ((Float.floatToIntBits(val1) & 0xFFFFFFFFL) << 32));

        cmdID.set(index, CMD_PUSH_CONSTANT);
        wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, float val0, float val1, float val2) {
    		int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 12);

    		setLongArg(1, index, (Float.floatToIntBits(val0) & 0xFFFFFFFFL) | ((Float.floatToIntBits(val1) & 0xFFFFFFFFL) << 32));
    		setLongArg(2, index, (Float.floatToIntBits(val2) & 0xFFFFFFFFL));

        cmdID.set(index, CMD_PUSH_CONSTANT);
        wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, float val0, float val1, float val2, float val3) {
    		int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 16);

    		setLongArg(1, index, (Float.floatToIntBits(val0) & 0xFFFFFFFFL) | ((Float.floatToIntBits(val1) & 0xFFFFFFFFL) << 32));
    		setLongArg(2, index, (Float.floatToIntBits(val2) & 0xFFFFFFFFL) | ((Float.floatToIntBits(val3) & 0xFFFFFFFFL) << 32));

        cmdID.set(index, CMD_PUSH_CONSTANT);
        wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, int val) {
      int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 4);

      setLongArg(1, index, val & 0xFFFFFFFFL);

      cmdID.set(index, CMD_PUSH_CONSTANT);
      wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, int val0, int val1) {
      int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 8);

      setLongArg(1, index, (val0 & 0xFFFFFFFFL) | ((val1 & 0xFFFFFFFFL) << 32));

      cmdID.set(index, CMD_PUSH_CONSTANT);
      wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, int val0, int val1, int val2) {
      int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 12);

      setLongArg(1, index, (val0 & 0xFFFFFFFFL) | ((val1 & 0xFFFFFFFFL) << 32));
      setLongArg(2, index, (val2 & 0xFFFFFFFFL));

      cmdID.set(index, CMD_PUSH_CONSTANT);
      wakeThread(index);
    }

    public void pushConstant(long pipelineLayout, int vertexOrFragment, int offset, int val0, int val1, int val2, int val3) {
      int index = getNextIndexForPushConstant(pipelineLayout, vertexOrFragment, offset, 16);

      setLongArg(1, index, (val0 & 0xFFFFFFFFL) | ((val1 & 0xFFFFFFFFL) << 32));
      setLongArg(2, index, (val2 & 0xFFFFFFFFL) | ((val3 & 0xFFFFFFFFL) << 32));

      cmdID.set(index, CMD_PUSH_CONSTANT);
      wakeThread(index);
    }


//    public void bufferData(long srcBuffer, long dstBuffer, int size) {
//        int index = getNextCMDIndex();
//        setLongArg(0, index, srcBuffer);
//        setLongArg(1, index, dstBuffer);
//        setIntArg(0, index, size);
//        // Remember, last thing we should set is cmdID, set it before and
//        // our thread may begin executing drawArrays without all the commands
//        // being properly set.
//        cmdID.set(index, CMD_BUFFER_DATA);
//        wakeThread(index);
//    }

    public void bindPipeline(long pipeline) {
    	if (currentPipeline != pipeline) {
	        int index = getNextCMDIndex();
    			println("call CMD_BIND_PIPELINE (index "+index+")");
    			currentPipeline = pipeline;
    			setLongArg(0, index, pipeline);
	        cmdID.set(index, CMD_BIND_PIPELINE);
	        wakeThread(index);
    	}
    }

    public void bindDescriptorSet(long pipelineLayout, long descriptorSet) {
      int index = getNextCMDIndex();
      println("call CMD_BIND_PIPELINE (index "+index+")");
      setLongArg(0, index, pipelineLayout);
      setLongArg(1, index, descriptorSet);
      cmdID.set(index, CMD_BIND_DESCRIPTOR);
      wakeThread(index);
    }


    public void bufferData(GraphicsBuffer graphicsBuffer, int size, ByteBuffer buffer, int instance) {
        int index = getNextCMDIndex();
        setIntArg(0, index, size);
        byteBuffers[byteBuffersIndex] = buffer;
        graphicsBuffers[graphicsBufferIndex] = graphicsBuffer;
        setIntArg(1, index, byteBuffersIndex++);
        setIntArg(2, index, instance);
        setIntArg(3, index, graphicsBufferIndex++);

        cmdID.set(index, CMD_BUFFER_BYTE_DATA);
        wakeThread(index);
    }


    public void bufferData(GraphicsBuffer graphicsBuffer, int size, FloatBuffer buffer, int instance) {

        int index = getNextCMDIndex();
        setIntArg(0, index, size);
        floatBuffers[floatBuffersIndex] = buffer;
        graphicsBuffers[graphicsBufferIndex] = graphicsBuffer;
        setIntArg(1, index, floatBuffersIndex++);
        setIntArg(2, index, instance);
        setIntArg(3, index, graphicsBufferIndex++);

        cmdID.set(index, CMD_BUFFER_FLOAT_DATA);
        wakeThread(index);
    }


    public void bufferData(GraphicsBuffer graphicsBuffer, int size, ShortBuffer buffer, int instance) {
        int index = getNextCMDIndex();
        setIntArg(0, index, size);
        shortBuffers[shortBuffersIndex] = buffer;
        graphicsBuffers[graphicsBufferIndex] = graphicsBuffer;
        setIntArg(1, index, shortBuffersIndex++);
        setIntArg(2, index, instance);
        setIntArg(3, index, graphicsBufferIndex++);

        cmdID.set(index, CMD_BUFFER_SHORT_DATA);
        wakeThread(index);
    }


    public void bufferData(GraphicsBuffer graphicsBuffer, int size, LongBuffer buffer, int instance) {
        int index = getNextCMDIndex();
        setIntArg(0, index, size);
        longBuffers[longBuffersIndex] = buffer;
        graphicsBuffers[graphicsBufferIndex] = graphicsBuffer;
        setIntArg(1, index, longBuffersIndex++);
        setIntArg(2, index, instance);
        setIntArg(3, index, graphicsBufferIndex++);

        cmdID.set(index, CMD_BUFFER_LONG_DATA);
        wakeThread(index);
    }


    public void bufferData(GraphicsBuffer graphicsBuffer, int size, IntBuffer buffer, int instance) {
        int index = getNextCMDIndex();
        setIntArg(0, index, size);
        intBuffers[intBuffersIndex] = buffer;
        graphicsBuffers[graphicsBufferIndex] = graphicsBuffer;
        setIntArg(1, index, intBuffersIndex++);
        setIntArg(2, index, instance);
        setIntArg(3, index, graphicsBufferIndex++);

        cmdID.set(index, CMD_BUFFER_INT_DATA);
        wakeThread(index);
    }


    public void clearColor(float r, float g, float b, float a) {
      int index = getNextCMDIndex();

      setIntArg(0, index, Float.floatToIntBits(r));
      setIntArg(1, index, Float.floatToIntBits(g));
      setIntArg(2, index, Float.floatToIntBits(b));
      setIntArg(3, index, Float.floatToIntBits(a));

      cmdID.set(index, CMD_CLEAR);
      wakeThread(index);
    }


  	public void beginRecord(int currentFrame, int currentImage) {
  	  // Reset it here idk
  	  byteBuffersIndex = 0;
      shortBuffersIndex = 0;
      longBuffersIndex = 0;
      floatBuffersIndex = 0;
      intBuffersIndex = 0;
      graphicsBufferIndex = 0;


	    println("call begin record");
      int index = getNextCMDIndex();
      setIntArg(0, index, currentImage);
      setIntArg(1, index, currentFrame);
      cmdID.set(index, CMD_BEGIN_RECORD);

      wakeThread(index);
  	}


  	public void endRecord() {
          int index = getNextCMDIndex();
          cmdID.set(index, CMD_END_RECORD);
  		println("call CMD_END_RECORD (index "+index+")");
          // No arguments
          wakeThread(index);
  		currentPipeline = 0;
  	}

  	public void kill() {
  		println("kill thread");
          int index = getNextCMDIndex();
          cmdID.set(index, CMD_KILL);
          // No arguments
          wakeThread(index);
  	}

  	public void killAndCleanup() {
  		kill();
  		// Wait for thread to end
  		while (threadState.get() != STATE_KILLED) {
  			// Do the classic ol "wait 1ms each loop"
  			try {
  				Thread.sleep(1);
  			} catch (InterruptedException e) {
  			}
  		}
  		// Now clean up our mess

  		if (openCmdBuffer.get() == true) {
  			vkEndCommandBuffer(cmdbuffers[currentFrame.get()]);
  		}

  		try(MemoryStack stack = stackPush()) {
  			ArrayList<VkCommandBuffer> deleteList = new ArrayList<>();
  			for (int i = 0; i < cmdbuffers.length; i++) {
  				deleteList.add(cmdbuffers[i]);
  			}
  			vkFreeCommandBuffers(system.device, commandPool, Util.asPointerBuffer(stack, deleteList));
  		}
  		vkDestroyCommandPool(system.device, commandPool, null);
  	}

  	public VkCommandBuffer getBuffer() {
  		return cmdbuffers[currentFrame.get()];
  	}

  	public void await() {
  		int count = 0;
  		// We wait until it has finished its commands

  		// In order for the thread to be properly done its work it must:
  		// - be in sleep mode
  		// - its cmd buffer must be closed

  		// Lets do accurate scientific measuring instead of just using count to get the
  		// benchmarks for threadFinishWait.
  		long before = System.nanoTime();
  		while (
  				!(threadState.get() == STATE_SLEEPING &&
  				openCmdBuffer.get() == false)
  				) {

//  			try {
//  				Thread.sleep(1);
//  			} catch (InterruptedException e) {
//  			}
//  			if (count == 500) {
//  				System.err.println("("+this.myID+") BUG WARNING  looplock'd waiting for a thread that won't respond (state "+threadState.get()+")");
//  				System.exit(1);
//  			}
//  			count++;
    		  if (System.nanoTime()-before > 500000000L) {
            System.err.println("("+this.myID+") BUG WARNING  looplock'd waiting for a thread that won't respond (state "+threadState.get()+")");
            System.exit(1);
          }

  		}
  		threadFinishWait += System.nanoTime()-before;
  	}
}