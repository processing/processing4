package processing.core;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PImageTest {

  private PImage img;
  private PApplet applet;

  @Before
  public void setUp() {
    applet = new PApplet();
    img = new PImage(10, 10, PConstants.ARGB);
    for (int i = 0; i < img.pixels.length; i++) {
      img.pixels[i] = 0xFF000000 | (i % 255) << 16 | ((i * 3) % 255) << 8 | ((i * 7) % 255);
    }
    img.updatePixels();
  }

  @Test
  public void testConstructors() {
    PImage img1 = new PImage();
    assertEquals(PConstants.ARGB, img1.format);
    
    PImage img2 = new PImage(20, 30);
    assertEquals(20, img2.width);
    assertEquals(30, img2.height);
    assertEquals(PConstants.RGB, img2.format);
    
    PImage img3 = new PImage(20, 30, PConstants.ALPHA);
    assertEquals(PConstants.ALPHA, img3.format);
    
    PImage img4 = new PImage(20, 30, PConstants.RGB, 2);
    assertEquals(2, img4.pixelDensity);
    assertEquals(40, img4.pixelWidth);
    assertEquals(60, img4.pixelHeight);
    
    PImage zeroImg = new PImage(0, 0);
    assertEquals(0, zeroImg.width);
    assertEquals(0, zeroImg.height);
    assertEquals(0, zeroImg.pixels.length);
  }

  @Test
  public void testPixelManipulation() {
    img.loadPixels();
    img.pixels[0] = 0xFFFF0000;
    img.updatePixels();
    assertEquals(0xFFFF0000, img.get(0, 0));
    
    assertEquals(0xFFFF0000, img.get(0, 0));
    assertEquals(0, img.get(-1, -1));
    assertEquals(0, img.get(100, 100));
    
    img.set(1, 1, 0xFF00FF00);
    assertEquals(0xFF00FF00, img.get(1, 1));
    
    img.set(-1, -1, 0xFFFFFFFF);
    img.set(100, 100, 0xFFFFFFFF);
    
    PImage region = img.get(0, 0, 2, 2);
    assertEquals(2, region.width);
    assertEquals(2, region.height);
    assertEquals(0xFFFF0000, region.get(0, 0));
    assertEquals(0xFF00FF00, region.get(1, 1));
    
    PImage copy = img.get();
    assertEquals(img.width, copy.width);
    assertEquals(img.height, copy.height);
    assertEquals(0xFFFF0000, copy.get(0, 0));
    assertEquals(0xFF00FF00, copy.get(1, 1));
    
    PImage negCopy = img.get(-5, -5, 20, 20);
    assertEquals(20, negCopy.width);
    assertEquals(20, negCopy.height);
  }

  @Test
  public void testCopyAndResize() {
    PImage copy = img.copy();
    assertEquals(img.width, copy.width);
    assertEquals(img.height, copy.height);
    assertEquals(img.get(0, 0), copy.get(0, 0));
    
    PImage resized = img.copy();
    resized.resize(20, 0);
    assertEquals(20, resized.width);
    assertTrue(resized.height > 0);
    
    PImage resized2 = img.copy();
    resized2.resize(20, 15);
    assertEquals(20, resized2.width);
    assertEquals(15, resized2.height);
        
    img.set(0, 0, 0xFFFF0000);
    img.set(1, 0, 0xFF00FF00);
    img.set(0, 1, 0xFF0000FF);
    img.set(1, 1, 0xFFFFFF00);
    
    PImage dest = new PImage(4, 4, PConstants.ARGB);
    dest.copy(img, 0, 0, 2, 2, 0, 0, 4, 4);
    
    int topLeft = dest.get(0, 0);
    int topRight = dest.get(3, 0);
    int bottomLeft = dest.get(0, 3);
    int bottomRight = dest.get(3, 3);
    
    assertTrue((topLeft & 0x00FF0000) > 0);
    assertTrue((topRight & 0x0000FF00) > 0); 
    assertTrue((bottomLeft & 0x000000FF) > 0); 
    assertTrue((bottomRight & 0x00FFFF00) > 0); 
    
    PImage smallImg = new PImage(5, 5, PConstants.ARGB);
    smallImg.copy(img, 0, 0, 10, 10, 0, 0, 5, 5);
    img.copy(smallImg, 0, 0, 5, 5, 0, 0, 10, 10);
  }

  @Test
  public void testMask() {
    PImage mask = new PImage(10, 10, PConstants.ALPHA);
    for (int i = 0; i < mask.pixels.length; i++) {
      mask.pixels[i] = (i * 255) / mask.pixels.length;
    }
    mask.updatePixels();
    
    PImage original = img.copy();
    img.mask(mask);
    
    assertTrue((img.get(0, 0) >>> 24) < 10);
    
    assertTrue((img.get(9, 9) >>> 24) > 240);
    
    img = original.copy();
    img.mask(mask.pixels);
    
    assertTrue((img.get(0, 0) >>> 24) < 10);
    
    assertTrue((img.get(9, 9) >>> 24) > 240);
    
    PImage smallMask = new PImage(5, 5);
    try {
      img.mask(smallMask.pixels);
      fail("Should throw IllegalArgumentException for wrong size mask");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testFilter() {
    for (int i = 0; i < img.pixels.length; i++) {
      img.pixels[i] = 0xFF808080;
    }
    img.updatePixels();
    
    PImage thresholdImg = img.copy();
    thresholdImg.filter(PConstants.THRESHOLD, 0.7f);
    int thresholdColor = thresholdImg.get(0, 0);
    assertTrue((thresholdColor & 0x00FFFFFF) < 0x00808080); 
    
    thresholdImg = img.copy();
    thresholdImg.filter(PConstants.THRESHOLD, 0.3f);
    thresholdColor = thresholdImg.get(0, 0);
    assertTrue((thresholdColor & 0x00FFFFFF) > 0x00808080);
    
    PImage grayImg = img.copy();
    grayImg.filter(PConstants.GRAY);
    int grayColor = grayImg.get(0, 0);
    int r = (grayColor >> 16) & 0xFF;
    int g = (grayColor >> 8) & 0xFF;
    int b = grayColor & 0xFF;
    assertEquals(r, g, 5); 
    assertEquals(g, b, 5); 
    
    PImage invertImg = img.copy();
    invertImg.filter(PConstants.INVERT);
    int originalColor = img.get(0, 0) & 0x00FFFFFF;
    int invertedColor = invertImg.get(0, 0) & 0x00FFFFFF;
    assertTrue(originalColor + invertedColor > 0x00FFFFFF - 10 && 
               originalColor + invertedColor < 0x00FFFFFF + 10);
    
    PImage posterizeImg = img.copy();
    posterizeImg.filter(PConstants.POSTERIZE, 2);
    
    PImage blurImg = img.copy();
    blurImg.filter(PConstants.BLUR, 1.0f);
    
    img.pixels[0] = 0x80808080;
    img.updatePixels();
    PImage opaqueImg = img.copy();
    opaqueImg.filter(PConstants.OPAQUE);
    assertTrue((opaqueImg.get(0, 0) >>> 24) > (img.get(0, 0) >>> 24));
    
    PImage img2 = new PImage(10, 10, PConstants.RGB);
    for (int y = 0; y < img2.height; y++) {
      for (int x = 0; x < img2.width; x++) {
        img2.pixels[y * img2.width + x] = (x == 5 || y == 5) ? 0xFFFFFFFF : 0xFF000000;
      }
    }
    img2.updatePixels();
    
    PImage erodeImg = img2.copy();
    erodeImg.filter(PConstants.ERODE);
    
    PImage dilateImg = img2.copy();
    dilateImg.filter(PConstants.DILATE);
    
    int blackPixelsInOriginal = 0;
    int blackPixelsInDilated = 0;
    for (int i = 0; i < img2.pixels.length; i++) {
      if ((img2.pixels[i] & 0x00FFFFFF) == 0) blackPixelsInOriginal++;
      if ((dilateImg.pixels[i] & 0x00FFFFFF) == 0) blackPixelsInDilated++;
    }
    assertTrue(blackPixelsInDilated < blackPixelsInOriginal);
  }

    @Test
    public void testAllBlendModesExactMatchStaticHelper() {
    final int W = 10, H = 10;
    final int red  = 0x80FF0000;  
    final int blue = 0x400000FF;

    PImage img1 = new PImage(W, H, PConstants.ARGB);
    PImage img2 = new PImage(W, H, PConstants.ARGB);
    Arrays.fill(img1.pixels, red);
    Arrays.fill(img2.pixels, blue);
    img1.updatePixels();
    img2.updatePixels();

    int[] modes = {
        PConstants.BLEND, PConstants.ADD, PConstants.SUBTRACT, PConstants.LIGHTEST,
        PConstants.DARKEST, PConstants.DIFFERENCE, PConstants.EXCLUSION,
        PConstants.MULTIPLY, PConstants.SCREEN, PConstants.REPLACE
    };

    for (int mode : modes) {
        PImage out = img1.copy();
        out.blend(img2, 0,0,W,H, 0,0,W,H, mode);
        out.loadPixels();

        int[] expected = new int[W*H];
        for (int i = 0; i < expected.length; i++) {
        expected[i] = (mode == PConstants.REPLACE)
                    ? img2.pixels[i]
                    : PImage.blendColor(img1.pixels[i], img2.pixels[i], mode);
        }

        for (int i = 0; i < expected.length; i++) {
        assertEquals(
            String.format("Mode %d failed at pixel %d: got 0x%08X, expected 0x%08X",
                        mode, i, out.pixels[i], expected[i]),
            expected[i], out.pixels[i]
        );
        }
    }
    }


    @Test
    public void testSaveAndLoad_pngRoundTrip() throws IOException {
      PImage out = new PImage(10, 10, PConstants.ARGB);
      for (int y = 0; y < out.height; y++) {
        for (int x = 0; x < out.width; x++) {
          out.pixels[y*out.width + x] =
            ((x + y) % 2 == 0)
              ? 0xFFFFFFFF 
              : 0xFF000000; 
        }
      }
      out.updatePixels();
      out.parent = applet;             
    
      File f = File.createTempFile("test", ".png");
      f.deleteOnExit();
      assertTrue(out.save(f.getAbsolutePath()));
    
      PImage in = applet.loadImage(f.getAbsolutePath());
      assertNotNull(in);
      assertEquals(out.width,  in.width);
      assertEquals(out.height, in.height);
    
      in.loadPixels();
      for (int i = 0; i < out.pixels.length; i++) {
        assertEquals(
          String.format(
            "Pixel %d mismatch: saved=0x%08X loaded=0x%08X",
            i, out.pixels[i], in.pixels[i]
          ),
          out.pixels[i],
          in.pixels[i]
        );
      }
    }
    

  @Test
  public void testCheckAlpha() {
    PImage opaqueImg = new PImage(5, 5, PConstants.RGB);
    for (int i = 0; i < opaqueImg.pixels.length; i++) {
      opaqueImg.pixels[i] = 0xFFFFFFFF;
    }
    opaqueImg.checkAlpha();
    assertEquals(PConstants.RGB, opaqueImg.format);
    
    PImage transImg = new PImage(5, 5, PConstants.RGB);
    for (int i = 0; i < transImg.pixels.length; i++) {
      transImg.pixels[i] = 0x80FFFFFF;
    }
    transImg.checkAlpha();
    assertEquals(PConstants.ARGB, transImg.format);
  }
  
}