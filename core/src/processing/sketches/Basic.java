package processing.sketches;

import processing.core.PApplet;

public class Basic extends PApplet {

    public void settings(){
        size(800, 800);
    }
    public void setup(){

    }

    public void draw(){

    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[]{Basic.class.getName()};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }

    }
}
