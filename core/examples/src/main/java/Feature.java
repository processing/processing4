/**
 * The Feature interface defines a standard contract for all sub-projects 
 * within the Project Hub. By implementing this, a system (like a maze or 
 * particle engine) can be seamlessly swapped and managed by the main sketch.
 */
public interface Feature {
    /**
     * Handles the mathematical and state logic of the feature.
     * Called once per frame before the display cycle.
     */
    void update();
    /**
     * Handles the visual rendering of the feature to the screen.
     */
    void display();
    /**
     * Processes mouse interaction specific to this feature.
     */
    void handleMouse();
    /**
     * Processes keyboard input specific to this feature.
     */
    void handleKeys();
    /**
     * Provides a short string of text explaining how to use the feature.
     * @return A string displayed in the instruction bar, or null if none.
     */
    String getInstructions();
}
