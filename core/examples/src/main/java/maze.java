import processing.core.PApplet;
import java.util.*;

/**
 * MAZE FEATURE
 * Implements a procedural maze using Depth-First Search (Recursive Backtracker)
 * and provides an automated solver using Breadth-First Search (BFS).
 */
public class maze implements Feature {
    PApplet p;
    int cols, rows, cellSize = 20;
    Cell[][] grid;
    Stack<Cell> stack;            // Stack for the DFS generation algorithm
    Cell current, player, goal;   // Pointers for generation and gameplay
    boolean generating = true;    // Flag to control the generation animation
    boolean solving = false;       // Flag to trigger the BFS solution animation
    ArrayList<Cell> solutionPath; // Resulting path from the BFS solver
    int solutionIndex = 0;        // Counter to animate the solution step-by-step

    public maze(PApplet parent) {
        this.p = parent;
        setupMaze();
    }

    /**
     * Returns dynamic instructions based on game state.
     */
    @Override
    public String getInstructions() {
        if (player == goal) return "VICTORY! Press 'R' to reset | Press 'M' for Menu";
        return "ARROW KEYS to move | SPACE to auto-solve | 'R' to reset | Press 'M' for Menu";
    }

    /**
     * Initializes the grid, algorithms, and start/end positions.
     */
    public void setupMaze() {
        cols = p.width / cellSize;
        rows = (p.height - 50) / cellSize; // Height adjusted to clear the instruction bar
        grid = new Cell[cols][rows];
        stack = new Stack<>();
        
        // Initialize the grid with individual Cell objects
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) grid[i][j] = new Cell(i, j);
        }
        
        // Start maze generation at the top-left
        current = grid[0][0];
        current.visited = true;
        
        // Initialize player and goal positions
        player = grid[0][0];
        goal = grid[cols - 1][rows - 1];
        
        generating = true;
        solving = false;
        solutionPath = null;
    }

    /**
     * Update loop: Handles the step-by-step maze generation logic.
     */
    @Override
    public void update() {
        if (generating) {
            // STEP 1: Select a random unvisited neighbor
            Cell next = current.getUnvisitedNeighbor();
            if (next != null) {
                next.visited = true;
                
                // STEP 2: Push current cell to stack for backtracking
                stack.push(current);
                
                // STEP 3: Remove the wall between current and next cell
                removeWalls(current, next);
                
                // STEP 4: Move to the next cell
                current = next;
            } else if (!stack.isEmpty()) {
                // Backtrack: If no neighbors found, pop from the stack
                current = stack.pop();
            } else {
                // Generation complete
                generating = false;
            }
        }
    }

    /**
     * Display loop: Renders the grid, player, goal, and solution path.
     */
    @Override
    public void display() {
        // Render the physical walls of the maze
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) grid[i][j].show();
        }

        // Render the automated solution path if active
        if (solving && solutionPath != null) drawSolutionStep();

        // Render game entities
        player.showPlayer();
        goal.showGoal();

        // Win state visual feedback
        if (player == goal) {
            p.textAlign(p.CENTER, p.CENTER);
            p.textSize(50);
            p.fill(120, 100, 100); // Bright green in HSB
            p.text("YOU WIN!", p.width/2f, p.height/2f);
        }
    }

    @Override
    public void handleMouse() {}

    /**
     * Maps user input to movement, solving, and resetting.
     */
    @Override
    public void handleKeys() {
        if (!generating) {
            // Movement: UP=0, RIGHT=1, DOWN=2, LEFT=3
            if (p.keyCode == p.UP) movePlayer(0, -1, 0);
            else if (p.keyCode == p.DOWN) movePlayer(0, 1, 2);
            else if (p.keyCode == p.LEFT) movePlayer(-1, 0, 3);
            else if (p.keyCode == p.RIGHT) movePlayer(1, 0, 1);
            // Functionality
            else if (p.key == ' ') solveMaze();
            else if (p.key == 'r' || p.key == 'R') setupMaze();
        }
    }

    // --- Logic Helpers ---

    /**
     * Handles player movement collision based on cell walls.
     */
    private void movePlayer(int dc, int dr, int w) {
        if (player == goal) return; // Prevent movement after winning
        Cell next = getCell(player.col + dc, player.row + dr);
        // Ensure neighbor exists and no wall blocks the specific direction
        if (next != null && !player.walls[w]) player = next;
    }

    /**
     * Safety check for grid boundaries.
     */
    private Cell getCell(int c, int r) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) return null;
        return grid[c][r];
    }

    /**
     * Removes the shared walls between two adjacent cells to create a path.
     */
    private void removeWalls(Cell a, Cell b) {
        int x = a.col - b.col;
        int y = a.row - b.row;
        // Horizontal wall removal
        if (x == 1) { a.walls[3] = false; b.walls[1] = false; }
        else if (x == -1) { a.walls[1] = false; b.walls[3] = false; }
        // Vertical wall removal
        if (y == 1) { a.walls[0] = false; b.walls[2] = false; }
        else if (y == -1) { a.walls[2] = false; b.walls[0] = false; }
    }

    /**
     * Animates the display of the found solution path.
     */
    private void drawSolutionStep() {
        if (solutionIndex < solutionPath.size()) {
            for (int i = 0; i <= solutionIndex; i++) {
                Cell c = solutionPath.get(i);
                p.fill(200, 80, 100, 150); // Light blue tint
                p.noStroke();
                p.rect(c.col * cellSize + 2, c.row * cellSize + 2, cellSize - 4, cellSize - 4);
            }
            solutionIndex++; // Reveal the path one cell at a time
        }
    }

    /**
     * BFS Pathfinding Algorithm: Finds the shortest distance through the maze.
     */
    private void solveMaze() {
        solving = true;
        solutionIndex = 0;
        solutionPath = new ArrayList<>();
        Queue<Cell> q = new LinkedList<>();
        HashMap<Cell, Cell> par = new HashMap<>(); // Used to reconstruct path from child to parent
        HashSet<Cell> vis = new HashSet<>();

        q.add(player); vis.add(player); par.put(player, null);

        while (!q.isEmpty()) {
            Cell curr = q.poll();
            if (curr == goal) {
                // Goal found: Reconstruct path from Goal -> Player and reverse it
                Cell s = goal;
                while (s != null) { solutionPath.add(0, s); s = par.get(s); }
                break;
            }
            // Add all unvisited, accessible (no walls) neighbors to the queue
            for (Cell n : curr.getAccessibleNeighbors()) {
                if (!vis.contains(n)) { vis.add(n); par.put(n, curr); q.add(n); }
            }
        }
    }

    /**
     * Individual grid cell representation.
     */
    class Cell {
        int col, row;
        boolean[] walls = {true, true, true, true}; // Clockwise: Top, Right, Bottom, Left
        boolean visited = false; // Internal flag for the DFS generator
        
        Cell(int c, int r) { col = c; row = r; }

        /**
         * Draws the lines representing the walls of the cell.
         */
        void show() {
            int x = col * cellSize, y = row * cellSize;
            p.stroke(0, 0, 100); p.strokeWeight(2);
            if (walls[0]) p.line(x, y, x + cellSize, y);
            if (walls[1]) p.line(x + cellSize, y, x + cellSize, y + cellSize);
            if (walls[2]) p.line(x, y + cellSize, x + cellSize, y + cellSize);
            if (walls[3]) p.line(x, y, x, y + cellSize);
        }

        /**
         * Renders the player as a circle.
         */
        void showPlayer() {
            p.fill(120, 100, 100);
            p.noStroke();
            p.ellipse(col * cellSize + cellSize/2f, row * cellSize + cellSize/2f, cellSize*0.6f, cellSize*0.6f);
        }

        /**
         * Renders the goal as a square.
         */
        void showGoal() {
            p.fill(0, 100, 100);
            p.noStroke();
            p.rect(col * cellSize + 4, row * cellSize + 4, cellSize - 8, cellSize - 8);
        }

        /**
         * Finds a random adjacent neighbor that the generator hasn't visited yet.
         */
        Cell getUnvisitedNeighbor() {
            ArrayList<Cell> nbs = new ArrayList<>();
            int[][] ds = {{0,-1},{1,0},{0,1},{-1,0}};
            for(int[] d : ds) {
                Cell c = getCell(col+d[0], row+d[1]);
                if(c != null && !c.visited) nbs.add(c);
            }
            return nbs.isEmpty() ? null : nbs.get((int)p.random(nbs.size()));
        }

        /**
         * Returns a list of neighbors that can be physically moved into (no walls).
         */
        ArrayList<Cell> getAccessibleNeighbors() {
            ArrayList<Cell> nbs = new ArrayList<>();
            if(!walls[0]) nbs.add(getCell(col, row-1));
            if(!walls[1]) nbs.add(getCell(col+1, row));
            if(!walls[2]) nbs.add(getCell(col, row+1));
            if(!walls[3]) nbs.add(getCell(col-1, row));
            nbs.removeIf(Objects::isNull);
            return nbs;
        }
    }
}