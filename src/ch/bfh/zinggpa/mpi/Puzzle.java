package ch.bfh.zinggpa.mpi;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Puzzle {
    public static final int DEPTH = 10;
    private int[][] puzzleRepresentation;
    private int size;
    private int[] spacerPos;
    private Stack<String> stack;

    public Puzzle(int[][] puzzleRepresentation, int[] spacerPos) {
        this.puzzleRepresentation = puzzleRepresentation;
        this.size = puzzleRepresentation.length;
        this.spacerPos = spacerPos;
        this.stack = new Stack<>();
    }

    public void solve() {
        int[][] arrayCopy = copyArray(puzzleRepresentation);
        solve(arrayCopy, null, spacerPos, DEPTH);
    }

    private void solve(int[][] puzzle, Direction actualDir, int[] spacerPos, int depth) {
        if (Arrays.deepEquals(puzzle, new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 0}})) {
            System.out.println(stack.toString() + "\n");
            System.out.println(Arrays.toString(puzzle[0]));
            System.out.println(Arrays.toString(puzzle[1]));
            System.out.println(Arrays.toString(puzzle[2]));
            System.exit(0);
        }
        if (depth == 0) {
            System.out.println(stack.toString() + "\n");
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
        if (x == size - 1) possibleDirections.remove(Direction.RIGHT);
        if (y == size - 1) possibleDirections.remove(Direction.DOWN);

        // Expand graph for each possible direction
        for (Direction dir : possibleDirections) {
            stack.push(dir.toString());
            int[][] arrayCopy = copyArray(puzzle);
            solve(arrayCopy, dir, spacerPos, depth - 1);
        }
        if (!stack.isEmpty()) stack.pop();
    }

    private int[][] copyArray(int[][] input) {
        int[][] arrayCopy = new int[input.length][];
        for (int i = 0; i < input.length; i++)
            arrayCopy[i] = Arrays.copyOf(input[i], input.length);
        return arrayCopy;
    }

    private void move(int[][] puzzle, int[] oldPos, int[] newPos) {
        int tmp = puzzle[oldPos[1]][oldPos[0]];
        puzzle[oldPos[1]][oldPos[0]] = puzzle[newPos[1]][newPos[0]];
        puzzle[newPos[1]][newPos[0]] = tmp;
    }


    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }
}
