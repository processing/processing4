import processing.core.PApplet;
import processing.awt.PSurfaceAWT;

// Reproduction of issue #1003
// Resizeable causes crash on linux mint
public class Issue1003 extends PApplet {

    public void settings(){
        size(200, 200);
    }

    public void setup() {
        surface.setTitle("Hello resize!");
        surface.setResizable(true);
        surface.setLocation(100, 100);
    }

    public void draw(){
        background(frameCount % 255);
        line(0, 0, width, height);
        line(width, 0, 0, height);
    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[]{ Issue1003.class.getName()};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }

    }
}
