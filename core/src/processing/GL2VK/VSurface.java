package processing.GL2VK;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import org.lwjgl.vulkan.VkInstance;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;

public class VSurface {
    public long window;
    public int width = 1200;
    public int height = 800;
    public long surface;

    private boolean framebufferResize = false;

    public VSurface(int width, int height) {
    	this.width = width;
    	this.height = height;
      initWindow();
    }
    public VSurface() {
      initWindow();
    }


    public void initWindow() {

        if(!glfwInit()) {
            throw new RuntimeException("Cannot initialize GLFW");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        String title = "Vulkan";

        window = glfwCreateWindow(width, height, title, NULL, NULL);

        if(window == NULL) {
            throw new RuntimeException("Cannot create window");
        }

        // In Java, we don't really need a user pointer here, because
        // we can simply pass an instance method reference to glfwSetFramebufferSizeCallback
        // However, I will show you how can you pass a user pointer to glfw in Java just for learning purposes:
        // long userPointer = JNINativeInterface.NewGlobalRef(this);
        // glfwSetWindowUserPointer(window, userPointer);
        // Please notice that the reference must be freed manually with JNINativeInterface.nDeleteGlobalRef
        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);
        glfwSetCursorPosCallback(window, this::cursorMoveCallback);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }



    private void framebufferResizeCallback(long window, int width, int height) {
        // HelloTriangleApplication app = MemoryUtil.memGlobalRefToObject(glfwGetWindowUserPointer(window));
        // app.framebufferResize = true;
        framebufferResize = true;
    }

    private void cursorMoveCallback(long window, double xpos, double ypos) {
      VMouseEvent.invokeMouseMove((int)xpos, (int)ypos);
    }

    public void createSurface(VkInstance instance) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            if(glfwCreateWindowSurface(instance, window, null, pSurface) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create window surface");
            }

            surface = pSurface.get(0);
        }
    }

    public boolean isResize() {
    	return framebufferResize;
    }

}
