package processing.opengl;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javaangle.GL;

import java.awt.DisplayMode;
import java.io.File;
import java.nio.BufferOverflowException;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import processing.awt.ShimAWT;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.opengl.PGraphicsOpenGL;

// NEXT TODO:
// Create a function which creates a surface with a specified width and height, puts it into
// animation thread.

public class PSurfaceANGLE implements PSurface {

  protected PGraphics graphics;
  protected PGLANGLE pgl;

  protected PApplet sketch;
  protected int sketchWidth = 0;
  protected int sketchHeight = 0;
  private boolean windowCreated = false;

  private Thread drawExceptionHandler;
  private Object drawExceptionMutex = new Object();
  protected Throwable drawException;

  private AnimatorTask animationThread;
  private boolean isStopped = true;

  /////////////////////////////////
  // GLFW window variables
  public int glfwwindow;
  public int glfwwidth = 1200;
  public int glfwheight = 800;
  public long glfwsurface;
  public boolean glfwframebufferResize = false;

  private int glfwMouseX = 0, glfwMouseY = 0;
  private int glfwButton = 0, glfwAction = 0;

  private static int GLFW_SHIFT = 340;
  private static int GLFW_ENTER = 257;
  private static int GLFW_BACKSPACE = 259;
  private static int GLFW_CTRL = 341;
  private static int GLFW_ALT = 342;

  // TODO: Make framerate dynamic.
  class AnimatorTask extends TimerTask {
    Timer timer;
    float fps = 60.0f;

    public AnimatorTask(float fps) {
      super();
      this.fps = fps;
    }

    public void start() {
      final long period = 0 < fps ? (long) (1000.0f / fps) : 1; // 0 -> 1: IllegalArgumentException: Non-positive period
      timer = new Timer();
      timer.scheduleAtFixedRate(this, 0, period);
    }

    long then = 0;

    @Override
    public void run() {
//      System.out.println("INTERVAL "+(System.nanoTime()-then));
//      Util.beginTmr();

      // We create the window here because glfw window needs to be created in the same
      // thread as it is run in.
      if (!windowCreated) {
        initWindow();
      }

      if (GL.glfwWindowShouldClose(glfwwindow) == 1) {
        sketch.exit();
      }
//
      if (!sketch.finished) {
        // pvk.getGL(pvk);

        sketch.handleDraw();
        PGraphicsOpenGL.completeFinishedPixelTransfers();
//        Util.endTmr("completeFinishedPixelTransfers");
      }

      GL.glfwSwapBuffers(glfwwindow);
      
      GL.glfwPollEvents();

      if (sketch.exitCalled()) {
        sketch.dispose();
        sketch.exitActual();
      }
//      Util.endTmr("end entire cycle");
//      then = System.nanoTime();
    }
  }

  public PSurfaceANGLE(PGraphics graphics) {
    this.graphics = graphics;
    this.pgl = (PGLANGLE)((PGraphicsOpenGLANGLE) graphics).pgl;
  }

  @Override
  public void initOffscreen(PApplet sketch) {
    this.sketch = sketch;

    sketchWidth = sketch.sketchWidth();
    sketchHeight = sketch.sketchHeight();
  }

  @Override
  public void initFrame(PApplet sketch) {
    this.sketch = sketch;
    sketchWidth = sketch.sketchWidth();
    sketchHeight = sketch.sketchHeight();

    initIcons();
    initAnimator();
  }

  private void initIcons() {
//    IOUtil.ClassResources res;
//    if (PJOGL.icons == null || PJOGL.icons.length == 0) {
//      // Default Processing icons
//      final int[] sizes = { 16, 32, 48, 64, 128, 256, 512 };
//      String[] iconImages = new String[sizes.length];
//      for (int i = 0; i < sizes.length; i++) {
//         iconImages[i] = "/icon/icon-" + sizes[i] + ".png";
//       }
//       res = new ClassResources(iconImages,
//                                PApplet.class.getClassLoader(),
//                                PApplet.class);
//    } else {
//      // Loading custom icons from user-provided files.
//      String[] iconImages = new String[PJOGL.icons.length];
//      for (int i = 0; i < PJOGL.icons.length; i++) {
//        iconImages[i] = resourceFilename(PJOGL.icons[i]);
//      }
//
//      res = new ClassResources(iconImages,
//                               sketch.getClass().getClassLoader(),
//                               sketch.getClass());
//    }
//    NewtFactory.setWindowIcons(res);
    // TODO: make work for vulkan
  }

  

  private static boolean isPCodedKey(int code) {
    return
//        code == com.jogamp.newt.event.KeyEvent.VK_UP ||
//           code == com.jogamp.newt.event.KeyEvent.VK_DOWN ||
//           code == com.jogamp.newt.event.KeyEvent.VK_LEFT ||
//           code == com.jogamp.newt.event.KeyEvent.VK_RIGHT ||
           code == GLFW_ALT ||
           code == GLFW_CTRL ||
           code == GLFW_SHIFT;
//           code == GLFW_WINDOWS ||
//           (!isHackyKey(code));
  }


  // Why do we need this mapping?
  // Relevant discussion and links here:
  // http://forum.jogamp.org/Newt-wrong-keycode-for-key-td4033690.html#a4033697
  // (I don't think this is a complete solution).
  private static int mapToPConst(int code) {
    return switch (code) {
      case com.jogamp.newt.event.KeyEvent.VK_UP -> PConstants.UP;
      case com.jogamp.newt.event.KeyEvent.VK_DOWN -> PConstants.DOWN;
      case com.jogamp.newt.event.KeyEvent.VK_LEFT -> PConstants.LEFT;
      case com.jogamp.newt.event.KeyEvent.VK_RIGHT -> PConstants.RIGHT;
      case com.jogamp.newt.event.KeyEvent.VK_ALT -> PConstants.ALT;
      case com.jogamp.newt.event.KeyEvent.VK_CONTROL -> PConstants.CONTROL;
      case com.jogamp.newt.event.KeyEvent.VK_SHIFT -> PConstants.SHIFT;
      case com.jogamp.newt.event.KeyEvent.VK_WINDOWS -> java.awt.event.KeyEvent.VK_META;
      default -> code;
    };
  }


  private static boolean isHackyKey(int code) {
    return (code == GLFW_BACKSPACE ||
//            code == com.jogamp.newt.event.KeyEvent.VK_TAB ||
            code == GLFW_ENTER
//            code == com.jogamp.newt.event.KeyEvent.VK_ESCAPE ||
//            code == com.jogamp.newt.event.KeyEvent.VK_DELETE
    );
  }


  private static char hackToChar(int code, char def) {
    return switch (code) {
      case com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE -> PConstants.BACKSPACE;
      case com.jogamp.newt.event.KeyEvent.VK_TAB -> PConstants.TAB;
      case com.jogamp.newt.event.KeyEvent.VK_ENTER -> PConstants.ENTER;
      case com.jogamp.newt.event.KeyEvent.VK_ESCAPE -> PConstants.ESC;
      case com.jogamp.newt.event.KeyEvent.VK_DELETE -> PConstants.DELETE;
      default -> def;
    };
  }


  protected void nativeKeyEvent(long millis, int action, int modifiers,
                                int key, int keyCode, boolean isAutoRepeat) {
    // SHIFT, CTRL, META, and ALT are identical to processing.event.Event
//    int modifiers = nativeEvent.getModifiers();
//    int peModifiers = nativeEvent.getModifiers() &
//                      (InputEvent.SHIFT_MASK |
//                       InputEvent.CTRL_MASK |
//                       InputEvent.META_MASK |
//                       InputEvent.ALT_MASK);




    // From http://jogamp.org/deployment/v2.1.0/javadoc/jogl/javadoc/com/jogamp/newt/event/KeyEvent.html
    // public final short getKeySymbol()
    // Returns the virtual key symbol reflecting the current keyboard layout.
    // public final short getKeyCode()
    // Returns the virtual key code using a fixed mapping to the US keyboard layout.
    // In contrast to key symbol, key code uses a fixed US keyboard layout and therefore is keyboard layout independent.
    // E.g. virtual key code VK_Y denotes the same physical key regardless whether keyboard layout QWERTY or QWERTZ is active. The key symbol of the former is VK_Y, where the latter produces VK_Y.


    if (!isPCodedKey(key)) {
      key = Character.toLowerCase(key);

      if (modifiers == GL.GLFW_MOD_SHIFT) {
        key = Character.toUpperCase(key);
      }

      KeyEvent ke = new KeyEvent(new Object(), millis,
                                 action, modifiers,
                                 (char)key,
                                 keyCode,
                                 isAutoRepeat);
      sketch.postEvent(ke);
    }

//    if (!isPCodedKey(code, nativeEvent.isPrintableKey()) && !isHackyKey(code)) {
//      if (peAction == KeyEvent.PRESS) {
//        // Create key typed event
//        // TODO: combine dead keys with the following key
//        KeyEvent tke = new KeyEvent(nativeEvent, nativeEvent.getWhen(),
//                                    KeyEvent.TYPE, modifiers,
//                                    keyChar,
//                                    0,
//                                    nativeEvent.isAutoRepeat());
//
//        sketch.postEvent(tke);
//      }
//    }
  }


  private void nativeMouseEvent(long millis, int action, int modifiers,
                                  int x, int y, int button, int count) {



    int peButton = switch (button) {
    case GL.GLFW_MOUSE_BUTTON_LEFT -> PConstants.LEFT;
    case GL.GLFW_MOUSE_BUTTON_MIDDLE -> PConstants.CENTER;
    case GL.GLFW_MOUSE_BUTTON_RIGHT -> PConstants.RIGHT;
    default -> 0;
    };

    int peaction = switch (action) {
    case GL.GLFW_PRESS -> MouseEvent.PRESS;
    case GL.GLFW_RELEASE -> MouseEvent.RELEASE;
    case 99 -> MouseEvent.MOVE;
    default -> 0;
    };

    int scale = 1;
//    if (PApplet.platform == PConstants.MACOS) {
//      scale = (int) getCurrentPixelScale();
//    } else {
//      scale = (int) getPixelScale();
//    }
    int sx = x / scale;
    int sy = y / scale;
    int mx = sx;
    int my = sy;

//    if (pvk.presentMode()) {
//      mx -= (int)pvk.presentX;
//      my -= (int)pvk.presentY;
//      //noinspection IntegerDivisionInFloatingPointContext
//      if (peAction == KeyEvent.RELEASE &&
//          pvk.insideStopButton(sx, sy - screenRect.height / windowScaleFactor)) {
//        sketch.exit();
//      }
//      if (mx < 0 || sketchWidth < mx || my < 0 || sketchHeight < my) {
//        return;
//      }
//    }

    MouseEvent me = new MouseEvent(new Object(), millis,
                                   peaction, modifiers,
                                   mx, my,
                                   peButton,
                                   count);

    sketch.postEvent(me);
  }





  protected void initListeners() {
  }

  private void initWindow() {

    GL.glfwInitHint(GL.GLFW_ANGLE_PLATFORM_TYPE, GL.GLFW_ANGLE_PLATFORM_TYPE_VULKAN);
    if(GL.glfwInit() == 0) {
      throw new RuntimeException("Cannot initialize GLFW");
    }

    GL.glfwWindowHint(GL.GLFW_CONTEXT_CREATION_API, GL.GLFW_EGL_CONTEXT_API);
    GL.glfwWindowHint(GL.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GL.glfwWindowHint(GL.GLFW_CONTEXT_VERSION_MINOR, 2);
    GL.glfwWindowHint(GL.GLFW_CLIENT_API, GL.GLFW_OPENGL_ES_API);

    // TODO: Update this.
    String title = "Sketch";

    glfwwidth = graphics.width;
    glfwheight = graphics.height;

    glfwwindow = GL.glfwCreateWindow(800, 600, title, null, 0);
    
    GL.glfwMakeContextCurrent(glfwwindow);

    if(glfwwindow == 0) {
        throw new RuntimeException("Cannot create window");
    }

    // glfwSetFramebufferSizeCallback(glfwwindow, this::framebufferResizeCallback);
    // glfwSetCursorPosCallback(glfwwindow, this::cursorMoveCallback);
    // glfwSetMouseButtonCallback(glfwwindow, this::mouseButtonCallback);
    // glfwSetKeyCallback(glfwwindow, this::keyActionCallback);
    // glfwSetInputMode(glfwwindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    windowCreated = true;
  }

  private void framebufferResizeCallback(long window, int width, int height) {
      glfwframebufferResize = true;
  }

  private void cursorMoveCallback(long window, double xpos, double ypos) {
    glfwMouseX = (int)xpos;
    glfwMouseY = (int)ypos;
    glfwAction = 99;
    nativeMouseEvent(0, glfwAction, 0,
                     glfwMouseX, glfwMouseY, glfwButton, 0);
  }

  private void mouseButtonCallback(long window, int button, int action, int mod) {
    glfwButton = button;
    glfwAction = action;
    nativeMouseEvent(0, glfwAction, mod,
                     glfwMouseX, glfwMouseY, glfwButton, 0);
  }

  private void keyActionCallback(long window, int key, int scancode, int action, int mods) {
//    System.out.println((char)scancode)
//    System.out.println(key);
    nativeKeyEvent(0L, action, mods,
                   (char)key, 0, false);
  }


  private void initAnimator() {
    if (PApplet.platform == PConstants.WINDOWS) {
      // Force Windows to keep timer resolution high by creating a dummy
      // thread that sleeps for a time that is not a multiple of 10 ms.
      // See section titled "Clocks and Timers on Windows" in this post:
      // https://web.archive.org/web/20160308031939/https://blogs.oracle.com/dholmes/entry/inside_the_hotspot_vm_clocks
      Thread highResTimerThread = new Thread(() -> {
        try {
          Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignore) { }
      }, "HighResTimerThread");
      highResTimerThread.setDaemon(true);
      highResTimerThread.start();
    }

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    DisplayMode dm = gd.getDisplayMode();

    // Get the refresh rate
    int refreshRate = dm.getRefreshRate();

    if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
      refreshRate = 60;
    }

    animationThread = new AnimatorTask(refreshRate);


    drawExceptionHandler = new Thread(() -> {
      synchronized (drawExceptionMutex) {
        try {
          while (drawException == null) {
            drawExceptionMutex.wait();
          }
          Throwable cause = drawException.getCause();
          if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          } else if (cause instanceof UnsatisfiedLinkError) {
            throw new UnsatisfiedLinkError(cause.getMessage());
          } else if (cause == null) {
            throw new RuntimeException(drawException.getMessage());
          } else {
            throw new RuntimeException(cause);
          }
        } catch (InterruptedException ignored) { }
      }
    });
    drawExceptionHandler.start();

  }

  public void swapBuffers() {
    GL.glfwSwapBuffers(glfwwindow);
  }

  public float getPixelScale() {
    return 1;
  }

  @Override
  public Object getNative() {
    // TODO Auto-generated method stub
    System.out.println("WARNING getNative() not implemented.");
    return null;
  }

  @Override
  public void setTitle(String title) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setVisible(boolean visible) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setResizable(boolean resizable) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setAlwaysOnTop(boolean always) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setIcon(PImage icon) {
    // TODO Auto-generated method stub

  }

  @Override
  public void placeWindow(int[] location, int[] editorLocation) {
    // TODO Auto-generated method stub

  }

  @Override
  public void placePresent(int stopColor) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLocation(int x, int y) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setSize(int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setFrameRate(float fps) {
//    System.out.println("SET FRAMERATE "+fps);
//    animationThread.cancel();
//    animationThread = new AnimatorTask(fps);
//    animationThread.start();
  }

  @Override
  public void setCursor(int kind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setCursor(PImage image, int hotspotX, int hotspotY) {
    // TODO Auto-generated method stub

  }

  @Override
  public void showCursor() {
    // TODO Auto-generated method stub

  }

  @Override
  public void hideCursor() {
    // TODO Auto-generated method stub

  }

  @Override
  public PImage loadImage(String path, Object... args) {
    // TODO Auto-generated method stub
    return ShimAWT.loadImage(sketch, path, args);
  }

  @Override
  public boolean openLink(String url) {
    // TODO Auto-generated method stub
    return ShimAWT.openLink(url);
  }

  @Override
  public void selectInput(String prompt, String callbackMethod,
                          File file, Object callbackObject) {
    EventQueue.invokeLater(() -> {
      // https://github.com/processing/processing/issues/3831
      boolean hide = (sketch != null) &&
              (PApplet.platform == PConstants.WINDOWS);
      if (hide) setVisible(false);

      ShimAWT.selectImpl(prompt, callbackMethod, file,
              callbackObject, null, FileDialog.LOAD);

      if (hide) setVisible(true);
    });
  }

  @Override
  public void selectOutput(String prompt, String callbackMethod,
                           File file, Object callbackObject) {
    EventQueue.invokeLater(() -> {
      // https://github.com/processing/processing/issues/3831
      boolean hide = (sketch != null) &&
              (PApplet.platform == PConstants.WINDOWS);
      if (hide) setVisible(false);

      ShimAWT.selectImpl(prompt, callbackMethod, file,
              callbackObject, null, FileDialog.SAVE);

      if (hide) setVisible(true);
    });
  }

  @Override
  public void selectFolder(String prompt, String callbackMethod,
                           File file, Object callbackObject) {
    EventQueue.invokeLater(() -> {
      // https://github.com/processing/processing/issues/3831
      boolean hide = (sketch != null) &&
              (PApplet.platform == PConstants.WINDOWS);
      if (hide) setVisible(false);

      ShimAWT.selectFolderImpl(prompt, callbackMethod, file,
              callbackObject, null);

      if (hide) setVisible(true);
    });
  }

  private AtomicBoolean paused = new AtomicBoolean(false);


  @Override
  public void startThread() {

    // OpenGL compatibility:
    // Window gets resized upon initialisation
//    pvk.resetFBOLayer();

    // Our animation thread here.
    animationThread.start();
    isStopped = false;
  }

  @Override
  public void pauseThread() {
    paused.set(true);
  }

  @Override
  public void resumeThread() {
    paused.set(false);
  }

  @Override
  public boolean stopThread() {
    if (animationThread != null) animationThread.cancel();
    isStopped = true;
    return true;
  }

  @Override
  public boolean isStopped() {
    return isStopped;
  }

}