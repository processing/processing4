package processing.GL2VK;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class TextureBuffer {

  private static VulkanSystem system;
  private static VKSetup vkbase;

  private int bufferCount = 0;

  private int[][] data;

  private long texture = -1;
  private volatile long textureMemory = -1;

  private long stagingBuffer = -1;
  private long stagingBufferMemory = -1;

  public long imageView = -1;
  public long sampler = -1;

  boolean initialized = false;

  private int width = 0;
  private int height = 0;

  public static int textureCount = 1;

  public int myTextureID = 0;




  public TextureBuffer(VulkanSystem s) {
    system = s;
    vkbase = s.vkbase;
  }

  // Debug mode constructor
  public TextureBuffer() {

  }

  {
    myTextureID = textureCount++;
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

  public static void unmap(long mem) {
    vkUnmapMemory(system.device, mem);
  }

  private void writeData(IntBuffer pdata, int xOffset, int yOffset, int width, int height) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        data[y+yOffset][x+xOffset] = pdata.get(y*width+x);
      }
    }
  }

  public void bufferData(IntBuffer data, int xOffset, int yOffset, int width, int height) {
//    int offset = yOffset*height + xOffset;
//    int newsize = width*height;
//
//    int currentSize = this.width*this.height;
    writeData(data, xOffset, yOffset, width, height);
    updateBuffer();

//    if (!initialized) {
//      createTextureBuffer(width, height);
//      bufferData(data, newsize, offset);
//    }
//    else {
//      // Just do a warning for now.
//      if (newsize > currentSize) {
//        System.err.println("bufferDataAuto: newsize ("+newsize+") > currentSize ("+currentSize+"); can't buffer bigger size when buffer has already been created.");
//        return;
//      }
//      bufferData(data, newsize, offset);
//    }
  }

  public void createBuffer(int width, int height) {
    if (initialized) {
      return;
    }

    createTextureBuffer(width, height);
  }



  public void createTextureBuffer(int width, int height) {
    try(MemoryStack stack = stackPush()) {
        // Info
        VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
        imageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
        imageInfo.imageType(VK_IMAGE_TYPE_2D);
        imageInfo.extent().width(width);
        imageInfo.extent().height(height);
        imageInfo.extent().depth(1);
        imageInfo.mipLevels(1);
        imageInfo.arrayLayers(1);
        imageInfo.format(VK_FORMAT_R8G8B8A8_UNORM );
        imageInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
        imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        imageInfo.usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
        imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
        imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

        // Actually creating the texture here.
        LongBuffer pTextureImage = stack.callocLong(1);
        if(vkCreateImage(system.device, imageInfo, null, pTextureImage) != VK_SUCCESS) {
          throw new RuntimeException("Failed to create image");
        }
        // Texture now set
        texture = pTextureImage.get(0);

        // Time to get some mem requirements for our texture
        VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
        vkGetImageMemoryRequirements(system.device, texture, memRequirements);


        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
        allocInfo.allocationSize(memRequirements.size());
        allocInfo.memoryTypeIndex(vkbase.findMemoryType(stack, memRequirements.memoryTypeBits(), VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

        LongBuffer pTextureImageMemory = stack.callocLong(1);
        System.out.println("TextureBuffer Allocation");
        if(vkAllocateMemory(system.device, allocInfo, null, pTextureImageMemory) != VK_SUCCESS) {
          throw new RuntimeException("Failed to allocate image memory");
        }
        textureMemory = pTextureImageMemory.get(0);

      vkBindImageMemory(system.device, texture, textureMemory, 0);
    }


    // STAGING BUFFER
    try(MemoryStack stack = stackPush()) {
      LongBuffer pBuffer = stack.mallocLong(1);
      LongBuffer pBufferMemory = stack.mallocLong(1);
      int size = width*height*4;

      vkbase.createBuffer(size,
              VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
              VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
              pBuffer,
              pBufferMemory);

      this.stagingBuffer = pBuffer.get(0);
      this.stagingBufferMemory = pBufferMemory.get(0);
    }
    bufferCount++;
    initialized = true;
    this.width = width;
    this.height = height;
    data = new int[height][width];
    imageView = vkbase.createImageView(texture, VK_FORMAT_R8G8B8A8_UNORM );
    createTextureSampler();
  }


  private void createTextureSampler() {

    try(MemoryStack stack = stackPush()) {

        VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
        samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
        samplerInfo.magFilter(VK_FILTER_LINEAR);
        samplerInfo.minFilter(VK_FILTER_LINEAR);
        samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
        samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
        samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
        samplerInfo.anisotropyEnable(true);
        samplerInfo.maxAnisotropy(16.0f);
        samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
        samplerInfo.unnormalizedCoordinates(false);
        samplerInfo.compareEnable(false);
        samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
        samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);

        LongBuffer pTextureSampler = stack.mallocLong(1);

        if(vkCreateSampler(system.device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create texture sampler");
        }

        sampler = pTextureSampler.get(0);
    }
  }

  private boolean undefinedLayout = true;

  // Buffer the whole data
  public void updateBuffer() {
    // Null data? Skip buffering. Null means "just create the buffer".
    if (data == null) return;

    int size = width*height*Integer.BYTES;

    if (system == null) return;

    IntBuffer datato = mapInt(size, stagingBufferMemory);
    datato.rewind();

    int x = 0;
    int y = 0;

//    try {
    while (datato.hasRemaining()) {
//      System.out.println(data[y][x]);
      datato.put(data[y][x]);
      x++;
      if (x >= width) {
        x = 0;
        y++;
      }
    }
//    }
//    catch (IndexOutOfBoundsException e) {
//
//    }

    datato.rewind();
    unmap(stagingBufferMemory);

    if (undefinedLayout) {
      vkbase.transitionImageLayout(texture,
                                   VK_FORMAT_R8G8B8A8_UNORM,
                                   VK_IMAGE_LAYOUT_UNDEFINED,
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
      undefinedLayout = false;
    }
    else {
      vkbase.transitionImageLayout(texture,
                                   VK_FORMAT_R8G8B8A8_UNORM,
                                   VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

    }

    vkbase.copyTextureAndWait(stagingBuffer, texture, width, height);

    vkbase.transitionImageLayout(texture,
                          VK_FORMAT_R8G8B8A8_UNORM ,
                          VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                          VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
  }

  public void clean() {
    vkDestroyImageView(system.device, imageView, null);
    vkDestroyImage(system.device, texture, null);
    vkDestroySampler(system.device, sampler, null);
    vkFreeMemory(system.device, textureMemory, null);

    texture = -1;
    textureMemory = -1;
    stagingBuffer = -1;
    stagingBufferMemory = -1;
    imageView = -1;
    sampler = -1;
    initialized = false;
  }
}

