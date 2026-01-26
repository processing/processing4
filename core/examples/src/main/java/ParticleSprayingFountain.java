import processing.core.PApplet;
import java.util.ArrayList;

public class ParticleSprayingFountain implements Feature {
    PApplet p;
    ArrayList<Particle> particles = new ArrayList<>();
    float gravity = 0.15f;
    float wind = 0.05f;

    public ParticleSprayingFountain(PApplet parent) {
        this.p = parent;
    }

    @Override
    public void update() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle part = particles.get(i);
            part.update();
            if (part.isDead()) {
                particles.remove(i);
            }
        }
    }

    @Override
    public void display() {
        // We don't call background() here because Basic.java handles it
        for (Particle part : particles) {
            part.display();
        }
    }

    @Override
    public void handleMouse() {
        if (p.mousePressed) {
            for (int i = 0; i < 5; i++) {
                particles.add(new Particle(p.mouseX, p.mouseY));
            }
        }
    }

    @Override
    public void handleKeys() {
        // Clear particles with 'C'
        if (p.keyPressed && (p.key == 'c' || p.key == 'C')) {
            particles.clear();
        }
    }

    @Override
    public String getInstructions() {
        return "DRAG MOUSE to spray | Press 'C' to clear | Press 'M' for Menu";
    }

    // --- Inner Particle Class ---
    class Particle {
        float x, y, vx, vy, lifespan, hue;

        Particle(float sx, float sy) {
            x = sx;
            y = sy;
            float angle = p.random(p.TWO_PI);
            float speed = p.random(1, 5);
            vx = p.cos(angle) * speed;
            vy = p.sin(angle) * speed;
            lifespan = 255;
            hue = p.random(360);
        }

        void update() {
            vy += gravity;
            vx += wind;
            x += vx;
            y += vy;

            // Floor bounce
            if (y > p.height - 10) {
                vy *= -0.7f;
                y = p.height - 10;
            }
            // Wall bounce
            if (x > p.width || x < 0) {
                vx *= -0.9f;
            }

            lifespan -= 2.0f;
        }

        void display() {
            p.noStroke();
            // Use HSB colors (Hue, Saturation, Brightness, Alpha)
            p.fill(hue, 80, 100, lifespan);
            p.circle(x, y, 8);
        }

        boolean isDead() {
            return lifespan <= 0;
        }
    }
}