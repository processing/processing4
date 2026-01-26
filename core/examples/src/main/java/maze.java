import processing.core.PApplet;
import java.util.*;

public class maze implements Feature {
    PApplet p;
    int cols, rows, cellSize = 20;
    Cell[][] grid;
    Stack<Cell> stack;
    Cell current, player, goal;
    boolean generating = true;
    boolean solving = false;
    ArrayList<Cell> solutionPath;
    int solutionIndex = 0;

    public maze(PApplet parent) {
        this.p = parent;
        setupMaze();
    }

    @Override
    public String getInstructions() {
        if (player == goal) return "VICTORY! Press 'R' to reset | Press 'M' for Menu";
        return "ARROW KEYS to move | SPACE to auto-solve | 'R' to reset | Press 'M' for Menu";
    }

    public void setupMaze() {
        cols = p.width / cellSize;
        rows = (p.height - 50) / cellSize;
        grid = new Cell[cols][rows];
        stack = new Stack<>();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) grid[i][j] = new Cell(i, j);
        }
        current = grid[0][0];
        current.visited = true;
        player = grid[0][0];
        goal = grid[cols - 1][rows - 1];
        generating = true;
        solving = false;
        solutionPath = null;
    }

    @Override
    public void update() {
        if (generating) {
            Cell next = current.getUnvisitedNeighbor();
            if (next != null) {
                next.visited = true;
                stack.push(current);
                removeWalls(current, next);
                current = next;
            } else if (!stack.isEmpty()) {
                current = stack.pop();
            } else {
                generating = false;
            }
        }
    }

    @Override
    public void display() {
        // Draw the maze grid
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) grid[i][j].show();
        }

        // Draw the solution if activated
        if (solving && solutionPath != null) drawSolutionStep();

        player.showPlayer();
        goal.showGoal();

        // Win Message
        if (player == goal) {
            p.textAlign(p.CENTER, p.CENTER);
            p.textSize(50);
            p.fill(120, 100, 100); // Green in HSB
            p.text("YOU WIN!", p.width/2f, p.height/2f);
        }
    }

    @Override
    public void handleMouse() {}

    @Override
    public void handleKeys() {
        if (!generating) {
            if (p.keyCode == p.UP) movePlayer(0, -1, 0);
            else if (p.keyCode == p.DOWN) movePlayer(0, 1, 2);
            else if (p.keyCode == p.LEFT) movePlayer(-1, 0, 3);
            else if (p.keyCode == p.RIGHT) movePlayer(1, 0, 1);
            else if (p.key == ' ') solveMaze();
            else if (p.key == 'r' || p.key == 'R') setupMaze();
        }
    }

    // --- Logic Helpers ---
    private void movePlayer(int dc, int dr, int w) {
        if (player == goal) return; // Stop movement if won
        Cell next = getCell(player.col + dc, player.row + dr);
        if (next != null && !player.walls[w]) player = next;
    }

    private Cell getCell(int c, int r) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) return null;
        return grid[c][r];
    }

    private void removeWalls(Cell a, Cell b) {
        int x = a.col - b.col;
        int y = a.row - b.row;
        if (x == 1) { a.walls[3] = false; b.walls[1] = false; }
        else if (x == -1) { a.walls[1] = false; b.walls[3] = false; }
        if (y == 1) { a.walls[0] = false; b.walls[2] = false; }
        else if (y == -1) { a.walls[2] = false; b.walls[0] = false; }
    }

    private void drawSolutionStep() {
        if (solutionIndex < solutionPath.size()) {
            for (int i = 0; i <= solutionIndex; i++) {
                Cell c = solutionPath.get(i);
                p.fill(200, 80, 100, 150);
                p.noStroke();
                p.rect(c.col * cellSize + 2, c.row * cellSize + 2, cellSize - 4, cellSize - 4);
            }
            solutionIndex++;
        }
    }

    private void solveMaze() {
        solving = true;
        solutionIndex = 0;
        solutionPath = new ArrayList<>();
        Queue<Cell> q = new LinkedList<>();
        HashMap<Cell, Cell> par = new HashMap<>();
        HashSet<Cell> vis = new HashSet<>();

        q.add(player); vis.add(player); par.put(player, null);

        while (!q.isEmpty()) {
            Cell curr = q.poll();
            if (curr == goal) {
                Cell s = goal;
                while (s != null) { solutionPath.add(0, s); s = par.get(s); }
                break;
            }
            for (Cell n : curr.getAccessibleNeighbors()) {
                if (!vis.contains(n)) { vis.add(n); par.put(n, curr); q.add(n); }
            }
        }
    }

    class Cell {
        int col, row;
        boolean[] walls = {true, true, true, true};
        boolean visited = false;
        Cell(int c, int r) { col = c; row = r; }

        void show() {
            int x = col * cellSize, y = row * cellSize;
            p.stroke(0, 0, 100); p.strokeWeight(2);
            if (walls[0]) p.line(x, y, x + cellSize, y);
            if (walls[1]) p.line(x + cellSize, y, x + cellSize, y + cellSize);
            if (walls[2]) p.line(x, y + cellSize, x + cellSize, y + cellSize);
            if (walls[3]) p.line(x, y, x, y + cellSize);
        }

        void showPlayer() {
            p.fill(120, 100, 100);
            p.noStroke();
            p.ellipse(col * cellSize + cellSize/2f, row * cellSize + cellSize/2f, cellSize*0.6f, cellSize*0.6f);
        }

        void showGoal() {
            p.fill(0, 100, 100);
            p.noStroke();
            p.rect(col * cellSize + 4, row * cellSize + 4, cellSize - 8, cellSize - 8);
        }

        Cell getUnvisitedNeighbor() {
            ArrayList<Cell> nbs = new ArrayList<>();
            int[][] ds = {{0,-1},{1,0},{0,1},{-1,0}};
            for(int[] d : ds) {
                Cell c = getCell(col+d[0], row+d[1]);
                if(c != null && !c.visited) nbs.add(c);
            }
            return nbs.isEmpty() ? null : nbs.get((int)p.random(nbs.size()));
        }

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