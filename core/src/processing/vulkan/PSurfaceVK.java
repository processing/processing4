package processing.vulkan;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.DisplayMode;
import java.io.File;
import java.nio.BufferOverflowException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import processing.GL2VK.GL2VK;
import processing.GL2VK.Util;
import processing.GL2VK.VMouseEvent;
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

public class PSurfaceVK implements PSurface {

  protected PGraphics graphics;

  protected PApplet sketch;
  protected int sketchWidth = 0;
  protected int sketchHeight = 0;

  private Thread drawExceptionHandler;
  private Object drawExceptionMutex = new Object();
  protected Throwable drawException;

  private AnimatorTask animationThread;
  private boolean isStopped = true;

  private NEWTMouseListener mouseListener;

  private PVK pvk;

  public static GL2VK gl2vk = null;

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
      if (gl2vk == null) {
        int nodes = sketch.maxNodes;
        if (nodes == -1) {
          int availableProcessors = Runtime.getRuntime().availableProcessors();
          nodes = availableProcessors;
        }
        gl2vk = new GL2VK(graphics.width, graphics.height, nodes);
        pvk.setGL2VK(gl2vk);
      }

      if (pvk.shouldClose()) {
        sketch.exit();
      }
//
      if (!sketch.finished) {
        pvk.getGL(pvk);

//      Util.beginTmr();
        pvk.beginRecord();
        pvk.selectNode(0);
        pvk.enableAutoMode();
        sketch.handleDraw();
        pvk.endRecord();

//      Util.beginTmr();

//        Util.beginTmr();
        PGraphicsOpenGL.completeFinishedPixelTransfers();
//        Util.endTmr("completeFinishedPixelTransfers");
      }
      if (sketch.exitCalled()) {
        sketch.dispose();
        sketch.exitActual();
        pvk.cleanup();
      }
//      Util.endTmr("end entire cycle");
//      then = System.nanoTime();
    }
  }

  public PSurfaceVK(PGraphics graphics) {
    this.graphics = graphics;
    this.pvk = (PVK)((PGraphicsVulkan) graphics).pgl;
  }

  public void initOffscreen(PApplet sketch) {
    this.sketch = sketch;

    sketchWidth = sketch.sketchWidth();
    sketchHeight = sketch.sketchHeight();
  }

  public void initFrame(PApplet sketch) {
    this.sketch = sketch;
    sketchWidth = sketch.sketchWidth();
    sketchHeight = sketch.sketchHeight();

    initIcons();
    initWindow();
    initListeners();
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


  protected void nativeMouseEvent(int mouseX, int mouseY, int peAction) {
    int scale = 1;
//    if (PApplet.platform == PConstants.MACOS) {
//      scale = (int) getCurrentPixelScale();
//    } else {
//      scale = (int) getPixelScale();
//    }
    int sx = mouseX / scale;
    int sy = mouseY / scale;
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

    MouseEvent me = new MouseEvent(new Object(), 0,
                                   peAction, 0,
                                   mx, my,
                                   0,
                                   0);

    sketch.postEvent(me);
  }



//NEWT mouse listener
 protected class NEWTMouseListener extends VMouseEvent {
   public NEWTMouseListener() {
     super();
   }

   @Override
   public void mouseMoved(int x, int y) {
     nativeMouseEvent(x, y, MouseEvent.MOVE);
   }
 }




  protected void initListeners() {
    // TODO: create NEWTMouseListener class which binds to glfw to provide mouse and keyboard input.

    mouseListener = new NEWTMouseListener();
  }

  private void initWindow() {

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