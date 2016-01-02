package ch.bfh.zinggpa.mpi;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Puzzle {
    public static final int DEPTH = 10;
    private int[] puzzleRepresentation;
    private int[] spacerPos;
    private Stack<String> stack;
    private int rows;
    private int cols;
    private boolean solved;

    public Puzzle(int[] puzzleRepresentation, int rows, int cols) {
        this.puzzleRepresentation = puzzleRepresentation;
        this.spacerPos = new int[]{0, 0};
        this.stack = new Stack<>();
        this.rows = rows;
        this.cols = cols;
        this.solved = false;
    }

    public boolean solve() {
        int[] arrayCopy = Arrays.copyOf(puzzleRepresentation, puzzleRepresentation.length);
        solve(arrayCopy, null, spacerPos, DEPTH);
        return solved;
    }

    private void solve(int[] puzzle, Direction actualDir, int[] spacerPos, int depth) {
        if (isSorted(puzzle)) {
            System.out.println(stack.toString());
            System.out.println(Arrays.toString(puzzle));
            solved = true;
        }
        if (depth == 0 || solved) {
            //System.out.println(stack.toString() + "\n");
            stack.pop();
            return;
        }

        int x = spacerPos[0];
        int y = spacerPos[1];

        if (depth != DEPTH) {
            int[] newSpacerPos = null;
            switch (actualDir) {
                case UP:
                    newSpacerPos = new int[]{x, y - 1};
                    break;
                case DOWN:
                    newSpacerPos = new int[]{x, y + 1};
                    break;
                case LEFT:
                    newSpacerPos = new int[]{x - 1, y};
                    break;
                case RIGHT:
                    newSpacerPos = new int[]{x + 1, y};
                    break;
            }
            move(puzzle, spacerPos, newSpacerPos);
            spacerPos = newSpacerPos;
            x = spacerPos[0];
            y = spacerPos[1];
        }

        List<Direction> possibleDirections = new ArrayList<>(Arrays.asList(Direction.values()));

        // No "reverse" move
        if (actualDir == Direction.UP) possibleDirections.remove(Direction.DOWN);
        if (actualDir == Direction.DOWN) possibleDirections.remove(Direction.UP);
        if (actualDir == Direction.LEFT) possibleDirections.remove(Direction.RIGHT);
        if (actualDir == Direction.RIGHT) possibleDirections.remove(Direction.LEFT);

        // Check if spacer is near a border
        if (x == 0) possibleDirections.remove(Direction.LEFT);
        if (y == 0) possibleDirections.remove(Direction.UP);
        if (x == cols - 1) possibleDirections.remove(Direction.RIGHT);
        if (y == rows - 1) possibleDirections.remove(Direction.DOWN);

        // Expand graph for each possible direction
        for (Direction dir : possibleDirections) {
            stack.push(dir.toString());
            int[] arrayCopy = Arrays.copyOf(puzzle, puzzle.length);
            solve(arrayCopy, dir, spacerPos, depth - 1);
        }
        if (!stack.isEmpty()) stack.pop();
    }


    private void move(int[] puzzle, int[] oldPos, int[] newPos) {
        int tmp = puzzle[oldPos[1] * rows + oldPos[0]];
        puzzle[oldPos[1] * rows + oldPos[0]] = puzzle[newPos[1] * rows + newPos[0]];
        puzzle[newPos[1] * rows + newPos[0]] = tmp;
    }

    private boolean isSorted(int[] array) {
        boolean sorted = true;
        if (array[array.length - 1] != 0) return false;
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i + 1] != 0 && array[i] > array[i + 1]) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }

    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }
}
