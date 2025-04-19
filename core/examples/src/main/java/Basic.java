import processing.core.PApplet;

import java.io.IOException;

public class Basic extends PApplet {
    public void settings(){
        size(500, 500);

        try {
            Runtime.getRuntime().exec("echo Hello World");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void draw(){
        background(255);
        fill(0);
        ellipse(mouseX, mouseY, 125f, 125f);
        println(frameRate);


    }


    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[]{ Basic.class.getName()};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }

    }
}
