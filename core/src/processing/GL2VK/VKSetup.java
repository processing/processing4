package processing.GL2VK;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.*;


import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import processing.vulkan.PSurfaceVK;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;


// Vulkan is highly customisable.
// VR, 3D screens, offscreen buffer, validation layers, rendering straight to
// the display etc etc.
// Let's use the default setup and dump it all into this one file.

public class VKSetup {

    public static final boolean ENABLE_VALIDATION_LAYERS = false; //DEBUG.get(true);

    private static final Set<String> VALIDATION_LAYERS;
    static {
        if(ENABLE_VALIDATION_LAYERS) {
            VALIDATION_LAYERS = new HashSet<>();
            VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
        } else {
            // We are not going to use it, so we don't create it
            VALIDATION_LAYERS = null;
        }
    }

    private static final int OPERATION_BUFFER  = 1;
    private static final int OPERATION_TEXTURE = 2;


    public VkInstance instance;
    public long debugMessenger;

    public VkQueue graphicsQueue;
    public VkQueue presentQueue;

    public long swapChain;
    public List<Long> swapChainImages;
    public int swapChainImageFormat;
    public VkExtent2D swapChainExtent;
    public List<Long> swapChainImageViews;

    public VkPhysicalDevice physicalDevice;
    public VkDevice device;
    public PSurfaceVK vkwindow;
    public long window;
    private VkCommandBuffer transferCommandBuffer;
    public VkQueue transferQueue;
    public long commandPool;
    public long transferCommandPool;
    public QueueFamilyIndices queueIndicies;
    public int pushConstantsSizeLimit = 0;

    public boolean useTransferQueue = true;

    public void initBase(PSurfaceVK surface) {
    	vkwindow = surface;
    	window = vkwindow.glfwwindow;
      createInstance();
      setupDebugMessenger();
      vkwindow.createGLFWSurface(instance);
      pickPhysicalDevice();
      createLogicalDevice();
      createSwapChain();
      createImageViews();
    }



    private static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
            .collect(toSet());



    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {

        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        System.err.println("Validation layer: " + callbackData.pMessageString());

        return VK_FALSE;
    }

    private static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo,
                                                    VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger) {

        if(vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
            return vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
        }

        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    public static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks) {

        if(vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
        }

    }

    public void destroyOtherThings() {
    	// Only need to destroy these "other things" (transfer stuff)
    	// if we're using the transfer queue.
  		if (useTransferQueue) {
  			try(MemoryStack stack = stackPush()) {
  				ArrayList<VkCommandBuffer> deleteList = new ArrayList<>();
  					deleteList.add(transferCommandBuffer);
  				vkFreeCommandBuffers(device, transferCommandPool, Util.asPointerBuffer(stack, deleteList));
  			}
  			vkDestroyCommandPool(device, transferCommandPool, null);
  		}
    }

    public class QueueFamilyIndices {

        // We use Integer to use null as the empty value
    	public Integer graphicsFamily;
        public Integer presentFamily;
        public Integer transferFamily;

        private boolean isComplete() {
        	if (useTransferQueue)
        		return graphicsFamily != null && presentFamily != null && transferFamily != null;
        	else
        		return graphicsFamily != null && presentFamily != null;
        }

        public int[] unique() {
        	if (useTransferQueue)
        		return IntStream.of(graphicsFamily, presentFamily, transferFamily).distinct().toArray();
        	else
        		return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
        }

        public int[] array() {
        	if (useTransferQueue)
        		return new int[] {graphicsFamily, presentFamily, transferFamily};
        	else
        		return new int[] {graphicsFamily, presentFamily};
        }
    }

    public class SwapChainSupportDetails {

        private VkSurfaceCapabilitiesKHR capabilities;
        private VkSurfaceFormatKHR.Buffer formats;
        private IntBuffer presentModes;

    }

    public void createInstance() {

        if(ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport()) {
            throw new RuntimeException("Validation requested but not supported");
        }

        try(MemoryStack stack = stackPush()) {

            // Use calloc to initialize the structs with 0s. Otherwise, the program can crash due to random values

            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);

            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8Safe("Hello Triangle"));
            appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.pEngineName(stack.UTF8Safe("No Engine"));
            appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.apiVersion(VK_API_VERSION_1_0);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(appInfo);
            // enabledExtensionCount is implicitly set when you call ppEnabledExtensionNames
            createInfo.ppEnabledExtensionNames(getRequiredExtensions(stack));

            if(ENABLE_VALIDATION_LAYERS) {

                createInfo.ppEnabledLayerNames(Util.asPointerBuffer(stack, VALIDATION_LAYERS));

                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
                populateDebugMessengerCreateInfo(debugCreateInfo);
                createInfo.pNext(debugCreateInfo.address());
            }

            // We need to retrieve the pointer of the created instance
            PointerBuffer instancePtr = stack.mallocPointer(1);

            if(vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create instance");
            }

            instance = new VkInstance(instancePtr.get(0), createInfo);
        }
    }



    private void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
        debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        debugCreateInfo.pfnUserCallback(VKSetup::debugCallback);
    }

    private void setupDebugMessenger() {

        if(!ENABLE_VALIDATION_LAYERS) {
            return;
        }

        try(MemoryStack stack = stackPush()) {

            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);

            populateDebugMessengerCreateInfo(createInfo);

            LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);

            if(createDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger) != VK_SUCCESS) {
                throw new RuntimeException("Failed to set up debug messenger");
            }

            debugMessenger = pDebugMessenger.get(0);
        }
    }



    public void createCommandPool() {

        try(MemoryStack stack = stackPush()) {

        	// Part 1: pool for graphics queue

            // God i wish this shiz was easier to read.
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            // Important: VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT (i had bugs lol)
            poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            // graphics family
            poolInfo.queueFamilyIndex(queueIndicies.graphicsFamily);

            // create our command pool vk
            LongBuffer pCommandPool = stack.mallocLong(1);
            if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create command pool");
            }
            // Boom. Assign'd
            commandPool = pCommandPool.get(0);

            // ===> Part 2: Create the transfer command pool <===
            // BUT ONLY IF WE'RE ALLOWED!!!
            if (useTransferQueue) {
	            poolInfo.queueFamilyIndex(queueIndicies.transferFamily);
	//            poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

	            // Tell Vulkan that the buffers of this pool will be constantly rerecorded
	//            poolInfo.flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
	            poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

	            // Create our transfer command pool
	            if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
	                throw new RuntimeException("Failed to create command pool");
	            }
	            // Boom. Assign'd again.
	            transferCommandPool = pCommandPool.get(0);

	            allocateTransferCommandBuffer();
            }
        }
    }

    private void allocateTransferCommandBuffer() {

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(transferCommandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
            transferCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);
        }
    }

    private void createLogicalDevice() {

        try(MemoryStack stack = stackPush()) {

            QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
            queueIndicies = indices;

            int[] uniqueQueueFamilies = indices.unique();

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.length, stack);

            for(int i = 0;i < uniqueQueueFamilies.length;i++) {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
            }

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
            deviceFeatures.samplerAnisotropy(true);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            // queueCreateInfoCount is automatically set

            createInfo.pEnabledFeatures(deviceFeatures);

            createInfo.ppEnabledExtensionNames(Util.asPointerBuffer(stack, DEVICE_EXTENSIONS));

            if(ENABLE_VALIDATION_LAYERS) {
                createInfo.ppEnabledLayerNames(Util.asPointerBuffer(stack, VALIDATION_LAYERS));
            }

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

            if(vkCreateDevice(physicalDevice, createInfo, null, pDevice) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create logical device");
            }

            device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);

            PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

            vkGetDeviceQueue(device, indices.graphicsFamily, 0, pQueue);
            graphicsQueue = new VkQueue(pQueue.get(0), device);

            vkGetDeviceQueue(device, indices.presentFamily, 0, pQueue);
            presentQueue = new VkQueue(pQueue.get(0), device);

            if (useTransferQueue) {
	            vkGetDeviceQueue(device, indices.transferFamily, 0, pQueue);
	            transferQueue = new VkQueue(pQueue.get(0), device);
            }
        }
    }


    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
//        return availableFormats.stream()
//                .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_UNORM)
//                .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
//                .findAny()
//                .orElse(availableFormats.get(0));

    	// We want one-to-one with the Processing program so no nonlinear "corrected" colors.

    	return availableFormats.stream()
              .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_UNORM)
              .findAny()
              .orElse(availableFormats.get(0));
    }

    private int chooseSwapPresentMode(IntBuffer availablePresentModes) {

        for(int i = 0;i < availablePresentModes.capacity();i++) {
            if(availablePresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                return availablePresentModes.get(i);
            }
        }

        return VK_PRESENT_MODE_FIFO_KHR;
    }

    private VkExtent2D chooseSwapExtent(MemoryStack stack, VkSurfaceCapabilitiesKHR capabilities) {

        if(capabilities.currentExtent().width() != Util.UINT32_MAX) {
            return capabilities.currentExtent();
        }

        IntBuffer width = stackGet().ints(0);
        IntBuffer height = stackGet().ints(0);

        glfwGetFramebufferSize(window, width, height);

        VkExtent2D actualExtent = VkExtent2D.malloc(stack).set(width.get(0), height.get(0));

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(Util.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(Util.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));


        return actualExtent;
    }




    public void createSwapChain() {

        try(MemoryStack stack = stackPush()) {

            SwapChainSupportDetails swapChainSupport = querySwapChainSupport(physicalDevice, stack);

            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
            int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
            VkExtent2D extent = chooseSwapExtent(stack, swapChainSupport.capabilities);

            IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

            if(swapChainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
                imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(vkwindow.glfwsurface);

            // Image settings
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);


            if(!queueIndicies.graphicsFamily.equals(queueIndicies.presentFamily)) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(queueIndicies.graphicsFamily, queueIndicies.presentFamily));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);

            createInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

            if(vkCreateSwapchainKHR(device, createInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain");
            }

            swapChain = pSwapChain.get(0);

            vkGetSwapchainImagesKHR(device, swapChain, imageCount, null);

            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

            vkGetSwapchainImagesKHR(device, swapChain, imageCount, pSwapchainImages);

            swapChainImages = new ArrayList<>(imageCount.get(0));

            for(int i = 0;i < pSwapchainImages.capacity();i++) {
                swapChainImages.add(pSwapchainImages.get(i));
            }

            swapChainImageFormat = surfaceFormat.format();
            swapChainExtent = VkExtent2D.create().set(extent);
        }
    }


    private void pickPhysicalDevice() {

        try(MemoryStack stack = stackPush()) {

            IntBuffer deviceCount = stack.ints(0);

            vkEnumeratePhysicalDevices(instance, deviceCount, null);

            if(deviceCount.get(0) == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support");
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

            vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);

            for(int i = 0;i < ppPhysicalDevices.capacity();i++) {

                VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);

                if(isDeviceSuitable(device)) {
                    physicalDevice = device;
                    return;
                }
            }

            throw new RuntimeException("Failed to find a suitable GPU");
        }
    }


    private boolean isDeviceSuitable(VkPhysicalDevice device) {

        boolean extensionsSupported = checkDeviceExtensionSupport(device);
        boolean swapChainAdequate = false;
        boolean integrated = false;
        boolean discrete = false;
        int pushConstantsSize = 0;
        boolean anisotropySupported = false;

        if(extensionsSupported) {
            try(MemoryStack stack = stackPush()) {

                VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.malloc(stack);
                VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.malloc(stack);
                vkGetPhysicalDeviceProperties(device, deviceProperties);
                vkGetPhysicalDeviceFeatures(device, deviceFeatures);

                pushConstantsSize = deviceProperties.limits().maxPushConstantsSize();

                SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device, stack);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();
                integrated = deviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU;
                discrete = deviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;

                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.malloc(stack);
                vkGetPhysicalDeviceFeatures(device, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();

            }
        }

        boolean suitable = findQueueFamilies(device).isComplete() && extensionsSupported && swapChainAdequate && integrated && anisotropySupported;
        // Set values here cus I'm too lazy to do it properly
        if (suitable) {
        	pushConstantsSizeLimit = pushConstantsSize;
        }
        return suitable;
    }


    public QueueFamilyIndices findQueueFamilies() {
    	return findQueueFamilies(physicalDevice);
    }


    private QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {

        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush()) {

            IntBuffer queueFamilyCount = stack.ints(0);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);


            for(int i = 0; i < queueFamilies.capacity() || !indices.isComplete(); i++) {

                if((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }
                // Only if transferQueue is enabled.
                else if (useTransferQueue && ((queueFamilies.get(i).queueFlags() & VK_QUEUE_TRANSFER_BIT) != 0)) {
                    indices.transferFamily = i;
                }
                // Crash fix
                // If the transferFamily has not been assigned yet and we're at the end of the
                // list, give up with the whole queueFamily thing.
                else if (useTransferQueue && i == queueFamilies.capacity()-1 && indices.transferFamily == null) {
                  useTransferQueue = false;
                }

                // In case of having only 1 queueFamily, it means we're gonna get a validation error if we
                // try using a transfer queue so let's not use a transfer queue.
                // Only if transferQueue is enabled.
                if(useTransferQueue && queueFamilies.capacity() == 1) {
//                    indices.transferFamily = i;
                	useTransferQueue = false;
                }

                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, vkwindow.glfwsurface, presentSupport);

                if(presentSupport.get(0) == VK_TRUE) {
                    indices.presentFamily = i;
                }

            }

            return indices;
        }
    }



    private SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, MemoryStack stack) {

        SwapChainSupportDetails details = new SwapChainSupportDetails();

        details.capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, vkwindow.glfwsurface, details.capabilities);

        IntBuffer count = stack.ints(0);

        vkGetPhysicalDeviceSurfaceFormatsKHR(device, vkwindow.glfwsurface, count, null);

        if(count.get(0) != 0) {
            details.formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, vkwindow.glfwsurface, count, details.formats);
        }

        vkGetPhysicalDeviceSurfacePresentModesKHR(device,vkwindow.glfwsurface, count, null);

        if(count.get(0) != 0) {
            details.presentModes = stack.mallocInt(count.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, vkwindow.glfwsurface, count, details.presentModes);
        }

        return details;
    }

    boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer extensionCount = stack.ints(0);

            vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0), stack);

            vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, availableExtensions);

            return availableExtensions.stream()
                    .map(VkExtensionProperties::extensionNameString)
                    .collect(toSet())
                    .containsAll(DEVICE_EXTENSIONS);
        }
    }



    private boolean checkValidationLayerSupport() {

        try(MemoryStack stack = stackPush()) {

            IntBuffer layerCount = stack.ints(0);

            vkEnumerateInstanceLayerProperties(layerCount, null);

            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);

            vkEnumerateInstanceLayerProperties(layerCount, availableLayers);

            Set<String> availableLayerNames = availableLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(toSet());

            return availableLayerNames.containsAll(VALIDATION_LAYERS);
        }
    }


    public void createImageViews() {

      swapChainImageViews = new ArrayList<>(swapChainImages.size());

      for(long swapChainImage : swapChainImages) {
          swapChainImageViews.add(createImageView(swapChainImage, swapChainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT));
      }
    }


    public long createImageView(long image, int format) {
      return createImageView(image, format, VK_IMAGE_ASPECT_COLOR_BIT);
    }

    public void createImage(int width, int height, int format, int tiling, int usage, int memProperties,
                             LongBuffer pTextureImage, LongBuffer pTextureImageMemory) {

        try(MemoryStack stack = stackPush()) {

            VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
            imageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            imageInfo.imageType(VK_IMAGE_TYPE_2D);
            imageInfo.extent().width(width);
            imageInfo.extent().height(height);
            imageInfo.extent().depth(1);
            imageInfo.mipLevels(1);
            imageInfo.arrayLayers(1);
            imageInfo.format(format);
            imageInfo.tiling(tiling);
            imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            imageInfo.usage(usage);
            imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
            imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            if(vkCreateImage(device, imageInfo, null, pTextureImage) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image");
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
            vkGetImageMemoryRequirements(device, pTextureImage.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(findMemoryType(stack, memRequirements.memoryTypeBits(), memProperties));

            if(vkAllocateMemory(device, allocInfo, null, pTextureImageMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate image memory");
            }

            vkBindImageMemory(device, pTextureImage.get(0), pTextureImageMemory.get(0), 0);
        }
    }


    public long createImageView(long image, int format, int aspectFlags) {
      try(MemoryStack stack = stackPush()) {

          VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
          viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
          viewInfo.image(image);
          viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
          viewInfo.format(format);

          viewInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY);
          viewInfo.components().g(VK_COMPONENT_SWIZZLE_IDENTITY);
          viewInfo.components().b(VK_COMPONENT_SWIZZLE_IDENTITY);
          viewInfo.components().a(VK_COMPONENT_SWIZZLE_IDENTITY);

          viewInfo.subresourceRange().aspectMask(aspectFlags);
          viewInfo.subresourceRange().baseMipLevel(0);
          viewInfo.subresourceRange().levelCount(1);
          viewInfo.subresourceRange().baseArrayLayer(0);
          viewInfo.subresourceRange().layerCount(1);

          LongBuffer pImageView = stack.mallocLong(1);

          if(vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
              throw new RuntimeException("Failed to create texture image view");
          }

          return pImageView.get(0);
      }
  }

    public void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {

        try(MemoryStack stack = stackPush()) {

            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(size);
            bufferInfo.usage(usage);

            // Change the sharing mode to concurrent (it will be shared between graphics and transfer queues)
            // but only if useTransferQueue enabled
            if (useTransferQueue) {
	            QueueFamilyIndices queueFamilies = findQueueFamilies(physicalDevice);
	            bufferInfo.pQueueFamilyIndices(stack.ints(queueFamilies.graphicsFamily, queueFamilies.transferFamily));
	            bufferInfo.sharingMode(VK_SHARING_MODE_CONCURRENT);
            }
            else {
                bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            if(vkCreateBuffer(device, bufferInfo, null, pBuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create vertex buffer");
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
            vkGetBufferMemoryRequirements(device, pBuffer.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(findMemoryType(stack, memRequirements.memoryTypeBits(), properties));


            if(vkAllocateMemory(device, allocInfo, null, pBufferMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate vertex buffer memory");
            }

            vkBindBufferMemory(device, pBuffer.get(0), pBufferMemory.get(0), 0);
        }
    }




    private void copyBufferDefault(long srcBuffer, long dstBuffer, int widthsize, int height, int operation) {

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkBeginCommandBuffer(commandBuffer, beginInfo);
            copyCommandOperation(srcBuffer, dstBuffer, widthsize, height, commandBuffer, operation);
            vkEndCommandBuffer(commandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(pCommandBuffer);

            if(vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit copy command buffer");
            }

            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffers(device, commandPool, pCommandBuffer);
        }
    }

    public void transitionImageLayout(long image, int format, int oldLayout, int newLayout) {

      try(MemoryStack stack = stackPush()) {

          VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
          barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
          barrier.oldLayout(oldLayout);
          barrier.newLayout(newLayout);
          barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
          barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
          barrier.image(image);
          barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
          barrier.subresourceRange().baseMipLevel(0);
          barrier.subresourceRange().levelCount(1);
          barrier.subresourceRange().baseArrayLayer(0);
          barrier.subresourceRange().layerCount(1);

          if(newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

            if(hasStencilComponent(format)) {
                barrier.subresourceRange().aspectMask(
                        barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
            }

          } else {
              barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
          }

          int sourceStage;
          int destinationStage;

          if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

              barrier.srcAccessMask(0);
              barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

              sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
              destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

          } else if(oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

              barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
              barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

              sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
              destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

          } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
              barrier.srcAccessMask(0);
              barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

              sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
              destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;

          } else if (oldLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
              barrier.srcAccessMask(VK_ACCESS_SHADER_READ_BIT);
              barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

              sourceStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
              destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
          }else {
              throw new IllegalArgumentException("Unsupported layout transition");
          }


          if (useTransferQueue) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkResetCommandBuffer(transferCommandBuffer, 0);
            // Transfer command buffer implicitly reset
            vkBeginCommandBuffer(transferCommandBuffer, beginInfo);

            vkCmdPipelineBarrier(transferCommandBuffer,
                    sourceStage, destinationStage,
                    0,
                    null,
                    null,
                    barrier);

            vkEndCommandBuffer(transferCommandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(transferCommandBuffer));
            if(vkQueueSubmit(transferQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit copy command buffer");
            }
            vkQueueWaitIdle(transferQueue);
          }
          else {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkBeginCommandBuffer(commandBuffer, beginInfo);

            vkCmdPipelineBarrier(commandBuffer,
                    sourceStage, destinationStage,
                    0,
                    null,
                    null,
                    barrier);

            vkEndCommandBuffer(commandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(pCommandBuffer);

            if(vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit copy command buffer");
            }

            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffers(device, commandPool, pCommandBuffer);
          }

      }
  }



    public void copyBufferTransfer(long srcBuffer, long dstBuffer, int widthsize, int height, int operation) {
//
        try(MemoryStack stack = stackPush()) {

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkResetCommandBuffer(transferCommandBuffer, 0);
            // Transfer command buffer implicitly reset
            vkBeginCommandBuffer(transferCommandBuffer, beginInfo);
            copyCommandOperation(srcBuffer, dstBuffer, widthsize, height, transferCommandBuffer, operation);
            vkEndCommandBuffer(transferCommandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(transferCommandBuffer));

            if(vkQueueSubmit(transferQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit copy command buffer");
            }
            vkQueueWaitIdle(transferQueue);

        }
    }


    public void copyBufferAndWait(long srcBuffer, long dstBuffer, int size) {
      // Too lazy to combine it into one function
      if (useTransferQueue) {
        copyBufferTransfer(srcBuffer, dstBuffer, size, 0, OPERATION_BUFFER);
      }
      else {
        copyBufferDefault(srcBuffer, dstBuffer, size, 0, OPERATION_BUFFER);
      }
      // The 0 here in both these functions is for height for textures.
      // But obviously, we're buffering buffers, not textures.
    }


    public void copyTextureAndWait(long srcTexture, long dstTexture, int width, int height) {
      if (useTransferQueue) {
        copyBufferTransfer(srcTexture, dstTexture, width, height, OPERATION_TEXTURE);
      }
      else {
        copyBufferDefault(srcTexture, dstTexture, width, height, OPERATION_TEXTURE);
      }
    }



    // To be used after starting a singletime command buffer.
    private void copyCommandOperation(long srcBuffer, long dstBuffer,
                                      int widthsize, int height,
                                      VkCommandBuffer cmdBuffer,
                                      int operation) {
      try(MemoryStack stack = stackPush()) {
        switch (operation) {
        case OPERATION_BUFFER:
        {
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
            copyRegion.size(widthsize);
            vkCmdCopyBuffer(cmdBuffer, srcBuffer, dstBuffer, copyRegion);
        }
        break;
        case OPERATION_TEXTURE:
          VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
          region.bufferOffset(0);
          region.bufferRowLength(0);   // Tightly packed
          region.bufferImageHeight(0);  // Tightly packed
          region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
          region.imageSubresource().mipLevel(0);
          region.imageSubresource().baseArrayLayer(0);
          region.imageSubresource().layerCount(1);
          region.imageOffset().set(0, 0, 0);
          region.imageExtent(VkExtent3D.calloc(stack).set(widthsize, height, 1));

          vkCmdCopyBufferToImage(cmdBuffer, srcBuffer, dstBuffer, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
        }
      }
    }


    public int findMemoryType(MemoryStack stack, int typeFilter, int properties) {

        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);

        for(int i = 0;i < memProperties.memoryTypeCount();i++) {
            if((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }

        throw new RuntimeException("Failed to find suitable memory type");
    }


    private PointerBuffer getRequiredExtensions(MemoryStack stack) {

        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

        if(VKSetup.ENABLE_VALIDATION_LAYERS) {

            PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);

            extensions.put(glfwExtensions);
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

            // Rewind the buffer before returning it to reset its position back to 0
            return extensions.rewind();
        }

        return glfwExtensions;
    }


    public int findSupportedFormat(IntBuffer formatCandidates, int tiling, int features) {

        try(MemoryStack stack = stackPush()) {

            VkFormatProperties props = VkFormatProperties.calloc(stack);

            for(int i = 0; i < formatCandidates.capacity(); ++i) {

                int format = formatCandidates.get(i);

                vkGetPhysicalDeviceFormatProperties(physicalDevice, format, props);

                if(tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                    return format;
                } else if(tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                    return format;
                }

            }
        }

        throw new RuntimeException("Failed to find supported format");
    }

    public int findDepthFormat() {
        return findSupportedFormat(
                stackGet().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

    public boolean hasStencilComponent(int format) {
        return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
    }



}
