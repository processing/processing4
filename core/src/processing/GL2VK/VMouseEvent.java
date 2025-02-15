package processing.GL2VK;

import java.util.ArrayList;


public class VMouseEvent {
    private static ArrayList<VMouseEvent> allObjects = new ArrayList<>();
    private boolean added = false;

    public VMouseEvent() {
      add();
    }

    public static void invokeMouseMove(int mouseX,
                                 int mouseY) {
      for (VMouseEvent m : allObjects) {
        m.mouseMoved(mouseX, mouseY);
      }
    }

    protected void add() {
      if (!added) {
        allObjects.add(this);
        added = true;
      }
    }

    public void mousePressed() {
    }
    public void mouseReleased() {
    }
    public void mouseClicked() {
    }
    public void mouseDragged() {
    }
    public void mouseMoved(int x, int y) {
    }
    public void mouseWheelMoved() {
    }
    public void mouseEntered() {
    }
    public void mouseExited() {
    }
}
