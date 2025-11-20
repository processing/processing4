package processing.webgpu;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GLFW-based surface implementation for WebGPU.
 */
public class PSurfaceGLFW implements PSurface {

    protected PApplet sketch;
    protected PGraphics graphics;

    protected long window;
    protected boolean running = false;

    protected boolean paused;
    private final Lock pauseLock = new ReentrantLock();
    private final Condition pauseCondition = pauseLock.newCondition();

    protected float frameRateTarget = 60;
    protected long frameRatePeriod = 1000000000L / 60L;

    private static final AtomicInteger windowCount = new AtomicInteger(0);
    private static AtomicBoolean glfwInitialized = new AtomicBoolean(false);

    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    private GLFWWindowPosCallback windowPosCallback;

    public PSurfaceGLFW(PGraphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void initOffscreen(PApplet sketch) {
        throw new IllegalStateException("PSurfaceGLFW does not support offscreen rendering");
    }

    @Override
    public void initFrame(PApplet sketch) {
        this.sketch = sketch;

        if (glfwInitialized.compareAndSet(false, true)) {
            GLFWErrorCallback.createPrint(System.err).set();
            if (!GLFW.glfwInit()) {
                glfwInitialized.set(false);
                throw new IllegalStateException("Failed to initialize GLFW");
            }
            System.out.println("PSurfaceGLFW: GLFW initialized successfully");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API); // no opengl
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(sketch.sketchWidth(), sketch.sketchHeight(), "Processing",
                MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        windowCount.incrementAndGet();

        // event callbacks
        initListeners();

        if (graphics instanceof PGraphicsWebGPU webgpu) {
            PWebGPU.init();

            long windowHandle = getWindowHandle();
            long displayHandle = getDisplayHandle();
            int width = sketch.sketchWidth();
            int height = sketch.sketchHeight();
            float scaleFactor = sketch.sketchPixelDensity();

            webgpu.initWebGPUSurface(windowHandle, displayHandle, width, height, scaleFactor);
        }
    }

    protected void initListeners() {
        framebufferSizeCallback = GLFW.glfwSetFramebufferSizeCallback(window,
                (window, width, height) -> {
                    if (sketch != null) {
                        sketch.postWindowResized(width, height);
                    }
                });

        windowPosCallback = GLFW.glfwSetWindowPosCallback(window,
                (window, xpos, ypos) -> {
                    if (sketch != null) {
                        sketch.postWindowMoved(xpos, ypos);
                    }
                });

        // TODO: all the callbacks
    }

    @Override
    public Object getNative() {
        return window;
    }

    public long getWindowHandle() {
        if (Platform.get() == Platform.MACOSX) {
            return GLFWNativeCocoa.glfwGetCocoaWindow(window);
        } else if (Platform.get() == Platform.WINDOWS) {
            return GLFWNativeWin32.glfwGetWin32Window(window);
        } else {
            throw new UnsupportedOperationException("Window handle retrieval not implemented for this platform");
        }
    }

    @Override
    public void setTitle(String title) {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetWindowTitle(window, title);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (window != MemoryUtil.NULL) {
            if (visible) {
                GLFW.glfwShowWindow(window);
            } else {
                GLFW.glfwHideWindow(window);
            }
        }
    }

    @Override
    public void setResizable(boolean resizable) {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE,
                    resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        }
    }

    @Override
    public void setAlwaysOnTop(boolean always) {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_FLOATING,
                    always ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        }
    }

    @Override
    public void setIcon(PImage icon) {
        // TODO: set icon with glfw
    }

    @Override
    public void placeWindow(int[] location, int[] editorLocation) {
        if (window == MemoryUtil.NULL) return;

        int x, y;
        if (location != null) {
            x = location[0];
            y = location[1];
        } else if (editorLocation != null) {
            x = editorLocation[0] - 20;
            y = editorLocation[1];

            if (x - sketch.sketchWidth() < 10) {
                // doesn't fit, center on screen instead
                long monitor = GLFW.glfwGetPrimaryMonitor();
                var vidmode = GLFW.glfwGetVideoMode(monitor);
                if (vidmode != null) {
                    x = (vidmode.width() - sketch.sketchWidth()) / 2;
                    y = (vidmode.height() - sketch.sketchHeight()) / 2;
                } else {
                    x = 100;
                    y = 100;
                }
            }
        } else {
            // center on primary monitor
            long monitor = GLFW.glfwGetPrimaryMonitor();
            var vidmode = GLFW.glfwGetVideoMode(monitor);
            if (vidmode != null) {
                x = (vidmode.width() - sketch.sketchWidth()) / 2;
                y = (vidmode.height() - sketch.sketchHeight()) / 2;
            } else {
                x = 100;
                y = 100;
            }
        }

        GLFW.glfwSetWindowPos(window, x, y);
    }

    @Override
    public void placePresent(int stopColor) {
        // TODO: implement present mode support
    }

    @Override
    public void setLocation(int x, int y) {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetWindowPos(window, x, y);
        }
    }

    @Override
    public void setSize(int width, int height) {
        if (width == sketch.width && height == sketch.height) {
            return;
        }

        sketch.width = width;
        sketch.height = height;
        graphics.setSize(width, height);

        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetWindowSize(window, width, height);
        }
    }

    @Override
    public void setFrameRate(float fps) {
        frameRateTarget = fps;
        frameRatePeriod = (long) (1000000000.0 / frameRateTarget);
    }

    @Override
    public void setCursor(int kind) {
        // TODO: implement cursor types
    }

    @Override
    public void setCursor(PImage image, int hotspotX, int hotspotY) {
        // TODO: implement custom cursor
    }

    @Override
    public void showCursor() {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    @Override
    public void hideCursor() {
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        }
    }

    @Override
    public PImage loadImage(String path, Object... args) {
        // TODO: implement image loading without awt
        throw new UnsupportedOperationException("Image loading not yet implemented for WebGPU");
    }

    @Override
    public boolean openLink(String url) {
        // TODO: implement links without awt
        return false;
    }

    @Override
    public void selectInput(String prompt, String callback, File file, Object callbackObject) {
        // TODO: select input without awt
        throw new UnsupportedOperationException("File dialogs not yet implemented for WebGPU");
    }

    @Override
    public void selectOutput(String prompt, String callback, File file, Object callbackObject) {
        // TODO: file dialogs without awt
        throw new UnsupportedOperationException("File dialogs not yet implemented for WebGPU");
    }

    @Override
    public void selectFolder(String prompt, String callback, File file, Object callbackObject) {
        // TODO: folder selection without awt
        throw new UnsupportedOperationException("Folder selection not yet implemented for WebGPU");
    }

    @Override
    public void startThread() {
        if (running) {
            throw new IllegalStateException("Draw loop already running");
        }

        running = true;
        runDrawLoop();
    }

    protected void runDrawLoop() {
        GLFW.glfwShowWindow(window);

        long beforeTime = System.nanoTime();
        long overSleepTime = 0L;

        sketch.start();

        while (running && !sketch.finished) {
            checkPause();

            GLFW.glfwPollEvents();

            if (GLFW.glfwWindowShouldClose(window)) {
                sketch.exit();
                break;
            }

            // render!
            sketch.handleDraw();

            // frame pacing
            long afterTime = System.nanoTime();
            long timeDiff = afterTime - beforeTime;
            long sleepTime = (frameRatePeriod - timeDiff) - overSleepTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000L, (int) (sleepTime % 1000000L));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
            } else {
                overSleepTime = 0L;
            }

            beforeTime = System.nanoTime();
        }

        // cleanup
        sketch.dispose();
    }

    @Override
    public void pauseThread() {
        paused = true;
    }

    protected void checkPause() {
        if (paused) {
            pauseLock.lock();
            try {
                while (paused) {
                    pauseCondition.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                pauseLock.unlock();
            }
        }
    }

    @Override
    public void resumeThread() {
        pauseLock.lock();
        try {
            paused = false;
            pauseCondition.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

    @Override
    public boolean stopThread() {
        if (!running) {
            return false;
        }

        running = false;

        try {
            if (window != MemoryUtil.NULL) {
                GLFW.glfwDestroyWindow(window);
                window = MemoryUtil.NULL;

                if (windowCount.decrementAndGet() == 0) {
                    if (glfwInitialized.compareAndSet(true, false)) {
                        GLFW.glfwTerminate();
                        System.out.println("PSurfaceGLFW: GLFW terminated");
                    }
                }
            }
        } finally {
            // run destructors always
            freeCallbacks();
        }

        return true;
    }

    private void freeCallbacks() {
        if (framebufferSizeCallback != null) {
            framebufferSizeCallback.free();
            framebufferSizeCallback = null;
        }
        if (windowPosCallback != null) {
            windowPosCallback.free();
            windowPosCallback = null;
        }
    }


    @Override
    public boolean isStopped() {
        return !running;
    }
}
