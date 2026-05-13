package processing.webgpu;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PSurfaceGLFW implements PSurface {

    protected PApplet sketch;
    protected PGraphics graphics;

    protected long window;
    protected long display;
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
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWScrollCallback scrollCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCharCallback charCallback;
    private GLFWCursorEnterCallback cursorEnterCallback;
    private GLFWWindowFocusCallback windowFocusCallback;

    private double lastCursorX;
    private double lastCursorY;
    private int currentMouseButton;
    private int currentModifiers;

    // Cursor callbacks fire from inside glfwPollEvents and must do no FFI
    // or allocation, or a high-poll-rate mouse can refill events faster
    // than glfwPollEvents drains. Buffer the raw (x, y) here and replay
    // them in runDrawLoop after polling returns.
    private double[] cursorXs = new double[256];
    private double[] cursorYs = new double[256];
    private int cursorCount;

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
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(sketch.sketchWidth(), sketch.sketchHeight(), "Processing",
                MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        display = GLFW.glfwGetPrimaryMonitor();

        windowCount.incrementAndGet();

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
        long surfaceId = getSurfaceId();

        // glfwSet*Callback returns the *previous* callback, not the new one.
        // The new wrapper must be held in a field or it will be GC'd while
        // still registered with native GLFW and events will silently stop.

        framebufferSizeCallback = GLFWFramebufferSizeCallback.create((win, w, h) -> {
            if (sketch != null) sketch.postWindowResized(w, h);
        });
        GLFW.glfwSetFramebufferSizeCallback(window, framebufferSizeCallback);

        windowPosCallback = GLFWWindowPosCallback.create((win, xpos, ypos) -> {
            if (sketch != null) sketch.postWindowMoved(xpos, ypos);
        });
        GLFW.glfwSetWindowPosCallback(window, windowPosCallback);

        cursorPosCallback = GLFWCursorPosCallback.create((win, xpos, ypos) -> {
            int n = cursorCount;
            if (n >= cursorXs.length) {
                int newLen = cursorXs.length * 2;
                double[] nx = new double[newLen];
                double[] ny = new double[newLen];
                System.arraycopy(cursorXs, 0, nx, 0, n);
                System.arraycopy(cursorYs, 0, ny, 0, n);
                cursorXs = nx;
                cursorYs = ny;
            }
            cursorXs[n] = xpos;
            cursorYs[n] = ypos;
            cursorCount = n + 1;
            lastCursorX = xpos;
            lastCursorY = ypos;
        });
        GLFW.glfwSetCursorPosCallback(window, cursorPosCallback);

        mouseButtonCallback = GLFWMouseButtonCallback.create((win, button, action, mods) -> {
            int peButton = switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> PConstants.LEFT;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> PConstants.CENTER;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> PConstants.RIGHT;
                default -> 0;
            };
            currentModifiers = glfwModsToProcessing(mods);
            boolean pressed = (action == GLFW.GLFW_PRESS);
            if (surfaceId != 0 && peButton != 0) {
                byte btn = (byte) (peButton == PConstants.LEFT ? 0
                        : peButton == PConstants.CENTER ? 1 : 2);
                PWebGPU.inputMouseButton(surfaceId, btn, pressed);
            }
            if (peButton != 0) {
                if (pressed) currentMouseButton = peButton;
                if (sketch != null) {
                    int peAction = pressed ? MouseEvent.PRESS : MouseEvent.RELEASE;
                    sketch.postEvent(new MouseEvent(null, System.currentTimeMillis(),
                            peAction, currentModifiers,
                            (int) lastCursorX, (int) lastCursorY, peButton, 1));
                }
                if (!pressed) currentMouseButton = 0;
            }
        });
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback);

        scrollCallback = GLFWScrollCallback.create((win, xoffset, yoffset) -> {
            if (surfaceId != 0) {
                PWebGPU.inputScroll(surfaceId, (float) xoffset, (float) yoffset);
            }
            if (sketch != null) {
                // Flip: Processing wheel is negative-up, GLFW is positive-up.
                sketch.postEvent(new MouseEvent(null, System.currentTimeMillis(),
                        MouseEvent.WHEEL, currentModifiers,
                        (int) lastCursorX, (int) lastCursorY, 0,
                        (int) -yoffset));
            }
        });
        GLFW.glfwSetScrollCallback(window, scrollCallback);

        keyCallback = GLFWKeyCallback.create((win, key, scancode, action, mods) -> {
            currentModifiers = glfwModsToProcessing(mods);
            if (surfaceId != 0 && action != GLFW.GLFW_REPEAT) {
                PWebGPU.inputKey(surfaceId, key, action == GLFW.GLFW_PRESS);
            }
            if (sketch != null && action != GLFW.GLFW_REPEAT) {
                int peAction = (action == GLFW.GLFW_PRESS) ? KeyEvent.PRESS : KeyEvent.RELEASE;
                sketch.postEvent(new KeyEvent(null, System.currentTimeMillis(),
                        peAction, currentModifiers,
                        glfwKeyToChar(key), key));
            }
        });
        GLFW.glfwSetKeyCallback(window, keyCallback);

        charCallback = GLFWCharCallback.create((win, codepoint) -> {
            if (surfaceId != 0) {
                PWebGPU.inputChar(surfaceId, 0, codepoint);
            }
            if (sketch != null) {
                sketch.postEvent(new KeyEvent(null, System.currentTimeMillis(),
                        KeyEvent.TYPE, currentModifiers,
                        (char) codepoint, 0));
            }
        });
        GLFW.glfwSetCharCallback(window, charCallback);

        cursorEnterCallback = GLFWCursorEnterCallback.create((win, entered) -> {
            if (surfaceId != 0) {
                if (entered) PWebGPU.inputCursorEnter(surfaceId);
                else PWebGPU.inputCursorLeave(surfaceId);
            }
        });
        GLFW.glfwSetCursorEnterCallback(window, cursorEnterCallback);

        windowFocusCallback = GLFWWindowFocusCallback.create((win, focused) -> {
            if (surfaceId != 0) PWebGPU.inputFocus(surfaceId, focused);
        });
        GLFW.glfwSetWindowFocusCallback(window, windowFocusCallback);
    }

    private static int glfwModsToProcessing(int mods) {
        int m = 0;
        if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) m |= Event.SHIFT;
        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) m |= Event.CTRL;
        if ((mods & GLFW.GLFW_MOD_ALT) != 0) m |= Event.ALT;
        if ((mods & GLFW.GLFW_MOD_SUPER) != 0) m |= Event.META;
        return m;
    }

    private static char glfwKeyToChar(int glfwKey) {
        if (glfwKey >= 32 && glfwKey < 127) {
            return (char) glfwKey;
        }
        return switch (glfwKey) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> '\n';
            case GLFW.GLFW_KEY_TAB -> '\t';
            case GLFW.GLFW_KEY_BACKSPACE -> '\b';
            case GLFW.GLFW_KEY_ESCAPE -> 27;
            case GLFW.GLFW_KEY_DELETE -> 127;
            default -> PConstants.CODED;
        };
    }

    private long getSurfaceId() {
        if (graphics instanceof PGraphicsWebGPU webgpu) {
            return webgpu.getSurfaceId();
        }
        return 0;
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
        } else if (Platform.get() == Platform.LINUX) {
            // TODO: need to check if x11 or wayland
            return GLFWNativeWayland.glfwGetWaylandWindow(window);
        } else {
            throw new UnsupportedOperationException("Window handle retrieval not implemented for this platform");
        }
    }

    public long getDisplayHandle() {
        if (Platform.get() == Platform.MACOSX) {
            return 0;
        } else if (Platform.get() == Platform.WINDOWS) {
            return 0;
        } else if (Platform.get() == Platform.LINUX) {
            // TODO: need to check if x11 or wayland
            return GLFWNativeWayland.glfwGetWaylandDisplay();
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
        throw new UnsupportedOperationException("File dialogs not yet implemented for WebGPU");
    }

    @Override
    public void selectOutput(String prompt, String callback, File file, Object callbackObject) {
        throw new UnsupportedOperationException("File dialogs not yet implemented for WebGPU");
    }

    @Override
    public void selectFolder(String prompt, String callback, File file, Object callbackObject) {
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
        // macOS: when the JVM is launched as a child of another GUI process,
        // NSApplication isn't activated and input events stop after a brief
        // initial spurt unless we force focus.
        GLFW.glfwFocusWindow(window);

        long beforeTime = System.nanoTime();
        long overSleepTime = 0L;

        sketch.start();

        while (running) {
            checkPause();

            GLFW.glfwPollEvents();

            if (GLFW.glfwWindowShouldClose(window)) {
                sketch.exit();
                break;
            }

            int n = cursorCount;
            if (n > 0) {
                long sid = getSurfaceId();
                long now = System.currentTimeMillis();
                for (int i = 0; i < n; i++) {
                    float x = (float) cursorXs[i];
                    float y = (float) cursorYs[i];
                    if (sid != 0) {
                        PWebGPU.inputMouseMove(sid, x, y);
                    }
                    if (sketch != null) {
                        int action = (currentMouseButton == 0) ? MouseEvent.MOVE : MouseEvent.DRAG;
                        sketch.postEvent(new MouseEvent(null, now, action, currentModifiers,
                                (int) x, (int) y, currentMouseButton, 0));
                    }
                }
                cursorCount = 0;
            }

            PWebGPU.inputFlush();

            if (!sketch.finished) {
                sketch.handleDraw();
            }

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
        if (cursorPosCallback != null) {
            cursorPosCallback.free();
            cursorPosCallback = null;
        }
        if (mouseButtonCallback != null) {
            mouseButtonCallback.free();
            mouseButtonCallback = null;
        }
        if (scrollCallback != null) {
            scrollCallback.free();
            scrollCallback = null;
        }
        if (keyCallback != null) {
            keyCallback.free();
            keyCallback = null;
        }
        if (charCallback != null) {
            charCallback.free();
            charCallback = null;
        }
        if (cursorEnterCallback != null) {
            cursorEnterCallback.free();
            cursorEnterCallback = null;
        }
        if (windowFocusCallback != null) {
            windowFocusCallback.free();
            windowFocusCallback = null;
        }
    }

    @Override
    public boolean isStopped() {
        return !running;
    }
}
