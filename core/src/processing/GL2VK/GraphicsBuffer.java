package processing.GL2VK;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;


//     ChatGPT response to an optimised buffering approach:
//Ah, I see what you're asking now! In Vulkan, you cannot directly "buffer" data during rendering within a VkRenderPass instance in the same way you might in some higher-level graphics APIs. However, you can achieve similar functionality through the use of dynamic buffers and descriptor sets. Here’s how that works:
//
//Dynamic Buffers in Vulkan
//Dynamic Vertex Buffers:
//
//You can create a vertex buffer that allows for dynamic updates.
//Use buffer updates (e.g., vkMapMemory followed by writing data) to change the contents of the buffer.
//Using Descriptor Sets:
//
//Create a descriptor set that points to the buffer.
//Update the descriptor set as needed during rendering.
//Steps to Buffer Data Dynamically
//Create Dynamic Buffers:
//
//When creating your vertex buffer, allocate enough space to allow for dynamic updates.
//Use VK_BUFFER_USAGE_VERTEX_BUFFER_BIT and VK_BUFFER_USAGE_TRANSFER_DST_BIT when creating the buffer.
//Map Buffer Memory:
//
//Use vkMapMemory to access the buffer memory and write your vertex data into it.
//void* data;
//vkMapMemory(device, vertexBufferMemory, 0, bufferSize, 0, &data);
//memcpy(data, newVertexData, sizeof(newVertexData));
//vkUnmapMemory(device, vertexBufferMemory);
//Update Descriptor Sets:
//
//If using descriptor sets, update them to point to the new data.
//Call vkUpdateDescriptorSets to update the descriptor set with new buffer information.
//Bind Buffers and Draw:
//
//In your command buffer, bind the vertex buffer and draw as usual.
//vkCmdBindVertexBuffers(commandBuffer, 0, 1, &vertexBuffer, offsets);
//vkCmdDraw(commandBuffer, vertexCount, 1, 0, 0);
//Considerations
//Performance: Dynamic updates to buffers during rendering can impact performance. It's usually better to batch updates and minimize the frequency of changes if possible.
//Synchronization: Be cautious of synchronization issues when updating buffers while rendering. Use fences and semaphores appropriately.
//Conclusion
//While you cannot directly buffer data inside a VkRenderPass instance, you can achieve dynamic updates to your vertex data by using dynamic buffers and updating descriptor sets appropriately. This allows for flexible rendering scenarios where vertex data can change during the rendering process.
//
//If you have any more questions or need clarification on specific parts, feel free to ask!



public class GraphicsBuffer {


    private int globalInstance = 0;

    // Is 0 or 1
    private static int frame = 0;
    private final static int MAX_INSTANCES = 128;

    private long[] buffers = new long[MAX_INSTANCES*2];
    private volatile long[] bufferMemory = new long[MAX_INSTANCES*2];
    // the bufferMemory list is at risk; if we createBufferAuto while a bufferData process is happening,
    // this could cause bad things.
    // So let's have a buncha booleans to specify if it's safe to use
    private AtomicBoolean[] safeToUpdateBuffer = new AtomicBoolean[MAX_INSTANCES*2];

    private long[] stagingBuffers = new long[MAX_INSTANCES*2];
    private long[] stagingBufferMemory = new long[MAX_INSTANCES*2];

    private int[] bufferSize = new int[MAX_INSTANCES*2];

    private boolean retainedMode = false;
    private boolean mapped = false;
    public boolean indexBuffer = false;

    private class DeleteEntry {
      public long buffer = 0L;
      public long mem = 0L;
      public int tmr = -1;

      public DeleteEntry(long b, long m) {
        buffer = b;
        mem = m;
        tmr = 6;
      }
    }

    // Buffers may be in use by GPU mid-frame.
    // queue for deletion next frame.
    private ArrayList<DeleteEntry> deleteQueue = new ArrayList<>();

    public ShortBuffer indexInstantAccessBuffer = null;

    private static VulkanSystem system;
    private static VKSetup vkbase;

    public GraphicsBuffer(VulkanSystem s) {
    	system = s;
    	vkbase = s.vkbase;
    }

    // Debug mode constructor
    public GraphicsBuffer() {
    }

    {
      for (int i = 0; i < buffers.length; i++) {
        buffers[i] = -1;
        bufferMemory[i] = -1;
        stagingBuffers[i] = -1;
        stagingBufferMemory[i] = -1;
        safeToUpdateBuffer[i] = new AtomicBoolean(true);
      }
    }

    public static void setFrame(int f) {
      frame = f;
    }


    // Releases any previous buffers and creates a buffer IF
    // - There's no previous buffer
    // - Buffer size != new size.
    // NOT THREAD SAFE HERE
    public void createBufferAuto(int size, int vertexIndexUsage, boolean retainedMode) {

      // TO BE SAFE:
      // bufferData is done in separate threads (for immediate mode).
      // Specifically, bufferMemory is at risk here.
      // Let's NOT do any operations until we know it's safe to do so.
      // While this looks bad, this is only used in very very rare cases, and
      // even when it does happen, it should only happen once per vkbuffer.
      int count = 0;
      while (safeToUpdateBuffer[actualInst(globalInstance)].get() == false) {
        // busy wait
        if (count > 9999999) {
          System.err.println("BUG WARNING  createBufferAuto: looplock'd waiting for buffer instance "+globalInstance+" to be safe.");
          System.exit(1);
        }
        count++;
      }
      if (count > 0) {
        System.out.println("createBufferAuto: Stall for "+count);
      }

      if (vertexIndexUsage == VK_BUFFER_USAGE_INDEX_BUFFER_BIT) {
        indexBuffer = true;
      }

  		// Delete old buffers
//    		destroy(globalInstance);

  		// Create new one
  		if (retainedMode) {
        if (buffers[globalInstance] == -1 || size > bufferSize[globalInstance]) {
          scheduleDestroy(globalInstance);
          createBufferRetainedMode(size, vertexIndexUsage, globalInstance);
          this.retainedMode = true;
        }
  		}
  		else {
        // One for each frame
        if (buffers[globalInstance] == -1 || size > bufferSize[globalInstance]
            || buffers[globalInstance+MAX_INSTANCES] == -1 || size > bufferSize[globalInstance+MAX_INSTANCES]) {
          scheduleDestroy(globalInstance);
          createBufferImmediateMode(size, vertexIndexUsage, globalInstance);
          createBufferImmediateMode(size, vertexIndexUsage, globalInstance+MAX_INSTANCES);
          this.retainedMode = false;
        }
  		}
    }

    private int actualInst(int instance) {
      if (this.retainedMode) {
        return instance;
      }
      else {
        return instance + (frame*MAX_INSTANCES);
      }
    }

    private void destroyScheduledBuffers() {
      for (DeleteEntry e : deleteQueue) {
        e.tmr--;
        if (e.tmr == 0) {
          if (e.buffer != -1) {
            vkDestroyBuffer(system.device, e.buffer, null);
            e.buffer = -1;
          }
          if (e.mem != -1) {
            vkFreeMemory(system.device, e.mem, null);
            e.mem = -1;
          }
        }
      }
    }

    public static int bufferCount = 0;


    // Creates a buffer without allocating any data
    private void createBufferImmediateMode(int size, int usage, int inst) {
    	// If in debug mode, just assign a dummy value
    	if (system == null) {
    		this.buffers[inst] = (long)(Math.random()*100000.);

    		return;
    	}

    	try(MemoryStack stack = stackPush()) {

    		// Not from anywhere, just alloc pointers we can use
    		// to get back from createbuffer method
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);


            // Buffer which is usable by both CPU and GPU
            vkbase.createBuffer(size,
                    usage,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer,
                    pBufferMemory);

            // Pointer variables now populated

            // GraphicsBuffedr object, set with our new pointer variables.
            buffers[inst] = pBuffer.get(0);
            bufferMemory[inst] = pBufferMemory.get(0);
            bufferSize[inst] = size;

    	}
    	bufferCount++;
    }


    // Needs to be called when it
    public long getCurrBuffer() {
      if (globalInstance == 0) globalInstance = 1;

      // Problem: we need to know whether we're doing retained or immediate.
      // We can quickly check stagingBuffer == -1 for immediate, != -1 for retained
      if (stagingBuffers[actualInst(globalInstance-1)] != -1) {
        // Retained
        return stagingBuffers[actualInst(globalInstance-1)];
      }
      else {
        // Immediate
        return buffers[actualInst(globalInstance-1)];
      }
    }


    public void reset() {
      globalInstance = 0;

      // Clear scheduled-for-deletion buffers.
      destroyScheduledBuffers();
    }

    // NOT THREAD SAFE
    private void createBufferRetainedMode(int size, int usage, int instance) {
      // If in debug mode, just assign a dummy value
      if (system == null) {
        this.buffers[(instance)] = (long)(Math.random()*100000.);

        return;
      }

      try(MemoryStack stack = stackPush()) {

        // Not from anywhere, just alloc pointers we can use
        // to get back from createbuffer method
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);


            // Buffer which is usable by both CPU and GPU
            vkbase.createBuffer(size,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage,
                    VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);

            // Pointer variables now populated

            // GraphicsBuffedr object, set with our new pointer variables.
            buffers[(instance)] = pBuffer.get(0);
            bufferMemory[(instance)] = pBufferMemory.get(0);
            bufferSize[(instance)] = size;
      }


      // STAGING BUFFER
      try(MemoryStack stack = stackPush()) {
        LongBuffer pBuffer = stack.mallocLong(1);
        LongBuffer pBufferMemory = stack.mallocLong(1);
        vkbase.createBuffer(size,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT | usage,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory);

        this.stagingBuffers[(instance)] = pBuffer.get(0);
        this.stagingBufferMemory[(instance)] = pBufferMemory.get(0);
      }
      bufferCount++;
    }






    public void destroy(int instance) {
    	// If debug mode enabled
    	if (system == null) return;

    	if (buffers[(instance)] != -1 && bufferMemory[(instance)] != -1) {
	        vkDestroyBuffer(system.device, buffers[(instance)], null);
	        vkFreeMemory(system.device, bufferMemory[(instance)], null);
    	}
//    	bufferCount--;
    }

    public void destroy() {
      for (int i = 0; i < buffers.length; i++) {
        if (buffers[i] != -1 && bufferMemory[i] != -1) {
          vkDestroyBuffer(system.device, buffers[i], null);
          vkFreeMemory(system.device, bufferMemory[i], null);
        }
      }
    }

    public void scheduleDestroy(int instance) {
      deleteQueue.add(new DeleteEntry(buffers[(instance)], bufferMemory[(instance)]));
      deleteQueue.add(new DeleteEntry(buffers[(instance+MAX_INSTANCES)], bufferMemory[(instance+MAX_INSTANCES)]));
    }

    public boolean mapped() {
      return mapped;
    }

    public ByteBuffer map(int instance) {
      mapped = true;



      // Problem: we need to know whether we're doing retained or immediate.
      // We can quickly check stagingBuffer == -1 for immediate, != -1 for retained
      if (stagingBufferMemory[actualInst(instance)] != -1) {
        // Retained
        if (indexBuffer) {
          indexInstantAccessBuffer = mapShort(bufferSize[actualInst(instance)], stagingBufferMemory[actualInst(instance)]);
          unmap(stagingBufferMemory[actualInst(instance)]);
        }
        return mapByte(bufferSize[actualInst(instance)], stagingBufferMemory[actualInst(instance)]);
      }
      else {
        // Immediate
        if (indexBuffer) {
          indexInstantAccessBuffer = mapShort(bufferSize[actualInst(instance)], bufferMemory[actualInst(instance)]);
          unmap(bufferMemory[actualInst(instance)]);
        }
        return mapByte(bufferSize[actualInst(instance)], bufferMemory[actualInst(instance)]);
      }
    }

    // For a very specific purpose
    // Just alt+shift+h it, i cant be bothered explaining.
    public ShortBuffer mapShort(int instance) {
      mapped = true;
      if (stagingBufferMemory[actualInst(instance)] != -1) {
        // Retained
        return mapShort(bufferSize[actualInst(instance)], stagingBufferMemory[actualInst(instance)]);
      }
      else {
        // Immediate
        return mapShort(bufferSize[actualInst(instance)], bufferMemory[actualInst(instance)]);
      }
    }

    // TODO: Make multithreaded
    public static ByteBuffer mapByte(int size, long mem) {
      try(MemoryStack stack = stackPush()) {


          // alloc pointer for our data
          PointerBuffer pointer = stack.mallocPointer(1);
          vkMapMemory(system.device, mem, 0, size, 0, pointer);

          // Here instead of some mem copy function we can just
          // copy each and every byte of buffer.
          ByteBuffer datato = pointer.getByteBuffer(0, size);

          return datato;
      }
    }

    public static FloatBuffer mapFloat(int size, long mem) {
      try(MemoryStack stack = stackPush()) {
          // alloc pointer for our data
          PointerBuffer pointer = stack.mallocPointer(1);
          vkMapMemory(system.device, mem, 0, size, 0, pointer);

          // Here instead of some mem copy function we can just
          // copy each and every byte of buffer.
          FloatBuffer datato = pointer.getFloatBuffer(0, size/Float.BYTES);

          return datato;
      }
    }

    public static ShortBuffer mapShort(int size, long mem) {
      try(MemoryStack stack = stackPush()) {
          // alloc pointer for our data
          PointerBuffer pointer = stack.mallocPointer(1);
          vkMapMemory(system.device, mem, 0, size, 0, pointer);

          // Here instead of some mem copy function we can just
          // copy each and every byte of buffer.
          ShortBuffer datato = pointer.getShortBuffer(0, size/Short.BYTES);

          return datato;
      }
    }

    public static IntBuffer mapInt(int size, long mem) {
      try(MemoryStack stack = stackPush()) {
          // alloc pointer for our data
          PointerBuffer pointer = stack.mallocPointer(1);
          vkMapMemory(system.device, mem, 0, size, 0, pointer);

          // Here instead of some mem copy function we can just
          // copy each and every byte of buffer.
          IntBuffer datato = pointer.getIntBuffer(0, size/Integer.BYTES);

          return datato;
      }
    }

    public static LongBuffer mapLong(int size, long mem) {
      try(MemoryStack stack = stackPush()) {
          // alloc pointer for our data
          PointerBuffer pointer = stack.mallocPointer(1);
          vkMapMemory(system.device, mem, 0, size, 0, pointer);

          // Here instead of some mem copy function we can just
          // copy each and every byte of buffer.
          LongBuffer datato = pointer.getLongBuffer(0, size/Integer.BYTES);

          return datato;
      }
    }

    public static void unmap(long mem) {
      vkUnmapMemory(system.device, mem);
    }

    public void increaseInstance() {
      globalInstance++;
    }

    public int getInstance() {
      return globalInstance;
    }

    public void unmap(int instance) {
      // Problem: we need to know whether we're doing retained or immediate.
      // We can quickly check stagingBuffer == -1 for immediate, != -1 for retained
      if (stagingBufferMemory[actualInst(instance)] != -1) {
        // Retained
        unmap(stagingBufferMemory[actualInst(instance)]);
        mapped = false;
      }
      else {
        // Immediate
        unmap(bufferMemory[actualInst(instance)]);
        mapped = false;
      }
    }

    /////////////////////////////////////////////////////
    // TODO: version where memory is constantly unmapped

    // IMMEDIATE MODE METHODS TO BE USED IN MULTITHREADING
    public void bufferDataImmediate(ByteBuffer data, int size, int instance) {
      	// If debug mode enabled
      	if (system == null) return;

      	safeToUpdateBuffer[actualInst(instance)].set(false);

      	long mem = bufferMemory[actualInst(instance)];

  	    ByteBuffer datato = mapByte(size, mem);

  	    try {
      		datato.rewind();
      		data.rewind();
      		while (datato.hasRemaining()) {
      			datato.put(data.get());
      		}
      		datato.rewind();
        }
        catch (BufferOverflowException e) {
          // Ignore and continue.
        }
        catch (BufferUnderflowException e) {
          // Ignore and continue.
        }

    		unmap(mem);
        safeToUpdateBuffer[actualInst(instance)].set(true);
    }

    public void bufferDataImmediate(FloatBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        safeToUpdateBuffer[actualInst(instance)].set(false);

        long mem = bufferMemory[actualInst(instance)];

        FloatBuffer datato = mapFloat(size, mem);

        try {
          datato.rewind();
          data.rewind();
          while (datato.hasRemaining()) {
            datato.put(data.get());
          }
          datato.rewind();
        }
        catch (BufferOverflowException e) {
          // Ignore and continue.
        }
        catch (BufferUnderflowException e) {
          // Ignore and continue.
        }

        unmap(mem);
        safeToUpdateBuffer[actualInst(instance)].set(true);
    }

    public void bufferDataImmediate(ShortBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        safeToUpdateBuffer[actualInst(instance)].set(false);

        long mem = bufferMemory[actualInst(instance)];

        ShortBuffer datato = mapShort(size, mem);


        try {
          datato.rewind();
          data.rewind();
          while (datato.hasRemaining()) {
            datato.put(data.get());
          }
          datato.rewind();
        }
        catch (BufferOverflowException e) {
          // Ignore and continue.
        }
        catch (BufferUnderflowException e) {
          // Ignore and continue.
        }

        if (indexBuffer) {
          indexInstantAccessBuffer = datato;
        }


        unmap(mem);
        safeToUpdateBuffer[actualInst(instance)].set(true);
    }

    public void bufferDataImmediate(IntBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        safeToUpdateBuffer[actualInst(instance)].set(false);

        long mem = bufferMemory[actualInst(instance)];

        IntBuffer datato = mapInt(size, mem);

        try {
          datato.rewind();
          data.rewind();
          while (datato.hasRemaining()) {
            datato.put(data.get());
          }
          datato.rewind();
        }
        catch (BufferOverflowException e) {
          // Ignore and continue.
        }
        catch (BufferUnderflowException e) {
          // Ignore and continue.
        }

        unmap(mem);
        safeToUpdateBuffer[actualInst(instance)].set(true);
    }

    public void bufferDataImmediate(LongBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        safeToUpdateBuffer[actualInst(instance)].set(false);

        long mem = bufferMemory[actualInst(instance)];

        LongBuffer datato = mapLong(size, mem);

        try {
          datato.rewind();
          data.rewind();
          while (datato.hasRemaining()) {
            datato.put(data.get());
          }
          datato.rewind();
        }
        catch (BufferOverflowException e) {
          // Ignore and continue.
        }
        catch (BufferUnderflowException e) {
          // Ignore and continue.
        }


        unmap(mem);
        safeToUpdateBuffer[actualInst(instance)].set(true);
    }









    ////////////////////////////////////////////////////////////////
    // Retained mode
    // NOT TO BE USED IN MULTITHREADING

    public void bufferDataRetained(ByteBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        ByteBuffer datato = mapByte(size, stagingBuffers[actualInst(instance)]);
        datato.rewind();
        data.rewind();
        while (datato.hasRemaining()) {
          datato.put(data.get());
        }
        datato.rewind();
        unmap(stagingBuffers[actualInst(instance)]);

        vkbase.copyBufferAndWait(stagingBuffers[actualInst(instance)], buffers[actualInst(instance)], size);
    }

    public void bufferDataRetained(FloatBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        FloatBuffer datato = mapFloat(size, stagingBuffers[actualInst(instance)]);

        datato.rewind();
        data.rewind();
        while (datato.hasRemaining()) {
          datato.put(data.get());
        }
        datato.rewind();

        unmap(stagingBuffers[actualInst(instance)]);

        vkbase.copyBufferAndWait(stagingBuffers[actualInst(instance)], buffers[actualInst(instance)], size);
    }

    public void bufferDataRetained(ShortBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        ShortBuffer datato = mapShort(size, stagingBuffers[actualInst(instance)]);
        datato.rewind();
        data.rewind();
        while (datato.hasRemaining()) {
          datato.put(data.get());
        }
        datato.rewind();

        if (indexBuffer) {
          indexInstantAccessBuffer = datato;
        }

        unmap(stagingBuffers[actualInst(instance)]);

        vkbase.copyBufferAndWait(stagingBuffers[actualInst(instance)], buffers[actualInst(instance)], size);
    }

    public void bufferDataRetained(IntBuffer data, int size, int instance) {
        // If debug mode enabled
        if (system == null) return;

        IntBuffer datato = mapInt(size, stagingBuffers[actualInst(instance)]);
        datato.rewind();
        data.rewind();
        while (datato.hasRemaining()) {
          datato.put(data.get());
        }
        datato.rewind();
        unmap(stagingBuffers[actualInst(instance)]);

        vkbase.copyBufferAndWait(stagingBuffers[actualInst(instance)], buffers[actualInst(instance)], size);
    }
}